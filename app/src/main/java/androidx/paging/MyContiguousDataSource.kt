package androidx.paging

import java.util.concurrent.Executor

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
abstract class MyContiguousDataSource<Key, Value> : DataSource<Key, Value>() {
    override fun isContiguous(): Boolean {
        return true
    }

    internal abstract fun dispatchLoadInitial(
            key: Key?,
            initialLoadSize: Int,
            pageSize: Int,
            enablePlaceholders: Boolean,
            mainThreadExecutor: Executor,
            receiver: PageResult.Receiver<Value>)

    internal abstract fun dispatchLoadAfter(
            currentEndIndex: Int,
            currentEndItem: Value,
            pageSize: Int,
            mainThreadExecutor: Executor,
            receiver: PageResult.Receiver<Value>)

    internal abstract fun dispatchLoadBefore(
            currentBeginIndex: Int,
            currentBeginItem: Value,
            pageSize: Int,
            mainThreadExecutor: Executor,
            receiver: PageResult.Receiver<Value>)

    internal abstract fun dispatchLoadInsert(
            value: Value,
            key: Key?,
            key2: Key?,
            currentEndIndex: Int,
            currentEndItem: Value,
            pageSize: Int,
            mainThreadExecutor: Executor,
            receiver: PageResult.Receiver<Value>)

    abstract fun getKey(position: Int, item: Value?): Key?
    open fun supportsPageDropping(): Boolean {
        return true
    }
}
