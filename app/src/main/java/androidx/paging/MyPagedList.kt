package androidx.paging

import androidx.annotation.AnyThread
import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
abstract class MyPagedList<T> internal constructor(val mStorage: MyPagedStorage<T>,
                                                   val mMainThreadExecutor: Executor,
                                                   val mBackgroundThreadExecutor: Executor,
                                                   val mBoundaryCallback: PagedList.BoundaryCallback<T>?,
                                                   val mConfig: PagedList.Config) : AbstractList<T>() {

    var mLastLoad = 0
    var mLastItem: T? = null
    val mRequiredRemainder: Int = mConfig.prefetchDistance * 2 + mConfig.pageSize

    var mBoundaryCallbackBeginDeferred = false
    var mBoundaryCallbackEndDeferred = false

    protected open var mLowestIndexAccessed = Int.MAX_VALUE
    protected open var mHighestIndexAccessed = Int.MIN_VALUE
    private val mDetached = AtomicBoolean(false)
    private val mCallbacks = ArrayList<WeakReference<Callback>>()

    class Builder<Key, Value>(dataSource: DataSource<Key, Value>, config: PagedList.Config) {
        private val mDataSource: DataSource<Key, Value>
        private val mConfig: PagedList.Config
        private var mNotifyExecutor: Executor? = null
        private var mFetchExecutor: Executor? = null
        private var mBoundaryCallback: PagedList.BoundaryCallback<Value>? = null
        private var mInitialKey: Key? = null

        constructor(dataSource: DataSource<Key, Value>, pageSize: Int) : this(dataSource, PagedList.Config.Builder().setPageSize(pageSize).build()) {}

        fun setNotifyExecutor(notifyExecutor: Executor): Builder<Key, Value> {
            mNotifyExecutor = notifyExecutor
            return this
        }

        fun setFetchExecutor(fetchExecutor: Executor): Builder<Key, Value> {
            mFetchExecutor = fetchExecutor
            return this
        }

        fun setBoundaryCallback(boundaryCallback: PagedList.BoundaryCallback<Value>?): Builder<Key, Value> {
            mBoundaryCallback = boundaryCallback
            return this
        }

        fun setInitialKey(initialKey: Key?): Builder<Key, Value> {
            mInitialKey = initialKey
            return this
        }

        @WorkerThread
        fun build(): MyPagedList<Value> {
            // TODO: define defaults, once they can be used in module without android dependency
            requireNotNull(mNotifyExecutor) { "MainThreadExecutor required" }
            requireNotNull(mFetchExecutor) { "BackgroundThreadExecutor required" }
            return create(
                    mDataSource,
                    mNotifyExecutor!!,
                    mFetchExecutor!!,
                    mBoundaryCallback,
                    mConfig,
                    mInitialKey)
        }

        init {
            requireNotNull(dataSource) { "DataSource may not be null" }
            requireNotNull(config) { "Config may not be null" }
            mDataSource = dataSource
            mConfig = config
        }
    }

    override fun get(index: Int): T? {
        val item = mStorage[index]
        if (item != null) {
            mLastItem = item
        }
        return item
    }

    open fun loadAround(index: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        mLastLoad = index + getPositionOffset()
        loadAroundInternal(index)
        mLowestIndexAccessed = mLowestIndexAccessed.coerceAtMost(index)
        mHighestIndexAccessed = mHighestIndexAccessed.coerceAtLeast(index)
        tryDispatchBoundaryCallbacks(true)
    }

    @AnyThread
    fun deferBoundaryCallbacks(deferEmpty: Boolean, deferBegin: Boolean, deferEnd: Boolean) {
        checkNotNull(mBoundaryCallback) { "Can't defer BoundaryCallback, no instance" }
        if (mLowestIndexAccessed == Int.MAX_VALUE) {
            mLowestIndexAccessed = mStorage.size
        }
        if (mHighestIndexAccessed == Int.MIN_VALUE) {
            mHighestIndexAccessed = 0
        }
        if (deferEmpty || deferBegin || deferEnd) {
            mMainThreadExecutor.execute {
                if (deferEmpty) {
                    mBoundaryCallback.onZeroItemsLoaded()
                }
                if (deferBegin) {
                    mBoundaryCallbackBeginDeferred = true
                }
                if (deferEnd) {
                    mBoundaryCallbackEndDeferred = true
                }
                tryDispatchBoundaryCallbacks(false)
            }
        }
    }

    fun tryDispatchBoundaryCallbacks(post: Boolean) {
        val dispatchBegin = (mBoundaryCallbackBeginDeferred && mLowestIndexAccessed <= mConfig.prefetchDistance)
        val dispatchEnd = (mBoundaryCallbackEndDeferred && mHighestIndexAccessed >= size - 1 - mConfig.prefetchDistance)
        if (!dispatchBegin && !dispatchEnd) {
            return
        }
        if (dispatchBegin) {
            mBoundaryCallbackBeginDeferred = false
        }
        if (dispatchEnd) {
            mBoundaryCallbackEndDeferred = false
        }
        if (post) {
            mMainThreadExecutor.execute { dispatchBoundaryCallbacks(dispatchBegin, dispatchEnd) }
        } else {
            dispatchBoundaryCallbacks(dispatchBegin, dispatchEnd)
        }
    }

    fun dispatchBoundaryCallbacks(begin: Boolean, end: Boolean) {
        if (begin) {
            mBoundaryCallback!!.onItemAtFrontLoaded(mStorage.getFirstLoadedItem())
        }
        if (end) {
            mBoundaryCallback!!.onItemAtEndLoaded(mStorage.getLastLoadedItem())
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun offsetAccessIndices(offset: Int) {
        mLastLoad += offset
        mLowestIndexAccessed += offset
        mHighestIndexAccessed += offset
    }

    override val size: Int
        get() = mStorage.size

    fun getLoadedCount(): Int {
        return mStorage.getLoadedCount()
    }

    open fun isImmutable(): Boolean {
        return isDetached()
    }

    fun snapshot(): List<T> {
        return if (isImmutable()) {
            this
        } else {
            MySnapshotPagedList(this)
        }
    }

    abstract fun isContiguous(): Boolean

    fun getConfig(): PagedList.Config {
        return mConfig
    }

    abstract fun getDataSource(): DataSource<*, T>

    abstract fun getLastKey(): Any?

    open fun isDetached(): Boolean {
        return mDetached.get()
    }

    fun detach() {
        mDetached.set(true)
    }

    fun getPositionOffset(): Int {
        return mStorage.getPositionOffset()
    }

    fun addWeakCallback(previousSnapshot: List<T>?, callback: Callback) {
        if (previousSnapshot != null && previousSnapshot !== this) {
            if (previousSnapshot.isEmpty()) {
                if (!mStorage.isEmpty()) {
                    callback.onInserted(0, mStorage.size)
                }
            } else {
                val storageSnapshot = previousSnapshot as PagedList<T>
                dispatchUpdatesSinceSnapshot(storageSnapshot, callback)
            }
        }

        for (i in mCallbacks.indices.reversed()) {
            val currentCallback = mCallbacks[i].get()
            if (currentCallback == null) {
                mCallbacks.removeAt(i)
            }
        }

        mCallbacks.add(WeakReference(callback))
    }

    fun removeWeakCallback(callback: Callback) {
        for (i in mCallbacks.indices.reversed()) {
            val currentCallback = mCallbacks[i].get()
            if (currentCallback == null || currentCallback === callback) {
                mCallbacks.removeAt(i)
            }
        }
    }

    fun notifyInserted(position: Int, count: Int) {
        if (count != 0) {
            for (i in mCallbacks.indices.reversed()) {
                val callback = mCallbacks[i].get()
                callback?.onInserted(position, count)
            }
        }
    }

    fun notifyChanged(position: Int, count: Int) {
        if (count != 0) {
            for (i in mCallbacks.indices.reversed()) {
                val callback = mCallbacks[i].get()
                callback?.onChanged(position, count)
            }
        }
    }

    fun notifyRemoved(position: Int, count: Int) {
        if (count != 0) {
            for (i in mCallbacks.indices.reversed()) {
                val callback = mCallbacks[i].get()
                callback?.onRemoved(position, count)
            }
        }
    }

    abstract fun dispatchUpdatesSinceSnapshot(snapshot: PagedList<T>, callback: Callback)

    abstract fun loadAroundInternal(index: Int)

    interface Callback {
        fun onChanged(position: Int, count: Int)
        fun onInserted(position: Int, count: Int)
        fun onRemoved(position: Int, count: Int)
    }

    companion object {
        fun <K, T> create(dataSource: DataSource<K, T>,
                          notifyExecutor: Executor,
                          fetchExecutor: Executor,
                          boundaryCallback: PagedList.BoundaryCallback<T>?,
                          config: PagedList.Config,
                          key: K?): MyPagedList<T> {
            var dataSource = dataSource
            return if (dataSource.isContiguous || !config.enablePlaceholders) {
                var lastLoad = ContiguousPagedList.LAST_LOAD_UNSPECIFIED
                if (!dataSource.isContiguous) {
                    dataSource = if (dataSource is MyPositionalDataSource<T>) {
                        dataSource.wrapAsContiguousWithoutPlaceholders() as DataSource<K, T>
                    } else {
                        (dataSource as PositionalDataSource<T>).wrapAsContiguousWithoutPlaceholders() as DataSource<K, T>
                    }
                    if (key != null) {
                        lastLoad = key as Int
                    }
                }
                val contigDataSource = dataSource as MyContiguousDataSource<K, T>
                MyContiguousPagedList(contigDataSource,
                        notifyExecutor,
                        fetchExecutor,
                        boundaryCallback,
                        config,
                        key,
                        lastLoad)
            } else {
                MyTiledPagedList((dataSource as MyPositionalDataSource<T>),
                        notifyExecutor,
                        fetchExecutor,
                        boundaryCallback,
                        config,
                        if (key != null) (key as Int?)!! else 0)
            }
        }
    }
}

open class MySnapshotPagedList<T>(pagedList: MyPagedList<T>) : MyPagedList<T>(pagedList.mStorage.snapshot(),
        pagedList.mMainThreadExecutor,
        pagedList.mBackgroundThreadExecutor,
        null,
        pagedList.mConfig) {
    private val mContiguous: Boolean = pagedList.isContiguous()
    private val mLastKey: Any?
    private val mDataSource: DataSource<*, T> = pagedList.getDataSource()
    override fun isImmutable(): Boolean {
        return true
    }

    override fun isDetached(): Boolean {
        return true
    }

    public override fun isContiguous(): Boolean {
        return mContiguous
    }

    override fun getLastKey(): Any? {
        return mLastKey
    }

    override fun getDataSource(): DataSource<*, T> {
        return mDataSource
    }

    override fun dispatchUpdatesSinceSnapshot(storageSnapshot: PagedList<T>, callback: Callback) {
    }

    override fun loadAroundInternal(index: Int) {}

    init {
        mLastLoad = pagedList.mLastLoad
        mLastKey = pagedList.getLastKey()
    }
}
