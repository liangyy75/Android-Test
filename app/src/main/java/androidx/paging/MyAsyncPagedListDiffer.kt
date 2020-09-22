package androidx.paging

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
open class MyAsyncPagedListDiffer<T>(open var mUpdateCallback: ListUpdateCallback, open var mConfig: AsyncDifferConfig<T>) {
    interface PagedListListener<T> {
        fun onCurrentListChanged(previousList: MyPagedList<T>?, currentList: MyPagedList<T>?)
    }

    @SuppressLint("RestrictedApi")
    open var mMainThreadExecutor = ArchTaskExecutor.getMainThreadExecutor()

    protected open var mListeners: MutableList<PagedListListener<T>> = CopyOnWriteArrayList()
    protected open var mIsContiguous = false
    protected open var mPagedList: MyPagedList<T>? = null
    protected open var mSnapshot: MyPagedList<T>? = null

    open var mMaxScheduledGeneration = 0

    constructor(adapter: RecyclerView.Adapter<*>, diffCallback: DiffUtil.ItemCallback<T>)
            : this(AdapterListUpdateCallback(adapter), AsyncDifferConfig.Builder(diffCallback).build())

    private val mPagedListCallback: MyPagedList.Callback = object : MyPagedList.Callback {
        override fun onInserted(position: Int, count: Int) = mUpdateCallback.onInserted(position, count)
        override fun onRemoved(position: Int, count: Int) = mUpdateCallback.onRemoved(position, count)
        override fun onChanged(position: Int, count: Int) = mUpdateCallback.onChanged(position, count, null)
    }

    open fun getItem(index: Int): T? {
        if (mPagedList == null) {
            return if (mSnapshot == null) {
                throw IndexOutOfBoundsException("Item count is zero, getItem() call is invalid")
            } else {
                mSnapshot!![index]
            }
        }
        mPagedList!!.loadAround(index)
        return mPagedList!![index]
    }

    open val itemCount: Int
        get() {
            if (mPagedList != null) {
                return mPagedList!!.size
            }
            return if (mSnapshot == null) 0 else mSnapshot!!.size
        }

    @JvmOverloads
    open fun submitList(pagedList: MyPagedList<T>?, commitCallback: Runnable? = null) {
        if (pagedList != null) {
            if (mPagedList == null && mSnapshot == null) {
                mIsContiguous = pagedList.isContiguous()
            } else {
                require(pagedList.isContiguous() == mIsContiguous) { ("AsyncPagedListDiffer cannot handle both contiguous and non-contiguous lists.") }
            }
        }
        val runGeneration = ++mMaxScheduledGeneration
        if (pagedList === mPagedList) {
            commitCallback?.run()
            return
        }
        val previous = if (mSnapshot != null) mSnapshot!! else mPagedList
        if (pagedList == null) {
            val removedCount = itemCount
            if (mPagedList != null) {
                mPagedList!!.removeWeakCallback(mPagedListCallback)
                mPagedList = null
            } else if (mSnapshot != null) {
                mSnapshot = null
            }
            mUpdateCallback.onRemoved(0, removedCount)
            onCurrentListChanged(previous, null, commitCallback)
            return
        }
        if (mPagedList == null && mSnapshot == null) {
            mPagedList = pagedList
            pagedList.addWeakCallback(null, mPagedListCallback)
            mUpdateCallback.onInserted(0, pagedList.size)
            onCurrentListChanged(null, pagedList, commitCallback)
            return
        }
        if (mPagedList != null) {
            mPagedList!!.removeWeakCallback(mPagedListCallback)
            mSnapshot = mPagedList!!.snapshot() as MyPagedList<T>
            mPagedList = null
        }
        check(!(mSnapshot == null || mPagedList != null)) { "must be in snapshot state to diff" }
        val oldSnapshot: MyPagedList<T> = mSnapshot!!
        val newSnapshot = pagedList.snapshot() as MyPagedList<T>
        mConfig.backgroundThreadExecutor.execute {
            val result: DiffResult = MyPagedStorageDiffHelper.computeDiff(oldSnapshot.mStorage, newSnapshot.mStorage, mConfig.diffCallback)
            mMainThreadExecutor.execute {
                if (mMaxScheduledGeneration == runGeneration) {
                    latchPagedList(pagedList, newSnapshot, result, oldSnapshot.mLastLoad, commitCallback)
                }
            }
        }
    }

    open fun latchPagedList(newList: MyPagedList<T>, diffSnapshot: MyPagedList<T>, diffResult: DiffResult, lastAccessIndex: Int, commitCallback: Runnable?) {
        check(!(mSnapshot == null || mPagedList != null)) { "must be in snapshot state to apply diff" }
        val previousSnapshot: MyPagedList<T> = mSnapshot!!
        mPagedList = newList
        mSnapshot = null

        // dispatch update callback after updating mPagedList/mSnapshot
        MyPagedStorageDiffHelper.dispatchDiff(mUpdateCallback,
                previousSnapshot.mStorage, newList.mStorage, diffResult)
        newList.addWeakCallback(diffSnapshot, mPagedListCallback)
        if (!mPagedList!!.isEmpty()) {
            val newPosition = MyPagedStorageDiffHelper.transformAnchorIndex(
                    diffResult, previousSnapshot.mStorage, diffSnapshot.mStorage, lastAccessIndex)
            mPagedList!!.loadAround(Math.max(0, Math.min(mPagedList!!.size - 1, newPosition)))
        }
        onCurrentListChanged(previousSnapshot, mPagedList, commitCallback)
    }

    open fun onCurrentListChanged(previousList: MyPagedList<T>?, currentList: MyPagedList<T>?, commitCallback: Runnable?) {
        for (listener in mListeners) {
            listener.onCurrentListChanged(previousList, currentList)
        }
        commitCallback?.run()
    }

    open fun addPagedListListener(listener: PagedListListener<T>) {
        mListeners.add(listener)
    }

    open fun removePagedListListener(listener: PagedListListener<T>) {
        mListeners.remove(listener)
    }

    open val currentList: MyPagedList<T>?
        get() = when {
            mSnapshot != null -> mSnapshot
            else -> mPagedList
        }

    // custom

    open fun loadAround(index: Int) = mPagedList?.loadAround(index) ?: Unit

    open fun loadAround2(index: Int): Long {
        mPagedList?.apply {
            if (this is MyTiledPagedList) {
                val time = System.currentTimeMillis()
                loadAround(index, time)
                return time
            } else {
                loadAround(index)
            }
        }
        return 0L
    }

    open fun <K : Any> scheduleInsert(t: T?, key: K?, key2: K?, position: Int) {
        if (mPagedList is MyContiguousPagedList<*, *>) {
            (mPagedList as MyContiguousPagedList<Any?, Any?>).scheduleInsert(t, key, key2, position)
        }
    }
}

object MyPagedStorageDiffHelper {
    fun <T> computeDiff(
            oldList: MyPagedStorage<T>,
            newList: MyPagedStorage<T>,
            diffCallback: DiffUtil.ItemCallback<T>): DiffResult {
        val oldOffset = oldList.computeLeadingNulls()
        val newOffset = newList.computeLeadingNulls()
        val oldSize = oldList.size - oldOffset - oldList.computeTrailingNulls()
        val newSize = newList.size - newOffset - newList.computeTrailingNulls()
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                val oldItem = oldList[oldItemPosition + oldOffset]
                val newItem = newList[newItemPosition + newList.getLeadingNullCount()]
                return if (oldItem == null || newItem == null) {
                    null
                } else diffCallback.getChangePayload(oldItem, newItem)
            }

            override fun getOldListSize(): Int {
                return oldSize
            }

            override fun getNewListSize(): Int {
                return newSize
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition + oldOffset]
                val newItem = newList[newItemPosition + newList.getLeadingNullCount()]
                if (oldItem === newItem) {
                    return true
                }
                return if (oldItem == null || newItem == null) {
                    false
                } else diffCallback.areItemsTheSame(oldItem, newItem)
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition + oldOffset]
                val newItem = newList[newItemPosition + newList.getLeadingNullCount()]
                if (oldItem === newItem) {
                    return true
                }
                return if (oldItem == null || newItem == null) {
                    false
                } else diffCallback.areContentsTheSame(oldItem, newItem)
            }
        }, true)
    }

    fun <T> dispatchDiff(callback: ListUpdateCallback,
                         oldList: MyPagedStorage<T>,
                         newList: MyPagedStorage<T>,
                         diffResult: DiffResult) {
        val trailingOld = oldList.computeTrailingNulls()
        val trailingNew = newList.computeTrailingNulls()
        val leadingOld = oldList.computeLeadingNulls()
        val leadingNew = newList.computeLeadingNulls()
        if (trailingOld == 0 && trailingNew == 0 && leadingOld == 0 && leadingNew == 0) {
            // Simple case, dispatch & return
            diffResult.dispatchUpdatesTo(callback)
            return
        }

        // First, remove or insert trailing nulls
        if (trailingOld > trailingNew) {
            val count = trailingOld - trailingNew
            callback.onRemoved(oldList.size - count, count)
        } else if (trailingOld < trailingNew) {
            callback.onInserted(oldList.size, trailingNew - trailingOld)
        }

        // Second, remove or insert leading nulls
        if (leadingOld > leadingNew) {
            callback.onRemoved(0, leadingOld - leadingNew)
        } else if (leadingOld < leadingNew) {
            callback.onInserted(0, leadingNew - leadingOld)
        }

        // apply the diff, with an offset if needed
        if (leadingNew != 0) {
            diffResult.dispatchUpdatesTo(OffsettingListUpdateCallback(leadingNew, callback))
        } else {
            diffResult.dispatchUpdatesTo(callback)
        }
    }

    /**
     * Given an oldPosition representing an anchor in the old data set, computes its new position
     * after the diff, or a guess if it no longer exists.
     */
    fun transformAnchorIndex(diffResult: DiffResult,
                             oldList: MyPagedStorage<*>, newList: MyPagedStorage<*>, oldPosition: Int): Int {
        val oldOffset = oldList.computeLeadingNulls()

        // diffResult's indices starting after nulls, need to transform to diffutil indices
        // (see also dispatchDiff(), which adds this offset when dispatching)
        val diffIndex = oldPosition - oldOffset
        val oldSize = oldList.size - oldOffset - oldList.computeTrailingNulls()

        // if our anchor is non-null, use it or close item's position in new list
        if (diffIndex >= 0 && diffIndex < oldSize) {
            // search outward from old position for position that maps
            for (i in 0..29) {
                val positionToTry = diffIndex + i / 2 * if (i % 2 == 1) -1 else 1

                // reject if (null) item was not passed to DiffUtil, and wouldn't be in the result
                if (positionToTry < 0 || positionToTry >= oldList.getStorageCount()) {
                    continue
                }
                try {
                    val result = diffResult.convertOldPositionToNew(positionToTry)
                    if (result != -1) {
                        // also need to transform from diffutil output indices to newList
                        return result + newList.getLeadingNullCount()
                    }
                } catch (e: java.lang.IndexOutOfBoundsException) {
                    // Rare crash, just give up the search for the old item
                    break
                }
            }
        }

        // not anchored to an item in new list, so just reuse position (clamped to newList size)
        return Math.max(0, Math.min(oldPosition, newList.size - 1))
    }

    private class OffsettingListUpdateCallback internal constructor(private val mOffset: Int, private val mCallback: ListUpdateCallback) : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            mCallback.onInserted(position + mOffset, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            mCallback.onRemoved(position + mOffset, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            mCallback.onMoved(fromPosition + mOffset, toPosition + mOffset)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            mCallback.onChanged(position + mOffset, count, payload)
        }
    }
}
