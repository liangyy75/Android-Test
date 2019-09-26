package com.liang.example.remote;

import com.liang.example.remoteutils.JsonApiKt;
import com.liang.example.typebuilder.TypeBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author liangyuyin
 * @since 2019/7/16
 */
public abstract class AbsRemoteMsgHandler<Req, Res> {
    private static final String TAG = "AbsRemoteMsgHandler";
    private RemoteClient remoteClient;
    private final Object clientLock = new Object();
    private String reqTypeStr;
    private String resTypeStr;
    private Type reqType;

    public AbsRemoteMsgHandler(String reqTypeStr, String resTypeStr) {
        this.reqTypeStr = reqTypeStr;
        this.resTypeStr = resTypeStr;
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        // 其实这里应该检查一下，然后选择是否抛出异常表明必须继承RemoteMsgHandler时指定Req和Res的类型，但我选择相信继承者
        Class<Req> reqClass = (Class<Req>) genericSuperclass.getActualTypeArguments()[0];
        this.reqType = TypeBuilder
                .newInstance(RemoteBasicMsg.class)
                .addTypeParam(reqClass)
                .build();
        RemoteMsgManager.logger.d(TAG, "reqTypeStr: %s, resTypeStr: %s, reqType: %s", this.reqTypeStr, this.resTypeStr, this.reqType);
    }

    /**
     * 如果 reqType 太过复杂就需要自己使用 TypeBuilder 来构建了，如 reqType 是 HashMap&lt;String, List&lt;String&gt;&gt;
     *
     * @param reqTypeStr req 标签
     * @param resTypeStr res 标签
     * @param reqType    req 类型
     */
    public AbsRemoteMsgHandler(String reqTypeStr, String resTypeStr, Type reqType) {
        this.reqTypeStr = reqTypeStr;
        this.resTypeStr = resTypeStr;
        this.reqType = reqType;
        RemoteMsgManager.logger.d(TAG, "reqTypeStr: %s, resTypeStr: %s, reqType: %s", this.reqTypeStr, this.resTypeStr, this.reqType);
    }

    String getReqTypeStr() {
        return reqTypeStr;
    }

    Type getReqType() {
        return reqType;
    }

    void setRemoteClient(RemoteClient remoteClient) {
        synchronized (clientLock) {
            this.remoteClient = remoteClient;
        }
    }

    public void onMessage(String msg, Req req) {
        onMessage(req);
    }

    public abstract void onMessage(Req req);

    public void send(Res res) {
        if (remoteClient != null) {
            RemoteBasicMsg<Res> resRemoteBasicMsg = new RemoteBasicMsg<>();
            resRemoteBasicMsg.setType(this.resTypeStr);
            resRemoteBasicMsg.setData(res);
            this.send(JsonApiKt.toJson(resRemoteBasicMsg));
        }
    }

    public void send(String msg) {
        if (remoteClient != null) {
            synchronized (clientLock) {
                if (remoteClient != null) {
                    remoteClient.send(msg, "string");
                } else {
                    RemoteMsgManager.logger.d(TAG, "RemoteClient has been set to null. The Abandoned msg: %s", msg);
                }
            }
        }
    }

    public void onOpen() {
    }

    public void onClose() {
    }

    public void onError(Throwable e) {
    }
}
