@file:Suppress("unused")

package com.liang.example.jetpacktest.pagingtest

import android.content.Context
import android.database.Observable
import android.os.Build
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.paging.AdapterListUpdateCallbackWrapper
import androidx.paging.MyAsyncPagedListDiffer2
import androidx.paging.MyContiguousPagedList2
import androidx.paging.PagedList
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.liang.example.androidtest.R

abstract class PagingViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(data: T?, position: Int)
    open fun unBind() = Unit

    open fun attach() = Unit
    open fun detach() = Unit

    open fun fullSpan() = false
}

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/17
 * <p>
 */
abstract class PagingAdapter3<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH> {
    open val mDiffer: MyAsyncPagedListDiffer2<T>
    private val mListener = object : MyAsyncPagedListDiffer2.PagedListListener<T> {
        override fun onCurrentListChanged(previousList: PagedList<T>?, currentList: PagedList<T>?) {
            this@PagingAdapter3.onCurrentListChanged(currentList)
            this@PagingAdapter3.onCurrentListChanged(previousList, currentList)
        }
    }

    protected constructor(diffCallback: DiffUtil.ItemCallback<T>) {
        super.registerAdapterDataObserver(observable)
        mDiffer = MyAsyncPagedListDiffer2(this, diffCallback)
        mDiffer.addPagedListListener(mListener)
    }

    protected constructor(config: AsyncDifferConfig<T>) {
        super.registerAdapterDataObserver(observable)
        mDiffer = MyAsyncPagedListDiffer2(AdapterListUpdateCallbackWrapper(this, AdapterListUpdateCallback(this)), config)
        mDiffer.addPagedListListener(mListener)
    }

    open fun submitList(pagedList: PagedList<T>?) {
        pagedList?.addWeakCallback(null, this.emptyCallback)
        mDiffer.submitList(pagedList)
    }

    open fun submitList(pagedList: PagedList<T>?, commitCallback: Runnable?) {
        pagedList?.addWeakCallback(null, this.emptyCallback)
        mDiffer.submitList(pagedList, commitCallback)
    }

    open fun getDataItemCount() = mDiffer.itemCount
    open fun getCurrentList(): PagedList<T>? = mDiffer.currentList
    protected fun getItem(position: Int): T? = mDiffer.getItem(position)
    override fun getItemCount(): Int {
        val header = when {
            hasHeader() -> 1
            else -> 0
        }
        val footer = when {
            hasFooter() -> 1
            else -> 0
        }
        val empty = when {
            hasEmpty() -> 1
            else -> 0
        }
        val loadingStatus = when {
            hasLoad() -> 1
            else -> 0
        }
        val count = header + getDataItemCount() + footer + loadingStatus + empty
        Log.e("PagingAdapter", "getItemCount: $count = $header, $footer, $loadingStatus, $empty")
        return count
    }

    open fun onCurrentListChanged(previousList: PagedList<T>?, currentList: PagedList<T>? = null) = Unit

    // 一级列表

    protected open fun loadAround(position: Int) {
        try {
            mDiffer.loadAround(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 二级列表

    open fun scheduleInsert(t: T) {
        try {
            mDiffer.scheduleInsert(t)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // attach state

    protected open var onAttachStateChangeListener: View.OnAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = doOnViewAttachedToWindow(v.getTag(R.id.paging_view_holder) as VH, v)
        override fun onViewDetachedFromWindow(v: View) = doOnViewDetachedFromWindow(v.getTag(R.id.paging_view_holder) as VH, v)
    }

    open fun doOnViewAttachedToWindow(holder: VH, v: View) {
        if (holder is PagingViewHolder<*>) {
            (holder as? PagingViewHolder<T>)?.attach()
        }
        val pos = holder.adapterPosition
        if (pos < 0) {
            return
        }
        when (v.getTag(R.id.paging_view_type) as Int) {
            TYPE_EMPTY -> onBindEmptyViewHolder(holder)
            TYPE_HEADER -> onBindHeaderViewHolder(holder)
            TYPE_FOOTER -> onBindFooterViewHolder(holder)
            TYPE_LOADING -> onBindLoadingViewHolder(holder)
            TYPE_INVALID -> {
                onBindInvalidViewHolder(holder)
            }
            else -> {
                onBindNormalViewHolder(holder)
                loadAround(pos)
            }
        }
    }

    open fun doOnViewDetachedFromWindow(holder: VH, v: View) {
        if (holder is PagingViewHolder<*>) {
            when (v.getTag(R.id.paging_view_type) as Int) {
                TYPE_EMPTY -> showEmptyNow = false
                TYPE_HEADER -> showHeaderNow = false
                TYPE_FOOTER -> showFooterNow = false
                TYPE_LOADING -> showLoadNow = false
            }
            (holder as? PagingViewHolder<T>)?.detach()
            (holder as? PagingViewHolder<T>)?.unBind()
        }
    }

    // viewHolder

    // 请返回正数
    override fun getItemViewType(position: Int): Int {
        val dataItemCount = itemCount - 1
        return when {
            hasEmpty() && position == 0 -> TYPE_EMPTY
            hasHeader() && position == 0 -> {
                showHeaderNow = true
                TYPE_HEADER
            }
            hasLoad() && position == dataItemCount -> {
                showLoadNow = true
                TYPE_LOADING
            }  // if (isError()) TYPE_LOAD_ERROR else TYPE_LOADING
            hasFooter() && position == dataItemCount -> {
                showFooterNow = true
                TYPE_FOOTER
            }
            else -> {
                val pos: Int = mappingAdapterPos2DataPos(position)
                // 不明所以
                when {
                    pos >= getDataItemCount() -> invalidateType()
                    else -> getNormalViewType(pos, getItem(pos))
                }
            }
        }
    }

    open fun invalidateType() = TYPE_INVALID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val viewHolder: VH = when (viewType) {
            TYPE_HEADER -> onCreateHeaderViewHolder(parent, viewType)
            TYPE_FOOTER -> onCreateFooterViewHolder(parent, viewType)
            TYPE_LOADING, TYPE_LOAD_ERROR -> onCreateLoadingViewHolder(parent, viewType)
            TYPE_EMPTY -> onCreateEmptyViewHolder(parent, viewType)
            TYPE_INVALID -> {
                mInvalidCount++
                onCreateInvalidViewHolder(parent, viewType)
            }
            else -> onCreateNormalViewHolder(parent, viewType)
        }
        viewHolder.itemView.setTag(R.id.paging_view_holder, viewHolder)
        viewHolder.itemView.setTag(R.id.paging_view_type, viewType)
        viewHolder.itemView.addOnAttachStateChangeListener(onAttachStateChangeListener)
        Log.e("PagingAdapter", "onCreateViewHolder: $viewType")
        return viewHolder
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val itemView = holder.itemView
        val viewType = itemView.getTag(R.id.paging_view_type) as? Int ?: TYPE_INVALID
        if (holder is PagingViewHolder<*> && viewType >= 0 || viewType == SinglePagingAdapter.TYPE_NORMAL) {
            (holder as? PagingViewHolder<T>)?.bind(getItem(mappingAdapterPos2DataPos(position)), position)
        }
        if (itemView.isViewAttachToWindow()) {
            doOnViewDetachedFromWindow(holder, itemView)
            doOnViewAttachedToWindow(holder, itemView)
        }
    }

    abstract fun onCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onCreateFooterViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onCreateLoadingViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onCreateInvalidViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onCreateEmptyViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onCreateNormalViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun getNormalViewType(position: Int, data: T?): Int

    open fun onBindHeaderViewHolder(holder: VH) {
        showHeaderNow = true
        setFullSpan(holder)
    }

    open fun onBindFooterViewHolder(holder: VH) {
        showFooterNow = true
        setFullSpan(holder)
    }

    open fun onBindLoadingViewHolder(holder: VH) {
        showLoadNow = true
        setFullSpan(holder)
    }

    open fun onBindEmptyViewHolder(holder: VH) {
        showEmptyNow = true
        setFullSpan(holder)
    }

    open fun onBindNormalViewHolder(holder: VH) {
        onBindInvalidViewHolder(holder)
    }

    open fun onBindInvalidViewHolder(holder: VH) {
        if (holder is PagingViewHolder<*> && holder.fullSpan() || holder is DefaultViewHolder && holder.fullSpan) {
            setFullSpan(holder)
        }
    }

    protected open fun setFullSpan(holder: VH) {
        val lp = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
            holder.itemView.layoutParams = lp
        }
    }

    protected open var mInvalidCount = 0
    open fun getInvalidCount() = mInvalidCount

    open var isEmpty = true
        set(value) {
            if (field != value) {
                field = value
                if (showEmptyNow) {
                    notifyDataSetChanged()
                }
            }
        }

    protected open var isEnd = false

    open val emptyCallback = object : MyContiguousPagedList2.MoreCallback<T>() {
        override fun emptyAppend() {
            isEnd = true
        }
    }

    open var supportEmptyView = true
        set(value) {
            if (field != value) {
                field = value
                if (showEmptyNow) {
                    notifyDataSetChanged()
                }
            }
        }

    @Support
    open var supportLoadView = SUPPORT_EVEN_EMPTY
        set(value) {
            if (field != value) {
                field = value
                if (showLoadNow) {
                    notifyDataSetChanged()
                }
            }
        }

    @Support
    open var supportHeader = SUPPORT_EVEN_EMPTY
        set(value) {
            if (field != value) {
                field = value
                if (showHeaderNow) {
                    notifyDataSetChanged()
                }
            }
        }

    @Support
    open var supportFooter = SUPPORT
        set(value) {
            if (field != value) {
                field = value
                if (showFooterNow) {
                    notifyDataSetChanged()
                }
            }
        }

    protected open var showEmptyNow = false
    protected open var showHeaderNow = false
    protected open var showFooterNow = false
    protected open var showLoadNow = false

    open fun hasEmpty(): Boolean = getDataItemCount() == 0 && supportEmptyView
            && supportHeader != SUPPORT_EVEN_EMPTY && supportLoadView != SUPPORT_EVEN_EMPTY

    open fun hasHeader(): Boolean = getDataItemCount() == 0 && supportHeader == SUPPORT_EVEN_EMPTY
            || getDataItemCount() > 0 && supportHeader != NOT_SUPPORT

    open fun hasLoad(): Boolean = !isEnd && (getDataItemCount() == 0 && supportLoadView == SUPPORT_EVEN_EMPTY
            || getDataItemCount() > 0 && supportLoadView != NOT_SUPPORT)

    open fun hasFooter(): Boolean = isEnd && (getDataItemCount() == 0 && supportFooter == SUPPORT_EVEN_EMPTY
            || getDataItemCount() > 0 && supportFooter != NOT_SUPPORT)

    open fun mappingAdapterPos2DataPos(position: Int) = when {
        hasHeader() -> position - 1
        else -> position
    }

    // 有 header 时， 通知的 position 是有问题的，所以需要转一次。
    protected open val observable = DataObservable()
    protected open val mObservable: AdapterDataObservable = AdapterDataObservable()

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) = mObservable.registerObserver(observer)
    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) = mObservable.unregisterObserver(observer)

    open inner class DataObservable : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            mObservable.notifyChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            mObservable.notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            mObservable.notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            mObservable.notifyItemRangeRemoved(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            mObservable.notifyItemMoved(fromPosition, toPosition)
        }
    }

    open inner class AdapterDataObservable : Observable<RecyclerView.AdapterDataObserver?>() {
        open fun hasObservers(): Boolean = mObservers.isNotEmpty()

        open fun notifyChanged() {
            for (i in mObservers.indices.reversed()) {
                mObservers[i]?.onChanged()
            }
        }

        protected open fun fixPositionStart(positionStart: Int): Int = when {
            hasHeader() -> positionStart + 1
            else -> positionStart
        }

        @JvmOverloads
        open fun notifyItemRangeChanged(_positionStart: Int, itemCount: Int, payload: Any? = null) {
            val positionStart = fixPositionStart(_positionStart)
            for (i in mObservers.indices.reversed()) {
                mObservers[i]?.onItemRangeChanged(positionStart, itemCount, payload)
            }
        }

        open fun notifyItemRangeInserted(_positionStart: Int, itemCount: Int) {
            val positionStart = fixPositionStart(_positionStart)
            for (i in mObservers.indices.reversed()) {
                mObservers[i]?.onItemRangeInserted(positionStart, itemCount)
            }
        }

        open fun notifyItemRangeRemoved(_positionStart: Int, itemCount: Int) {
            val positionStart = fixPositionStart(_positionStart)
            for (i in mObservers.indices.reversed()) {
                mObservers[i]?.onItemRangeRemoved(positionStart, itemCount)
            }
        }

        open fun notifyItemMoved(_fromPosition: Int, _toPosition: Int) {
            val fromPosition = fixPositionStart(_fromPosition)
            val toPosition = fixPositionStart(_toPosition)
            for (i in mObservers.indices.reversed()) {
                mObservers[i]?.onItemRangeMoved(fromPosition, toPosition, 1)
            }
        }
    }

    companion object {
        const val TYPE_HEADER = -1
        const val TYPE_FOOTER = -2
        const val TYPE_LOADING = -3
        const val TYPE_LOAD_ERROR = -4
        const val TYPE_EMPTY = -5
        const val TYPE_INVALID = -6

        const val TYPE_SECOND_HEADER = -11
        const val TYPE_SECOND_FOOTER = -12
        const val TYPE_SECOND_LOADING = -13
        const val TYPE_SECOND_LOAD_ERROR = -14

        const val NOT_SUPPORT = 0
        const val SUPPORT = 1
        const val SUPPORT_EVEN_EMPTY = 2

        @IntDef(value = [NOT_SUPPORT, SUPPORT, SUPPORT_EVEN_EMPTY])
        annotation class Support

        fun View.isViewAttachToWindow(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                isAttachedToWindow
            } else {
                handler != null
            }
        }
    }

    // 二级的 header / footer / loading

    open val supportSecondHeader = true
    open val supportSecondFooter = true
    open val supportSecondLoad = true
    open val isSecondEnds = mutableMapOf<T, Boolean>()
}

abstract class SinglePagingAdapter<T, VH : RecyclerView.ViewHolder> : PagingAdapter3<T, VH> {
    constructor(config: AsyncDifferConfig<T>) : super(config)
    constructor(diffCallback: DiffUtil.ItemCallback<T>) : super(diffCallback)

    override fun getNormalViewType(position: Int, data: T?): Int = TYPE_NORMAL

    companion object {
        const val TYPE_NORMAL = -7
    }
}

open class DefaultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open val subViews: SparseArray<View> = SparseArray()

    open var fullSpan = false

    open operator fun get(id: Int): View? {
        var view = subViews[id]
        if (view == null) {
            view = itemView.findViewById(id)
            subViews.put(id, view)
        }
        return view
    }

    open fun getNotNull(id: Int) = get(id)!!
    open fun getOrElse(id: Int, default: View) = get(id) ?: default
    open fun getOrElse(id: Int, default: (DefaultViewHolder, View) -> View) = get(id)
            ?: default(this, itemView)
}

abstract class DefaultPagingAdapter<T> : PagingAdapter3<T, RecyclerView.ViewHolder> {
    open val context: Context
    open val inflater: LayoutInflater

    constructor(config: AsyncDifferConfig<T>, context: Context) : super(config) {
        this.context = context
        this.inflater = LayoutInflater.from(context)
    }

    constructor(diffCallback: DiffUtil.ItemCallback<T>, context: Context) : super(diffCallback) {
        this.context = context
        this.inflater = LayoutInflater.from(context)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getHeaderLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    override fun onCreateFooterViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getFooterLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    override fun onCreateLoadingViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getLoadingLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    override fun onCreateInvalidViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getInvalidLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    override fun onCreateEmptyViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getEmptyLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    open fun defaultHandle(vh: DefaultViewHolder, itemView: View) = Unit

    open fun getHeaderLayout() = R.layout.layout_default_paing_item
    open fun getFooterLayout() = R.layout.layout_default_paing_item
    open fun getLoadingLayout() = R.layout.layout_default_paing_item
    open fun getInvalidLayout() = R.layout.layout_default_paing_item
    open fun getEmptyLayout() = R.layout.layout_default_paing_item
}

abstract class SingleDefaultPagingAdapter<T> : DefaultPagingAdapter<T> {
    constructor(config: AsyncDifferConfig<T>, context: Context) : super(config, context)
    constructor(diffCallback: DiffUtil.ItemCallback<T>, context: Context) : super(diffCallback, context)

    override fun getNormalViewType(position: Int, data: T?): Int = SinglePagingAdapter.TYPE_NORMAL
}
