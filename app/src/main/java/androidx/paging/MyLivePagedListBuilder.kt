package androidx.paging

import android.annotation.SuppressLint
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.ComputableLiveData
import androidx.lifecycle.LiveData
import androidx.paging.DataSource.InvalidatedCallback
import androidx.paging.PagedList.BoundaryCallback
import java.util.concurrent.Executor

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 */
open class MyLivePagedListBuilder<Key, Value>(dataSourceFactory: DataSource.Factory<Key, Value>, config: PagedList.Config) {
    private var mInitialLoadKey: Key? = null
    private val mConfig: PagedList.Config = config
    private val mDataSourceFactory: DataSource.Factory<Key, Value> = dataSourceFactory
    private var mBoundaryCallback: BoundaryCallback<Value>? = null

    @SuppressLint("RestrictedApi")
    private var mFetchExecutor = ArchTaskExecutor.getIOThreadExecutor()

    constructor(dataSourceFactory: DataSource.Factory<Key, Value>, pageSize: Int)
            : this(dataSourceFactory, PagedList.Config.Builder().setPageSize(pageSize).build())

    open fun setInitialLoadKey(key: Key?): MyLivePagedListBuilder<Key, Value> {
        mInitialLoadKey = key
        return this
    }

    open fun setBoundaryCallback(boundaryCallback: BoundaryCallback<Value>?): MyLivePagedListBuilder<Key, Value> {
        mBoundaryCallback = boundaryCallback
        return this
    }

    open fun setFetchExecutor(fetchExecutor: Executor): MyLivePagedListBuilder<Key, Value> {
        mFetchExecutor = fetchExecutor
        return this
    }

    @SuppressLint("RestrictedApi")
    open fun build(): LiveData<MyPagedList<Value>?> {
        return create(mInitialLoadKey, mConfig, mBoundaryCallback, mDataSourceFactory,
                ArchTaskExecutor.getMainThreadExecutor(), mFetchExecutor)
    }

    companion object {
        @AnyThread
        @SuppressLint("RestrictedApi")
        private fun <Key, Value> create(
                initialLoadKey: Key?, config: PagedList.Config, boundaryCallback: BoundaryCallback<Value>?,
                dataSourceFactory: DataSource.Factory<Key, Value>, notifyExecutor: Executor, fetchExecutor: Executor
        ): LiveData<MyPagedList<Value>?> {
            return object : ComputableLiveData<MyPagedList<Value>>(fetchExecutor) {
                private var mList: MyPagedList<Value>? = null
                private var mDataSource: DataSource<Key, Value>? = null
                private val mCallback = InvalidatedCallback { invalidate() }
                override fun compute(): MyPagedList<Value> {
                    var initializeKey = initialLoadKey
                    if (mList != null) {
                        initializeKey = mList!!.getLastKey() as Key?
                    }
                    do {
                        if (mDataSource != null) {
                            mDataSource!!.removeInvalidatedCallback(mCallback)
                        }
                        mDataSource = dataSourceFactory.create()
                        mDataSource!!.addInvalidatedCallback(mCallback)
                        mList = PageListBuilder2(mDataSource!!, config)
                                .setNotifyExecutor(notifyExecutor)
                                .setFetchExecutor(fetchExecutor)
                                .setBoundaryCallback(boundaryCallback)
                                .setInitialKey(initializeKey)
                                .build()
                    } while (mList!!.isDetached())
                    return mList!!
                }
            }.liveData
        }
    }
}

open class PageListBuilder2<Key, Value>(dataSource: DataSource<Key, Value>, config: PagedList.Config) {
    private val mDataSource: DataSource<Key, Value> = dataSource
    private val mConfig: PagedList.Config = config
    private var mNotifyExecutor: Executor? = null
    private var mFetchExecutor: Executor? = null
    private var mBoundaryCallback: BoundaryCallback<Value>? = null
    private var mInitialKey: Key? = null

    constructor(dataSource: DataSource<Key, Value>, pageSize: Int) : this(dataSource, PagedList.Config.Builder().setPageSize(pageSize).build()) {}

    open fun setNotifyExecutor(notifyExecutor: Executor): PageListBuilder2<Key, Value> {
        mNotifyExecutor = notifyExecutor
        return this
    }

    open fun setFetchExecutor(fetchExecutor: Executor): PageListBuilder2<Key, Value> {
        mFetchExecutor = fetchExecutor
        return this
    }

    open fun setBoundaryCallback(boundaryCallback: BoundaryCallback<Value>?): PageListBuilder2<Key, Value> {
        mBoundaryCallback = boundaryCallback
        return this
    }

    open fun setInitialKey(initialKey: Key?): PageListBuilder2<Key, Value> {
        mInitialKey = initialKey
        return this
    }

    @WorkerThread
    open fun build(): MyPagedList<Value> {
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

    companion object {
        fun <K, T> create(dataSource: DataSource<K, T>,
                          notifyExecutor: Executor,
                          fetchExecutor: Executor,
                          boundaryCallback: BoundaryCallback<T>?,
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
