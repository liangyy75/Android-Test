package androidx.paging

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import java.util.concurrent.Executor

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
open class MyTiledPagedList<T> @WorkerThread constructor(
        dataSource: MyPositionalDataSource<T>,
        mainThreadExecutor: Executor,
        backgroundThreadExecutor: Executor,
        boundaryCallback: PagedList.BoundaryCallback<T>?,
        config: PagedList.Config,
        position: Int
) : MyPagedList<T>(MyPagedStorage<T>(), mainThreadExecutor, backgroundThreadExecutor, boundaryCallback, config), MyPagedStorage.Callback {
    val mDataSource: MyPositionalDataSource<T> = dataSource
    internal var mReceiver: PageResult.Receiver<T> = object : PageResult.Receiver<T>() {
        @AnyThread
        override fun onPageResult(@PageResult.ResultType type: Int, pageResult: PageResult<T>) {
            if (pageResult.isInvalid) {
                detach()
                return
            }
            if (isDetached()) {
                return
            }
            require(!(type != PageResult.INIT && type != PageResult.TILE)) { "unexpected resultType$type" }
            val page = pageResult.page
            if (mStorage.getPageCount() == 0) {
                mStorage.initAndSplit(pageResult.leadingNulls, page, pageResult.trailingNulls, pageResult.positionOffset, mConfig.pageSize, this@MyTiledPagedList)
            } else {
                mStorage.tryInsertPageAndTrim(pageResult.positionOffset, page, mLastLoad, mConfig.maxSize, mRequiredRemainder, this@MyTiledPagedList)
            }
            if (mBoundaryCallback != null) {
                val deferEmpty = mStorage.size == 0
                val deferBegin = (!deferEmpty && pageResult.leadingNulls == 0 && pageResult.positionOffset == 0)
                val size = size
                val deferEnd = (!deferEmpty && (type == PageResult.INIT && pageResult.trailingNulls == 0 || (type == PageResult.TILE
                        && pageResult.positionOffset + mConfig.pageSize >= size)))
                deferBoundaryCallbacks(deferEmpty, deferBegin, deferEnd)
            }
        }
    }

    override fun isContiguous(): Boolean {
        return false
    }

    override fun getDataSource(): DataSource<*, T> {
        return mDataSource
    }

    override fun getLastKey(): Any? {
        return mLastLoad
    }

    override fun dispatchUpdatesSinceSnapshot(pagedListSnapshot: PagedList<T>, callback: Callback) {
        val snapshot = pagedListSnapshot.mStorage
        require(!(snapshot.isEmpty()
                || mStorage.size != snapshot.size)) {
            ("Invalid snapshot provided - doesn't appear"
                    + " to be a snapshot of this PagedList")
        }

        // loop through each page and signal the callback for any pages that are present now,
        // but not in the snapshot.
        val pageSize = mConfig.pageSize
        val leadingNullPages = mStorage.getLeadingNullCount() / pageSize
        val pageCount = mStorage.getPageCount()
        var i = 0
        while (i < pageCount) {
            val pageIndex = i + leadingNullPages
            var updatedPages = 0
            // count number of consecutive pages that were added since the snapshot...
            while (updatedPages < mStorage.getPageCount() && mStorage.hasPage(pageSize, pageIndex + updatedPages)
                    && !snapshot.hasPage(pageSize, pageIndex + updatedPages)) {
                updatedPages++
            }
            // and signal them all at once to the callback
            if (updatedPages > 0) {
                callback.onChanged(pageIndex * pageSize, pageSize * updatedPages)
                i += updatedPages - 1
            }
            i++
        }
    }

    override fun loadAroundInternal(index: Int) {
        mStorage.allocatePlaceholders(index, mConfig.prefetchDistance, mConfig.pageSize, this)
    }

    open fun loadAround(index: Int, timeStamp: Long) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        mLastLoad = index + getPositionOffset()
        mLowestIndexAccessed = Math.min(mLowestIndexAccessed, index)
        mHighestIndexAccessed = Math.max(mHighestIndexAccessed, index)
        tryDispatchBoundaryCallbacks(true)
        mStorage.allocatePlaceholders(index, mConfig.prefetchDistance, mConfig.pageSize, this, timeStamp)
    }

    override fun onInitialized(count: Int) {
        notifyInserted(0, count)
    }

    override fun onPagePrepended(leadingNulls: Int, changed: Int, added: Int) {
        throw IllegalStateException("Contiguous callback on TiledPagedList")
    }

    override fun onPageAppended(endPosition: Int, changed: Int, added: Int) {
        throw IllegalStateException("Contiguous callback on TiledPagedList")
    }

    override fun onEmptyPrepend() {
        throw IllegalStateException("Contiguous callback on TiledPagedList")
    }

    override fun onEmptyAppend() {
        throw IllegalStateException("Contiguous callback on TiledPagedList")
    }

    override fun onPagePlaceholderInserted(pageIndex: Int, timeStamp: Long) {
        mBackgroundThreadExecutor.execute(Runnable {
            if (isDetached()) {
                return@Runnable
            }
            val pageSize = mConfig.pageSize
            if (mDataSource.isInvalid) {
                detach()
            } else {
                val startPosition = pageIndex * pageSize
                val count = pageSize.coerceAtMost(mStorage.size - startPosition)
                mDataSource.dispatchLoadRange(PageResult.TILE, startPosition, count, mMainThreadExecutor, mReceiver, timeStamp)
            }
        })
    }

    override fun onPageInserted(start: Int, count: Int) {
        notifyChanged(start, count)
    }

    override fun onPagesRemoved(startOfDrops: Int, count: Int) {
        notifyRemoved(startOfDrops, count)
    }

    override fun onPagesSwappedToPlaceholder(startOfDrops: Int, count: Int) {
        notifyChanged(startOfDrops, count)
    }

    init {
        val pageSize = mConfig.pageSize
        mLastLoad = position
        if (mDataSource.isInvalid) {
            detach()
        } else {
            val firstLoadSize = (mConfig.initialLoadSizeHint / pageSize).coerceAtLeast(2) * pageSize
            val idealStart = position - firstLoadSize / 2
            val roundedPageStart = 0.coerceAtLeast(idealStart / pageSize * pageSize)
            mDataSource.dispatchLoadInitial(true, roundedPageStart, firstLoadSize, pageSize, mMainThreadExecutor, mReceiver, System.currentTimeMillis())
        }
    }
}
