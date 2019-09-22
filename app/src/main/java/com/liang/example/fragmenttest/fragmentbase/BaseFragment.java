package com.liang.example.fragmenttest.fragmentbase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liang.example.utils.ApiManager;

public class BaseFragment extends LoggableFragment {
    private BaseFragmentHolder baseFragmentHolder;

    public BaseFragment(@LayoutRes int layoutId, BaseFragmentHolder baseFragmentHolder) {
        super(layoutId);
        this.baseFragmentHolder = baseFragmentHolder;
        nullableLogger.setLogger(ApiManager.LOGGER);
    }

    public BaseFragment setTag(String tag) {
        TAG = tag;
        return this;
    }

    public BaseFragment setBundle(Bundle args) {
        super.setArguments(args);
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (baseFragmentHolder != null) {
            baseFragmentHolder.init(view, getArguments());
        }
        return view;
    }

    public interface BaseFragmentHolder {
        void init(View view, Bundle bundle);
    }
}
