package com.liang.example.shelltest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.liang.example.androidtest.R;
import com.liang.example.apttest.bind.InjectUtils;
import com.liang.example.apttest.bind.InjectView;
import com.liang.example.apttest.bind.OnClick;
import com.liang.example.remote.RemoteMsgManager;
import com.liang.example.utils.ApiManager;

import java.util.Arrays;

// TODO
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    static final String DEFAULT_SERVER_URL = "ws://157.255.228.135";

    private boolean useDefault = false;
    @InjectView(R.id.test_remote_shell_server_url)
    private EditText urlEditText;
    @InjectView(R.id.test_remote_shell_uid)
    private EditText uidEditText;
    @InjectView(R.id.test_remote_shell_guid)
    private EditText guidEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell_main);
        // ApiManager.LOGGER.d(TAG, "onCreate and call wakeLock.lock");
        // WakeLockUtil.lock(this, "remote:main", 60 * 60 * 1000L /* 60 minutes */);

        InjectUtils.getInstance().injectViews(this);
        InjectUtils.getInstance().injectEvents(this);
        useDefault = true;
        verifyPermissions();

        // startService(new Intent(this, RemoteShellService.class));
    }

    @OnClick(R.id.test_start)
    public void startTest(View view) {
        ApiManager.LOGGER.d(TAG, "start test event");
        final String serverUrl = urlEditText.getText().toString().trim();
        String uidStr = uidEditText.getText().toString().trim();
        final String guid = guidEditText.getText().toString().trim();
        if (TextUtils.isEmpty(serverUrl) && !useDefault) {
            Toast.makeText(MainActivity.this, "Please input server url", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(uidStr)) {
            Toast.makeText(MainActivity.this, "Please input your uid", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(guid)) {
            Toast.makeText(MainActivity.this, "Please input your guid", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            final long uid = Long.parseLong(uidStr);
            RemoteMsgManager
                    .getInstance()
                    .setUid(uid)
                    .setGuid(guid)
                    .startRemoteClient(serverUrl, MainActivity.this, "remote");
            Toast.makeText(MainActivity.this, "Starting remote client", Toast.LENGTH_LONG).show();
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Uid should be integer", Toast.LENGTH_LONG).show();
            ApiManager.LOGGER.d(TAG, e);
        }
    }

    @OnClick(R.id.test_stop)
    public void stopTest(View view) {
        ApiManager.LOGGER.d(TAG, "stop test event");
        RemoteMsgManager.getInstance().closeRemoteClient();
    }

    // TODO: permission utils
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };

    public void verifyPermissions() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ApiManager.LOGGER.d(TAG, "package:" + packageName);
            if (pm != null && pm.isIgnoringBatteryOptimizations(packageName)) {
                // intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                ApiManager.LOGGER.d(TAG, "have purchase permission: IGNORE_BATTERY_OPTIMIZATIONS");
            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

        try {
            int permission = ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            } else {
                ApiManager.LOGGER.d(TAG, "have purchase permission about storage");
            }
        } catch (Exception e) {
            ApiManager.LOGGER.e(TAG, e);
        }
    }

    // @Override
    // protected void onPause() {
    //     super.onPause();
    //     ApiManager.LOGGER.d(TAG, "onPause");
    //     startService(new Intent(this, RemoteShellService.class));
    // }
    //
    // @Override
    // protected void onStop() {
    //     super.onStop();
    //     ApiManager.LOGGER.d(TAG, "onStop");
    //     startService(new Intent(this, RemoteShellService.class));
    // }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ApiManager.LOGGER.d(TAG, "requestCode: %d; permissions: %s; grantResults: %s", requestCode, Arrays.toString(permissions),
                Arrays.toString(grantResults));
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // @Override
    // protected void onDestroy() {
    //     super.onDestroy();
    //     WakeLockUtil.release();
    //     ApiManager.LOGGER.d(TAG, "onDestroy and call wakeLock.release");
    // }
}
