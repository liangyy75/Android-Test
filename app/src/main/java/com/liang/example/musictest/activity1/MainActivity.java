package com.liang.example.musictest.activity1;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.liang.example.androidtest.R;
import com.liang.example.fragmenttest.bottombar.FragmentPagerAdapterTest;
import com.liang.example.fragmenttest.fragmentbase.BaseFragment;
import com.liang.example.utils.ApiManager;
import com.liang.example.utils.view.ToastApiKt;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MUSIC_FIRST";

    private TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_first);

        DrawerLayout drawerLayout = findViewById(R.id.music_first_drawer_layout);
        findViewById(R.id.music_first_navigation_iv).setOnClickListener((View v) -> {
            ToastApiKt.showToastWithLog("click navigation button");
            drawerLayout.openDrawer(GravityCompat.START);
        });
        findViewById(R.id.music_first_search_view).setOnClickListener((v) -> ToastApiKt.showToastWithLog("click search button"));

        tabLayout = findViewById(R.id.music_first_tab_layout);
        ViewPager viewPager = findViewById(R.id.music_first_view_pager);
        List<String> pageTitles = new ArrayList<>(Arrays.asList("我的", "发现", "云村", "视频"));
        int size = pageTitles.size();
        for (int i = 0; i < size; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(pageTitles.get(i)));
        }
        tabLayout.post(() -> setIndicator(tabLayout, 15, 15));
        tabLayout.setupWithViewPager(viewPager);
        List<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String pageTitle = pageTitles.get(i);
            fragments.add(new BaseFragment(R.layout.fragment_test_fragment_bottombar, (v, b) -> {
                ((TextView) v.findViewById(R.id.test_fragment_bottombar_item_name)).setText(pageTitle);
            }).setTag(pageTitle));
        }
        viewPager.setAdapter(new FragmentPagerAdapterTest(getSupportFragmentManager(), fragments, pageTitles,
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
    }

    public boolean setIndicator(TabLayout tabs, int leftDip, int rightDip) {
        Class<?> tabLayout = tabs.getClass();
        Field tabStrip;
        try {
            tabStrip = tabLayout.getDeclaredField("mTabStrip");
        } catch (NoSuchFieldException e) {
            ApiManager.LOGGER.e(TAG, "setIndicator", e);
            return false;
        }
        tabStrip.setAccessible(true);
        LinearLayout llTab;
        try {
            llTab = (LinearLayout) tabStrip.get(tabs);
        } catch (IllegalAccessException e) {
            ApiManager.LOGGER.e(TAG, "setIndicator", e);
            return false;
        }
        if (llTab == null) {
            ApiManager.LOGGER.d(TAG, "setIndicator -- llTab is null");
            return false;
        }
        int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftDip, Resources.getSystem().getDisplayMetrics());
        int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightDip, Resources.getSystem().getDisplayMetrics());
        for (int i = 0; i < llTab.getChildCount(); i++) {
            View child = llTab.getChildAt(i);
            child.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            params.leftMargin = left;
            params.rightMargin = right;
            child.setLayoutParams(params);
            child.invalidate();
        }
        ApiManager.LOGGER.d(TAG, "setIndicator -- successfully");
        return true;
    }
}
