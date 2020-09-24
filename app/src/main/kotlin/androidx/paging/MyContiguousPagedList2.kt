package androidx.paging

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.IntDef
import androidx.annotation.MainThread
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Executor

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 */
@Suppress("MemberVisibilityCanBePrivate")
open class MyContiguousPagedList2<K, V>(dataSource: MyContiguousDataSource2<K, V>, mainThreadExecutor: Executor, backgroundThreadExecutor: Executor,
                                        boundaryCallback: BoundaryCallback<V>?, config: Config, key: K?, lastLoad: Int
) : PagedList<V>(PagedStorage<V>(), mainThreadExecutor, backgroundThreadExecutor, boundaryCallback, config), PagedStorage.Callback,
        DataSource.InvalidatedCallback {
    open val mDataSource: MyContiguousDataSource2<K, V> = dataSource

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(READY_TO_FETCH, FETCHING, DONE_FETCHING)
    internal annotation class FetchState

    @FetchState
    var mPrependWorkerState = READY_TO_FETCH

    @FetchState
    var mAppendWorkerState = READY_TO_FETCH

    var mPrependItemsRequested = 0
    var mAppendItemsRequested = 0
    var mReplacePagesWithNulls = false
    val mShouldTrim = (mDataSource.supportsPageDropping() && mConfig.maxSize != Config.MAX_SIZE_UNBOUNDED)
    internal var mReceiver: PageResult.Receiver<V> = object : PageResult.Receiver<V>() {
        @SuppressLint("WrongThread")
        @AnyThread
        override fun onPageResult(@PageResult.ResultType resultType: Int, pageResult: PageResult<V>) {
            if (pageResult.isInvalid) {
                detach()
                return
            }
            if (isDetached) {
                return
            }
            val page = pageResult.page
            if (resultType == PageResult.INIT) {
                if (page.isNotEmpty()) {
                    mPages.add(page)
                }
                mStorage.init(pageResult.leadingNulls, page, pageResult.trailingNulls, pageResult.positionOffset, this@MyContiguousPagedList2)
                if (mLastLoad == LAST_LOAD_UNSPECIFIED) {
                    mLastLoad = pageResult.leadingNulls + pageResult.positionOffset + page.size / 2
                }
            } else {
                val trimFromFront = mLastLoad > mStorage.middleOfLoadedRange
                val skipNewPage = (mShouldTrim && mStorage.shouldPreTrimNewPage(mConfig.maxSize, mRequiredRemainder, page.size))
                if (resultType == PageResult.APPEND) {
                    if (skipNewPage && !trimFromFront) {
                        mAppendItemsRequested = 0
                        mAppendWorkerState = READY_TO_FETCH
                    } else {
                        if (page.isNotEmpty()) {
                            mPages.add(page)
                        }
                        mStorage.appendPage(page, this@MyContiguousPagedList2)
                    }
                } else if (resultType == PageResult.PREPEND) {
                    if (skipNewPage && trimFromFront) {
                        mPrependItemsRequested = 0
                        mPrependWorkerState = READY_TO_FETCH
                    } else {
                        if (page.isNotEmpty()) {
                            mPages.add(0, page)
                        }
                        mStorage.prependPage(page, this@MyContiguousPagedList2)
                    }
                } else if (resultType == MyPageKeyedDataSource2.INSERT) {
                    if (pageResult is GradePageResult) {
                        insertPage(pageResult, page)
                    }
                } else {
                    throw IllegalArgumentException("unexpected resultType $resultType")
                }
                if (mShouldTrim) {
                    if (trimFromFront) {
                        if (mPrependWorkerState != FETCHING) {
                            beforeTrim()
                            if (mStorage.trimFromFront(mReplacePagesWithNulls, mConfig.maxSize, mRequiredRemainder, this@MyContiguousPagedList2)) {
                                mPrependWorkerState = READY_TO_FETCH
                            }
                        }
                    } else {
                        if (mAppendWorkerState != FETCHING) {
                            beforeTrim()
                            if (mStorage.trimFromEnd(mReplacePagesWithNulls, mConfig.maxSize, mRequiredRemainder, this@MyContiguousPagedList2)) {
                                mAppendWorkerState = READY_TO_FETCH
                            }
                        }
                    }
                }
            }
            if (mBoundaryCallback != null) {
                val deferEmpty = mStorage.size == 0
                val deferBegin = (!deferEmpty && resultType == PageResult.PREPEND && pageResult.page.size == 0)
                val deferEnd = (!deferEmpty && resultType == PageResult.APPEND && pageResult.page.size == 0)
                deferBoundaryCallbacks(deferEmpty, deferBegin, deferEnd)
            }
        }
    }

    // ---------- below: custom ----------

    // TODO: 只考虑一级+二级，暂时不考虑往二级中插入三级、四级
    open val mPages = arrayListOf<MutableList<V>>()
    open val insertPages = mutableMapOf<V, MutableList<V>>()
    open var insertSize = 0
    open val insertWorkerStates = mutableMapOf<V, Int>()

    open fun getInsertWorkState(v: V): Int {
        var state = insertWorkerStates[v]
        if (state == null) {
            state = READY_TO_FETCH
            insertWorkerStates[v] = state
        }
        return state
    }

    internal fun insertPage(pageResult: GradePageResult<V>, page: MutableList<V>) {
        val origin = pageResult.origin
        if (page.isEmpty()) {
            mCallbacks.forEach {
                it.get()?.emptyInsert(origin)
            }
            insertWorkerStates[origin] = DONE_FETCHING
            return
        }
        var start = 0
        for (page2 in mPages) {
            if (page2.contains(origin)) {
                val originIndex = page2.indexOf(origin)
                var index = originIndex + 1
                if (insertPages.containsKey(origin)) {
                    index += insertPages[origin]!!.size
                    insertPages[origin]!!.addAll(page)
                } else {
                    insertPages[origin] = page
                }
                page2.addAll(index, page)
                insertSize += page.size
                this@MyContiguousPagedList2.onPageInserted(start + index, page.size)
                mCallbacks.forEach { it.get()?.insertPage(origin, start + originIndex, start + index, page.size) }
                break
            } else {
                start += page2.size
            }
        }
        insertWorkerStates[origin] = READY_TO_FETCH
    }

    protected open var trimFlag = false
    open fun beforeTrim() {
        if (insertSize == 0) {
            return
        }
        val abouts = insertPages.keys
        for (page2 in mPages) {
            for (about in abouts) {
                if (page2.contains(about)) {
                    page2.removeAll(insertPages[about]!!)
                    // page2.subList(page2.indexOf(about) + 1, insertPages[about]!!.size + page2.indexOf(about) + 1).clear()
                }
            }
        }
        insertSize = 0
        trimFlag = true
        Log.d(TAG, "beforeTrim")
    }

    protected open fun trimPages(startOfDrops: Int, count: Int) {
        if (!trimFlag) {
            return
        }
        val origins = insertPages.keys
        var realCount = count
        var limitFlag = false
        if (startOfDrops <= mStorage.leadingNullCount) {
            val first = mStorage.firstLoadedItem
            var pageCount = 0
            for (page in mPages) {
                if (limitFlag) {
                    page.forEach {
                        if (origins.contains(it)) {
                            page.addAll(page.indexOf(it) + 1, insertPages[it]!!)
                        }
                    }
                    continue
                }
                if (page[0] == first) {
                    limitFlag = true
                    continue
                }
                page.forEach {
                    if (origins.contains(it)) {
                        realCount += insertPages[it]!!.size
                        origins.remove(it)
                        insertWorkerStates.remove(it)
                    }
                }
                pageCount++
            }
            mPages.subList(0, pageCount).clear()
        } else {
            val last = mStorage.lastLoadedItem
            var pageCount = 0
            for (page in mPages.reversed()) {
                if (limitFlag) {
                    page.forEach {
                        if (origins.contains(it)) {
                            page.addAll(page.indexOf(it) + 1, insertPages[it]!!)
                        }
                    }
                    continue
                }
                if (page.last() == last) {
                    limitFlag = true
                    continue
                }
                page.forEach {
                    if (origins.contains(it)) {
                        realCount += insertPages[it]!!.size
                        origins.remove(it)
                        insertWorkerStates.remove(it)
                    }
                }
                pageCount++
            }
            mPages.subList(mPages.size - pageCount, mPages.size).clear()
        }
        notifyRemoved(startOfDrops, realCount)
        trimFlag = false
        Log.d(TAG, "trimPages: $startOfDrops")
    }

    override val size: Int
        get() {
            Log.d(TAG, "getSize: ${super.size + insertSize}")
            return super.size + insertSize
        }

    override fun get(index: Int): V? {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        val localIndex = index - mStorage.leadingNullCount
        if (localIndex < 0 || localIndex >= mStorage.storageCount + insertSize) {
            return null
        }
        var pageInternalIndex: Int = localIndex
        val localPageCount = mPages.size
        var localPageIndex: Int = 0
        while (localPageIndex < localPageCount) {
            val pageSize = mPages[localPageIndex].size
            if (pageSize > pageInternalIndex) {
                break
            }
            pageInternalIndex -= pageSize
            localPageIndex++
        }
        val page = mPages.getOrNull(localPageIndex)
        return when {
            page.isNullOrEmpty() -> null
            else -> page[pageInternalIndex]
        }
    }

    @MainThread
    open fun scheduleInsert(v: V) {
        if (getInsertWorkState(v) != READY_TO_FETCH) {
            return
        }
        insertWorkerStates[v] = FETCHING
        mBackgroundThreadExecutor.execute(Runnable {
            if (isDetached) {
                return@Runnable
            }
            if (mDataSource.isInvalid) {
                detach()
            } else {
                mDataSource.dispatchLoadInsert(v, mConfig.pageSize, mMainThreadExecutor, mReceiver)
            }
        })
        Log.d(TAG, "scheduleInsert: $v")
    }

    // ---------- above: custom ----------

    @MainThread
    override fun dispatchUpdatesSinceSnapshot(pagedListSnapshot: PagedList<V>, callback: Callback) {
        val snapshot = pagedListSnapshot.mStorage
        val newlyAppended = mStorage.numberAppended - snapshot.numberAppended
        val newlyPrepended = mStorage.numberPrepended - snapshot.numberPrepended
        val previousTrailing = snapshot.trailingNullCount
        val previousLeading = snapshot.leadingNullCount
        require(!(snapshot.isEmpty() || newlyAppended < 0 || newlyPrepended < 0
                || mStorage.trailingNullCount != (previousTrailing - newlyAppended).coerceAtLeast(0)
                || mStorage.leadingNullCount != (previousLeading - newlyPrepended).coerceAtLeast(0)
                || (mStorage.storageCount != snapshot.storageCount + newlyAppended + newlyPrepended))) {
            "Invalid snapshot provided - doesn't appear to be a snapshot of this PagedList"
        }
        if (newlyAppended != 0) {
            val changedCount = previousTrailing.coerceAtMost(newlyAppended)
            val addedCount = newlyAppended - changedCount
            val endPosition = snapshot.leadingNullCount + snapshot.storageCount
            if (changedCount != 0) {
                callback.onChanged(endPosition, changedCount)
            }
            if (addedCount != 0) {
                callback.onInserted(endPosition + changedCount, addedCount)
            }
        }
        if (newlyPrepended != 0) {
            val changedCount = previousLeading.coerceAtMost(newlyPrepended)
            val addedCount = newlyPrepended - changedCount
            if (changedCount != 0) {
                callback.onChanged(previousLeading, changedCount)
            }
            if (addedCount != 0) {
                callback.onInserted(0, addedCount)
            }
        }
    }

    @MainThread
    override fun loadAroundInternal(index: Int) {
        val prependItems = getPrependItemsRequested(mConfig.prefetchDistance, index, mStorage.leadingNullCount)
        val appendItems = getAppendItemsRequested(mConfig.prefetchDistance, index, mStorage.leadingNullCount + mStorage.storageCount + insertSize)
        mPrependItemsRequested = prependItems.coerceAtLeast(mPrependItemsRequested)
        if (mPrependItemsRequested > 0) {
            schedulePrepend()
        }
        mAppendItemsRequested = appendItems.coerceAtLeast(mAppendItemsRequested)
        if (mAppendItemsRequested > 0) {
            scheduleAppend()
        }
    }

    @MainThread
    private fun schedulePrepend() {
        if (mPrependWorkerState != READY_TO_FETCH) {
            return
        }
        mPrependWorkerState = FETCHING
        val position = mStorage.leadingNullCount + mStorage.positionOffset
        val item: V = mStorage.firstLoadedItem
        mBackgroundThreadExecutor.execute(Runnable {
            if (isDetached) {
                return@Runnable
            }
            if (mDataSource.isInvalid) {
                detach()
            } else {
                mDataSource.dispatchLoadBefore(position, item, mConfig.pageSize, mMainThreadExecutor, mReceiver)
            }
        })
    }

    @MainThread
    private fun scheduleAppend() {
        if (mAppendWorkerState != READY_TO_FETCH) {
            return
        }
        mAppendWorkerState = FETCHING
        val position = mStorage.leadingNullCount + mStorage.storageCount - 1 + mStorage.positionOffset
        val item: V = mStorage.lastLoadedItem
        mBackgroundThreadExecutor.execute(Runnable {
            if (isDetached) {
                return@Runnable
            }
            if (mDataSource.isInvalid) {
                detach()
            } else {
                mDataSource.dispatchLoadAfter(position + insertSize, item, mConfig.pageSize, mMainThreadExecutor, mReceiver)
            }
        })
    }

    override fun isContiguous(): Boolean {
        return true
    }

    override fun getDataSource(): DataSource<*, V> {
        return mDataSource
    }

    override fun getLastKey(): Any? {
        return mDataSource.getKey(mLastLoad, mLastItem)
    }

    @MainThread
    override fun onInitialized(count: Int) {
        notifyInserted(0, count)
        mCallbacks.forEach { it.get()?.initialPage(mStorage.leadingNullCount, mStorage.trailingNullCount, count) }
        mReplacePagesWithNulls = mStorage.leadingNullCount > 0 || mStorage.trailingNullCount > 0
    }

    @SuppressLint("RestrictedApi")
    @MainThread
    override fun onPagePrepended(leadingNulls: Int, changedCount: Int, addedCount: Int) {
        mPrependItemsRequested = mPrependItemsRequested - changedCount - addedCount
        mPrependWorkerState = READY_TO_FETCH
        if (mPrependItemsRequested > 0) {
            schedulePrepend()
        }
        notifyChanged(leadingNulls, changedCount)
        notifyInserted(0, addedCount)
        mCallbacks.forEach { it.get()?.prependPage(leadingNulls, changedCount, addedCount) }
        offsetAccessIndices(addedCount)
    }

    @MainThread
    override fun onEmptyPrepend() {
        mPrependWorkerState = DONE_FETCHING
        mCallbacks.forEach {
            it.get()?.emptyPrepend()
        }
    }

    @MainThread
    override fun onPageAppended(endPosition: Int, changedCount: Int, addedCount: Int) {
        mAppendItemsRequested = mAppendItemsRequested - changedCount - addedCount
        mAppendWorkerState = READY_TO_FETCH
        if (mAppendItemsRequested > 0) {
            scheduleAppend()
        }
        notifyChanged(endPosition + insertSize, changedCount)
        notifyInserted(endPosition + changedCount + insertSize, addedCount)
        mCallbacks.forEach { it.get()?.appendPage(endPosition + insertSize, changedCount, addedCount) }
    }

    override fun onPagePlaceholderInserted(pageIndex: Int) {
        throw IllegalStateException("Tiled callback on MyContiguousPagedList")
    }

    @MainThread
    override fun onEmptyAppend() {
        mAppendWorkerState = DONE_FETCHING
        mCallbacks.forEach {
            it.get()?.emptyAppend()
        }
    }

    @MainThread
    override fun onPageInserted(start: Int, count: Int) {
        mAppendWorkerState = READY_TO_FETCH
        notifyInserted(start, count)
    }

    override fun onPagesRemoved(startOfDrops: Int, count: Int) {
        if (!trimFlag) {
            notifyRemoved(startOfDrops, count)
        }
        trimPages(startOfDrops, count)
    }

    override fun onPagesSwappedToPlaceholder(startOfDrops: Int, count: Int) {
        if (!trimFlag) {
            notifyChanged(startOfDrops, count)
        }
        trimPages(startOfDrops, count)
    }

    companion object {
        private const val READY_TO_FETCH = 0
        private const val FETCHING = 1
        private const val DONE_FETCHING = 2
        const val LAST_LOAD_UNSPECIFIED = -1

        private val TAG = MyContiguousPagedList2::class.java.canonicalName
                ?: "MyContiguousPagedList2"

        fun getPrependItemsRequested(prefetchDistance: Int, index: Int, leadingNulls: Int): Int {
            return prefetchDistance - (index - leadingNulls)
        }

        fun getAppendItemsRequested(prefetchDistance: Int, index: Int, itemsBeforeTrailingNulls: Int): Int {
            return index + prefetchDistance + 1 - itemsBeforeTrailingNulls
        }
    }

    init {
        mLastLoad = lastLoad
        if (mDataSource.isInvalid) {
            detach()
        } else {
            mDataSource.dispatchLoadInitial(key, mConfig.initialLoadSizeHint, mConfig.pageSize, mConfig.enablePlaceholders, mMainThreadExecutor,
                    mReceiver)
        }
    }

    // custom

    abstract class DefaultCallback : Callback() {
        override fun onChanged(position: Int, count: Int) = Unit
        override fun onInserted(position: Int, count: Int) = Unit
        override fun onRemoved(position: Int, count: Int) = Unit
    }

    abstract class MoreCallback<V> : DefaultCallback() {
        open fun emptyPrepend() = Unit
        open fun emptyAppend() = Unit
        open fun emptyInsert(v: V) = Unit

        // 下面的 position 相对于该 pageList 绝对正确，但不一定相对于 adapter 正确，因为 adapter 中可能有 footer / header / loading / load_error

        // 根据 loadInitial 决定有多少 leadingNull ，有多少 trailingNull ，这时候 count = mStorage.size = leadingNull + size + trailingNull
        open fun initialPage(leadingNulls: Int, trailingNulls: Int, count: Int) = Unit
        open fun prependPage(position: Int, changedCount: Int, count: Int) = Unit
        open fun appendPage(position: Int, changedCount: Int, count: Int) = Unit
        open fun insertPage(v: V, vPos: Int, position: Int, count: Int) = Unit
    }

    protected open val mCallbacks = ArrayList<WeakReference<MoreCallback<V>>>()

    override fun addWeakCallback(previousSnapshot: MutableList<V>?, callback: Callback) {
        super.addWeakCallback(previousSnapshot, callback)
        if (callback is MoreCallback<*>) {
            for (i in mCallbacks.indices.reversed()) {
                val currentCallback: Callback? = mCallbacks[i].get()
                if (currentCallback == null) {
                    mCallbacks.removeAt(i)
                }
            }
            mCallbacks.add(WeakReference(callback as MoreCallback<V>))
        }
    }

    override fun removeWeakCallback(callback: Callback) {
        super.removeWeakCallback(callback)
        if (callback is MoreCallback<*>) {
            for (i in mCallbacks.indices.reversed()) {
                val currentCallback: Callback? = mCallbacks[i].get()
                if (currentCallback == null || currentCallback === callback) {
                    mCallbacks.removeAt(i)
                }
            }
        }
    }

    override fun onInvalidated() {
        mPages.clear()
        insertPages.clear()
        insertSize = 0
        insertWorkerStates.clear()
    }

    // TODO: init 加上 cache
    // TODO: 对二级的增加删除改变

    open fun invalidate() {
        mDataSource.invalidate()
    }
}
