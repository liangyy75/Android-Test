package com.liang.example.jetpacktest.pagingtest

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.MyLivePagedListBuilder2
import androidx.paging.MyPageKeyedDataSource2
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liang.example.androidtest.R
import com.liang.example.utils.r.dp2px
import com.liang.example.utils.r.getColor
import com.liang.example.utils.view.showToast
import com.liang.example.view_ktx.layoutHeight
import com.liang.example.view_ktx.setMarginBottom
import kotlinx.android.synthetic.main.activity_test_recyclerview.test_recyclerview
import kotlinx.android.synthetic.main.item_paging_foot.view.collapse_all
import kotlinx.android.synthetic.main.item_paging_load.view.view_more
import kotlinx.android.synthetic.main.item_paging_test.view.image_cover
import kotlinx.android.synthetic.main.item_paging_test.view.value
import kotlinx.android.synthetic.main.layout_default_paing_item.view.default_paging_item_root

const val initialSize = 10
const val initialKey = 4
const val beforeSize = 10
const val minBefore = 0
const val afterSize = 10
const val maxAfter = 10
const val insertSize = 3
const val maxInsert = 3
const val leadingNulls = 0
const val trailingNulls = 0
const val prefetchDistance = 5
const val threadSleep = 1000L
const val randomSleep = 2000L

class LocalDataSourceFactory2(val handler: Handler) : DataSource.Factory<Int, Entity>() {
    override fun create(): DataSource<Int, Entity> = object : MyPageKeyedDataSource2<Int, Entity>() {
        private fun loadRangeInternal(key: Int, loadCount: Int, key2: Int = -1, origin: Entity? = null): List<Entity> {
            val articleList = mutableListOf<Entity>()
            for (i in 0 until loadCount) {
                val articleEntity = Entity()
                articleEntity.key = key
                articleEntity.index = i
                articleEntity.origin = origin
                articleEntity.text = when {
                    key2 == -1 -> "$key - $i"
                    origin != null -> "$key - ${origin.index} - $key2 - $i"
                    else -> "$key - $key2 - $i"
                }
                if (key2 != -1) {
                    articleEntity.coverLeft = dp2px(66f)
                }
                articleList.add(articleEntity)
            }
            articleList.last().last = true
            if (threadSleep > 0L || randomSleep > 0L) {
                val totalSleep = threadSleep + (0 .. randomSleep).random()
                Log.e("loadData", "totalSleep: $totalSleep")
                Thread.sleep(totalSleep)
            }
            return articleList
        }

        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Int, Entity>) {
            Log.e("loadData", "loadInitial: $initialKey")
            val data = loadRangeInternal(initialKey, initialSize)
            handler.post {
                callback.onResult(data, leadingNulls * beforeSize, trailingNulls * afterSize + initialSize + leadingNulls * beforeSize,
                        initialKey - 1, initialKey + 1)
            }
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Entity>) {
            Log.e("loadData", "loadBefore: ${params.key}")
            if (params.key < minBefore) {
                callback.onResult(listOf(), params.key - 1)
                return
            }
            val data = loadRangeInternal(params.key, beforeSize)
            handler.post { callback.onResult(data, params.key - 1) }
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Entity>) {
            Log.e("loadData", "loadAfter: ${params.key}")
            if (params.key >= maxAfter) {
                callback.onResult(listOf(), params.key + 1)
                return
            }
            val data = loadRangeInternal(params.key, afterSize)
            handler.post { callback.onResult(data, params.key + 1) }
        }

        override fun loadInsert(params: LoadInsertParams<Int>, callback: LoadCallback<Int, Entity>) {
            Log.e("loadData", "loadInsert: ${params.key}")
            if (params.key >= maxInsert) {
                callback.onResult(listOf(), params.key + 1)
                return
            }
            (params.value as? Entity)?.hasChild = true
            val data = loadRangeInternal((params.value as? Entity)?.key
                    ?: 0, insertSize, params.key, origin = params.value as? Entity)
            handler.post { callback.onResult(data, params.key + 1) }
        }

        override fun getInitialInsertKey(value: Entity): Int? = 0
    }
}

class ArticlePageAdapter2(context: Context) : DefaultPagingAdapterEnhance<Entity>(diffCallback, context) {
    init {
        supportEmptyView = false
        supportHeader = SUPPORT_EVEN_EMPTY
        supportSecondHeader = false
        supportSecondFooter = true
        supportSecondLoader = true
        hasSecond = object : MyContiguousPagedListWrapper.HasSecond<Entity> {
            override fun has(v: Entity): Boolean = v.index % 3 == 0
        }
        secondFlagChanged(true)
    }

    override fun getNormalLayout(): Int = R.layout.item_paging_test

    override fun onBindNormalViewHolder(holder: RecyclerView.ViewHolder, viewType: Int, value: Entity?) {
        super.onBindNormalViewHolder(holder, viewType, value)
        value?.apply {
            holder.itemView.value.text = text
            holder.itemView.image_cover.apply {
                val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return
                lp.marginStart = coverLeft
                lp.leftMargin = coverLeft
                layoutParams = lp
            }
        }
    }

    override fun onBindLoaderViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindLoaderViewHolder(holder)
        defaultText(holder, "loading")
    }

    override fun onBindEmptyViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindEmptyViewHolder(holder)
        defaultText(holder, "empty")
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindHeaderViewHolder(holder)
        defaultText(holder, "header")
    }

    override fun onBindFooterViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindFooterViewHolder(holder)
        defaultText(holder, "footer")
    }

    private fun defaultText(holder: RecyclerView.ViewHolder, typeText: String) {
        holder.itemView.default_paging_item_root.addView(TextView(context).apply {
            gravity = Gravity.CENTER
            text = typeText
        })
    }

    override fun defaultHandle(holder: DefaultViewHolder, itemView: View) {
        itemView.layoutHeight = dp2px(60f)
        itemView.setMarginBottom(dp2px(8f))
        itemView.setBackgroundColor(getColor(android.R.color.white))
    }

    override fun onCreateSecondFooterViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(R.layout.item_paging_foot, parent, false))
        vh.itemView.collapse_all.setOnClickListener {
            val entity = getOrigin(vh.adapterPosition)
            showToast("load_second_collapse_click -- entity: $entity")
            TODO("收起")
        }
        return vh
    }

    override fun onCreateSecondLoaderViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DefaultViewHolder(inflater.inflate(R.layout.item_paging_load, parent, false))
        vh.itemView.view_more.setOnClickListener {
            val entity = getOrigin(vh.adapterPosition)
            showToast("load_second_more_click -- entity: $entity")
            if (entity != null) {
                scheduleInsert(entity)
            }
        }
        return vh
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Entity>() {
            override fun areItemsTheSame(oldItem: Entity, newItem: Entity): Boolean = oldItem.text == newItem.text
            override fun areContentsTheSame(oldItem: Entity, newItem: Entity): Boolean {
                return oldItem == newItem
            }
        }
    }
}

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
class SecondActivity : AppCompatActivity() {
    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_recyclerview)

        val pageAdapter = ArticlePageAdapter2(this)
        test_recyclerview.adapter = pageAdapter
        test_recyclerview.layoutManager = LinearLayoutManager(this)

        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(afterSize)
                .setInitialLoadSizeHint(initialSize)
                .setPrefetchDistance(prefetchDistance)
                .build()
        val postList = MyLivePagedListBuilder2(LocalDataSourceFactory2(handler), pagedListConfig)
                .build()
        postList.observe(this, Observer {
            Log.e("LoadAround", "observe")
            pageAdapter.submitList(it)
        })
    }
}
