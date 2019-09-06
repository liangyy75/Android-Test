package com.liang.example.androidtest;

import android.content.Intent;
import android.os.PersistableBundle;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import com.liang.example.recyclerviewtest.RVAdapterTest;
import com.liang.example.recyclerviewtest.RVViewHolderTest;
import com.liang.example.utils.ApiManager;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "AndroidTestMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiManager.LOGGER.d(TAG, "onCreate -- begin creating Activity List");
        int length = Constants.classes.length;
        List<ActivityItem> activityItemList = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
            activityItemList.add(new ActivityItem(Constants.names[i], Constants.descs[i],
                    Constants.authors[i], Constants.created[i], Constants.updated[i], Constants.classes[i]));

        RecyclerView activityList = findViewById(R.id.test_activity_list);
        activityList.setHasFixedSize(true);
        LinearLayoutManager activityLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        activityLayoutManager.setInitialPrefetchItemCount(4);  // 数据预取(https://juejin.im/entry/58a3f4f62f301e0069908d8f)
        activityLayoutManager.setItemPrefetchEnabled(true);
        activityList.setLayoutManager(activityLayoutManager);

        RVAdapterTest<ActivityItem> activityAdapter = new RVAdapterTest<ActivityItem>(activityItemList, this, R.layout.item_activity_list, activityList) {
            public void bindView(RVViewHolderTest viewHolder, ActivityItem data, int position) {
                viewHolder.getViewById(R.id.test_activity_item_image).setBackgroundResource(R.mipmap.ic_launcher);
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_name)).setText(data.getName());
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_message)).setText(
                        String.format("%s / %s / %s", data.getAuthor(), data.getCreated(), data.getUpdated()));
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_desc)).setText(data.getDesc());
            }
        };
        activityAdapter.setOnItemClickListener(new RVAdapterTest.OnItemClickListener<ActivityItem>() {
            @Override
            public void onItemClick(View view, ActivityItem data, int position) {
                startActivity(new Intent(MainActivity.this, data.getClazz()));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }

            @Override
            public boolean onItemLongClick(View view, ActivityItem data, int position) {
                Toast.makeText(MainActivity.this, data.getDesc(), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        // activityList.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
        //     @Override
        //     public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        //         View view = activityList.findChildViewUnder(e.getX(), e.getY());
        //         if (view == null) return false;
        //         // final RVViewHolderTest holder = (RVViewHolderTest) activityList.getChildViewHolder(view);
        //         int position = activityList.getChildAdapterPosition(view);
        //         ActivityItem data = activityAdapter.getItem(position);
        //         startActivity(new Intent(MainActivity.this, data.getClazz()));
        //         overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        //         overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        //         return false;
        //     }
        // });
        activityList.setAdapter(activityAdapter);
        // activityList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ApiManager.LOGGER.d(TAG, "onCreate -- finish creating Activity List");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState, @NotNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void in(View v) {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);  // 可以同时使用两个动画，甚至更多
    }

    public void out(View v) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
