package com.liang.example.recyclerviewtest;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;

public abstract class LoadMoreScrollListener2 extends RecyclerView.OnScrollListener {
    private static final String TAG = "LoadMoreScrollListener2";

    private LinearLayoutManager layoutManager;
    private int lastState = RecyclerView.SCROLL_STATE_IDLE;
    private long timeLimit;
    private long lastTimeMillis = -1;

    public LoadMoreScrollListener2(LinearLayoutManager layoutManager) {
        this(layoutManager, 256);
    }

    public LoadMoreScrollListener2(LinearLayoutManager layoutManager, long timeLimit) {
        this.layoutManager = layoutManager;
        this.timeLimit = timeLimit;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            Log.d(TAG, "SCROLL_STATE_IDLE time: " + System.currentTimeMillis());
            if (layoutManager.findLastCompletelyVisibleItemPosition() < layoutManager.getItemCount() - 1)
                return;
            if (System.currentTimeMillis() - lastTimeMillis > timeLimit)
                onLoadMore();
        } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING && lastState == RecyclerView.SCROLL_STATE_IDLE) {
            lastTimeMillis = System.currentTimeMillis();
            Log.d(TAG, "SCROLL_STATE_DRAGGING time: " + lastTimeMillis);
        }
        lastState = newState;

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
        lastTimeMillis = System.currentTimeMillis();
        Log.d(TAG, "onScrolled time -- Instant.ofEpochMilli: " + Instant.ofEpochMilli(lastTimeMillis).toString() + "onScrolled time: " + lastTimeMillis);

        Log.d(TAG, "totalItemCount: " + layoutManager.getItemCount()
                + "\nvisibleItemCount: " + layoutManager.getChildCount()
                + "\n部分可见 - firstVisibleItemPosition: " + layoutManager.findFirstVisibleItemPosition()
                + "\n部分可见 - lastVisibleItemPosition: " + layoutManager.findLastVisibleItemPosition()
                + "\n完全可见 - firstCompletelyVisibleItemPosition: " + layoutManager.findFirstCompletelyVisibleItemPosition()
                + "\n完全可见 - lastCompletelyVisibleItemPosition: " + layoutManager.findLastCompletelyVisibleItemPosition()
                + "\ndx: " + dx + ", dy: " + dy);
    }

    public abstract void onLoadMore();
}
