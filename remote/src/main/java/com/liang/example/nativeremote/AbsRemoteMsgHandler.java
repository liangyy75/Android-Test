package com.liang.example.nativeremote;

public abstract class AbsRemoteMsgHandler<Req, Res> {
    private String reqTypeStr;
    private String resTypeStr;

    public AbsRemoteMsgHandler(String reqTypeStr, String resTypeStr) {
        this.reqTypeStr = reqTypeStr;
        this.resTypeStr = resTypeStr;
    }

    public void onOpen() {
    }

    public void onClose() {
    }

    public void onError() {
    }

    public void onFatalError(String what) {
    }

    public void onMessage(String msg, Req req) {
        onMessage(req);
    }

    public abstract void onMessage(Req req);

    public native void send(Res res);

    public native void send(String msg);
}
