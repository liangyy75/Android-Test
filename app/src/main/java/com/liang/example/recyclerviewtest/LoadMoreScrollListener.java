package com.liang.example.recyclerviewtest;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class LoadMoreScrollListener extends RecyclerView.OnScrollListener {
    private static final String TAG = "LoadMoreScrollListener";

    private LinearLayoutManager mLinearLayoutManager;
    private boolean isLoading = false;
    private int previousTotal = 0;

    public LoadMoreScrollListener(LinearLayoutManager mLinearLayoutManager) {
        this.mLinearLayoutManager = mLinearLayoutManager;
    }

    // @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        String message = "UnKnown";
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                message = "SCROLL_STATE_IDLE";
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                message = "SCROLL_STATE_DRAGGING";
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                message = "SCROLL_STATE_SETTLING";
                break;
        }
        Log.d(TAG, "state: " + message);
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        Log.d(TAG, "totalItemCount: " + mLinearLayoutManager.getItemCount()
                + "\nvisibleItemCount: " + mLinearLayoutManager.getChildCount()
                + "\n部分可见 - firstVisibleItemPosition: " + mLinearLayoutManager.findFirstVisibleItemPosition()
                + "\n部分可见 - lastVisibleItemPosition: " + mLinearLayoutManager.findLastVisibleItemPosition()
                + "\n完全可见 - firstCompletelyVisibleItemPosition: " + mLinearLayoutManager.findFirstCompletelyVisibleItemPosition()
                + "\n完全可见 - lastCompletelyVisibleItemPosition: " + mLinearLayoutManager.findLastCompletelyVisibleItemPosition()
                + "\ndx: " + dx + ", dy: " + dy);

        int totalItemCount = mLinearLayoutManager.getItemCount();
        int visibleItemCount = mLinearLayoutManager.getChildCount();
        if (isLoading) {
            if (totalItemCount > previousTotal) {  // 说明数据已经加载完了
                isLoading = false;
                previousTotal = totalItemCount;
            }
        } else if (visibleItemCount > 0 &&
                mLinearLayoutManager.findLastVisibleItemPosition() >= totalItemCount - 1 &&  // 最后一个item可见
                totalItemCount > visibleItemCount) {  // 数据不足一屏幕不触发加载更多
            onLoadMore();
            isLoading = true;
        }
    }

    public abstract void onLoadMore();
}
