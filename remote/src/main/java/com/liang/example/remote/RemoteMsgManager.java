package com.liang.example.remote;

import android.content.Context;

import com.liang.example.remoteutils.JsonApiKt;
import com.liang.example.remoteutils.Logger;
import com.liang.example.remoteutils.NullableLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liangyuyin
 * @since 2019/7/16
 */
public class RemoteMsgManager {
    private static final String TAG = "RemoteMsgManager";
    // private static final int MAX_CACHED_CONSOLE = 2;
    // private static final int MAX_FIXED_THREAD = 2;

    // private final Map<String, RemoteClient> mActiveClients = new HashMap<>();
    // private final RemoteClient[] mCachedClients = new RemoteClient[MAX_CACHED_CONSOLE];
    // private int cachedNum = 0;
    private RemoteClient remoteClient;
    private long uid;
    private String guid;
    private Map<String, AbsRemoteMsgHandler> msgHandlerMap = new HashMap<>();
    public static Logger logger = new NullableLogger(true);
    // private ExecutorService executorService;

    private RemoteMsgManager() {
        // executorService = Executors.newFixedThreadPool(MAX_CACHED_CONSOLE);
        JsonApiKt.init();
    }

    private static volatile RemoteMsgManager INSTANCE = null;

    public static RemoteMsgManager getInstance() {
        if (INSTANCE == null) {
            synchronized (RemoteMsgManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemoteMsgManager();
                }
            }
        }
        return INSTANCE;
    }

    public static void setLogger(Logger logger) {
        RemoteMsgManager.logger = logger;
    }

    public static void setInnerLogger(Logger logger) {
        if (logger instanceof NullableLogger) {
            ((NullableLogger) RemoteMsgManager.logger).setLogger(logger);
        } else {
            RemoteMsgManager.logger = logger;
        }
    }

    public RemoteMsgManager setUid(long uid) {
        this.uid = uid;
        logger.d(TAG, "set uid %d", this.uid);
        return this;
    }

    public RemoteMsgManager setGuid(String guid) {
        this.guid = guid;
        logger.d(TAG, "set guid %s", this.guid);
        return this;
    }

    public long getUid() {
        return uid;
    }

    public String getGuid() {
        return guid;
    }

    public Map<String, AbsRemoteMsgHandler> getMsgHandlerMap() {
        return msgHandlerMap;
    }

    public RemoteClient getRemoteClient() {
        return remoteClient;
    }

    // public RemoteMsgManager addRemoteClient(String serverUrl) {
    //     // RemoteClient remoteClient = mActiveClients.get(serverUrl);
    //     // if (remoteClient == null) {
    //     //     synchronized (mCachedClients) {
    //     //         if (cachedNum == 0) {
    //     //             remoteClient = new RemoteShellClient(serverUrl);
    //     //             logger.debug(TAG, "create new remote client");
    //     //         } else {
    //     //             cachedNum--;
    //     //             remoteClient = mCachedClients[cachedNum];
    //     //             mCachedClients[cachedNum] = null;
    //     //             remoteClient.setServerUrl(serverUrl);
    //     //             logger.debug(TAG, "get cached remote client");
    //     //         }
    //     //     }
    //     //     synchronized (mActiveClients) {
    //     //         mActiveClients.put(serverUrl, remoteClient);
    //     //     }
    //     // }
    //     logger.debug(TAG, "create new remote client");
    //     return this;
    // }

    // public RemoteMsgManager addRemoteClient(/*String serverUrl, */RemoteClient remoteClient) {
    //     // RemoteClient remoteClient2 = mActiveClients.get(serverUrl);
    //     // if (remoteClient2 == null && remoteClient != null) {
    //     //     synchronized (mActiveClients) {
    //     //         mActiveClients.put(serverUrl, remoteClient);
    //     //     }
    //     // }
    //     this.remoteClient = remoteClient;
    //     logger.debug(TAG, "replace remote client");
    //     return this;
    // }

    public RemoteMsgManager addMsgHandler(AbsRemoteMsgHandler remoteMsgHandler) {
        msgHandlerMap.put(remoteMsgHandler.getReqTypeStr(), remoteMsgHandler);
        if (remoteClient != null) {
            remoteMsgHandler.setRemoteClient(remoteClient);
        }
        logger.d(TAG, "add msg handler with msgType: %s", remoteMsgHandler.getReqTypeStr());
        return this;
    }

    public RemoteMsgManager startRemoteClient(String serverUrl, Context context, String prefix) {
        // RemoteClient remoteClient = mActiveClients.get(serverUrl);
        // if (remoteClient != null) {
        //     for (Map.Entry<String, AbsRemoteMsgHandler> msgHandlerEntry : msgHandlerMap.entrySet()) {
        //         msgHandlerEntry.getValue().setRemoteClient(remoteClient);
        //     }
        //     new Thread(remoteClient).start();
        // }
        if (remoteClient != null) {
            closeRemoteClient();
        }
        remoteClient = new RemoteShellClient(serverUrl, uid, guid);
        for (Map.Entry<String, AbsRemoteMsgHandler> msgHandlerEntry : msgHandlerMap.entrySet()) {
            msgHandlerEntry.getValue().setRemoteClient(remoteClient);
        }
        // WakeLockUtil.lock(context, prefix + ":" + TAG, 60 * 60 * 1000L /*60 minutes*/);
        // new Thread(remoteClient).start();
        remoteClient.run();
        // context.startService(new Intent(context, RemoteShellService.class));
        logger.d(TAG, "start remote client");
        return this;
    }

    public RemoteMsgManager closeRemoteClient() {
        for (Map.Entry<String, AbsRemoteMsgHandler> msgHandlerEntry : msgHandlerMap.entrySet()) {
            msgHandlerEntry.getValue().setRemoteClient(null);
        }
        if (remoteClient != null && remoteClient.isOpened()) {
            remoteClient.close();
        }
        remoteClient = null;
        // WakeLockUtil.release();
        logger.d(TAG, "close remote client");
        return this;
    }

    // public RemoteMsgManager recycleRemoteClient(/*String serverUrl*/) {
    //     RemoteClient remoteClient = mActiveClients.get(serverUrl);
    //     if (remoteClient != null) {
    //         remoteClient.close();
    //         synchronized (mCachedClients) {
    //             if (cachedNum < MAX_CACHED_CONSOLE) {
    //                 mCachedClients[cachedNum++] = remoteClient;
    //             }
    //         }
    //         synchronized (mActiveClients) {
    //             mActiveClients.remove(serverUrl);
    //         }
    //         for (Map.Entry<String, AbsRemoteMsgHandler> msgHandlerEntry : msgHandlerMap.entrySet()) {
    //             msgHandlerEntry.getValue().setRemoteClient(null);
    //         }
    //     }
    //     return this;
    // }
}

// TODO: 优化弱网
//  1. jec;
//  2. 断网续传;
