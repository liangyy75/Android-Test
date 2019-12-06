package com.liang.example.apttest.dagger2;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.R;
import com.liang.example.apttest.dagger2.demo1.Car1;
import com.liang.example.apttest.dagger2.demo2.Car2;
import com.liang.example.apttest.route.Route;

@Route(path = "apt_dagger2_main")
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apt_dagger2);

        new Car1().getEngine().run();
        new Car2().getEngine().run();
    }
}
