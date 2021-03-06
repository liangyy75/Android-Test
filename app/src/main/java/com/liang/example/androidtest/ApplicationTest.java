package com.liang.example.androidtest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import com.liang.example.nativeremote.RemoteManager;
import com.liang.example.remote.RemoteMsgManager;
import com.liang.example.shell.ShellMsgHandler;
import com.liang.example.shelltest.RemoteShellService;
import com.liang.example.shelltest.TestMsgHandler;
import com.liang.example.utils.ApiManager;

/**
 * 能实现的功能:
 * 1. 数据传递
 * 2. 数据共享
 * 3. 数据缓存
 * 4. Activity监控
 * <p>
 * 注意点:
 * 1. 使用Application如果保存了一些不该保存的对象很容易导致内存泄漏。如果在Application的oncreate中执行比较耗时的操作，将直接影响的程序的启动时间。
 * 清理工作不能依靠onTerminate完成，因为android会尽量让你的程序一直运行，所以很有可能 onTerminate不会被调用。
 * 2.
 */
public class ApplicationTest extends Application {
    private static final String TAG = "ApplicationTest";

    public TextView textView;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        ApiManager.init(this);

        System.setProperty("groovy.use.classvalue", "false");

        textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        textView.setText("app text");

        // RemoteMsgManager.setLogger(ApiManager.LOGGER);
        RemoteMsgManager
                .getInstance()
                .addMsgHandler(new ShellMsgHandler())
                .addMsgHandler(new TestMsgHandler())
                .setUid(Long.parseLong(getString(R.string.uid_value)))
                .setGuid(getString(R.string.guid_value));

        RemoteManager.getInstance().setUid(Long.parseLong(getString(R.string.uid_value)));
        RemoteManager.getInstance().setGuid(getString(R.string.guid_value));
        RemoteManager.getInstance().setServerUrl("ws://157.255.228.135");
        // ApiManager.LOGGER.d(TAG, "RemoteManager.startManager: %s", String.valueOf(RemoteManager.getInstance().startRemoteClient()));

        // [Android O Preview 之 通知渠道（Notification Channels）](https://www.jianshu.com/p/92afa56aee05)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(RemoteShellService.CHANNEL_ID_SHELL_SERVICE, "shell service",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("shell service");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
                ApiManager.LOGGER.d(TAG, "create notification channel: %s successfully", RemoteShellService.CHANNEL_ID_SHELL_SERVICE);
            } else {
                ApiManager.LOGGER.d(TAG, "create notification channel: %s failed", RemoteShellService.CHANNEL_ID_SHELL_SERVICE);
            }
        }
    }
}
