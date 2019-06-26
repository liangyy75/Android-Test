package liang.example.fragmenttest;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import liang.example.androidtest.ActivityItem;
import liang.example.androidtest.R;
import liang.example.recyclerviewtest.RVAdapterTest;
import liang.example.recyclerviewtest.RVViewHolderTest;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fragment);

        int length = Constants.classes.length;
        List<ActivityItem> activityItemList = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
            activityItemList.add(new ActivityItem(Constants.names[i], Constants.descs[i],
                    Constants.authors[i], Constants.created[i], Constants.updated[i], Constants.classes[i]));

        RecyclerView fragmentList = findViewById(R.id.test_fragment_list);
        fragmentList.setHasFixedSize(true);
        LinearLayoutManager fragmentLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        fragmentLayoutManager.setInitialPrefetchItemCount(5);
        fragmentLayoutManager.setItemPrefetchEnabled(true);
        fragmentList.setLayoutManager(fragmentLayoutManager);

        RVAdapterTest<ActivityItem> fragmentAdapter = new RVAdapterTest<ActivityItem>(activityItemList, this, R.layout.item_activity_list, fragmentList) {
            @Override
            public void bindView(RVViewHolderTest viewHolder, ActivityItem data, int position) {
                viewHolder.getViewById(R.id.test_activity_item_image).setBackgroundResource(R.mipmap.ic_launcher);
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_name)).setText(data.getName());
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_message)).setText(
                        String.format("%s / %s / %s", data.getAuthor(), data.getCreated(), data.getUpdated()));
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_desc)).setText(data.getDesc());
            }
        };
        fragmentList.setAdapter(fragmentAdapter);

        fragmentList.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            private GestureDetectorCompat gestureDetectorCompat = new GestureDetectorCompat(
                    fragmentList.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    View view = fragmentList.findChildViewUnder(e.getX(), e.getY());if (view == null) return false;
                    int position = fragmentList.getChildAdapterPosition(view);
                    ActivityItem data = fragmentAdapter.getItem(position);
                    startActivity(new Intent(MainActivity.this, data.getClazz()));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    return true;
                }
            });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetectorCompat.onTouchEvent(e);
                return false;
            }
        });
    }
}
