package com.liang.example.hotfixtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import com.liang.example.androidtest.R;
import com.liang.example.utils.ApiManager;

// TODO
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "HotFixMainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix_test);
        ApiManager.LOGGER.d(TAG, "onCreate -- start");
        findViewById(R.id.test_androidfix_fix).setOnClickListener(view -> {
            Log.d("Android Fix", "start fix");
            // 获取权限
            getPermissions();
            // 遍历所有的修复dex , 因为可能是多个dex修复包
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            File fileDir = externalStorageDirectory != null ?
                    new File(externalStorageDirectory, "007") :
                    new File(getFilesDir(), FixDexUtil.DEX_DIR);  // data/user/0/包名/files/odex（这个可以任意位置）
            if (!fileDir.exists()) {
                // noinspection ResultOfMethodCallIgnored
                fileDir.mkdirs();
            }
            if (FixDexUtil.isGoingToFix(MainActivity.this, null)) {
                FixDexUtil.loadFixedDex(MainActivity.this);
            }
            ApiManager.LOGGER.d(TAG, "finish fix");
        });
        findViewById(R.id.test_androidfix_test).setOnClickListener(view -> {
            new BugClass(MainActivity.this);
            ApiManager.LOGGER.d(TAG, "test fix");
        });
    }

    private void getPermissions() {
        int permissionCheck1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
            ApiManager.LOGGER.d(TAG, "begin getting permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 124);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 124) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 结果 grantResults 的两个值是 -1，及PackageManager.java中的Denied
                ApiManager.LOGGER.d(TAG, "get permission successfully");
            } else {
                ApiManager.LOGGER.d(TAG, "get permission failed");
            }
        }
    }
}
