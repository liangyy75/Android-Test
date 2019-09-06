package com.liang.example.remote;

import com.google.gson.reflect.TypeToken;
import com.liang.example.utils.JsonApiKt;

import java.util.Map;

class RemoteShellClient extends RemoteClient {
    private static final String TAG = "RemoteShellClient";

    private long uid;
    private String guid;

    RemoteShellClient(String serverUrl, long uid, String guid) {
        super(serverUrl);
        this.uid = uid;
        this.guid = guid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public void onMessage(final String msg) {
        RemoteMsgManager.logger.d(TAG, "receive str: %s", msg);
        RemoteBasicMsg<Object> objectRemoteBasicMsg = JsonApiKt.parseJson(msg, new TypeToken<RemoteBasicMsg<Object>>() {
        }.getType());
        if (objectRemoteBasicMsg != null) {
            String type = objectRemoteBasicMsg.getType();
            Map<String, AbsRemoteMsgHandler> msgHandlerMap = RemoteMsgManager.getInstance().getMsgHandlerMap();
            final AbsRemoteMsgHandler msgHandler = msgHandlerMap.get(type);
            StringBuilder allReqType = new StringBuilder();
            for (String reqType : msgHandlerMap.keySet()) {
                allReqType.append(reqType).append("\n");
            }
            RemoteMsgManager.logger.d(TAG, "receive msgType: %s; and all reqType are: %s", type, allReqType.toString());
            if (msgHandler != null) {
                RemoteMsgManager.logger.d(TAG, "dispatch msg to handler with msgType: %s", type);
                Runnable task = () -> {
                    RemoteBasicMsg remoteBasicMsg = JsonApiKt.parseJson(msg, msgHandler.getReqType());
                    msgHandler.onMessage(msg, remoteBasicMsg.getData());
                };
                new Thread(task).start();
            } else {
                RemoteMsgManager.logger.d(TAG, "no such msgType's handler");
            }
        } else {
            RemoteMsgManager.logger.d(TAG, "cannot parse json: %s", msg);
        }
        // else {
        //     for (Map.Entry<String, AbsRemoteMsgHandler> msgHandlerEntry : msgHandlerMap.entrySet()) {
        //         final AbsRemoteMsgHandler msgHandler = msgHandlerEntry.getValue();
        //         Runnable task = new Runnable() {
        //             @Override
        //             public void run() {
        //                 msgHandler.onMessage(msg, null);
        //             }
        //         };
        //         new Thread(task).start();
        //     }
        // }
    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.send("{\"UserId\":" + uid + ",\"Guid\":\"" + guid + "\"}", "string");
        Map<String, AbsRemoteMsgHandler> msgHandlerMap = RemoteMsgManager.getInstance().getMsgHandlerMap();
        for (Map.Entry<String, AbsRemoteMsgHandler> msgHandlerEntry : msgHandlerMap.entrySet()) {
            msgHandlerEntry.getValue().onOpen();
        }
        RemoteMsgManager.logger.d(TAG, "remoteClient.onOpen");
        // TODO 或许不应该这样定义 onOpen / onClose / onError ，因为它们没有 type 字段，所以无法分辨。
        //  除非记录下 type ，但 onOpen 是一定无法通知到的了，除非改后台，在启动 RemoteClient 时就指定 type 。
    }

    @Override
    public void onClose() {
        super.onClose();
        Map<String, AbsRemoteMsgHandler> msgHandlerMap = RemoteMsgManager.getInstance().getMsgHandlerMap();
        for (Map.Entry<String, AbsRemoteMsgHandler> msgHandlerEntry : msgHandlerMap.entrySet()) {
            AbsRemoteMsgHandler msgHandler = msgHandlerEntry.getValue();
            msgHandler.onClose();
        }
        RemoteMsgManager.logger.d(TAG, "remoteClient.onClose");
        this.close();
        RemoteMsgManager.getInstance().closeRemoteClient();
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
        Map<String, AbsRemoteMsgHandler> msgHandlerMap = RemoteMsgManager.getInstance().getMsgHandlerMap();
        for (Map.Entry<String, AbsRemoteMsgHandler> msgHandlerEntry : msgHandlerMap.entrySet()) {
            AbsRemoteMsgHandler msgHandler = msgHandlerEntry.getValue();
            msgHandler.onError(e);
        }
        RemoteMsgManager.logger.d(TAG, "remoteClient.onClose", e);
        this.close();
        RemoteMsgManager.getInstance().closeRemoteClient();
    }
}
