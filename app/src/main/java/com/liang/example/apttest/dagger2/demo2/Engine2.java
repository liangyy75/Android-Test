package com.liang.example.apttest.dagger2.demo2;

import android.util.Log;

public class Engine2 {
    private String msg;

    Engine2(String msg) {
        this.msg = msg;
    }

    public void run() {
        Log.d("Dagger2", msg);
    }
}
