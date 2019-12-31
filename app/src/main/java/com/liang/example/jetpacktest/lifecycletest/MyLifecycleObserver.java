package com.liang.example.jetpacktest.lifecycletest;

import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.liang.example.utils.ApiManager;
import com.liang.example.utils.view.ToastApiKt;

public class MyLifecycleObserver implements LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void connectListener() {
        ToastApiKt.showToastWithLog("MyLifecycleObserver -- onResume", ApiManager.CONTEXT.getApp(), Toast.LENGTH_SHORT);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void disConnectListener() {
        ToastApiKt.showToastWithLog("MyLifecycleObserver -- onPause", ApiManager.CONTEXT.getApp(), Toast.LENGTH_SHORT);
    }
}
