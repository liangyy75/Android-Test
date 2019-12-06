package com.liang.example.jetpacktest.livedatatest;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.liang.example.utils.ApiManager;

import java.util.Timer;
import java.util.TimerTask;

// 只有 onResume 和 onPause 的会被调用，其他的延迟直到 Activity / Fragment 变得 active
public class FirstActivity extends AppCompatActivity {
    private MutableLiveData<String> mutableLiveData;
    private int start = 0;
    private int step = 0;
    private static int MAX_STEP = 3;
    private Timer timer;
    private MutableLiveData<Boolean> liveDataSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mutableLiveData = new MutableLiveData<>();
        mutableLiveData.observe(this, s -> {
            ApiManager.LOGGER.d("LiveData", "MutableLiveData -- onChanged: " + s);
            liveDataSwitch.postValue(start % 2 == 0);
        });
        getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull LifecycleOwner owner) {
                mutableLiveData.postValue("onCreate");
                mutableLiveData.postValue("onCreate");
            }

            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                mutableLiveData.postValue("onStart");
                mutableLiveData.postValue("onStart");
            }

            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                mutableLiveData.postValue("onResume");
                mutableLiveData.postValue("onResume");

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        step++;
                        if (step >= MAX_STEP) {
                            start++;
                            step = 0;
                        }
                        mutableLiveData.postValue("timer change number: " + start);
                    }
                }, 1000 * 2, 1000 * 2);
                // 这里证明即使多次 postValue 传入的数据是相同的也是会被 log 的
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                mutableLiveData.postValue("onPause");
                mutableLiveData.postValue("onPause");

                timer.cancel();
                timer = null;
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                mutableLiveData.postValue("onStop");
                mutableLiveData.postValue("onStop");
            }

            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                mutableLiveData.postValue("onDestroy");
                mutableLiveData.postValue("onDestroy");
            }
        });

        Transformations.map(mutableLiveData, value -> value + " -- transform")
                .observe(this, s -> ApiManager.LOGGER.d("LiveData", "Transformations.map -- onChanged: " + s));

        MutableLiveData<String> mutableLiveData1 = new MutableLiveData<>("mutableLiveData1");
        MutableLiveData<String> mutableLiveData2 = new MutableLiveData<>("mutableLiveData2");
        liveDataSwitch = new MutableLiveData<>(false);
        Transformations.switchMap(liveDataSwitch, value -> value ? mutableLiveData1 : mutableLiveData2)
                .observe(this, b -> ApiManager.LOGGER.d("LiveData", "Transformations.switchMap -- onChanged: " + b));

        Transformations.distinctUntilChanged(mutableLiveData)
                .observe(this, s -> ApiManager.LOGGER.d("LiveData", "Transformations.distinctUntilChanged -- onChanged: " + s));

        MediatorLiveData liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.addSource(liveDataSwitch, s -> ApiManager.LOGGER.d("LiveData", " -- onChanged: liveDataSwitch -- " + s));
        liveDataMerger.addSource(mutableLiveData, s -> ApiManager.LOGGER.d("LiveData", " -- onChanged: mutableLiveData -- " + s));
    }
}
