package com.liang.example.shelltest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.liang.example.androidtest.R;
import com.liang.example.remote.RemoteClient;
import com.liang.example.remote.RemoteMsgManager;
import com.liang.example.utils.ApiManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author liangyuyin
 * @since 2019/9/4
 */
public class RemoteShellService extends Service {
    private static final String TAG = "RemoteShellService";

    public static final String CHANNEL_ID_SHELL_SERVICE = "CHANNEL_ID_SHELL_SERVICE";

    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        CharSequence title = getText(R.string.notification_title_shell_service);
        CharSequence content = getText(R.string.notification_message_shell_service);
        int icon = R.drawable.ic_launcher_foreground;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID_SHELL_SERVICE)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(icon)
                    .setContentIntent(pendingIntent)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this, CHANNEL_ID_SHELL_SERVICE)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(icon)
                    .setContentIntent(pendingIntent)
                    .build();
        }
        startForeground(1, notification);
        // new Thread(RemoteMsgManager.getInstance().getRemoteClient()).start();

        // makeTimer(10 * 1000);
        // ApiManager.LOGGER.d(TAG, "onCreate");
    }

    private void makeTimer(long period) {
        if (RemoteMsgManager.getInstance().getRemoteClient() != null) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            ApiManager.LOGGER.d(TAG, "make timer");
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                RemoteClient remoteClient = RemoteMsgManager.getInstance().getRemoteClient();
                if (remoteClient == null || !remoteClient.isOpened()) {
                    RemoteMsgManager.getInstance()
                            .startRemoteClient(MainActivity.DEFAULT_SERVER_URL, RemoteShellService.this, "remote");
                    ApiManager.LOGGER.d(TAG, "start remote client");
                } else {
                    // this.cancel();
                    ApiManager.LOGGER.d(TAG, "have had remote client");
                }
            }
        }, period, period);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // RemoteMsgManager.getInstance()
        //         .startRemoteClient("ws://157.255.228.135", this, "remote");
        ApiManager.LOGGER.d(TAG, "onStartCommand");
        makeTimer(5 * 1000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ApiManager.LOGGER.d(TAG, "onDestroy");
        RemoteMsgManager.getInstance().closeRemoteClient();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // RemoteMsgManager.getInstance()
        //         .startRemoteClient("ws://157.255.228.135", this, "remote");
        ApiManager.LOGGER.d(TAG, "onBind");
        makeTimer(5 * 1000);
        return null;
    }
}
