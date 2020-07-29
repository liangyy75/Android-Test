package com.liang.example.recyclerviewtest.recycler1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;

/**
 * 可添加的功能 1. 点击事件处理 2. 删除和添加item 3. 添加item绑定抽象方法 4. 下拉刷新和加载更多 1.
 * SwipeRefreshLayout(下拉刷新) 1. setOnRefreshListener(OnRefreshListener)
 * 添加下拉刷新监听，顶部下拉时会调用这个方法，在里面实现请求数据的逻辑，设置下拉进度条消失等等 2. setRefreshing(boolean)
 * 显示或者隐藏刷新动画 3. isRefreshing() 检查是否处于刷新状态 4. setColorSchemeResources()
 * 设置进度条的颜色主题，参数为可变参数，并且是资源id，可以设置多种不同的颜色，每转一圈就显示一种颜色，以前的setColorScheme()方法已经弃用了
 * 2. 5. 多item布局
 * <p>
 * recyclerview可以做的 * recyclerview.Adapter * recyclerview.ViewHolder *
 * recyclerview.ItemDecoration * recyclerview.ItemAnimator *
 * recyclerview.LayoutManager * recyclerview.AdapterDataObserver
 * <p>
 * * recyclerview.OnScrollListener + SimpleOnItemTouchListener 1. public void
 * onTouchEvent(RecyclerView rv, MotionEvent e); 2. public boolean
 * onInterceptTouchEvent(RecyclerView rv, MotionEvent e); 3. public void
 * onRequestDisallowInterceptTouchEvent(boolean disallowIntercept); 4.
 * 可以有这个，自己用在onTouchEvent中: private class MyGestureListener extends
 * GestureDetector.SimpleOnGestureListener {...} *
 * recyclerview.OnItemTouchListener * recyclerview.OnFlingListener *
 * recyclerview.OnChildAttachStateChangeListener 1.public void
 * onChildViewAttachedToWindow(View view); 2. public void
 * onChildViewDetachedFromWindow(View view); *
 * recyclerview.OnApplyWindowInsetsListener * recyclerview.RecyclerListener *
 * recyclerview.
 * <p>
 * * recyclerview.ChildDrawingCallback * recyclerview.EdgeEffectFactory *
 * recyclerview.
 * <p>
 * 可以实现的功能 1. adapter + viewHolder + onItemClickListener 2. Footer + Header +
 * EmptyView TODO: emptyView 3. 上拉刷新和下载加载 4. 多Item布局 + AdapterDataObserver 5.
 * ItemDecoration + ItemAnimator + LayoutManager 6. onScrollListener +
 * onItemTouchListener 7. TODO: TreeView
 */
public abstract class RVAdapterTest<T> extends RecyclerView.Adapter<RVViewHolderTest> {
    private List<T> datas;
    private Context context;
    private int itemLayoutId;
    private RecyclerView recyclerView;
    private OnItemViewTypeListener<T> onItemViewTypeListener;
    private int[] itemLayoutIds = null; // TODO: 好像弄错了
    private OnItemClickListener<T> onItemClickListener;
    private View headerView = null;
    private View footerView = null;

    private static final int TYPE_HEADER = -1;
    private static final int TYPE_FOOTER = -2;

    protected RVAdapterTest(List<T> datas, Context context, int itemLayoutId, RecyclerView recyclerView) {
        this.datas = datas;
        this.context = context;
        this.itemLayoutId = itemLayoutId;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        if (headerView != null) {
            if (position == 0)
                return TYPE_HEADER;
            position--;
        }
        if (footerView != null && position == datas.size())
            return TYPE_FOOTER;
        return onItemViewTypeListener != null && itemLayoutIds != null
                ? onItemViewTypeListener.getItemViewType(position, datas.get(position))
                : super.getItemViewType(position);
    }

    public void setOnItemViewTypeListener(OnItemViewTypeListener<T> onItemViewTypeListener) {
        this.onItemViewTypeListener = onItemViewTypeListener;
    }

    public void setItemLayoutIds(int[] itemLayoutIds) {
        this.itemLayoutIds = itemLayoutIds;
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
    }

    public void setFooterView(View footerView) {
        this.footerView = footerView;
    }

    public boolean hasHeaderView() {
        return this.headerView != null;
    }

    @NonNull
    @Override
    public RVViewHolderTest onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER && headerView != null)
            return new RVViewHolderTest(headerView);
        if (viewType == TYPE_FOOTER && footerView != null)
            return new RVViewHolderTest(footerView);
        return onItemViewTypeListener == null || itemLayoutIds == null || viewType >= itemLayoutIds.length
                || viewType < 0 ? RVViewHolderTest.get(context, parent, itemLayoutId)
                        : RVViewHolderTest.get(context, parent, itemLayoutIds[viewType]);
    }

    @Override
    public void onBindViewHolder(@NonNull final RVViewHolderTest viewHolder, int position) {
        if (datas == null || datas.size() == 0)
            return;
        if (headerView != null) {
            if (position == 0)
                return;
            position--;
        }
        if (footerView != null && position == datas.size())
            return;
        final int pos = position;
        final T data = datas.get(position);
        bindView(viewHolder, data, position);
        if (onItemClickListener != null) {
            viewHolder.getRoot().setOnClickListener(v -> onItemClickListener.onItemClick(v, data, pos));
            viewHolder.getRoot().setOnLongClickListener(v -> onItemClickListener.onItemLongClick(v, data, pos));
        }
        // 去除冗余的setItemClick事件 -- 但是需要用到data属性啊，真难，上面只有ViewHolder而没有position，所以。。。
        // 应该考虑使用 setOnTouchListener 与 GestureDetectorCompat(为什么使用这个???) ，不仅优雅，而且只需要一个
        // Listener 。
    }

    public abstract void bindView(RVViewHolderTest viewHolder, T data, int position);

    @Override
    public int getItemCount() {
        int size = datas.size();
        if (headerView != null)
            size++;
        if (footerView != null)
            size++;
        return size;
    }

    public int getDataSize() {
        return datas.size();
    }

    public T getItem(int position) {
        return datas.get(position);
    }

    public void addData(T data) {
        int position = datas.size();
        datas.add(data);
        if (headerView != null)
            position++;
        notifyItemInserted(position);
        if (footerView != null)
            position++;
        recyclerView.scrollToPosition(position);
    }

    public void addData(T data, int position) {
        datas.add(position, data);
        if (headerView != null)
            position++;
        notifyItemInserted(position);
        if (footerView != null && position == datas.size() - 1)
            position++;
        recyclerView.scrollToPosition(position);
    }

    public T remove(int position) {
        int dataPosition = position;
        T old = datas.remove(position);
        if (headerView != null)
            position++;
        notifyItemRemoved(position);
        scrollToPosition(position, dataPosition);
        return old;
    }

    public void remove(T data) {
        int position = datas.indexOf(data);
        int dataPosition = position;
        if (headerView != null)
            position++;
        datas.remove(data);
        notifyItemRemoved(position);
        scrollToPosition(position, dataPosition);
    }

    private void scrollToPosition(int position, int dataPosition) {
        if (dataPosition == 0)
            recyclerView.scrollToPosition(0);
        else if (dataPosition < datas.size() || footerView != null)
            recyclerView.scrollToPosition(position);
        else
            recyclerView.scrollToPosition(position - 1);
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
        notifyDataSetChanged();
        if (datas.size() > 0)
            recyclerView.scrollToPosition(0);
    }

    public void move(int fromPosition, int toPosition) {
        T sourceData = datas.get(fromPosition);
        int increment = fromPosition < toPosition ? 1 : -1;
        for (int i = fromPosition; i != toPosition; i += increment)
            datas.set(i, datas.get(i + increment)); // TODO some bugs should to be fixed:
                                                    // java.lang.ArrayIndexOutOfBoundsException: length=15; index=-1
        datas.set(toPosition, sourceData);
        if (headerView != null) {
            fromPosition++;
            toPosition++;
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (headerView != null && position == 0 || footerView != null && position == datas.size() + 1)
                        return gridLayoutManager.getSpanCount();
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RVViewHolderTest holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams)
            ((StaggeredGridLayoutManager.LayoutParams) layoutParams)
                    .setFullSpan(holder.getItemViewType() == TYPE_FOOTER || holder.getItemViewType() == TYPE_HEADER);
    }

    public interface OnItemViewTypeListener<T> {
        int getItemViewType(int position, T data);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(View view, T data, int position);

        boolean onItemLongClick(View view, T data, int position);
    }
}
