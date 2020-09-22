package androidx.paging

import android.annotation.SuppressLint
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.ComputableLiveData
import androidx.lifecycle.LiveData
import java.util.concurrent.Executor

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/17
 * <p>
 * todo 描述
 */
open class MyLivePagedListBuilder2<Key, Value>(dataSourceFactory: DataSource.Factory<Key, Value>, config: PagedList.Config) {
    private var mInitialLoadKey: Key? = null
    private val mConfig: PagedList.Config = config
    private val mDataSourceFactory: DataSource.Factory<Key, Value> = dataSourceFactory
    private var mBoundaryCallback: PagedList.BoundaryCallback<Value>? = null

    @SuppressLint("RestrictedApi")
    private var mFetchExecutor = ArchTaskExecutor.getIOThreadExecutor()

    constructor(dataSourceFactory: DataSource.Factory<Key, Value>, pageSize: Int)
            : this(dataSourceFactory, PagedList.Config.Builder().setPageSize(pageSize).build())

    open fun setInitialLoadKey(key: Key?): MyLivePagedListBuilder2<Key, Value> {
        mInitialLoadKey = key
        return this
    }

    open fun setBoundaryCallback(boundaryCallback: PagedList.BoundaryCallback<Value>?): MyLivePagedListBuilder2<Key, Value> {
        mBoundaryCallback = boundaryCallback
        return this
    }

    open fun setFetchExecutor(fetchExecutor: Executor): MyLivePagedListBuilder2<Key, Value> {
        mFetchExecutor = fetchExecutor
        return this
    }

    @SuppressLint("RestrictedApi")
    open fun build(): LiveData<PagedList<Value>?> {
        return create(mInitialLoadKey, mConfig, mBoundaryCallback, mDataSourceFactory,
                ArchTaskExecutor.getMainThreadExecutor(), mFetchExecutor)
    }

    companion object {
        @AnyThread
        @SuppressLint("RestrictedApi")
        private fun <Key, Value> create(
                initialLoadKey: Key?, config: PagedList.Config, boundaryCallback: PagedList.BoundaryCallback<Value>?,
                dataSourceFactory: DataSource.Factory<Key, Value>, notifyExecutor: Executor, fetchExecutor: Executor
        ): LiveData<PagedList<Value>?> {
            return object : ComputableLiveData<PagedList<Value>>(fetchExecutor) {
                private var mList: PagedList<Value>? = null
                private var mDataSource: DataSource<Key, Value>? = null
                private val mCallback = DataSource.InvalidatedCallback { invalidate() }
                override fun compute(): PagedList<Value> {
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
                        mList = PageListBuilder3(mDataSource!!, config)
                                .setNotifyExecutor(notifyExecutor)
                                .setFetchExecutor(fetchExecutor)
                                .setBoundaryCallback(boundaryCallback)
                                .setInitialKey(initializeKey)
                                .build()
                    } while (mList!!.isDetached)
                    return mList!!
                }
            }.liveData
        }
    }
}

open class PageListBuilder3<Key, Value>(dataSource: DataSource<Key, Value>, config: PagedList.Config) {
    private val mDataSource: DataSource<Key, Value> = dataSource
    private val mConfig: PagedList.Config = config
    private var mNotifyExecutor: Executor? = null
    private var mFetchExecutor: Executor? = null
    private var mBoundaryCallback: PagedList.BoundaryCallback<Value>? = null
    private var mInitialKey: Key? = null

    constructor(dataSource: DataSource<Key, Value>, pageSize: Int) : this(dataSource, PagedList.Config.Builder().setPageSize(pageSize).build()) {}

    open fun setNotifyExecutor(notifyExecutor: Executor): PageListBuilder3<Key, Value> {
        mNotifyExecutor = notifyExecutor
        return this
    }

    open fun setFetchExecutor(fetchExecutor: Executor): PageListBuilder3<Key, Value> {
        mFetchExecutor = fetchExecutor
        return this
    }

    open fun setBoundaryCallback(boundaryCallback: PagedList.BoundaryCallback<Value>?): PageListBuilder3<Key, Value> {
        mBoundaryCallback = boundaryCallback
        return this
    }

    open fun setInitialKey(initialKey: Key?): PageListBuilder3<Key, Value> {
        mInitialKey = initialKey
        return this
    }

    @WorkerThread
    open fun build(): PagedList<Value> {
        // TODO: define defaults, once they can be used in module without android dependency
        requireNotNull(mNotifyExecutor) { "MainThreadExecutor required" }
        requireNotNull(mFetchExecutor) { "BackgroundThreadExecutor required" }
        return create(mDataSource, mNotifyExecutor!!, mFetchExecutor!!, mBoundaryCallback, mConfig, mInitialKey)
    }

    companion object {
        fun <K, T> create(dataSource: DataSource<K, T>, notifyExecutor: Executor, fetchExecutor: Executor,
                          boundaryCallback: PagedList.BoundaryCallback<T>?, config: PagedList.Config, key: K?): PagedList<T> {
            var dataSource2 = dataSource
            return if (dataSource2.isContiguous || !config.enablePlaceholders) {
                var lastLoad = ContiguousPagedList.LAST_LOAD_UNSPECIFIED
                if (!dataSource2.isContiguous) {
                    if (dataSource2 is PositionalDataSource<T>) {
                        dataSource2 = dataSource2.wrapAsContiguousWithoutPlaceholders() as DataSource<K, T>
                    }
                    if (key != null) {
                        lastLoad = key as Int
                    }
                }
                if (dataSource2 is MyContiguousDataSource2) {
                    MyContiguousPagedList2(dataSource2, notifyExecutor, fetchExecutor, boundaryCallback, config, key, lastLoad)
                } else {
                    ContiguousPagedList(dataSource2 as ContiguousDataSource<K, T>, notifyExecutor, fetchExecutor, boundaryCallback, config, key,
                            lastLoad)
                }
            } else {
                TiledPagedList(dataSource2 as PositionalDataSource<T>, notifyExecutor, fetchExecutor, boundaryCallback, config, key as? Int
                        ?: 0)
            }
        }
    }
}
