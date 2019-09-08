package com.liang.example.remote;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.PingFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

// import org.java_websocket.framing.PingFrame;

/**
 * @author liangyuyin
 * @since 2019/6/28
 */
public class WSClientWrapper extends WebSocketClient {

    private final String TAG = "WSClientWrapper";

    /**
     * The connection has not yet been established.
     */
    public final static int WEBSOCKET_STATE_CONNECTING = 0;
    /**
     * The WebSocket connection is established and communication is possible.
     */
    public final static int WEBSOCKET_STATE_OPEN = 1;
    /**
     * The connection is going through the closing handshake.
     */
    public final static int WEBSOCKET_STATE_CLOSING = 2;
    /**
     * The connection has been closed or could not be opened.
     */
    public final static int WEBSOCKET_STATE_CLOSED = 3;

    private boolean running = false;
    private SocketStateListener listener;
    private Timer timer;
    private long headInterval = 20 * 1000;

    public boolean isRunning() {
        return running;
    }

    public WSClientWrapper(URI serverUri, SocketStateListener listener) {
        super(serverUri);
        this.listener = listener;
    }

    public void setHeadInterval(long headInterval) {
        this.headInterval = headInterval;
    }

    public long getHeadInterval() {
        return headInterval;
    }

    public void safeClose() {
        try {
            close();
        } catch (Throwable e) {
            RemoteMsgManager.logger.e(TAG, e);
        }
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        RemoteMsgManager.logger.d(TAG, "opened connection");
        running = true;
        if (null != listener) {
            listener.onOpen();
        }
        timer = new Timer();
        // 心跳，维持活力
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // FramedataImpl1 framedata = new FramedataImpl1(Framedata.Opcode.PING);
                // WSClientWrapper.this.getConnection().sendFrame(framedata);
                WSClientWrapper.this.getConnection().sendFrame(new PingFrame());
            }
        }, headInterval, headInterval);
    }

    @Override
    public void onMessage(String message) {
        RemoteMsgManager.logger.d(TAG, "send message");
        if (null != listener) {
            listener.onMessage(message);
        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        RemoteMsgManager.logger.d(TAG, "send message");
        super.onMessage(bytes);
        if (null != listener) {
            listener.onMessage(bytes.array());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        RemoteMsgManager.logger.d(TAG, "Connection closed by server :" + remote);
        running = false;
        if (null != listener) {
            listener.onClose();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onError(Exception ex) {
        if (ex != null) {
            RemoteMsgManager.logger.e(TAG, ex);
        } else {
            RemoteMsgManager.logger.e(TAG, "Connection error");
        }
        running = false;
        if (null != listener) {
            listener.onError(ex);
        }
    }

    public void removeListener() {
        listener = null;
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f);
        RemoteMsgManager.logger.d(TAG, "Ping opCode: " + f.getOpcode());
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        super.onWebsocketPong(conn, f);
        RemoteMsgManager.logger.d(TAG, "Pong opCode: " + f.getOpcode());
    }

    public interface SocketStateListener {

        void onOpen();

        void onClose();

        void onMessage(String msg);

        void onMessage(byte[] msg);

        void onError(Throwable e);
    }
}
