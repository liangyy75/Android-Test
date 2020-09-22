package androidx.paging

import androidx.annotation.GuardedBy
import androidx.arch.core.util.Function
import java.util.concurrent.Executor

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
abstract class MyPageKeyedDataSource<Key, Value> : MyContiguousDataSource<Key, Value>() {
    private val mKeyLock = Any()

    @GuardedBy("mKeyLock")
    private var mNextKey: Key? = null

    @GuardedBy("mKeyLock")
    private var mPreviousKey: Key? = null
    fun initKeys(previousKey: Key?, nextKey: Key?) {
        synchronized(mKeyLock) {
            mPreviousKey = previousKey
            mNextKey = nextKey
        }
    }

    fun setPreviousKey(previousKey: Key?) {
        synchronized(mKeyLock) { mPreviousKey = previousKey }
    }

    fun setNextKey(nextKey: Key?) {
        synchronized(mKeyLock) { mNextKey = nextKey }
    }

    private fun getPreviousKey(): Key? {
        synchronized(mKeyLock) { return mPreviousKey }
    }

    private fun getNextKey(): Key? {
        synchronized(mKeyLock) { return mNextKey }
    }

    open class LoadInitialParams(val requestedLoadSize: Int, val placeholdersEnabled: Boolean)

    open class LoadParams<Key>(val key: Key, val requestedLoadSize: Int)
    open class Level2LoadParams<Key>(key: Key, requestedLoadSize: Int, open val key2: Key?) : LoadParams<Key>(key, requestedLoadSize)

    abstract class LoadInitialCallback<Key, Value> {
        abstract fun onResult(data: List<Value>, position: Int, totalCount: Int, previousPageKey: Key?, nextPageKey: Key?)
        abstract fun onResult(data: List<Value>, previousPageKey: Key?, nextPageKey: Key?)
    }

    abstract class LoadCallback<Key, Value> {
        abstract fun onResult(data: List<Value>, adjacentPageKey: Key?)
    }

    internal class LoadInitialCallbackImpl<Key, Value>(dataSource: MyPageKeyedDataSource<Key, Value>,
                                                       countingEnabled: Boolean, receiver: PageResult.Receiver<Value>) : LoadInitialCallback<Key, Value>() {
        val mCallbackHelper: LoadCallbackHelper<Value> = LoadCallbackHelper(
                dataSource, PageResult.INIT, null, receiver)
        private val mDataSource: MyPageKeyedDataSource<Key, Value> = dataSource
        private val mCountingEnabled: Boolean = countingEnabled
        override fun onResult(data: List<Value>, position: Int, totalCount: Int, previousPageKey: Key?, nextPageKey: Key?) {
            if (!mCallbackHelper.dispatchInvalidResultIfInvalid()) {
                LoadCallbackHelper.validateInitialLoadParams(data, position, totalCount)

                mDataSource.initKeys(previousPageKey, nextPageKey)
                val trailingUnloadedCount = totalCount - position - data.size
                if (mCountingEnabled) {
                    mCallbackHelper.dispatchResultToReceiver(PageResult(
                            data, position, trailingUnloadedCount, 0))
                } else {
                    mCallbackHelper.dispatchResultToReceiver(PageResult(data, position))
                }
            }
        }

        override fun onResult(data: List<Value>, previousPageKey: Key?, nextPageKey: Key?) {
            if (!mCallbackHelper.dispatchInvalidResultIfInvalid()) {
                mDataSource.initKeys(previousPageKey, nextPageKey)
                mCallbackHelper.dispatchResultToReceiver(PageResult(data, 0, 0, 0))
            }
        }
    }

    internal class LoadCallbackImpl<Key, Value>(dataSource: MyPageKeyedDataSource<Key, Value>,
                                                @PageResult.ResultType type: Int, mainThreadExecutor: Executor?,
                                                receiver: PageResult.Receiver<Value>) : LoadCallback<Key, Value>() {
        val mCallbackHelper: LoadCallbackHelper<Value> = LoadCallbackHelper(dataSource, type, mainThreadExecutor, receiver)
        var aboutItem: Value? = null
        private val mDataSource: MyPageKeyedDataSource<Key, Value> = dataSource
        override fun onResult(data: List<Value>, adjacentPageKey: Key?) {
            if (!mCallbackHelper.dispatchInvalidResultIfInvalid()) {
                if (mCallbackHelper.mResultType == PageResult.APPEND) {
                    mDataSource.setNextKey(adjacentPageKey)
                } else if (mCallbackHelper.mResultType == PageResult.PREPEND) {
                    mDataSource.setPreviousKey(adjacentPageKey)
                }
                mCallbackHelper.dispatchResultToReceiver(PageResult2(data, 0, 0, 0, aboutItem))
            }
        }
    }

    override fun getKey(position: Int, item: Value?): Key? {
        return null
    }

    override fun supportsPageDropping(): Boolean {
        return false
    }

    override fun dispatchLoadInitial(key: Key?, initialLoadSize: Int, pageSize: Int, enablePlaceholders: Boolean, mainThreadExecutor: Executor,
                                     receiver: PageResult.Receiver<Value>) {
        val callback = LoadInitialCallbackImpl(this, enablePlaceholders, receiver)
        loadInitial(LoadInitialParams(initialLoadSize, enablePlaceholders), callback)
        callback.mCallbackHelper.setPostExecutor(mainThreadExecutor)
    }

    override fun dispatchLoadAfter(currentEndIndex: Int, currentEndItem: Value, pageSize: Int, mainThreadExecutor: Executor,
                                   receiver: PageResult.Receiver<Value>) {
        val key = getNextKey()
        if (key != null) {
            loadAfter(LoadParams(key, pageSize), LoadCallbackImpl(this, PageResult.APPEND, mainThreadExecutor, receiver))
        } else {
            receiver.onPageResult(PageResult.APPEND, PageResult.getEmptyResult())
        }
    }

    override fun dispatchLoadInsert(value: Value, key: Key?, key2: Key?, currentEndIndex: Int, currentEndItem: Value, pageSize: Int,
                                    mainThreadExecutor: Executor,
                                   receiver: PageResult.Receiver<Value>) {
        if (key != null) {
            val callback = LoadCallbackImpl(this, INSERT, mainThreadExecutor, receiver)
            callback.aboutItem = value
            loadInsert(Level2LoadParams(key, pageSize, key2), callback)
        } else {
            receiver.onPageResult(INSERT, PageResult.getEmptyResult())
        }
    }

    override fun dispatchLoadBefore(currentBeginIndex: Int, currentBeginItem: Value,
                                             pageSize: Int, mainThreadExecutor: Executor,
                                             receiver: PageResult.Receiver<Value>) {
        val key = getPreviousKey()
        if (key != null) {
            loadBefore(LoadParams(key, pageSize),
                    LoadCallbackImpl(this, PageResult.PREPEND, mainThreadExecutor, receiver))
        } else {
            receiver.onPageResult(PageResult.PREPEND, PageResult.getEmptyResult())
        }
    }

    abstract fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Key, Value>)

    abstract fun loadBefore(params: LoadParams<Key>, callback: LoadCallback<Key, Value>)

    abstract fun loadAfter(params: LoadParams<Key>, callback: LoadCallback<Key, Value>)

    abstract fun loadInsert(params: Level2LoadParams<Key>, callback: LoadCallback<Key, Value>)

    override fun <ToValue> mapByPage(function: Function<List<Value>, List<ToValue>>): MyPageKeyedDataSource<Key, ToValue> {
        return MyWrapperPageKeyedDataSource(this, function)
    }

    override fun <ToValue> map(function: Function<Value, ToValue>): MyPageKeyedDataSource<Key, ToValue> {
        return mapByPage(createListFunction(function))
    }

    companion object {
        const val INSERT = 4
    }
}

open class MyWrapperPageKeyedDataSource<K, A, B>(
        private val mSource: MyPageKeyedDataSource<K, A>,
        val mListFunction: Function<List<A>, List<B>>) : MyPageKeyedDataSource<K, B>() {
    override fun addInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {
        mSource.addInvalidatedCallback(onInvalidatedCallback)
    }

    override fun removeInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {
        mSource.removeInvalidatedCallback(onInvalidatedCallback)
    }

    override fun invalidate() {
        mSource.invalidate()
    }

    override fun isInvalid(): Boolean {
        return mSource.isInvalid
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<K, B>) {
        mSource.loadInitial(params, object : MyPageKeyedDataSource.LoadInitialCallback<K, A>() {
            override fun onResult(data: List<A>, position: Int, totalCount: Int, previousPageKey: K?, nextPageKey: K?) {
                callback.onResult(convert(mListFunction, data), position, totalCount,
                        previousPageKey, nextPageKey)
            }

            override fun onResult(data: List<A>, previousPageKey: K?, nextPageKey: K?) {
                callback.onResult(convert(mListFunction, data), previousPageKey, nextPageKey)
            }
        })
    }

    override fun loadBefore(params: LoadParams<K>, callback: LoadCallback<K, B>) {
        mSource.loadBefore(params, object : MyPageKeyedDataSource.LoadCallback<K, A>() {
            override fun onResult(data: List<A>, adjacentPageKey: K?) {
                callback.onResult(convert(mListFunction, data), adjacentPageKey)
            }
        })
    }

    override fun loadAfter(params: LoadParams<K>, callback: LoadCallback<K, B>) {
        mSource.loadAfter(params, object : MyPageKeyedDataSource.LoadCallback<K, A>() {
            override fun onResult(data: List<A>, adjacentPageKey: K?) {
                callback.onResult(convert(mListFunction, data), adjacentPageKey)
            }
        })
    }

    override fun loadInsert(params: Level2LoadParams<K>, callback: LoadCallback<K, B>) {
        mSource.loadInsert(params, object : MyPageKeyedDataSource.LoadCallback<K, A>() {
            override fun onResult(data: List<A>, adjacentPageKey: K?) {
                callback.onResult(convert(mListFunction, data), adjacentPageKey)
            }
        })
    }
}

internal class PageResult2<T>(list: List<T>, leadingNulls: Int, trailingNulls: Int, positionOffset: Int, var aboutItem: T)
    : PageResult<T>(list, leadingNulls, trailingNulls, positionOffset)
