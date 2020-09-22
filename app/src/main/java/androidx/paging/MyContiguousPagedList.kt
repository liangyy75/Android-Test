package androidx.paging

import android.annotation.SuppressLint
import androidx.annotation.AnyThread
import androidx.annotation.IntDef
import androidx.annotation.MainThread
import com.liang.example.basic_ktx.MutablePair
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.concurrent.Executor

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 */
open class MyContiguousPagedList<K, V>(
        dataSource: MyContiguousDataSource<K, V>,
        mainThreadExecutor: Executor,
        backgroundThreadExecutor: Executor,
        boundaryCallback: PagedList.BoundaryCallback<V>?,
        config: PagedList.Config,
        key: K?,
        lastLoad: Int) : MyPagedList<V>(MyPagedStorage<V>(), mainThreadExecutor, backgroundThreadExecutor,
        boundaryCallback, config), MyPagedStorage.Callback {
    val mDataSource: MyContiguousDataSource<K, V> = dataSource

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
    val mShouldTrim = (mDataSource.supportsPageDropping() && mConfig.maxSize != PagedList.Config.MAX_SIZE_UNBOUNDED)
    internal var mReceiver: PageResult.Receiver<V> = object : PageResult.Receiver<V>() {
        @SuppressLint("WrongThread")
        @AnyThread
        override fun onPageResult(@PageResult.ResultType resultType: Int, pageResult: PageResult<V>) {
            if (pageResult.isInvalid) {
                detach()
                return
            }
            if (isDetached()) {
                return
            }
            val page = pageResult.page
            if (resultType == PageResult.INIT) {
                mStorage.init(pageResult.leadingNulls, page, pageResult.trailingNulls, pageResult.positionOffset, this@MyContiguousPagedList)
                if (mLastLoad == LAST_LOAD_UNSPECIFIED) {
                    mLastLoad = pageResult.leadingNulls + pageResult.positionOffset + page.size / 2
                }
                if (page.isNotEmpty()) {
                    mPages.add(page)
                }
            } else {
                val trimFromFront = mLastLoad > mStorage.getMiddleOfLoadedRange()
                val skipNewPage = (mShouldTrim && mStorage.shouldPreTrimNewPage(mConfig.maxSize, mRequiredRemainder, page.size))
                if (resultType == PageResult.APPEND) {
                    if (skipNewPage && !trimFromFront) {
                        mAppendItemsRequested = 0
                        mAppendWorkerState = READY_TO_FETCH
                    } else {
                        mStorage.appendPage(page, this@MyContiguousPagedList)
                        if (page.isNotEmpty()) {
                            mPages.add(page)
                        }
                    }
                } else if (resultType == PageResult.PREPEND) {
                    if (skipNewPage && trimFromFront) {
                        mPrependItemsRequested = 0
                        mPrependWorkerState = READY_TO_FETCH
                    } else {
                        mStorage.prependPage(page, this@MyContiguousPagedList)
                        if (page.isNotEmpty()) {
                            mPages.add(0, page)
                        }
                    }
                } else if (resultType == MyPageKeyedDataSource.INSERT) {
                    if (pageResult is PageResult2) {
                        val about = pageResult.aboutItem
                        // mStorage.insertPage2(about, page, this@MyContiguousPagedList)
                        var start = 0
                        for (page2 in mPages) {
                            if (page2.contains(about)) {
                                var index = page2.indexOf(about) + 1
                                if (aboutPages.containsKey(about)) {
                                    index += aboutPages[about]!!.size
                                    aboutPages[about]!!.addAll(page)
                                } else {
                                    aboutPages[about] = page
                                }
                                page2.addAll(index, page)
                                aboutSize += page.size
                                this@MyContiguousPagedList.onPageInserted(start + index, page.size)
                                break
                            } else {
                                start += page2.size
                            }
                        }
                    } else {
                        mAppendWorkerState = READY_TO_FETCH
                    }
                } else {
                    throw IllegalArgumentException("unexpected resultType $resultType")
                }
                if (mShouldTrim) {
                    if (trimFromFront) {
                        if (mPrependWorkerState != FETCHING) {
                            trimAbout()
                            if (mStorage.trimFromFront(mReplacePagesWithNulls, mConfig.maxSize, mRequiredRemainder, this@MyContiguousPagedList)) {
                                mPrependWorkerState = READY_TO_FETCH
                            }
                            restoreAbout()
                        }
                    } else {
                        if (mAppendWorkerState != FETCHING) {
                            trimAbout()
                            if (mStorage.trimFromEnd(mReplacePagesWithNulls, mConfig.maxSize, mRequiredRemainder, this@MyContiguousPagedList)) {
                                mAppendWorkerState = READY_TO_FETCH
                            }
                            restoreAbout()
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

    // TODO: 只考虑一级+二级，暂时不考虑往二级中插入三级
    open val mPages = arrayListOf<MutableList<V>>()
    open val aboutPages = mutableMapOf<V, MutableList<V>>()
    open var aboutSize = 0

    open fun trimAbout() {
        val abouts = aboutPages.keys
        for (page2 in mPages) {
            for (about in abouts) {
                if (page2.contains(about)) {
                    page2.removeAll(aboutPages[about]!!)
                }
            }
        }
        aboutSize = 0
    }

    open fun restoreAbout() {
        val abouts = aboutPages.keys.map { MutablePair(it, true) }
        for (page2 in mPages) {
            for (about in abouts) {
                val trueAbout = about.first
                if (page2.contains(trueAbout)) {
                    val aboutPage = aboutPages[trueAbout]!!
                    page2.addAll(aboutPage)
                    aboutSize += aboutPage.size
                    about.second = false
                }
            }
        }
        for (about in abouts) {
            if (about.second) {
                aboutPages.remove(about.first)
            }
        }
    }

    open protected fun handlePages(startOfDrops: Int) {
        if (startOfDrops <= mStorage.getLeadingNullCount()) {
            val first = mStorage.getFirstLoadedItem()
            var count = 0
            for (page in mPages) {
                if (page[0] == first) {
                    break
                }
                count++
            }
            mPages.subList(0, count).clear()
        } else {
            val last = mStorage.getLastLoadedItem()
            var count = 0
            for (page in mPages.reversed()) {
                if (page.last() == last) {
                    break
                }
                count++
            }
            mPages.subList(mPages.size - count, mPages.size).clear()
        }
    }

    override val size: Int
        get() = super.size + aboutSize

    override fun get(index: Int): V? {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        val localIndex = index - mStorage.getLeadingNullCount()
        if (localIndex < 0 || localIndex >= mStorage.getStorageCount() + aboutSize) {
            return null
        }
        var pageInternalIndex: Int  = localIndex
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
    override fun dispatchUpdatesSinceSnapshot(pagedListSnapshot: PagedList<V>, callback: Callback) {
        val snapshot = pagedListSnapshot.mStorage
        val newlyAppended = mStorage.getNumberAppended() - snapshot.numberAppended
        val newlyPrepended = mStorage.getNumberPrepended() - snapshot.numberPrepended
        val previousTrailing = snapshot.trailingNullCount
        val previousLeading = snapshot.leadingNullCount
        require(!(snapshot.isEmpty()
                || newlyAppended < 0 || newlyPrepended < 0 || mStorage.getTrailingNullCount() != Math.max(previousTrailing - newlyAppended, 0)
                || mStorage.getLeadingNullCount() != Math.max(previousLeading - newlyPrepended, 0) || (mStorage.getStorageCount()
                != snapshot.storageCount + newlyAppended + newlyPrepended))) {
            ("Invalid snapshot provided - doesn't appear"
                    + " to be a snapshot of this PagedList")
        }
        if (newlyAppended != 0) {
            val changedCount = Math.min(previousTrailing, newlyAppended)
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
            val changedCount = Math.min(previousLeading, newlyPrepended)
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
        val prependItems = getPrependItemsRequested(mConfig.prefetchDistance, index,
                mStorage.getLeadingNullCount())
        val appendItems = getAppendItemsRequested(mConfig.prefetchDistance, index,
                mStorage.getLeadingNullCount() + mStorage.getStorageCount() + aboutSize)
        mPrependItemsRequested = Math.max(prependItems, mPrependItemsRequested)
        if (mPrependItemsRequested > 0) {
            schedulePrepend()
        }
        mAppendItemsRequested = Math.max(appendItems, mAppendItemsRequested)
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
        val position = mStorage.getLeadingNullCount() + mStorage.getPositionOffset()
        val item: V = mStorage.getFirstLoadedItem()
        mBackgroundThreadExecutor.execute(Runnable {
            if (isDetached()) {
                return@Runnable
            }
            if (mDataSource.isInvalid) {
                detach()
            } else {
                mDataSource.dispatchLoadBefore(position, item, mConfig.pageSize,
                        mMainThreadExecutor, mReceiver)
            }
        })
    }

    @MainThread
    private fun scheduleAppend() {
        if (mAppendWorkerState != READY_TO_FETCH) {
            return
        }
        mAppendWorkerState = FETCHING
        val position = mStorage.getLeadingNullCount() + mStorage.getStorageCount() - 1 + mStorage.getPositionOffset()
        val item: V = mStorage.getLastLoadedItem()
        mBackgroundThreadExecutor.execute(Runnable {
            if (isDetached()) {
                return@Runnable
            }
            if (mDataSource.isInvalid) {
                detach()
            } else {
                mDataSource.dispatchLoadAfter(position + aboutSize, item, mConfig.pageSize, mMainThreadExecutor, mReceiver)
            }
        })
    }

    @MainThread
    open fun scheduleInsert(v: V, key: K?, key2: K?, position: Int) {
        if (mAppendWorkerState != READY_TO_FETCH) {
            return
        }
        mAppendWorkerState = FETCHING
        val item: V = mStorage.getLastLoadedItem()
        mBackgroundThreadExecutor.execute(Runnable {
            if (isDetached()) {
                return@Runnable
            }
            if (mDataSource.isInvalid) {
                detach()
            } else {
                mDataSource.dispatchLoadInsert(v, key, key2, position, item, mConfig.pageSize, mMainThreadExecutor, mReceiver)
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
        mReplacePagesWithNulls = mStorage.getLeadingNullCount() > 0 || mStorage.getTrailingNullCount() > 0
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
        offsetAccessIndices(addedCount)
    }

    @MainThread
    override fun onEmptyPrepend() {
        mPrependWorkerState = DONE_FETCHING
    }

    @MainThread
    override fun onPageAppended(endPosition: Int, changedCount: Int, addedCount: Int) {
        mAppendItemsRequested = mAppendItemsRequested - changedCount - addedCount
        mAppendWorkerState = READY_TO_FETCH
        if (mAppendItemsRequested > 0) {
            scheduleAppend()
        }
        notifyChanged(endPosition, changedCount)
        notifyInserted(endPosition + changedCount + aboutSize, addedCount)
    }

    @MainThread
    override fun onEmptyAppend() {
        mAppendWorkerState = DONE_FETCHING
    }

    @MainThread
    override fun onPagePlaceholderInserted(pageIndex: Int, timeStamp: Long) {
        throw IllegalStateException("Tiled callback on MyContiguousPagedList")
    }

    @MainThread
    override fun onPageInserted(start: Int, count: Int) {
        mAppendWorkerState = READY_TO_FETCH
        notifyInserted(start, count)
    }

    override fun onPagesRemoved(startOfDrops: Int, count: Int) {
        notifyRemoved(startOfDrops, count)
        handlePages(startOfDrops)
    }

    override fun onPagesSwappedToPlaceholder(startOfDrops: Int, count: Int) {
        notifyChanged(startOfDrops, count)
        handlePages(startOfDrops)
    }

    companion object {
        private const val READY_TO_FETCH = 0
        private const val FETCHING = 1
        private const val DONE_FETCHING = 2
        const val LAST_LOAD_UNSPECIFIED = -1
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
            mDataSource.dispatchLoadInitial(key, mConfig.initialLoadSizeHint, mConfig.pageSize, mConfig.enablePlaceholders, mMainThreadExecutor, mReceiver)
        }
    }
}
