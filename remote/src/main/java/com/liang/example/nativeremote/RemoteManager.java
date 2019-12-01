package com.liang.example.nativeremote;

import android.app.Application;

import com.liang.example.remote.RemoteMsgManager;

public class RemoteManager {
    private static final String TAG = "RemoteManager";
    private static volatile RemoteManager instance;
    private long uid;
    private String guid;
    private String serverUrl;

    static {
        System.loadLibrary("remote-jni");
    }

    private RemoteManager() {
    }

    public static RemoteManager getInstance() {
        if (instance == null) {
            synchronized (RemoteManager.class) {
                if (instance == null) {
                    instance = new RemoteManager();
                }
            }
        }
        return instance;
    }

    public RemoteManager setUid(long uid) {
        this.uid = uid;
        return this;
    }

    public RemoteManager setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public RemoteManager setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public long getUid() {
        return uid;
    }

    public String getGuid() {
        return guid;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public RemoteManager startRemoteClient() {
        RemoteMsgManager.logger.d(TAG, "startRemoteClient -- uid: %d, guid: %s, serverUrl: %s", uid, guid, serverUrl);
        startRemoteClient(uid, guid, serverUrl);
        return this;
    }

    public boolean stopRemoteClient() {
        RemoteMsgManager.logger.d(TAG, "stopRemoteClient -- uid: %d, guid: %s, serverUrl: %s", uid, guid, serverUrl);
        return stopRemoteClient(uid, guid, serverUrl);
    }

    public RemoteManager init(boolean useShell, boolean useEcho, boolean useUtil, Application application) {
        init(useShell, useEcho);
        if (useUtil) {
            assert application != null;
            addRemoteMsgHandler(new UtilMsgHandler(application));
        }
        return this;
    }

    private native void init(boolean useShell, boolean useEcho);

    public native void startRemoteClient(long uid, String guid, String serverUrl);

    public native boolean stopRemoteClient(long uid, String guid, String serverUrl);

    public native boolean hasRemoteClient(long uid, String guid, String serverUrl);

    public native boolean hasRemoteClientByUrl(String serverUrl);

    public native boolean addRemoteMsgHandler(AbsMsgHandler handler);

    public native boolean removeRemoteMsgHandler(String reqType);

    public native boolean hasRemoteMsgHandler(String reqType);


    public native Object getObjectFromJni(String className, String jsonStr);

    public native String getStringFromJni(Object obj, String className);
}
