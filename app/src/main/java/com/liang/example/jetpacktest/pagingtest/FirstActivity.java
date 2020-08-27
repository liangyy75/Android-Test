package com.liang.example.jetpacktest.pagingtest;

import com.liang.example.androidtest.R;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author liangyuying.lyy75
 * @date 2020/8/17
 * <p>
 */
public class FirstActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_recyclerview);
    }
}
