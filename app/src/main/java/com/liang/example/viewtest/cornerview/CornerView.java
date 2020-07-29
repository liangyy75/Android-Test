package com.liang.example.viewtest.cornerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

// https://blog.csdn.net/wei1583812/article/details/53130637
// TODO: radius
public class CornerView extends View {
    public CornerView(Context context) {
        super(context);
        init();
    }

    public CornerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CornerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, float radius) {
        super(context, attrs, defStyleAttr);
        this.radius = radius;
        init();
    }

    public CornerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes, float radius) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.radius = radius;
        init();
    }

    private float radius = -1;
    private Paint paint;

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        radius = radius * density;
        paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  // 抗锯齿
        paint.setDither(true);  // 防抖动
    }

    public void setPaintColor(int color) {
        this.paint.setColor(color);
    }

    public void setCorner(float radius) {
        this.radius = radius;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        canvas.drawCircle(w / 2, h / 2, radius > 0 ? radius : Math.max(w, h) / 2, paint);
        super.draw(canvas);
    }
}
