package androidx.paging

import androidx.annotation.WorkerThread
import androidx.arch.core.util.Function
import java.util.concurrent.Executor

interface ParamsDelegate {
    fun getInitialParams(acceptCount: Boolean, requestedStartPosition: Int, requestedLoadSize: Int,
                         pageSize: Int, recentTimeStamp: Long): MyPositionalDataSource.MyLoadInitialParams {
        return MyPositionalDataSource.MyLoadInitialParams(requestedStartPosition, requestedLoadSize, pageSize, acceptCount)
    }
    fun getRangeParams(startPosition: Int, count: Int, recentTimeStamp: Long): MyPositionalDataSource.MyLoadRangeParams {
        return MyPositionalDataSource.MyLoadRangeParams(startPosition, count)
    }
}

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
abstract class MyPositionalDataSource<T> : DataSource<Int, T>() {
    open var delegate: ParamsDelegate? = null

    open class MyLoadInitialParams(
            open val requestedStartPosition: Int,
            open val requestedLoadSize: Int,
            open val pageSize: Int,
            open val placeholdersEnabled: Boolean)

    open class MyLoadRangeParams(open val startPosition: Int, open val loadSize: Int)

    abstract class LoadInitialCallback<T> {
        abstract fun onResult(data: List<T>, position: Int, totalCount: Int)
        abstract fun onResult(data: List<T>, position: Int)
    }

    abstract class LoadRangeCallback<T> {
        abstract fun onResult(data: List<T>)
    }

    internal class LoadInitialCallbackImpl<T>(dataSource: MyPositionalDataSource<*>, countingEnabled: Boolean,
                                              pageSize: Int, receiver: PageResult.Receiver<T>?) : LoadInitialCallback<T>() {
        val mCallbackHelper: LoadCallbackHelper<T?> = LoadCallbackHelper(dataSource, PageResult.INIT, null, receiver!!)
        val mCountingEnabled: Boolean = countingEnabled
        val mPageSize: Int = pageSize
        override fun onResult(data: List<T>, position: Int, totalCount: Int) {
            if (!mCallbackHelper.dispatchInvalidResultIfInvalid()) {
                LoadCallbackHelper.validateInitialLoadParams(data, position, totalCount)
                require(!(position + data.size != totalCount && data.size % mPageSize != 0)) {
                    ("MyPositionalDataSource requires initial load"
                            + " size to be a multiple of page size to support internal tiling."
                            + " loadSize " + data.size + ", position " + position
                            + ", totalCount " + totalCount + ", pageSize " + mPageSize)
                }
                if (mCountingEnabled) {
                    val trailingUnloadedCount = totalCount - position - data.size
                    mCallbackHelper.dispatchResultToReceiver(PageResult(data, position, trailingUnloadedCount, 0))
                } else {
                    mCallbackHelper.dispatchResultToReceiver(PageResult(data, position))
                }
            }
        }

        override fun onResult(data: List<T>, position: Int) {
            if (!mCallbackHelper.dispatchInvalidResultIfInvalid()) {
                require(position >= 0) { "Position must be non-negative" }
                require(!(data.isEmpty() && position != 0)) { "Initial result cannot be empty if items are present in data set." }
                check(!mCountingEnabled) {
                    ("Placeholders requested, but totalCount not"
                            + " provided. Please call the three-parameter onResult method, or"
                            + " disable placeholders in the PagedList.Config")
                }
                mCallbackHelper.dispatchResultToReceiver(PageResult(data, position))
            }
        }

        init {
            require(mPageSize >= 1) { "Page size must be non-negative" }
        }
    }

    internal class LoadRangeCallbackImpl<T>(dataSource: MyPositionalDataSource<*>, @PageResult.ResultType resultType: Int, positionOffset: Int,
                                            mainThreadExecutor: Executor?, receiver: PageResult.Receiver<T>) : LoadRangeCallback<T>() {
        val mCallbackHelper: LoadCallbackHelper<T> = LoadCallbackHelper(dataSource, resultType, mainThreadExecutor, receiver)
        val mPositionOffset: Int = positionOffset
        override fun onResult(data: List<T>) {
            if (!mCallbackHelper.dispatchInvalidResultIfInvalid()) {
                mCallbackHelper.dispatchResultToReceiver(PageResult(data, 0, 0, mPositionOffset))
            }
        }
    }

    internal fun dispatchLoadInitial(acceptCount: Boolean, requestedStartPosition: Int, requestedLoadSize: Int, pageSize: Int, mainThreadExecutor: Executor,
                                     receiver: PageResult.Receiver<T>, recentTimeStamp: Long) {
        val callback = LoadInitialCallbackImpl(this, acceptCount, pageSize, receiver)
        loadInitial(getInitialParams(acceptCount, requestedStartPosition, requestedLoadSize, pageSize, recentTimeStamp), callback)
        callback.mCallbackHelper.setPostExecutor(mainThreadExecutor)
    }

    open fun getInitialParams(acceptCount: Boolean, requestedStartPosition: Int, requestedLoadSize: Int,
                              pageSize: Int, recentTimeStamp: Long): MyLoadInitialParams {
        return delegate?.getInitialParams(acceptCount, requestedStartPosition, requestedLoadSize, pageSize, recentTimeStamp)
                ?: MyLoadInitialParams(requestedStartPosition, requestedLoadSize, pageSize, acceptCount)
    }

    internal fun dispatchLoadRange(@PageResult.ResultType resultType: Int, startPosition: Int, count: Int, mainThreadExecutor: Executor,
                                   receiver: PageResult.Receiver<T>, recentTimeStamp: Long) {
        val callback: LoadRangeCallback<T> = LoadRangeCallbackImpl(this, resultType, startPosition, mainThreadExecutor, receiver)
        if (count == 0) {
            callback.onResult(emptyList())
        } else {
            loadRange(getRangeParams(startPosition, count, recentTimeStamp), callback)
        }
    }

    open fun getRangeParams(startPosition: Int, count: Int, recentTimeStamp: Long): MyLoadRangeParams {
        return delegate?.getRangeParams(startPosition, count, recentTimeStamp)
                ?: MyLoadRangeParams(startPosition, count)
    }

    @WorkerThread
    abstract fun loadInitial(params: MyLoadInitialParams, callback: LoadInitialCallback<T>)

    @WorkerThread
    abstract fun loadRange(params: MyLoadRangeParams, callback: LoadRangeCallback<T>)

    override fun isContiguous(): Boolean = false

    internal fun wrapAsContiguousWithoutPlaceholders(): ContiguousDataSource<Int, T> {
        return ContiguousWithoutPlaceholdersWrapper(this)
    }

    internal class ContiguousWithoutPlaceholdersWrapper<Value>(val mSource: MyPositionalDataSource<Value>) : ContiguousDataSource<Int, Value>() {
        override fun addInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) = mSource.addInvalidatedCallback(onInvalidatedCallback)
        override fun removeInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) = mSource.removeInvalidatedCallback(onInvalidatedCallback)
        override fun invalidate() = mSource.invalidate()
        override fun isInvalid(): Boolean = mSource.isInvalid

        override fun <ToValue> mapByPage(function: Function<List<Value>, List<ToValue>>): DataSource<Int, ToValue> {
            throw UnsupportedOperationException("Inaccessible inner type doesn't support map op")
        }

        override fun <ToValue> map(function: Function<Value, ToValue>): DataSource<Int, ToValue> {
            throw UnsupportedOperationException("Inaccessible inner type doesn't support map op")
        }

        override fun dispatchLoadInitial(position: Int?, initialLoadSize: Int, pageSize: Int, enablePlaceholders: Boolean, mainThreadExecutor: Executor, receiver: PageResult.Receiver<Value>) {
            var position = position
            var initialLoadSize = initialLoadSize
            if (position == null) {
                position = 0
            } else {
                initialLoadSize = Math.max(initialLoadSize / pageSize, 2) * pageSize
                val idealStart = position - initialLoadSize / 2
                position = Math.max(0, idealStart / pageSize * pageSize)
            }
            mSource.dispatchLoadInitial(false, position, initialLoadSize, pageSize, mainThreadExecutor, receiver, 0L)
        }

        override fun dispatchLoadAfter(currentEndIndex: Int, currentEndItem: Value, pageSize: Int, mainThreadExecutor: Executor, receiver: PageResult.Receiver<Value>) {
            val startIndex = currentEndIndex + 1
            mSource.dispatchLoadRange(PageResult.APPEND, startIndex, pageSize, mainThreadExecutor, receiver, 0L)
        }

        override fun dispatchLoadBefore(currentBeginIndex: Int, currentBeginItem: Value, pageSize: Int, mainThreadExecutor: Executor, receiver: PageResult.Receiver<Value>) {
            var startIndex = currentBeginIndex - 1
            if (startIndex < 0) {
                mSource.dispatchLoadRange(PageResult.PREPEND, startIndex, 0, mainThreadExecutor, receiver, 0L)
            } else {
                val loadSize = Math.min(pageSize, startIndex + 1)
                startIndex = startIndex - loadSize + 1
                mSource.dispatchLoadRange(PageResult.PREPEND, startIndex, loadSize, mainThreadExecutor, receiver, 0L)
            }
        }

        override fun getKey(position: Int, item: Value): Int? {
            return position
        }
    }

    override fun <V> mapByPage(function: Function<List<T>, List<V>>): MyPositionalDataSource<V> {
        return MyWrapperPositionalDataSource(this, function)
    }

    override fun <V> map(function: Function<T, V>): MyPositionalDataSource<V> {
        return mapByPage(createListFunction(function))
    }

    companion object {
        fun computeInitialLoadPosition(params: MyLoadInitialParams, totalCount: Int): Int {
            val position = params.requestedStartPosition
            val initialLoadSize = params.requestedLoadSize
            val pageSize = params.pageSize
            var pageStart = position / pageSize * pageSize

            val maximumLoadPage = (totalCount - initialLoadSize + pageSize - 1) / pageSize * pageSize
            pageStart = maximumLoadPage.coerceAtMost(pageStart)

            pageStart = 0.coerceAtLeast(pageStart)
            return pageStart
        }

        fun computeInitialLoadSize(params: MyLoadInitialParams, initialLoadPosition: Int, totalCount: Int): Int =
                (totalCount - initialLoadPosition).coerceAtMost(params.requestedLoadSize)
    }
}
