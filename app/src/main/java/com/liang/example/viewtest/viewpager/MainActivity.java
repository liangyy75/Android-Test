package com.liang.example.viewtest.viewpager;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.liang.example.androidtest.R;
import com.liang.example.utils.ApiManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ViewPager_Main";

    private List<String> dataSet = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));
    private PagerAdapterTest<String> pagerAdapterTest;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager_test);
        List<Integer> colors = Arrays.asList(
                android.R.color.background_light,
                android.R.color.holo_red_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark,
                R.color.color888888
        );
        int background_dark = ContextCompat.getColor(this, android.R.color.background_dark);
        viewPager = findViewById(R.id.test_viewpager_viewpager);
        pagerAdapterTest = new PagerAdapterTest<>(dataSet, new PagerAdapterTest.PagerAdapterItemHolder<String>() {
            @Override
            public View instantiateItem(@NonNull ViewGroup container, int position, String data) {
                ApiManager.LOGGER.d(TAG, "instantiateItem -- position: %d, data: %s", position, data);
                TextView view = new TextView(MainActivity.this);
                view.setBackgroundResource(colors.get(position % colors.size()));
                view.setText(data);
                view.setGravity(Gravity.CENTER);
                view.setTextColor(background_dark);
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
                return view;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return ((TextView) view).getText().toString().equals(object);
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object, View view, String data) {
                ApiManager.LOGGER.d(TAG, "destroyItem: " + position);
            }
        }/*, 5 * 1000, viewPager*/);
        findViewById(R.id.test_viewpager_start).setOnClickListener((v) -> pagerAdapterTest.startCarousel());
        findViewById(R.id.test_viewpager_stop).setOnClickListener((v) -> pagerAdapterTest.stopCarousel());
        // findViewById(R.id.test_viewpager_add_item).setOnClickListener((v) -> pagerAdapterTest.addItem(String.valueOf(pagerAdapterTest.getSize() + 1), 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPager.setAdapter(pagerAdapterTest);
        viewPager.setCurrentItem(pagerAdapterTest.getFirstItemPos());
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewPager.removeOnPageChangeListener(pagerAdapterTest);
        viewPager.setAdapter(null);
    }
}
