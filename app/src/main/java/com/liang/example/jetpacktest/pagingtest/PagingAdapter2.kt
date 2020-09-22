package com.liang.example.jetpacktest.pagingtest

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.paging.MyAsyncPagedListDiffer.PagedListListener
import androidx.paging.MyAsyncPagedListDiffer
import androidx.paging.MyPagedList
import androidx.paging.PagedList
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.liang.example.androidtest.R

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 */
abstract class PagingAdapter2<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH> {
    val mDiffer: MyAsyncPagedListDiffer<T>
    private val mListener = object : PagedListListener<T> {
        override fun onCurrentListChanged(previousList: MyPagedList<T>?, currentList: MyPagedList<T>?) {
            this@PagingAdapter2.onCurrentListChanged(currentList)
            this@PagingAdapter2.onCurrentListChanged(previousList, currentList)
        }
    }

    protected constructor(diffCallback: DiffUtil.ItemCallback<T>) {
        mDiffer = MyAsyncPagedListDiffer(this, diffCallback)
        mDiffer.addPagedListListener(mListener)
    }

    protected constructor(config: AsyncDifferConfig<T>) {
        mDiffer = MyAsyncPagedListDiffer(AdapterListUpdateCallback(this), config)
        mDiffer.addPagedListListener(mListener)
    }

    open fun submitList(pagedList: MyPagedList<T>?) = mDiffer.submitList(pagedList)
    open fun submitList(pagedList: MyPagedList<T>?, commitCallback: Runnable?) = mDiffer.submitList(pagedList, commitCallback)

    protected fun getItem(position: Int): T? = mDiffer.getItem(position)
    override fun getItemCount(): Int = mDiffer.itemCount
    open fun getCurrentList(): MyPagedList<T>? = mDiffer.currentList

    open fun onCurrentListChanged(previousList: MyPagedList<T>?, currentList: MyPagedList<T>? = null) = Unit

    // load more

    protected open fun loadAround(position: Int) {
        try {
            mDiffer.loadAround(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun loadAround2(position: Int): Long {
        return try {
            mDiffer.loadAround2(position)
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    open fun <K : Any> scheduleInsert(t: T, key: K?, key2: K?, position: Int) {
        try {
            mDiffer.scheduleInsert(t, key, key2, position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected var onAttachStateChangeListener: View.OnAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = doOnViewAttachedToWindow(v)
        override fun onViewDetachedFromWindow(v: View) = doOnViewDetachedFromWindow(v)
    }

    open fun doOnViewAttachedToWindow(v: View) {
        loadAround((v.getTag(R.id.PAGE_TAG_KEY) as? VH)?.adapterPosition ?: return)
    }

    open fun doOnViewDetachedFromWindow(v: View) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // val viewHolder: VH
        // if (viewType == TYPE_HEADER) {
        //     viewHolder = createHeaderViewHolder(parent, viewType)
        // } else if (viewType == TYPE_FOOTER) {
        //     viewHolder = createFooterViewHolder(parent, viewType)
        // } else if (viewType == TYPE_LOADING || viewType == TYPE_LOAD_ERROR) {
        //     viewHolder = createLoadingViewHolder(parent, viewType)
        // } else if (viewType == TYPE_EMPTY) {
        //     viewHolder = onCreateEmptyViewHolder(parent, viewType)
        // } else {
        //     viewHolder = onCreateNormalViewHolder(parent, viewType)
        // }
        val viewHolder = onCreateNormalViewHolder(parent, viewType)
        viewHolder.itemView.setTag(R.id.PAGE_TAG_KEY, viewHolder)
        viewHolder.itemView.addOnAttachStateChangeListener(onAttachStateChangeListener)
        return viewHolder
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val itemView = holder.itemView
        if (itemView != null && itemView.isViewAttachToWindow()) {
            doOnViewDetachedFromWindow(itemView)
            doOnViewAttachedToWindow(itemView)
        }
    }

    // abstract fun createHeaderViewHolder(parent: ViewGroup, viewType: Int): VH
    // abstract fun createFooterViewHolder(parent: ViewGroup, viewType: Int): VH
    // abstract fun createLoadingViewHolder(parent: ViewGroup, viewType: Int): VH
    // abstract fun onCreateEmptyViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onCreateNormalViewHolder(parent: ViewGroup, viewType: Int): VH

    companion object {
        const val TYPE_HEADER = 1
        const val TYPE_FOOTER = 2
        const val TYPE_LOADING = 3
        const val TYPE_LOAD_ERROR = 4
        const val TYPE_EMPTY = 5

        fun View.isViewAttachToWindow(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                isAttachedToWindow
            } else {
                handler != null
            }
        }
    }
}
