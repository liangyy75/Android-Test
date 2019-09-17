package com.liang.example.musictest.activity1;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.liang.example.fragmenttest.bottombar2.FragmentBarHelper2;

import java.util.List;

public class TabFragmentHelper<T> {
    private static final String TAG = "TabFragmentHelper";

    private FragmentActivity fragmentActivity;
    private List<T> dataSet;
    private List<FragmentBarHelper2.FragmentWrapper> fragmentWrappers;
    private int currPos = -1;
}
