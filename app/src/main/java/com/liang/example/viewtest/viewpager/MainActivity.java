package com.liang.example.viewtest.viewpager;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    private EditText posET;
    private EditText valET;

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
        posET = findViewById(R.id.test_viewpager_position);
        valET = findViewById(R.id.test_viewpager_value);
        pagerAdapterTest = new PagerAdapterTest<>(dataSet, new PagerAdapterTest.PagerAdapterItemHolder<String>() {
            @Override
            public View instantiateItem(@NonNull ViewGroup container, int position, String data) {
                ApiManager.LOGGER.d(TAG, "instantiateItem -- position: %d, data: %s", position, data);
                TextView view = new TextView(MainActivity.this);
                view.setBackgroundResource(colors.get(Integer.valueOf(data) % colors.size()));
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
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object, View view) {
                ApiManager.LOGGER.d(TAG, "destroyItem: " + position);
            }

            @Override
            public void startUpdate(@NonNull ViewGroup container, List<String> dataSet, SparseArray<View> subViews) {
                showDataSet(dataSet, "startUpdate");
                showSubViews(subViews, "startUpdate");
            }

            @Override
            public void finishUpdate(@NonNull ViewGroup container, List<String> dataSet, SparseArray<View> subViews) {
                showDataSet(dataSet, "finishUpdate");
                showSubViews(subViews, "finishUpdate");
            }
        }, -1, viewPager, true, false);
        findViewById(R.id.test_viewpager_start).setOnClickListener((v) -> pagerAdapterTest.startCarousel());
        findViewById(R.id.test_viewpager_stop).setOnClickListener((v) -> pagerAdapterTest.stopCarousel());
        findViewById(R.id.test_viewpager_add_item).setOnClickListener((v) -> {
            int intPos = getPos(true);
            if (intPos != -1) {
                pagerAdapterTest.addItem(String.valueOf(pagerAdapterTest.getSize() + 1), intPos);
            }
        });
        findViewById(R.id.test_viewpager_remove_item).setOnClickListener((v) -> {
            if (dataSet.size() == 0) {
                Toast.makeText(MainActivity.this, "dataSet have been empty.", Toast.LENGTH_LONG).show();
                return;
            }
            int intPos = getPos(false);
            if (intPos != -1) {
                pagerAdapterTest.removeItem(intPos);
            }
        });
        findViewById(R.id.test_viewpager_set_item).setOnClickListener((v) -> {
            int intVal = parseStr(valET, "value");
            int intPos = parseStr(posET, "position");
            if (intVal < 0 || intPos < 0) {
                return;
            }
            pagerAdapterTest.setItem(String.valueOf(intVal), intPos);
        });
    }

    private void showDataSet(List<String> dataSet, String tag) {
        int size = dataSet.size();
        if (size == 0) {
            ApiManager.LOGGER.d(TAG, "%s -- showDataSet: []", tag);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(0).append(": ").append(dataSet.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(", ").append(i).append(": ").append(dataSet.get(i));
        }
        ApiManager.LOGGER.d(TAG, "%s -- showDataSet: [%s]", tag, sb.toString());
    }

    private void showSubViews(SparseArray<View> subViews, String tag) {
        int size = subViews.size();
        if (size == 0) {
            ApiManager.LOGGER.d(TAG, "%s -- showSubViews: []", tag);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(0).append(": ").append(subViews.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(", ").append(i).append(": ").append(subViews.get(i));
        }
        ApiManager.LOGGER.d(TAG, "%s -- showDataSet: [%s]", tag, sb.toString());
    }

    private int getPos(boolean flag) {
        int intPos = parseStr(posET, "position");
        if (intPos < 0) {
            return intPos;
        }
        int size = dataSet.size();
        if (intPos >= size) {
            intPos = flag ? size : size - 1;
            Toast.makeText(MainActivity.this, "position should not be larger than dataSet's size, so position changed to " + intPos,
                    Toast.LENGTH_LONG).show();
        }
        return intPos;
    }

    private int parseStr(EditText et, String hint) {
        String str = et.getText().toString().trim();
        if (TextUtils.isEmpty(str)) {
            Toast.makeText(MainActivity.this, hint + " should not be empty", Toast.LENGTH_LONG).show();
            return -1;
        }
        int int_;
        try {
            int_ = Integer.valueOf(str);
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, hint + "'s format is wrong, and the input is " + str, Toast.LENGTH_LONG).show();
            return -1;
            // 其实不可能发生的，因为这些EditText的inputType是Number
        }
        return int_;
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
        pagerAdapterTest.setUseCache(false);
        pagerAdapterTest.clearCache();
        viewPager.removeOnPageChangeListener(pagerAdapterTest);
        viewPager.setAdapter(null);
    }
}
