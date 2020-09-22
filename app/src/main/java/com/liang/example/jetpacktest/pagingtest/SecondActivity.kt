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
import androidx.core.view.marginBottom
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
import kotlinx.android.synthetic.main.item_paging_test1.view.image_cover
import kotlinx.android.synthetic.main.item_paging_test1.view.value
import kotlinx.android.synthetic.main.layout_default_paing_item.view.default_paging_item_root

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
            Thread.sleep(3000)
            return articleList
        }

        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Int, Entity>) {
            Log.d("loadInitial", "1")
            val data = loadRangeInternal(0, 20)
            handler.post { callback.onResult(data, null, 1) }
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Entity>) = showToast("Not yet implemented")

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Entity>) {
            Log.d("loadAfter", "${params.key}")
            val data = if (params.key < 5) loadRangeInternal(params.key, 10) else listOf()
            handler.post { callback.onResult(data, params.key + 1) }
        }

        override fun loadInsert(params: LoadInsertParams<Int>, callback: LoadCallback<Int, Entity>) {
            Log.d("loadInsert", "${params.key}")
            (params.value as? Entity)?.hasChild = true
            val data = loadRangeInternal((params.value as? Entity)?.key
                    ?: 0, 3, params.key, origin = params.value as? Entity)
            handler.post { callback.onResult(data, params.key + 1) }
        }

        override fun getInitialInsertKey(value: Entity): Int? = 0
    }
}

class ArticleViewHolder2(itemView: View, private val adapter2: PagingAdapter3<Entity, RecyclerView.ViewHolder>) : PagingViewHolder<Entity>(itemView) {
    override fun bind(data: Entity?, position: Int) {
        data?.apply {
            itemView.value.text = text
            itemView.image_cover.apply {
                val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return
                lp.marginStart = coverLeft
                lp.leftMargin = coverLeft
                layoutParams = lp
            }
            // if (origin == null && !hasChild) {
            //     itemView.view_more.setOnClickListener { adapter2.scheduleInsert(data) }
            // } else if (origin != null && last) {
            //     itemView.view_more.setOnClickListener { adapter2.scheduleInsert(data.origin!!) }
            // } else {
            //     itemView.view_more.setOnClickListener(null)
            // }
        }
    }
}

class ArticlePageAdapter2(context: Context) : SingleDefaultPagingAdapter<Entity>(diffCallback, context) {
    override fun onCreateNormalViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = inflater.inflate(R.layout.item_paging_test1, parent, false)
        return ArticleViewHolder2(itemView, this)
    }

    override fun onBindLoadingViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindLoadingViewHolder(holder)
        onBindOther(holder)
        holder.itemView.default_paging_item_root.addView(TextView(context).apply {
            gravity = Gravity.CENTER
            text = "loading"
        })
    }

    override fun onBindEmptyViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindEmptyViewHolder(holder)
        onBindOther(holder)
        holder.itemView.default_paging_item_root.addView(TextView(context).apply {
            gravity = Gravity.CENTER
            text = "empty"
        })
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindHeaderViewHolder(holder)
        onBindOther(holder)
        holder.itemView.default_paging_item_root.addView(TextView(context).apply {
            gravity = Gravity.CENTER
            text = "header"
        })
    }

    override fun onBindFooterViewHolder(holder: RecyclerView.ViewHolder) {
        super.onBindFooterViewHolder(holder)
        onBindOther(holder)
        holder.itemView.default_paging_item_root.addView(TextView(context).apply {
            gravity = Gravity.CENTER
            text = "footer"
        })
    }

    private fun onBindOther(holder: RecyclerView.ViewHolder) {
        holder.itemView.layoutHeight = dp2px(60f)
        holder.itemView.setMarginBottom(dp2px(8f))
        holder.itemView.setBackgroundColor(getColor(android.R.color.white))
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
                .setPageSize(10)
                .setInitialLoadSizeHint(20)
                .build()
        val postList = MyLivePagedListBuilder2(LocalDataSourceFactory2(handler), pagedListConfig)
                .build()
        postList.observe(this, Observer {
            Log.d("LoadAround", "observe")
            pageAdapter.submitList(it)
        })
    }
}
