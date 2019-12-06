package com.liang.example.jetpacktest.viewmodeltest;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.utils.ApiManager;

public class FirstActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // MyViewModel model = ViewModelProviders.of(this).get(MyViewModel.class);  deprecated
        // MyViewModel model = new ViewModelProvider(this).get(MyViewModel.class);
        // model.getName().observe(this, s -> Log.d(TAG, "畅销书："+s));
        MyViewModel model = new MyViewModelProvider(this).get(MyViewModel.class);
        model.getName().observe(this, s -> ApiManager.LOGGER.d("ViewModelTest", "畅销书：" + s));
    }
}
