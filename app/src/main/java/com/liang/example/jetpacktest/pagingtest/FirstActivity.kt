package com.liang.example.jetpacktest.pagingtest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.MyLivePagedListBuilder
import androidx.paging.MyPositionalDataSource
import androidx.paging.PagedList
import androidx.paging.ParamsDelegate
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liang.example.androidtest.R
import com.liang.example.utils.r.dp2px
import kotlinx.android.synthetic.main.activity_test_recyclerview.test_recyclerview
import kotlinx.android.synthetic.main.item_paging_test1.view.image_cover
import kotlinx.android.synthetic.main.item_paging_test1.view.value

open class Entity {
    open var text: String = ""

    open var coverLeft = dp2px(18f)
    open var key: Int = 0
    open var index: Int = 0

    open var origin: Entity? = null
    open var last = false
    open var hasChild = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entity

        if (text != other.text) return false
        if (key != other.key) return false
        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + key
        result = 31 * result + index
        return result
    }
}

open class MyLoadRangeParams2(startPosition: Int, loadSize: Int, open val type: Int) : MyPositionalDataSource.MyLoadRangeParams(startPosition, loadSize)

class LocalDataSourceFactory(delegate: ParamsDelegate) : DataSource.Factory<Int, Entity>() {
    override fun create(): DataSource<Int, Entity> = object : MyPositionalDataSource<Entity>() {
        private fun computeCount(): Int {
            return 1000
        }

        private fun loadRangeInternal(startPosition: Int, loadCount: Int, position: Int = -1): List<Entity> {
            val articleList = mutableListOf<Entity>()
            for (i in 0 until loadCount) {
                val articleEntity = Entity()
                if (position == -1) {
                    articleEntity.text = (startPosition + i).toString()
                } else {
                    articleEntity.text = "$startPosition - $i"
                    articleEntity.coverLeft = dp2px(66f)
                }
                articleList.add(articleEntity)
            }
            return articleList
        }

        override fun loadRange(params: MyLoadRangeParams, callback: LoadRangeCallback<Entity>) {
            Log.e("LoadRange", "range" + params.startPosition)
            if (params is ArticlePageAdapter.RangeParams) {
                callback.onResult(loadRangeInternal(params.startPosition, params.loadSize, params.type))
            } else {
                callback.onResult(loadRangeInternal(params.startPosition, params.loadSize))
            }
        }

        override fun loadInitial(params: MyLoadInitialParams, callback: LoadInitialCallback<Entity>) {
            val totalCount = computeCount()
            val position = computeInitialLoadPosition(params, totalCount)
            val loadSize = computeInitialLoadSize(params, position, totalCount)
            callback.onResult(loadRangeInternal(position, loadSize), position, totalCount)
        }
    }.apply {
        this.delegate = delegate
    }
}

class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bindTo(data: Entity?) {
        data?.apply {
            itemView.value.text = text
            itemView.image_cover.apply {
                val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return@apply
                lp.marginStart = coverLeft
                lp.leftMargin = coverLeft
                layoutParams = lp
            }
        }
    }
}

class ArticlePageAdapter(val context: Context) : PagingAdapter2<Entity, ArticleViewHolder>(diffCallback), ParamsDelegate {
    val inflater = LayoutInflater.from(context)
    val level2s = mutableMapOf<Long, Int>()

    override fun onCreateNormalViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val itemView = inflater.inflate(R.layout.item_paging_test1, parent, false)
        itemView.setOnClickListener {
            val pos = (itemView.getTag(R.id.PAGE_TAG_KEY) as? ArticleViewHolder)?.adapterPosition ?: return@setOnClickListener
            val time = loadAround2(pos)
            if (time > 0) {
                level2s[time] = pos
            }
        }
        return ArticleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Entity>() {
            override fun areItemsTheSame(oldItem: Entity, newItem: Entity): Boolean = oldItem.text == newItem.text
            override fun areContentsTheSame(oldItem: Entity, newItem: Entity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun getRangeParams(startPosition: Int, count: Int, recentTimeStamp: Long): MyPositionalDataSource.MyLoadRangeParams {
        return if (recentTimeStamp != 0L && level2s.contains(recentTimeStamp)) {
            RangeParams(startPosition, count, level2s[recentTimeStamp]!!)
        } else {
            MyPositionalDataSource.MyLoadRangeParams(startPosition, count)
        }
    }

    open class RangeParams(startPosition: Int, loadSize: Int, open val type: Int) : MyPositionalDataSource.MyLoadRangeParams(startPosition, loadSize)
}

/**
 * @author liangyuying.lyy75
 * @date 2020/8/17
 * <p>
 */
class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_recyclerview)

        val pageAdapter = ArticlePageAdapter(this)
        test_recyclerview.adapter = pageAdapter
        test_recyclerview.layoutManager = LinearLayoutManager(this)

        val pagedListConfig = PagedList.Config.Builder().setEnablePlaceholders(true).setPageSize(10).setInitialLoadSizeHint(20).build()
        val postList = MyLivePagedListBuilder(LocalDataSourceFactory(pageAdapter), pagedListConfig).build()
        postList.observe(this, Observer {
            Log.d("LoadAround", "observe")
            pageAdapter.submitList(it)
        })
    }
}
