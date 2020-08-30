package com.liang.example.androidtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.liang.example.ktandroidtest.ConstantsKt;
import com.liang.example.recyclerviewtest.recycler1.RVAdapterTest;
import com.liang.example.recyclerviewtest.recycler1.RVViewHolderTest;
import com.liang.example.utils.ApiManager;
import com.liang.example.basic_ktx.ArrayApiKt;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static void bindActivityList(String[] ns, String[] ds, String[] as, String[] crs, String[] us, Class[] cls, Activity a, String t) {
        ApiManager.LOGGER.d(t, "onCreate -- begin creating Activity List");
        int length = cls.length;
        List<ActivityItem> dataSet = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            dataSet.add(new ActivityItem(ns[i], ds[i], as[i], crs[i], us[i], cls[i]));
        }

        RecyclerView rv = a.findViewById(R.id.test_activity_list);
        rv.setHasFixedSize(true);
        LinearLayoutManager activityLayoutManager = new LinearLayoutManager(a, RecyclerView.VERTICAL, false);
        activityLayoutManager.setInitialPrefetchItemCount(4);  // 数据预取(https://juejin.im/entry/58a3f4f62f301e0069908d8f)
        activityLayoutManager.setItemPrefetchEnabled(true);
        rv.setLayoutManager(activityLayoutManager);

        RVAdapterTest<ActivityItem> rvAdapter = new RVAdapterTest<ActivityItem>(dataSet, a, R.layout.item_activity_list, rv) {
            public void bindView(RVViewHolderTest viewHolder, ActivityItem data, int position) {
                viewHolder.getViewById(R.id.test_activity_item_image).setBackgroundResource(R.mipmap.ic_launcher);
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_name)).setText(data.getName());
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_message)).setText(
                        String.format("%s / %s / %s", data.getAuthor(), data.getCreated(), data.getUpdated()));
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_desc)).setText(data.getDesc());
            }
        };
        rvAdapter.setOnItemClickListener(new RVAdapterTest.OnItemClickListener<ActivityItem>() {
            @Override
            public void onItemClick(View view, ActivityItem data, int position) {
                a.startActivity(new Intent(a, data.getClazz()));
                // a.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                // a.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }

            @Override
            public boolean onItemLongClick(View view, ActivityItem data, int position) {
                Toast.makeText(a, data.getDesc(), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        // rv.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
        //     @Override
        //     public boolean onInterceptTouchEvent(@NotNull RecyclerView rv, @NotNull MotionEvent e) {
        //         View view = rv.findChildViewUnder(e.getX(), e.getY());
        //         if (view == null) return false;
        //         // final RVViewHolderTest holder = (RVViewHolderTest) activityList.getChildViewHolder(view);
        //         int position = rv.getChildAdapterPosition(view);
        //         ActivityItem data = rvAdapter.getItem(position);
        //         a.startActivity(new Intent(a, data.getClazz()));
        //         a.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        //         a.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        //         return false;
        //     }
        //
        //     // private GestureDetectorCompat gestureDetectorCompat = new GestureDetectorCompat(a, new GestureDetector.SimpleOnGestureListener() {
        //     //     @Override
        //     //     public boolean onSingleTapConfirmed(MotionEvent e) {
        //     //         View view = rv.findChildViewUnder(e.getX(), e.getY());
        //     //         if (view == null) return false;
        //     //         int position = rv.getChildAdapterPosition(view);
        //     //         ActivityItem data = rvAdapter.getItem(position);
        //     //         a.startActivity(new Intent(a, data.getClazz()));
        //     //         a.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        //     //         a.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        //     //         return true;
        //     //     }
        //     // });
        //     //
        //     // @Override
        //     // public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        //     //     gestureDetectorCompat.onTouchEvent(e);
        //     //     return false;
        //     // }
        // });
        rv.setAdapter(rvAdapter);
        // activityList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ApiManager.LOGGER.d(t, "onCreate -- finish creating Activity List, ANDROID_VERSION_CODE: %d", Build.VERSION.SDK_INT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindActivityList(
                ArrayApiKt.plus(Constants.names, ConstantsKt.getNames()),
                ArrayApiKt.plus(Constants.descs, ConstantsKt.getDescs()),
                ArrayApiKt.plus(Constants.authors, ConstantsKt.getAuthors()),
                ArrayApiKt.plus(Constants.created, ConstantsKt.getCreated()),
                ArrayApiKt.plus(Constants.updated, ConstantsKt.getUpdated()),
                ArrayApiKt.plus(Constants.classes, ConstantsKt.getClasses()),
                this, "App_Main");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView textView = ((ApplicationTest) getApplication()).textView;
        if (textView.getParent() != null && textView.getParent() instanceof ViewGroup) {
            ((ViewGroup) textView.getParent()).removeView(textView);
        }
        ((LinearLayout) findViewById(R.id.test_activity_root)).addView(textView, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((LinearLayout) findViewById(R.id.test_activity_root)).removeViewAt(0);
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

    @SuppressWarnings("unused")
    public void in(View v) {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);  // 可以同时使用两个动画，甚至更多
    }

    @SuppressWarnings("unused")
    public void out(View v) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
