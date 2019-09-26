package com.liang.example.nativeremote;

public class RemoteManager {
    private static final String TAG = "RemoteManager";
    private static volatile RemoteManager instance;
    private long uid;
    private String guid;
    private String serverUrl;

    static {
        System.loadLibrary("remote-jni");
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

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
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

    public boolean startRemoteClient() {
        return startRemoteClient(uid, guid, serverUrl);
    }

    public boolean stopRemoteClient() {
        return stopRemoteClient(serverUrl);
    }

    private native boolean startRemoteClient(long uid, String guid, String serverUrl);

    private native boolean stopRemoteClient(String serverUrl);
}
