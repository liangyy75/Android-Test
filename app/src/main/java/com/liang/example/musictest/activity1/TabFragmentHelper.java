package com.liang.example.musictest.activity1;

import androidx.fragment.app.FragmentActivity;

import com.liang.example.fragmenttest.bottombar2.FragmentBarHelper2;

import java.util.List;

/**
 * 1. ViewPager加上FragmentPagerAdapter后已有的功能
 * 1.1 支持切换
 * 1.2
 * 1.3
 * 1.4
 *
 * @param <T>
 */
public class TabFragmentHelper<T> {
    private static final String TAG = "TabFragmentHelper";

    private FragmentActivity fragmentActivity;
    private List<T> dataSet;
    private List<FragmentBarHelper2.FragmentWrapper> fragmentWrappers;
    private int currPos = -1;
}
