package com.liang.example.musictest;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.liang.example.androidtest.R;
import com.liang.example.utils.ApiManager;

import java.util.Objects;

public class LoadActivity extends FragmentActivity {
    private boolean flag = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(ApiManager.CONTEXT.getHandler()).postDelayed(() -> {
            startActivity(new Intent(LoadActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.screen_zoom_in, R.anim.screen_zoom_out);
            ApiManager.LOGGER.d("Music_Load", "startActivity");
            flag = true;
        }, 500);
        ApiManager.LOGGER.d("Music_Load", "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flag) {
            ApiManager.LOGGER.d("Music_Load", "onBackPressed");
            Objects.requireNonNull(ApiManager.CONTEXT.getHandler()).post(LoadActivity.this::onBackPressed);
        }
    }
}
