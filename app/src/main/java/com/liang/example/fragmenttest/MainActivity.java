package com.liang.example.fragmenttest;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.ApplicationTest;
import com.liang.example.androidtest.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.liang.example.androidtest.MainActivity.bindActivityList(Constants.names, Constants.descs,
                Constants.authors, Constants.created, Constants.updated, Constants.classes, this, "Fragment_Main");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((LinearLayout) findViewById(R.id.test_activity_root)).addView(((ApplicationTest) getApplication()).textView, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((LinearLayout) findViewById(R.id.test_activity_root)).removeViewAt(0);
    }
}
