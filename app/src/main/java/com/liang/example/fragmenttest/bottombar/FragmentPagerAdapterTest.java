package com.liang.example.fragmenttest.bottombar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FragmentPagerAdapterTest extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private List<String> pageTitles;

    public FragmentPagerAdapterTest(FragmentManager fm, List<Fragment> fragments, int behavior) {
        this(fm, fragments, null, behavior);
    }

    public FragmentPagerAdapterTest(FragmentManager fm, List<Fragment> fragments, List<String> pageTitles, int behavior) {
        super(fm, behavior);
        this.fragments = fragments;
        this.pageTitles = pageTitles;
    }

    @NotNull
    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments == null ? 0 : fragments.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles != null ? pageTitles.get(position) : super.getPageTitle(position);
    }
}
