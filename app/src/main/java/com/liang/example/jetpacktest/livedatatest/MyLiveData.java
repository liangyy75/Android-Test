package com.liang.example.jetpacktest.livedatatest;

import androidx.lifecycle.LiveData;

public class MyLiveData<T> extends LiveData<T> {
    private boolean useEquals = false;
    private MyObserverListener myObserverListener = null;

    public void setUseEquals(boolean useEquals) {
        this.useEquals = useEquals;
    }

    public boolean isUseEquals() {
        return useEquals;
    }

    public void setMyObserverListener(MyObserverListener myObserverListener) {
        this.myObserverListener = myObserverListener;
    }

    public MyObserverListener getMyObserverListener() {
        return myObserverListener;
    }

    public MyLiveData(T value) {
        super(value);
    }

    public MyLiveData() {
    }

    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    @Override
    public void setValue(T value) {
        if (!useEquals || value == null || !value.equals(getValue())) {
            super.setValue(value);
        }
    }

    @Override
    protected void onActive() {
        if (myObserverListener != null) {
            myObserverListener.onActive();
        }
    }

    @Override
    protected void onInactive() {
        if (myObserverListener != null) {
            myObserverListener.onInactive();
        }
    }

    public interface MyObserverListener {
        default void onActive() {
        }

        default void onInactive() {
        }
    }
}
