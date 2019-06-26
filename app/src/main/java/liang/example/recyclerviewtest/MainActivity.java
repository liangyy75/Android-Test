package liang.example.recyclerviewtest;

import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import liang.example.androidtest.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_recyclerview);

        int size = 10;
        List<ExampleItem> dataSet = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            dataSet.add(new ExampleItem("string" + i, i, i + 0.5d, i % 2 == 0));
        }
        RecyclerView recyclerView = findViewById(R.id.test_recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.setInitialPrefetchItemCount(5);
        layoutManager.setItemPrefetchEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        RVAdapterTest<ExampleItem> adapter = new RVAdapterTest<ExampleItem>(dataSet, this, R.layout.item_recyclerview_list, recyclerView) {
            @Override
            public void bindView(RVViewHolderTest viewHolder, ExampleItem data, int position) {
                ((TextView) viewHolder.getViewById(R.id.test_recyclerview_item_num)).setText(String.valueOf(data.getAnInt()));
                ((TextView) viewHolder.getViewById(R.id.test_recyclerview_item_str)).setText(data.getString());
                ((TextView) viewHolder.getViewById(R.id.test_recyclerview_item_double)).setText(String.valueOf(data.getaDouble()));
                ((TextView) viewHolder.getViewById(R.id.test_recyclerview_item_bool)).setText(String.valueOf(data.isaBoolean()));
            }
        };

        adapter.setHeaderView(LayoutInflater.from(this).inflate(R.layout.item_recyclerview_header, recyclerView, false));
        adapter.setFooterView(LayoutInflater.from(this).inflate(R.layout.item_recyclerview_footer, recyclerView, false));

        adapter.setItemLayoutIds(new int[] { R.layout.item_recyclerview_list, R.layout.item_recyclerview_list2, R.layout.item_recyclerview_list3 });
        adapter.setOnItemViewTypeListener((position, data) -> data.getAnInt() % 3);

        // adapter.setOnItemClickListener(new RVAdapterTest.OnItemClickListener<ExampleItem>() {
        //     @Override
        //     public void onItemClick(View view, ExampleItem data, int position) {
        //         Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_LONG).show();
        //     }
        //
        //     @Override
        //     public boolean onItemLongClick(View view, ExampleItem data, int position) {
        //         return false;
        //     }
        // });
        recyclerView.setAdapter(adapter);
        // recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
        //     @Override
        //     public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        //         View view = rv.findChildViewUnder(e.getX(), e.getY());
        //         if (view == null) return false;
        //         int position = rv.getChildAdapterPosition(view);
        //         ExampleItem data = adapter.getItem(position);
        //         Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_LONG).show();
        //         return false;
        //     }
        // });
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            private GestureDetectorCompat gestureDetectorCompat = new GestureDetectorCompat(
                    recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (view == null) return false;
                    int position = recyclerView.getChildAdapterPosition(view);
                    if (adapter.hasHeaderView()) {
                        position--;
                    }
                    ExampleItem data = adapter.getItem(position);
                    Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_LONG).show();
                    return true;
                }
            });
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetectorCompat.onTouchEvent(e);
                return false;
            }
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // recyclerView.postDelayed(() -> {
        //     adapter.addData(new ExampleItem("add string", 99, 99.5, false), 0);
        //     recyclerView.scrollToPosition(0);
        // }, 5000);
        // recyclerView.postDelayed(() -> {
        //     adapter.remove(0);
        // }, 10000);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.test_recyclerview_swipe);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(() -> recyclerView.postDelayed(() -> {
            int size1 = adapter.getDataSize();
            adapter.addData(new ExampleItem("string" + size1, size1, size1 + 0.5f, size1 % 2 == 0), 0);
            swipeRefreshLayout.setRefreshing(false);
        }, 1000));
        // recyclerView.addOnScrollListener(new LoadMoreScrollListener(layoutManager) {
        //     @Override
        //     public void onLoadMore() {
        //         swipeRefreshLayout.setRefreshing(true);
        //         recyclerView.postDelayed(() -> {
        //             int size1 = adapter.getDataSize();
        //             adapter.addData(new ExampleItem("string" + size1, size1, size1 + 0.5f, size1 % 2 == 0));
        //             swipeRefreshLayout.setRefreshing(false);
        //         }, 1000);
        //     }
        // });
        recyclerView.addOnScrollListener(new LoadMoreScrollListener2(layoutManager, 256) {
            @Override
            public void onLoadMore() {
                if (swipeRefreshLayout.isRefreshing())
                    return;
                swipeRefreshLayout.setRefreshing(true);
                recyclerView.postDelayed(() -> {
                    int size1 = adapter.getDataSize();
                    adapter.addData(new ExampleItem("string" + size1, size1, size1 + 0.5f, size1 % 2 == 0));
                    swipeRefreshLayout.setRefreshing(false);
                }, 1000);
            }
        });

        // recyclerView.postDelayed(() -> {
        //     int size1 = adapter.getDataSize();
        //     adapter.addData(new ExampleItem("string" + size1, size1, size1 + 0.5f, size1 % 2 == 0));
        // }, 5000);
        // recyclerView.postDelayed(() -> {
        //     int size1 = adapter.getDataSize();
        //     adapter.addData(new ExampleItem("string" + size1, size1, size1 + 0.5f, size1 % 2 == 0), 0);
        // }, 10000);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                adapter.move(viewHolder.getAdapterPosition() - 1, target.getAdapterPosition() - 1);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.remove(viewHolder.getAdapterPosition() - 1);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // 滑动时改变Item的透明度
                    final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        // 不起效果 TODO
    }
}
