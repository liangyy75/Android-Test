package com.liang.example.apttest.dagger2.demo1;

import android.service.autofill.AutofillService;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

public class Engine1 {
    // TextView textView;
    // Button button;
    // ImageView imageView;
    // RecyclerView recyclerView;
    // Fragment fragment;ger2
    // ScrollView scrollView;
    // Switch aSwitch;
    // GridLayout gridLayout;
    // AutofillService autofillService;

    private String msg;

    @Inject
    Engine1() {
        msg = "引擎1转起来了~~~";
    }

    public void run() {
        Log.d("Dagger2", msg);
    }
}
