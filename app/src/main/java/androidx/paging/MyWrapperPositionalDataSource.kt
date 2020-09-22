package androidx.paging

import androidx.arch.core.util.Function

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
open class MyWrapperPositionalDataSource<A, B>(private val mSource: MyPositionalDataSource<A>, val mListFunction: Function<List<A>, List<B>>) : MyPositionalDataSource<B>() {
    override fun addInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) = mSource.addInvalidatedCallback(onInvalidatedCallback)
    override fun removeInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) = mSource.removeInvalidatedCallback(onInvalidatedCallback)
    override fun invalidate() = mSource.invalidate()
    override fun isInvalid(): Boolean = mSource.isInvalid

    override fun loadInitial(params: MyLoadInitialParams, callback: LoadInitialCallback<B>) {
        mSource.loadInitial(params, object : LoadInitialCallback<A>() {
            override fun onResult(data: List<A>, position: Int, totalCount: Int) {
                callback.onResult(convert(mListFunction, data), position, totalCount)
            }

            override fun onResult(data: List<A>, position: Int) {
                callback.onResult(convert(mListFunction, data), position)
            }
        })
    }

    override fun loadRange(params: MyLoadRangeParams, callback: LoadRangeCallback<B>) {
        mSource.loadRange(params, object : LoadRangeCallback<A>() {
            override fun onResult(data: List<A>) {
                callback.onResult(convert(mListFunction, data))
            }
        })
    }
}
