package com.liang.example.jetpacktest.pagingtest

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.MyContiguousPagedList2
import androidx.paging.PagedList
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.liang.example.androidtest.R

val Collection<*>.reversedIndices: IntProgression
    get() = IntProgression.fromClosedRange(size - 1, 0, -1)

inline fun <T> List<T>.forEachReverseIndexed(action: (index: Int, T) -> Unit) {
    var index = size - 1
    while (index >= 0) {
        action(index, get(index))
        index--
    }
}

// TODO: 支持 leadingNulls 和 trailingNulls
open class MyContiguousPagedListWrapper<V>(
        open var pagedList2: MyContiguousPagedList2<Any, V>? = null,
        open val supportHeader: Boolean = false,
        open val supportFooter: Boolean = false,
        open val supportLoader: Boolean = false,
        hasSecond: HasSecond<V>? = null
) {
    open val items = mutableListOf<Any?>()
    open val mCallbacks = mutableListOf<DataChangedCallback>()

    open val isSecondEnd = mutableSetOf<V>()
    open val isSecondNotEmpty = mutableSetOf<V>()

    open val secondFooterAdded = mutableSetOf<V>()
    open val secondLoaderAdded = mutableSetOf<V>()
    open val secondHeaderAdded = mutableSetOf<V>()

    open var hasSecond: HasSecond<V> = hasSecond ?: DEFAULT_HAS_SECOND as HasSecond<V>

    // open val secondFooterSupport = mutableMapOf<V, Int>()
    // open val secondLoaderSupport = mutableMapOf<V, Int>()
    // open val secondHeaderSupport = mutableMapOf<V, Int>()

    open fun addHeader(v: V): Boolean = supportHeader && hasSecond.has(v)/* && secondHeaderSupport.containsKey(v)*/ && !secondHeaderAdded.contains(v)
    open fun addFooter(v: V): Boolean = supportFooter && hasSecond.has(v)/* && secondFooterSupport.containsKey(v)*/ && !secondFooterAdded.contains(v) && isSecondEnd.contains(v)
    open fun addLoader(v: V): Boolean = supportLoader && hasSecond.has(v)/* && secondLoaderSupport.containsKey(v)*/ && !secondLoaderAdded.contains(v) && !isSecondEnd.contains(v)

    protected open fun addLoaderAndHeader(it: Int) {
        val v = items[it] as? V ?: return
        try {
            if (addLoader(v)) {
                items.add(it + 1, LOADER)
                secondLoaderAdded.add(v)
            }
            if (addHeader(v)) {
                items.add(it + 1, HEADER)
                secondHeaderAdded.add(v)
            }
        } catch (e: Exception) {
            Log.e("PagingWrapper", "addLoaderAndHeader -- exception: $e")
        }
    }

    open val mCallback: MyContiguousPagedList2.MoreCallback<V> = object : MyContiguousPagedList2.MoreCallback<V>() {
        override fun initialPage(leadingNulls: Int, trailingNulls: Int, count: Int) {
            items.addAll(pagedList2?.subList(0, count) ?: return)
            items.reversedIndices.forEach {
                addLoaderAndHeader(it)
            }
            mCallbacks.forEach { it.initialPage(leadingNulls, trailingNulls, count, secondHeaderAdded.size, secondLoaderAdded.size) }
        }

        override fun appendPage(position: Int, changedCount: Int, count: Int) {
            var headerCountBefore = secondHeaderAdded.size
            var loaderCountBefore = secondLoaderAdded.size
            pagedList2?.subList(position, position + changedCount)?.forEachReverseIndexed { index, v ->
                val trueIndex = items.size - 1 - changedCount + index
                if (items[trueIndex] != null) {
                    Log.e("PagingWrapper", "items[$trueIndex] != null, items[$trueIndex] = ${items[trueIndex]}, v: $v")
                }
                items[trueIndex] = v
                addLoaderAndHeader(trueIndex)
            } ?: return
            mCallbacks.forEach {
                it.appendChangedPage(position, changedCount, secondHeaderAdded.size - headerCountBefore, secondLoaderAdded.size - loaderCountBefore)
            }
            headerCountBefore = secondHeaderAdded.size
            loaderCountBefore = secondLoaderAdded.size
            val startPos = items.size
            items.addAll(pagedList2!!.subList(position + changedCount, position + changedCount + count))
            ((items.size - 1) downTo (items.size - 1 - count)).forEach {
                addLoaderAndHeader(it)
            }
            mCallbacks.forEach {
                it.appendInsertPage(startPos, count, secondHeaderAdded.size - headerCountBefore, secondLoaderAdded.size - loaderCountBefore)
            }
        }

        override fun prependPage(position: Int, changedCount: Int, count: Int) {
            var headerCountBefore = secondHeaderAdded.size
            var loaderCountBefore = secondLoaderAdded.size
            pagedList2?.subList(position, position + changedCount)?.forEachReverseIndexed { index, v ->
                val trueIndex = position + index
                if (items[trueIndex] != null) {
                    Log.e("PagingWrapper", "items[$trueIndex] != null, items[$trueIndex] = ${items[trueIndex]}, v: $v")
                }
                items[trueIndex] = v
                addLoaderAndHeader(trueIndex)
            } ?: return
            mCallbacks.forEach {
                it.prependChangedPage(position, changedCount, secondHeaderAdded.size - headerCountBefore, secondLoaderAdded.size - loaderCountBefore)
            }
            headerCountBefore = secondHeaderAdded.size
            loaderCountBefore = secondLoaderAdded.size
            items.addAll(0, pagedList2!!.subList(0, count))
            ((count - 1) downTo 0).forEach {
                addLoaderAndHeader(it)
            }
            mCallbacks.forEach {
                it.prependInsertPage(count, secondHeaderAdded.size - headerCountBefore, secondLoaderAdded.size - loaderCountBefore)
            }
        }

        override fun insertPage(v: V, vPos: Int, position: Int, count: Int) {
            isSecondNotEmpty.add(v)
            pagedList2 ?: return
            val trueStartPos = getTruePos(vPos) + when (secondHeaderAdded.contains(v)) {
                true -> 1
                else -> 0
            } + position - vPos
            items.addAll(trueStartPos, pagedList2!!.subList(position, position + count))
            mCallbacks.forEach { it.insertPage(trueStartPos, count) }
        }

        override fun emptyInsert(v: V) {
            if (isSecondNotEmpty.contains(v)) {
                isSecondEnd.add(v)
                if (addFooter(v)) {
                    var index = items.indexOf(v)
                    if (index != -1) {
                        while (items[index] != LOADER) {
                            index++
                        }
                        items[index] = FOOTER
                        secondFooterAdded.add(v)
                        secondLoaderAdded.remove(v)
                        mCallbacks.forEach { it.emptyInsert(index) }
                    }
                }
            }
        }

        override fun onRemoved(position: Int, count: Int) {
            val truePos = getTruePos(position)
            var tempIndex = 0
            var trueEnd = truePos
            for (index in truePos..items.size) {
                trueEnd++
                if (isItem(items[index])) {
                    tempIndex++
                    if (tempIndex >= count) {
                        break
                    }
                }
            }
            var end = items.getOrNull(trueEnd)
            if (end == FOOTER || end == LOADER) {
                trueEnd++
            } else if (end == HEADER) {
                while ((end != FOOTER || end != LOADER) && trueEnd < items.size) {
                    trueEnd++
                    end = items.getOrNull(trueEnd)
                }
            } else {
                Log.e("PagingWrapper", "onRemoved should not reach here")
            }
            items.subList(truePos, trueEnd).apply {
                forEach { it ->
                    val item = it as? V ?: return@forEach
                    isSecondEnd.remove(item)
                    isSecondNotEmpty.remove(item)
                    secondHeaderAdded.remove(item)
                    secondFooterAdded.remove(item)
                    secondLoaderAdded.remove(item)
                }
                clear()
            }
            mCallbacks.forEach { it.onRemoved(truePos, trueEnd) }
        }
    }

    init {
        pagedList2?.addWeakCallback(null, mCallback)
    }

    open fun submitList(pagedList: MyContiguousPagedList2<Any, V>?) {
        this.pagedList2 = pagedList
        pagedList?.apply {
            items.addAll(this)
            items.reversedIndices.forEach {
                addLoaderAndHeader(it)
            }
            addWeakCallback(this@MyContiguousPagedListWrapper.pagedList2, mCallback)
        }
    }

    open val size: Int
        get() {
            Log.e("PagingWrapper", this.toString())
            return items.size
        }

    // 考虑 header / footer / loading
    open fun getOrAny(index: Int): V? = when (items[index]) {
        HEADER, FOOTER, LOADER -> null
        else -> items[index] as? V
    }

    open fun get(index: Int): Any? = items[index]

    // 仅支持 header / footer / loader 找对应的一级评论
    open fun getOrigin(index: Int): V? {
        if (index < 1 || index >= items.size) {
            throw IndexOutOfBoundsException()
        }
        return when (items[index]) {
            HEADER -> getOrAny(index - 1)
            FOOTER -> findRecentAddedItem(index, secondFooterAdded)
            LOADER -> findRecentAddedItem(index, secondLoaderAdded)
            else -> null
        }
    }

    protected open fun findRecentAddedItem(index: Int, added: MutableSet<V>): V? {
        var originIndex = index - 1
        var result = getOrAny(originIndex)
        while (originIndex >= 0 && (result == null || !added.contains(result))) {
            originIndex--
            result = getOrAny(originIndex)
        }
        return when {
            originIndex < 0 -> null
            else -> result
        }
    }

    open fun getTruePos(position: Int): Int {
        if (position == 0) {
            return 0
        }
        var tempIndex = 0
        var truePos = 0
        for (item in items) {
            truePos++
            if (isItem(item)) {
                tempIndex++
                if (tempIndex == position) {
                    break
                }
            }
        }
        return truePos
    }

    open fun getOriginPos(position: Int): Int {
        if (position == 0) {
            return 0
        }
        var originIndex = 0
        var truePos = 0
        for (item in items) {
            truePos++
            if (isItem(item)) {
                originIndex++
            }
            if (truePos == position) {
                break
            }
        }
        return originIndex
    }

    override fun toString(): String {
        return items.joinToString {
            when (it) {
                HEADER -> "header"
                FOOTER -> "footer"
                LOADER -> "loader"
                else -> "item"
            }
        }
    }

    companion object {
        val HEADER = Any()
        val FOOTER = Any()
        val LOADER = Any()

        fun isItem(item: Any?) = item == null || item != HEADER && item != FOOTER && item != LOADER

        val DEFAULT_HAS_SECOND = object : HasSecond<Any> {
            override fun has(v: Any): Boolean = false
        }
    }

    interface HasSecond<V> {
        fun has(v: V): Boolean
    }

    interface DataChangedCallback {
        fun initialPage(leadingNulls: Int, trailingNulls: Int, count: Int, headerCount: Int, loaderCount: Int)
        fun appendChangedPage(position: Int, changedCount: Int, headerCount: Int, loaderCount: Int)
        fun appendInsertPage(startPos: Int, count: Int, headerCount: Int, loaderCount: Int)
        fun prependChangedPage(position: Int, changedCount: Int, headerCount: Int, loaderCount: Int)
        fun prependInsertPage(count: Int, headerCount: Int, loaderCount: Int)
        fun insertPage(position: Int, count: Int)
        fun emptyInsert(position: Int)  // 该 position 的 item 类型从 loader 变为 footer
        fun onRemoved(position: Int, count: Int)
    }
}

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/24
 * <p>
 * 支持二级的 footer / header / loading
 */
abstract class PagingAdapterEnhance<T, VH : RecyclerView.ViewHolder> : PagingAdapter3<T, VH> {
    constructor(config: AsyncDifferConfig<T>) : super(config)
    constructor(diffCallback: DiffUtil.ItemCallback<T>) : super(diffCallback)

    // 二级的 header / footer / load
    protected open val dataChangedCallback: MyContiguousPagedListWrapper.DataChangedCallback by lazy {
        return@lazy object : MyContiguousPagedListWrapper.DataChangedCallback {
            override fun initialPage(leadingNulls: Int, trailingNulls: Int, count: Int, headerCount: Int, loaderCount: Int) {
                Log.e("PagingWrapper", "initialPage -- notifyItemRangeInserted(${fixPositionStart(0)}, ${count + headerCount + loaderCount})")
                notifyItemRangeInserted(fixPositionStart(0), count + headerCount + loaderCount)
            }

            override fun appendChangedPage(position: Int, changedCount: Int, headerCount: Int, loaderCount: Int) {
                Log.e("PagingWrapper", "appendChangedPage -- notifyItemRangeChanged(${fixPositionStart(position)}, ${changedCount + headerCount + loaderCount})")
                notifyItemRangeChanged(fixPositionStart(position), changedCount + headerCount + loaderCount)
            }

            override fun appendInsertPage(startPos: Int, count: Int, headerCount: Int, loaderCount: Int) {
                Log.e("PagingWrapper", "appendInsertPage -- notifyItemRangeInserted(${fixPositionStart(startPos)}, ${count + headerCount + loaderCount})")
                notifyItemRangeInserted(fixPositionStart(startPos), count + headerCount + loaderCount)
            }

            override fun prependChangedPage(position: Int, changedCount: Int, headerCount: Int, loaderCount: Int) {
                Log.e("PagingWrapper", "prependChangedPage -- notifyItemRangeChanged(${fixPositionStart(position)}, ${changedCount + headerCount + loaderCount})")
                notifyItemRangeChanged(fixPositionStart(position), changedCount + headerCount + loaderCount)
            }

            override fun prependInsertPage(count: Int, headerCount: Int, loaderCount: Int) {
                Log.e("PagingWrapper", "prependInsertPage -- notifyItemRangeInserted(${fixPositionStart(0)}, ${count + headerCount + loaderCount})")
                notifyItemRangeInserted(fixPositionStart(0), count + headerCount + loaderCount)
            }

            override fun insertPage(position: Int, count: Int) {
                Log.e("PagingWrapper", "insertPage -- notifyItemRangeInserted(${fixPositionStart(position)}, ${count})")
                notifyItemRangeInserted(fixPositionStart(position), count)
            }

            override fun emptyInsert(position: Int) {
                Log.e("PagingWrapper", "emptyInsert -- notifyItemChanged(${fixPositionStart(position)})")
                notifyItemChanged(fixPositionStart(position))
            }

            override fun onRemoved(position: Int, count: Int) {
                Log.e("PagingWrapper", "onRemoved -- notifyItemRangeRemoved(${fixPositionStart(position)}, ${count})")
                notifyItemRangeRemoved(fixPositionStart(position), count)
            }
        }
    }

    @Volatile
    open var listWrapper: MyContiguousPagedListWrapper<T>? = null
        set(value) {
            field = value
            value?.mCallbacks?.add(dataChangedCallback)
        }
    open var hasSecond: MyContiguousPagedListWrapper.HasSecond<T>? = null
        set(value) {
            if (field != value) {
                field = value
                secondFlagChanged(field != null)
                notifyDataSetChangedAfterGetItemCount()
            }
        }

    open var supportSecondHeader = true
        set(value) {
            if (field != value) {
                field = value
                secondFlagChanged(field)
                notifyDataSetChangedAfterGetItemCount()
            }
        }

    protected open fun secondFlagChanged(secondFlag: Boolean) {
        listWrapper = when {
            secondFlag -> MyContiguousPagedListWrapper(mDiffer.currentList as? MyContiguousPagedList2<Any, T>,
                    supportSecondHeader, supportSecondFooter, supportSecondLoader, hasSecond)
            else -> null
        }
    }

    open var supportSecondFooter = true
        set(value) {
            if (field != value) {
                field = value
                secondFlagChanged(field)
                notifyDataSetChangedAfterGetItemCount()
            }
        }
    open var supportSecondLoader = true
        set(value) {
            if (field != value) {
                field = value
                secondFlagChanged(field)
                notifyDataSetChangedAfterGetItemCount()
            }
        }

    init {
        secondFlagChanged(supportSecondFooter || supportSecondHeader || supportSecondLoader)
    }

    override fun loadAround(position: Int) {
        if (listWrapper == null) {
            super.loadAround(position)
        } else {
            super.loadAround(listWrapper!!.getOriginPos(position))
        }
    }

    override fun submitList(pagedList: PagedList<T>?) {
        super.submitList(pagedList)
        listWrapper?.submitList(pagedList as? MyContiguousPagedList2<Any, T>)
    }

    override fun submitList(pagedList: PagedList<T>?, commitCallback: Runnable?) {
        super.submitList(pagedList, commitCallback)
        listWrapper?.submitList(pagedList as? MyContiguousPagedList2<Any, T>)
    }

    open fun getOrigin(index: Int) = listWrapper?.getOrigin(mappingAdapterPos2DataPos(index))
    open fun getOrigin(holder: VH) = listWrapper?.getOrigin(mappingAdapterPos2DataPos(holder.adapterPosition))

    // override

    override fun getItem(position: Int): T? = when {
        listWrapper != null -> listWrapper!!.getOrAny(position)
        else -> super.getItem(position)
    }

    override fun getDataItemCount(): Int = listWrapper?.size ?: super.getDataItemCount()

    override fun getDataViewType(position: Int): Int {
        val item = when (listWrapper) {
            null -> super.getItem(position)
            else -> listWrapper!!.get(position)
        }
        return when (item) {
            MyContiguousPagedListWrapper.HEADER -> TYPE_SECOND_HEADER
            MyContiguousPagedListWrapper.FOOTER -> TYPE_SECOND_FOOTER
            MyContiguousPagedListWrapper.LOADER -> TYPE_SECOND_LOADER
            else -> getNormalViewType(position, item as? T)
        }
    }

    override fun createViewHolder(viewType: Int, parent: ViewGroup): VH = when (viewType) {
        TYPE_SECOND_HEADER -> onCreateSecondHeaderViewHolder(parent, viewType)
        TYPE_SECOND_FOOTER -> onCreateSecondFooterViewHolder(parent, viewType)
        TYPE_SECOND_LOADER -> onCreateSecondLoaderViewHolder(parent, viewType)
        else -> super.createViewHolder(viewType, parent)
    }

    abstract fun onCreateSecondHeaderViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onCreateSecondFooterViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun onCreateSecondLoaderViewHolder(parent: ViewGroup, viewType: Int): VH

    override fun onBindNormalViewHolder(holder: VH, viewType: Int, value: T?) = when (viewType) {
        TYPE_SECOND_HEADER -> onBindSecondHeaderViewHolder(holder)
        TYPE_SECOND_FOOTER -> onBindSecondFooterViewHolder(holder)
        TYPE_SECOND_LOADER -> onBindSecondLoaderViewHolder(holder)
        else -> super.onBindNormalViewHolder(holder, viewType, value)
    }

    open fun onBindSecondHeaderViewHolder(holder: VH) = Unit
    open fun onBindSecondFooterViewHolder(holder: VH) = Unit
    open fun onBindSecondLoaderViewHolder(holder: VH) = Unit

    companion object {
        const val TYPE_SECOND_HEADER = -11
        const val TYPE_SECOND_FOOTER = -12
        const val TYPE_SECOND_LOADER = -13
        const val TYPE_SECOND_LOAD_ERROR = -14
    }
}

abstract class DefaultPagingAdapterEnhance<T> : PagingAdapterEnhance<T, RecyclerView.ViewHolder> {
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
        val vh = DefaultViewHolder(inflater.inflate(getLoaderLayout(), parent, false))
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

    override fun onCreateNormalViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getNormalLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    open fun defaultHandle(holder: DefaultViewHolder, itemView: View) = Unit

    open fun getHeaderLayout() = R.layout.layout_default_paing_item
    open fun getFooterLayout() = R.layout.layout_default_paing_item
    open fun getLoaderLayout() = R.layout.layout_default_paing_item
    open fun getInvalidLayout() = R.layout.layout_default_paing_item
    open fun getEmptyLayout() = R.layout.layout_default_paing_item
    open fun getNormalLayout() = R.layout.layout_default_paing_item

    override fun onCreateSecondHeaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getSecondHeaderLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    override fun onCreateSecondFooterViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getSecondFooterLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    override fun onCreateSecondLoaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(getSecondLoaderLayout(), parent, false))
        defaultHandle(vh, vh.itemView)
        return vh
    }

    open fun getSecondHeaderLayout() = R.layout.layout_default_paing_item
    open fun getSecondFooterLayout() = R.layout.layout_default_paing_item
    open fun getSecondLoaderLayout() = R.layout.layout_default_paing_item
}
