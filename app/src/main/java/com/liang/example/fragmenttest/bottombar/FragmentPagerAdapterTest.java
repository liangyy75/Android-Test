package com.liang.example.fragmenttest.bottombar;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FragmentPagerAdapterTest extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public FragmentPagerAdapterTest(FragmentManager fm, List<Fragment> fragments, int behavior) {
        super(fm, behavior);
        this.fragments = fragments;
    }

    @NotNull
    @Override
    public Fragment getItem(int i) {
        Log.d("FragmentPagerAdapterTest.getItem", String.valueOf(i));
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        Log.d("FragmentPagerAdapterTest.getCount", String.valueOf(fragments == null ? 0 : fragments.size()));
        return fragments == null ? 0 : /*fragments.size()*/ Integer.MAX_VALUE;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        Log.d("FragmentPagerAdapterTest.getItemPosition", object.getClass().getName());
        return super.getItemPosition(object);
    }
}
