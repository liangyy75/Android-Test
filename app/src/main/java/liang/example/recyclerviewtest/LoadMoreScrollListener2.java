package liang.example.recyclerviewtest;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;

public abstract class LoadMoreScrollListener2 extends RecyclerView.OnScrollListener {
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
            // Log.d("RecyclerView.SCROLL_STATE_IDLE time: ", String.valueOf(System.currentTimeMillis()));
            if (layoutManager.findLastCompletelyVisibleItemPosition() < layoutManager.getItemCount() - 1)
                return;
            if (System.currentTimeMillis() - lastTimeMillis > timeLimit)
                onLoadMore();
        } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING && lastState == RecyclerView.SCROLL_STATE_IDLE) {
            lastTimeMillis = System.currentTimeMillis();
            // Log.d("RecyclerView.SCROLL_STATE_DRAGGING time: ", String.valueOf(lastTimeMillis));
        }
        lastState = newState;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        lastTimeMillis = System.currentTimeMillis();
        // Log.d("onScrolled time: ", Instant.ofEpochMilli(lastTimeMillis).toString());
        // Log.d("onScrolled time: ", String.valueOf(lastTimeMillis));
    }

    public abstract void onLoadMore();
}
