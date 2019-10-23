package com.liang.example.jetpacktest;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.R;

import static com.liang.example.androidtest.MainActivity.bindActivityList;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindActivityList(Constants.names, Constants.descs, Constants.authors, Constants.created, Constants.updated, Constants.classes, this, "App_Main");
    }
}
