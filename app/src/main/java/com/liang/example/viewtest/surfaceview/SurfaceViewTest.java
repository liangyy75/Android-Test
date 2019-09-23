package com.liang.example.viewtest.surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.liang.example.utils.ApiManager;

// https://www.jianshu.com/p/b037249e6d31
public class SurfaceViewTest extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "SurfaceViewTest";

    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private boolean mIsDrawing;
    private SurfaceViewHolder surfaceViewHolder;
    private long duration;

    public SurfaceViewTest(Context context) {
        super(context);
        init(context, null, -1, -1);
    }

    public SurfaceViewTest(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    public SurfaceViewTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    public SurfaceViewTest(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mSurfaceHolder = getHolder();
        // 注册回调方法
        mSurfaceHolder.addCallback(this);
    }

    public void setSurfaceViewHolder(SurfaceViewHolder surfaceViewHolder) {
        this.surfaceViewHolder = surfaceViewHolder;
        this.surfaceViewHolder.init(this);
        // // 设置一些参数方便后面绘图
        // setFocusable(true);
        // setKeepScreenOn(true);
        // setFocusableInTouchMode(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        surfaceViewHolder.surfaceCreated(this, holder);
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceViewHolder.surfaceChanged(this, holder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
        surfaceViewHolder.surfaceDestroyed(this, holder);
    }

    @Override
    public void run() {
        while (mIsDrawing) {
            long start = System.currentTimeMillis();
            surfaceViewHolder.run(this);
            long cost = System.currentTimeMillis() - start;
            if (cost < duration) {
                try {
                    Thread.sleep(duration - cost);
                } catch (InterruptedException e) {
                    ApiManager.LOGGER.d(TAG, "while doing something in loop", e);
                }
            }
        }
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public interface SurfaceViewHolder {
        void init(SurfaceView surfaceView);

        void run(SurfaceView surfaceView);

        default void surfaceCreated(SurfaceView surfaceView, SurfaceHolder holder) {
        }

        default void surfaceChanged(SurfaceView surfaceView, SurfaceHolder holder, int format, int width, int height) {
        }

        default void surfaceDestroyed(SurfaceView surfaceView, SurfaceHolder holder) {
        }
    }
}
