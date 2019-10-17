package com.liang.example.nativeremote;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbsRemoteMsgHandler<Req, Res> {
    protected String reqTypeStr;
    protected String resTypeStr;
    // protected Class reqType;
    // protected Class resType;
    protected Type reqType;
    protected Type resType;
    protected String reqClassStr;
    protected String resClassStr;

    public AbsRemoteMsgHandler(String reqTypeStr, String resTypeStr) {
        this.reqTypeStr = reqTypeStr;
        this.resTypeStr = resTypeStr;
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        // 其实这里应该检查一下，然后选择是否抛出异常表明必须继承RemoteMsgHandler时指定Req和Res的类型，但我选择相信继承者
        if (genericSuperclass != null) {
            this.reqType = genericSuperclass.getActualTypeArguments()[0];
            this.resType = genericSuperclass.getActualTypeArguments()[1];
            this.reqClassStr = this.reqType.toString().substring(6);
            this.resClassStr = this.resType.toString().substring(6);
        }
    }

    public String getReqTypeStr() {
        return reqTypeStr;
    }

    public AbsRemoteMsgHandler(String reqTypeStr, String resTypeStr, Class<Req> reqType, Class<Res> resType) {
        this.reqTypeStr = reqTypeStr;
        this.resTypeStr = resTypeStr;
        this.reqType = reqType;
        this.resType = resType;
    }

    public void onOpen() {
    }

    public void onClose() {
    }

    public void onError(String what) {
    }

    public void onFatalError(String what) {
    }

    public void onMessage(String serverUrl, String msg, Req req) {
        onMessage(serverUrl, req);
    }

    public abstract void onMessage(String serverUrl, Req req);

    public void send(String serverUrl, Res res) {
        sendObj(serverUrl, res, resClassStr, resTypeStr);
    }

    public void send(String serverUrl, String msg) {
        sendMsg(serverUrl, msg, resTypeStr);
    }

    private native void sendObj(String serverUrl, Object res, String resClassStr, String resTypeStr);

    private native void sendMsg(String serverUrl, String msg, String resTypeStr);
}
