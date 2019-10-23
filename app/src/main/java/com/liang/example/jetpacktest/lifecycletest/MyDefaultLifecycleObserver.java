package com.liang.example.jetpacktest.lifecycletest;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.liang.example.utils.ApiManager;
import com.liang.example.utils.view.ToastApiKt;

public class MyDefaultLifecycleObserver implements DefaultLifecycleObserver {
    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        ToastApiKt.showToastWithLog("MyDefaultLifecycleObserver -- onCreate", ApiManager.CONTEXT.getApplication(), Toast.LENGTH_SHORT);
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        ToastApiKt.showToastWithLog("MyDefaultLifecycleObserver -- onDestroy", ApiManager.CONTEXT.getApplication(), Toast.LENGTH_SHORT);
    }
}
