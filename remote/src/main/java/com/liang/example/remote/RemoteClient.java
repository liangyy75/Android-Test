package com.liang.example.remote;

import android.util.Base64;

import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.ExecutorService;

/**
 * @author liangyuyin
 * @since 2019/6/28
 */
public class RemoteClient implements Runnable, WSClientWrapper.SocketStateListener {
    private static final String TAG = "RemoteClient";

    private int readyState = WSClientWrapper.WEBSOCKET_STATE_CONNECTING;
    private WSClientWrapper webSocketClient;
    private String serverUrl;
    private ExecutorService mExecutorService;

    public RemoteClient(String serverUrl) {
        this(serverUrl, null);
    }

    public RemoteClient(String serverUrl, ExecutorService mExecutorService) {
        this.serverUrl = serverUrl;
        this.mExecutorService = mExecutorService;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public int getReadyState() {
        return readyState;
    }

    private void closeConnection() {
        if (webSocketClient != null && webSocketClient.isRunning()) {
            readyState = WSClientWrapper.WEBSOCKET_STATE_CLOSED;
            webSocketClient.safeClose();
            webSocketClient.removeListener();
            webSocketClient = null;
        }
    }

    private void sendMessage(String msg) {
        if (msg == null) {
            msg = "";
            RemoteMsgManager.logger.e(TAG, "try to send null string");
        }
        if (webSocketClient != null && webSocketClient.isRunning()) {
            webSocketClient.send(msg);
            RemoteMsgManager.logger.d(TAG, "send: " + msg);
        }
    }

    private void sendMessage(byte[] msg) {
        if (msg == null) {
            msg = new byte[]{};
            RemoteMsgManager.logger.e(TAG, "try to send null bytes");
        }
        if (webSocketClient != null && webSocketClient.isRunning()) {
            webSocketClient.send(msg);
            RemoteMsgManager.logger.d(TAG, "send: " + new String(msg));
        }
    }

    @Override
    public void run() {
        if (readyState != WSClientWrapper.WEBSOCKET_STATE_OPEN) {
            try {
                closeConnection();
                webSocketClient = new WSClientWrapper(new URI(serverUrl), this) {
                    @Override
                    public void run() {
                        try {
                            super.run();
                        } catch (Throwable e) {
                            RemoteClient.this.close();
                            RemoteMsgManager.logger.e(TAG, "start connect error", e);
                        }
                    }
                };
                webSocketClient.connect();
            } catch (Exception ex) {
                RemoteMsgManager.logger.e(TAG, "start connect error", ex);
            }
        }
    }

    public void close() {
        readyState = WSClientWrapper.WEBSOCKET_STATE_CLOSING;
        closeConnection();
    }

    public boolean isOpened() {
        return readyState == WSClientWrapper.WEBSOCKET_STATE_OPEN;
    }

    public void send(final String bytes, final String type) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (readyState == WSClientWrapper.WEBSOCKET_STATE_OPEN) {
                    try {
                        if (type.equals("string")) {
                            sendMessage(bytes);
                        } else {
                            sendMessage(Base64.decode(bytes, Base64.DEFAULT));
                        }
                    } catch (Exception e) {
                        onError(e);
                    }
                } else {
                    onError(new NotYetConnectedException());
                }
            }
        };
        if (mExecutorService != null) {
            mExecutorService.execute(task);
        } else {
            new Thread(task).start();
        }
    }

    @Override
    public void onOpen() {
        // RemoteMsgManager.logger.debug(TAG, "connect " + serverUrl + " successfully");
        readyState = WSClientWrapper.WEBSOCKET_STATE_OPEN;
        RemoteMsgManager.logger.d(TAG, "connect " + this.serverUrl + " successfully");
    }

    @Override
    public void onClose() {
        RemoteMsgManager.logger.d(TAG, "close connection");
        readyState = WSClientWrapper.WEBSOCKET_STATE_CLOSED;
    }

    @Override
    public void onMessage(String msg) {
        RemoteMsgManager.logger.d(TAG, "receive str: %s", msg);
    }

    @Override
    public void onMessage(byte[] msg) {
        onMessage(new String(msg));
    }

    @Override
    public void onError(Throwable e) {
        RemoteMsgManager.logger.e(TAG, "connect " + serverUrl + " failed", e);
    }
}
