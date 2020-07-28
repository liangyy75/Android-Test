package com.liang.example.recyclerviewtest.recycler2

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

open class SimpleRVViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected open val subViews = SparseArray<View>()

    open operator fun get(id: Int): View? {
        var view = subViews[id]
        if (view == null) {
            view = itemView.findViewById(id)
            subViews.put(id, view)
        }
        return view
    }

    open fun getNotNull(id: Int) = get(id)!!

    open fun text(id: Int, text: String): TextView? {
        val view = get(id) as? TextView ?: return null
        view.text = text
        return view
    }

    open fun bg(id: Int, bgResId: Int): View? {
        val view = get(id) ?: return null
        view.setBackgroundResource(bgResId)
        return view
    }

    open fun img(id: Int, imageResId: Int): View? {
        val view = get(id) as? ImageView ?: return null
        view.setImageResource(imageResId)
        return view
    }

    open fun onClick(id: Int, listener: View.OnClickListener): View? {
        val view = get(id) ?: return null
        view.setOnClickListener(listener)
        return view
    }

    open fun onLongClick(id: Int, listener: View.OnLongClickListener): View? {
        val view = get(id) ?: return null
        view.setOnLongClickListener(listener)
        return view
    }

    open fun gone(id: Int): View? {
        val view = get(id) ?: return null
        view.visibility = View.GONE
        return view
    }

    open fun visible(id: Int): View? {
        val view = get(id) ?: return null
        view.visibility = View.VISIBLE
        return view
    }

    open fun inVisible(id: Int): View? {
        val view = get(id) ?: return null
        view.visibility = View.INVISIBLE
        return view
    }

    companion object {
        fun new(context: Context, layoutResId: Int, parent: ViewGroup?): SimpleRVViewHolder =
                SimpleRVViewHolder(LayoutInflater.from(context).inflate(layoutResId, parent, false))
    }
}

/**
 * 1. click
 * 2. itemLayoutId
 * 3. header / footer
 * 4. add / remove / move
 * 5. TODO: 将指定的 item 滑动到当前的屏幕顶端或中间 -- https://www.jianshu.com/p/c0654b395849
 */
abstract class SimpleRVAdapter<T>(val dataSet: MutableList<T>, val context: Context) : RecyclerView.Adapter<SimpleRVViewHolder>() {
    open var clickListener: ClickListener<T>? = null
    protected open var onClickListener: View.OnClickListener = View.OnClickListener { v ->
        clickListener!!.onClick(v, v.getTag(KEY_HOLDER) as SimpleRVViewHolder, v.getTag(KEY_POS) as Int, v.getTag(KEY_DATA) as T)
    }
    protected open var onLongClickListener: View.OnLongClickListener = View.OnLongClickListener lc@{ v ->
        return@lc clickListener!!.onLongClick(v, v.getTag(KEY_HOLDER) as SimpleRVViewHolder, v.getTag(KEY_POS) as Int, v.getTag(KEY_DATA) as T)
    }

    abstract fun getItemLayoutId(position: Int, data: T?): Int
    override fun getItemViewType(position: Int): Int {
        val flag = headerView != null
        if (position == 0 && flag) {
            return VIEW_TYPE_HEADER
        }
        var pos = position
        if (flag) {
            pos--
        }
        val size = dataSet.size
        if (pos == size) {
            if (footerView == null) {
                throw NullPointerException("footerView shouldn't be null")
            }
            return VIEW_TYPE_HEADER
        }
        return getItemLayoutId(pos, dataSet[pos])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRVViewHolder = when (viewType) {
        VIEW_TYPE_HEADER -> SimpleRVViewHolder(headerView!!)
        VIEW_TYPE_FOOTER -> SimpleRVViewHolder(footerView!!)
        else -> SimpleRVViewHolder.new(context, viewType, parent)
    }

    override fun getItemCount(): Int {
        var size = dataSet.size
        if (headerView != null) {
            size++
        }
        if (footerView != null) {
            size++
        }
        return size
    }

    open var headerView: View? = null
        set(value) {
            val flag = field != null
            val flag2 = value != null
            field = value
            if (flag && flag2) {
                notifyItemChanged(0)
            } else if (flag && !flag2) {
                notifyItemRemoved(0)
            } else if (!flag && flag2) {
                notifyItemInserted(0)
            }
        }
    open var footerView: View? = null
        set(value) {
            val flag = field != null
            val flag2 = value != null
            field = value
            val position = adapterPosition(dataSet.size)
            if (flag && flag2) {
                notifyItemChanged(position)
            } else if (flag && !flag2) {
                notifyItemRemoved(position)
            } else if (!flag && flag2) {
                notifyItemInserted(position)
            }
        }

    open fun header(layoutResId: Int): View? {
        headerView = when (layoutResId) {
            0 -> null
            else -> View.inflate(context, layoutResId, null)
        }
        return headerView
    }

    open fun footer(layoutResId: Int): View? {
        footerView = when (layoutResId) {
            0 -> null
            else -> View.inflate(context, layoutResId, null)
        }
        return footerView
    }

    abstract fun bind(holder: SimpleRVViewHolder, position: Int, data: T?)
    override fun onBindViewHolder(holder: SimpleRVViewHolder, position: Int) {
        val flag = headerView != null
        val size = dataSet.size
        if (flag && position == 0 || flag && position == size + 1 || position == size) {
            return
        }
        val pos = dataSetPosition(position)
        val data = dataSet[pos]
        holder.itemView.apply {
            setTag(KEY_HOLDER, holder)
            setTag(KEY_POS, pos)
            setTag(KEY_DATA, data)
        }
        bind(holder, pos, data)
        clickListener?.let {
            holder.itemView.setOnClickListener(onClickListener)
            holder.itemView.setOnLongClickListener(onLongClickListener)
        }
    }

    open fun add(data: T, position: Int = dataSet.size) {
        dataSet.add(position, data)
        notifyItemInserted(adapterPosition(position))
    }

    // TODO: 删除 item 的时候，可能会出现 item 的下标没有刷新、位置错位的问题。这时候需要 notifyItemRangeChanged(position, dataSet.size)
    open fun remove(position: Int = dataSet.size): T {
        val data = dataSet.removeAt(position)
        notifyItemRemoved(adapterPosition(position))
        return data
    }

    open fun remove(data: T): Boolean {
        val position = dataSet.indexOf(data)
        if (position != -1) {
            val result = dataSet.remove(data)
            if (result) {
                notifyItemRemoved(adapterPosition(position))
            }
            return result
        }
        return false
    }

    open fun move(from: Int, to: Int) {
        if (from == to) {
            return
        }
        val data = dataSet[to]
        val increment = when {
            from > to -> 1
            else -> -1
        }
        var index = to
        while (index != from) {
            dataSet[index] = dataSet[index + increment]
            index += increment
        }
        dataSet[from] = data
        notifyItemMoved(from, to)
    }

    // override fun onBindViewHolder(holder: SimpleRVViewHolder, position: Int, payloads: MutableList<Any>) {
    //     super.onBindViewHolder(holder, position, payloads)
    // }
    // TODO: 配合 notifyItemChanged(int position, Object payload) 不刷新整个holder(比如图片请求)，可以根据Object来刷新(不是TODO，而是知识)

    open fun adapterPosition(position: Int): Int = when {
        headerView != null -> position + 1
        else -> position
    }

    open fun dataSetPosition(position: Int): Int = when {
        headerView != null -> position - 1
        else -> position
    }

    interface ClickListener<T> {
        fun onClick(view: View, holder: SimpleRVViewHolder, position: Int, data: T?)
        fun onLongClick(view: View, holder: SimpleRVViewHolder, position: Int, data: T?): Boolean = false
    }

    companion object {
        val KEY_HOLDER = View.generateViewId()
        val KEY_POS = View.generateViewId()
        val KEY_DATA = View.generateViewId()

        const val VIEW_TYPE_HEADER = -1
        const val VIEW_TYPE_FOOTER = -2
    }
}
