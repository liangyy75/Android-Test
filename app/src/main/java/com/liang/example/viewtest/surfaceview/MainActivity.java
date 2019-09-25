package com.liang.example.viewtest.surfaceview;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.liang.example.androidtest.R;
import com.liang.example.utils.ApiManager;
import com.liang.example.utils.ScreenApiKt;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "View_Surface_Main";

    SurfaceViewTest surfaceViewTest1;
    SurfaceViewTest surfaceViewTest2;
    FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surfaceview_test);

        surfaceViewTest1 = findViewById(R.id.test_surfaceview_surfaceview1);
        surfaceViewTest1.setSurfaceViewHolder(new SurfaceViewTest.SurfaceViewHolder() {
            private Canvas mCanvas;
            private Paint mPaint;
            private Path mPath;
            private int x = 0, y = 0;

            @Override
            public void init(SurfaceView surfaceView, SurfaceHolder surfaceHolder) {
                surfaceView.setFocusable(true);
                surfaceView.setKeepScreenOn(true);
                surfaceView.setFocusableInTouchMode(true);
                mPaint = new Paint();
                mPaint.setColor(Color.BLACK);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setAntiAlias(true);
                mPaint.setStrokeWidth(5);
                mPath = new Path();
                // 路径起始点(0, 100)
                mPath.moveTo(0, 100);
            }

            @Override
            public void run(SurfaceView surfaceView, SurfaceHolder surfaceHolder) {
                try {
                    mCanvas = surfaceHolder.lockCanvas();
                    mCanvas.drawColor(Color.WHITE);
                    mCanvas.drawPath(mPath, mPaint);
                } catch (Exception e) {
                    ApiManager.LOGGER.d(TAG, "surfaceViewTest1", e);
                } finally {
                    if (mCanvas != null) {
                        surfaceHolder.unlockCanvasAndPost(mCanvas);
                    }
                }
                x += 1;
                y = (int) (100 * Math.sin(2 * x * Math.PI / 180) + 400);
                // 加入新的坐标点
                mPath.lineTo(x, y);
            }
        });

        surfaceViewTest2 = findViewById(R.id.test_surfaceview_surfaceview2);
        final int screenWidthPixels = ScreenApiKt.getScreenWidthPixels(this);
        surfaceViewTest2.setTranslationX(screenWidthPixels);
        surfaceViewTest2.setSurfaceViewHolder(new SurfaceViewTest.SurfaceViewHolder() {
            private Canvas mCanvas;
            private Paint mPaint;
            private Path mPath;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void init(SurfaceView surfaceView, SurfaceHolder surfaceHolder) {
                surfaceView.setFocusable(true);
                surfaceView.setKeepScreenOn(true);
                surfaceView.setFocusableInTouchMode(true);
                mPaint = new Paint();
                mPaint.setColor(Color.BLACK);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(5);
                mPaint.setAntiAlias(true);
                mPath = new Path();
                mPath.moveTo(0, 100);
                surfaceViewTest2.setOnTouchListener((v, e) -> {
                    int x = (int) e.getX();
                    int y = (int) e.getY();
                    int action = e.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        mPath.moveTo(x, y);
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        mPath.lineTo(x, y);
                    }
                    return true;
                });
            }

            @Override
            public void run(SurfaceView surfaceView, SurfaceHolder surfaceHolder) {
                try {
                    mCanvas = surfaceHolder.lockCanvas();
                    mCanvas.drawColor(Color.WHITE);
                    mCanvas.drawPath(mPath, mPaint);
                } catch (Exception e) {
                    ApiManager.LOGGER.d(TAG, "surfaceViewTest1", e);
                } finally {
                    if (mCanvas != null) {
                        surfaceHolder.unlockCanvasAndPost(mCanvas);
                    }
                }
            }
        });

        floatingActionButton = findViewById(R.id.test_surfaceview_float_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            private boolean flag = false;

            @Override
            public void onClick(View v) {
                View leaveView, showView;
                float start = 0f;
                if (flag) {
                    leaveView = surfaceViewTest1;
                    showView = surfaceViewTest2;
                    start = 180f;
                } else {
                    showView = surfaceViewTest1;
                    leaveView = surfaceViewTest2;
                }
                flag = !flag;
                leaveView.setTranslationX(screenWidthPixels);
                showView.setTranslationX(0);
                leaveView.animate().setDuration(1000).translationX(0).start();
                showView.animate().setDuration(1000).translationX(-screenWidthPixels).start();
                ObjectAnimator.ofFloat(floatingActionButton, "rotation", start, start + 180).setDuration(1000).start();
            }
        });
    }
}
