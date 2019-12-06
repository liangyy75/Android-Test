package com.liang.example.jetpacktest.viewmodeltest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.liang.example.jetpacktest.livedatatest.MyLiveData;

public class MyViewModel extends ViewModel {
    private MyLiveData<String> name;

    @Override
    protected void onCleared() {
        // TODO:
    }

    public LiveData<String> getName() {
        if (name == null) {
            name = new MyLiveData<>();
            addName();
        }
        return name;
    }

    private void addName() {
        name.setValue("ViewModel -- LiveData -- Test");
    }
}
