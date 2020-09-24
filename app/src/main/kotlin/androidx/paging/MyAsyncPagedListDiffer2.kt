package androidx.paging

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.liang.example.jetpacktest.pagingtest.PagingAdapter3
import com.liang.example.jetpacktest.pagingtest.PagingAdapterEnhance
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/17
 * <p>
 * todo 描述
 */
open class MyAsyncPagedListDiffer2<T>(open var mUpdateCallback: ListUpdateCallback, open var mConfig: AsyncDifferConfig<T>) {
    interface PagedListListener<T> {
        fun onCurrentListChanged(previousList: PagedList<T>?, currentList: PagedList<T>?)
    }

    @SuppressLint("RestrictedApi")
    open var mMainThreadExecutor = ArchTaskExecutor.getMainThreadExecutor()

    protected open var mListeners: MutableList<PagedListListener<T>> = CopyOnWriteArrayList()
    protected open var mIsContiguous = false
    protected open var mPagedList: PagedList<T>? = null
    protected open var mSnapshot: PagedList<T>? = null

    open var mMaxScheduledGeneration = 0

    constructor(adapter: RecyclerView.Adapter<*>, diffCallback: DiffUtil.ItemCallback<T>)
            : this(AdapterListUpdateCallbackWrapper(adapter, AdapterListUpdateCallback(adapter)), AsyncDifferConfig.Builder(diffCallback).build())

    protected open var mPagedListCallback: PagedList.Callback = object : PagedList.Callback() {
        override fun onInserted(position: Int, count: Int) {
            mUpdateCallback.onInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            mUpdateCallback.onRemoved(position, count)
        }

        override fun onChanged(position: Int, count: Int) {
            mUpdateCallback.onChanged(position, count, null)
        }
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
    open fun submitList(pagedList: PagedList<T>?, commitCallback: Runnable? = null) {
        if (pagedList != null) {
            if (mPagedList == null && mSnapshot == null) {
                mIsContiguous = pagedList.isContiguous
            } else {
                require(pagedList.isContiguous == mIsContiguous) { ("AsyncPagedListDiffer cannot handle both contiguous and non-contiguous lists.") }
            }
        }
        val runGeneration = ++mMaxScheduledGeneration
        if (pagedList === mPagedList) {
            commitCallback?.run()
            return
        }
        val previous = when {
            mSnapshot != null -> mSnapshot!!
            else -> mPagedList
        }
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
            mSnapshot = mPagedList!!.snapshot() as PagedList<T>
            mPagedList = null
        }
        check(!(mSnapshot == null || mPagedList != null)) { "must be in snapshot state to diff" }
        val oldSnapshot: PagedList<T> = mSnapshot!!
        val newSnapshot = pagedList.snapshot() as PagedList<T>
        mConfig.backgroundThreadExecutor.execute {
            val result: DiffUtil.DiffResult = PagedStorageDiffHelper.computeDiff(oldSnapshot.mStorage, newSnapshot.mStorage, mConfig.diffCallback)
            mMainThreadExecutor.execute {
                if (mMaxScheduledGeneration == runGeneration) {
                    latchPagedList(pagedList, newSnapshot, result, oldSnapshot.mLastLoad, commitCallback)
                }
            }
        }
    }

    open fun latchPagedList(newList: PagedList<T>, diffSnapshot: PagedList<T>, diffResult: DiffUtil.DiffResult, lastAccessIndex: Int,
                            commitCallback: Runnable?) {
        check(!(mSnapshot == null || mPagedList != null)) { "must be in snapshot state to apply diff" }
        val previousSnapshot: PagedList<T> = mSnapshot!!
        mPagedList = newList
        mSnapshot = null
        // dispatch update callback after updating mPagedList/mSnapshot
        PagedStorageDiffHelper.dispatchDiff(mUpdateCallback, previousSnapshot.mStorage, newList.mStorage, diffResult)
        newList.addWeakCallback(diffSnapshot, mPagedListCallback)
        if (!mPagedList!!.isEmpty()) {
            val newPosition = PagedStorageDiffHelper.transformAnchorIndex(diffResult, previousSnapshot.mStorage, diffSnapshot.mStorage,
                    lastAccessIndex)
            mPagedList!!.loadAround(0.coerceAtLeast((mPagedList!!.size - 1).coerceAtMost(newPosition)))
        }
        onCurrentListChanged(previousSnapshot, mPagedList, commitCallback)
    }

    open fun onCurrentListChanged(previousList: PagedList<T>?, currentList: PagedList<T>?, commitCallback: Runnable?) {
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

    open val currentList: PagedList<T>?
        get() = when {
            mSnapshot != null -> mSnapshot
            else -> mPagedList
        }

    // custom

    open fun loadAround(index: Int) = mPagedList?.loadAround(index) ?: Unit

    open fun scheduleInsert(t: T?) {
        if (mPagedList is MyContiguousPagedList2<*, *>) {
            (mPagedList as MyContiguousPagedList2<Any?, Any?>).scheduleInsert(t)
        }
    }
}

open class AdapterListUpdateCallbackWrapper(open val adapter: RecyclerView.Adapter<*>, open val mUpdateCallback: ListUpdateCallback) : ListUpdateCallback {
    open fun updatePos(mAdapter: PagingAdapter3<Any, RecyclerView.ViewHolder>, position: Int): Int {
        val header = when (mAdapter.hasHeader()) {
            true -> 1
            else -> 0
        }
        return position + header
    }

    override fun onInserted(position: Int, count: Int) {
        if (adapter is PagingAdapterEnhance<*, *> && (adapter as PagingAdapterEnhance<*, *>).listWrapper != null) {
            return
        }
        var pos = position
        if (adapter is PagingAdapter3<*, *>) {
            val mAdapter = adapter as PagingAdapter3<Any, RecyclerView.ViewHolder>
            if (count > 0 && mAdapter.isEmpty) {
                mAdapter.isEmpty = false
            }
            pos = updatePos(mAdapter, pos)
        }
        mUpdateCallback.onInserted(pos, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        if (adapter is PagingAdapterEnhance<*, *> && (adapter as PagingAdapterEnhance<*, *>).listWrapper != null) {
            return
        }
        var pos = position
        if (adapter is PagingAdapter3<*, *>) {
            val mAdapter = adapter as PagingAdapter3<Any, RecyclerView.ViewHolder>
            if (count > 0 && position == 0 && mAdapter.getDataItemCount() == count) {
                mAdapter.isEmpty = true
            }
            pos = updatePos(mAdapter, pos)
        }
        mUpdateCallback.onInserted(pos, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        if (adapter is PagingAdapterEnhance<*, *> && (adapter as PagingAdapterEnhance<*, *>).listWrapper != null) {
            return
        }
        var from = fromPosition
        var to = toPosition
        if (adapter is PagingAdapter3<*, *>) {
            val mAdapter = adapter as PagingAdapter3<Any, RecyclerView.ViewHolder>
            if (mAdapter.isEmpty) {
                mAdapter.isEmpty = false
            }
            from = updatePos(mAdapter, from)
            to = updatePos(mAdapter, to)
        }
        mUpdateCallback.onMoved(from, to)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        if (adapter is PagingAdapterEnhance<*, *> && (adapter as PagingAdapterEnhance<*, *>).listWrapper != null) {
            return
        }
        var pos = position
        if (adapter is PagingAdapter3<*, *>) {
            val mAdapter = adapter as PagingAdapter3<Any, RecyclerView.ViewHolder>
            if (count > 0 && mAdapter.isEmpty) {
                mAdapter.isEmpty = false
            }
            pos = updatePos(mAdapter, pos)
        }
        mUpdateCallback.onChanged(pos, count, payload)
    }
}
