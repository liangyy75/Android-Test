package com.liang.example.fragmenttest.bottombar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FragmentStatePagerAdapterTest extends FragmentStatePagerAdapter {
    private List<Fragment> fragments;
    private List<String> pageTitles;

    public FragmentStatePagerAdapterTest(FragmentManager fm, List<Fragment> fragments, int behavior) {
        this(fm, fragments, null, behavior);
    }

    public FragmentStatePagerAdapterTest(FragmentManager fm, List<Fragment> fragments, List<String> pageTitles, int behavior) {
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

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles != null ? pageTitles.get(position) : super.getPageTitle(position);
    }
}
