package com.liang.example.apttest;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.R;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "AptTestMainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.liang.example.androidtest.MainActivity.bindActivityList(
                Constants.names, Constants.descs, Constants.authors, Constants.created, Constants.updated, Constants.classes, this, TAG);
    }
}
