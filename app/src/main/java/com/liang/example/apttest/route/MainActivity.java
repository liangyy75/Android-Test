package com.liang.example.apttest.route;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.R;
import com.liang.example.utils.ApiManager;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apt_route);

        makeRoute(R.id.test_apt_route_to_bind, "apt_bind_main");
        makeRoute(R.id.test_apt_route_to_spi, "apt_spi_main");
        makeRoute(R.id.test_apt_route_to_dagger2, "apt_dagger2_main");

        // RouteManager.getInstance().build(this).putExtra("key1", "value").putExtra("key2", 1.1f).navigate("path");
    }

    private void makeRoute(int p, String path) {
        findViewById(p).setOnClickListener((v) -> {
            ApiManager.LOGGER.d("Route_Main", "result: " + RouteManager.getInstance().navigate(this, path));
        });
    }
}
