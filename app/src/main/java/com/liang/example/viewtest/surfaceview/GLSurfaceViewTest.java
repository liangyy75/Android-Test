package com.liang.example.viewtest.surfaceview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

// TODO: [GLSurfaceView的简单分析及巧妙借用](https://blog.csdn.net/junzia/article/details/73717506)
// TODO: [Android OpenGLES](https://blog.csdn.net/junzia/article/category/9269184)
public class GLSurfaceViewTest extends GLSurfaceView {
    public GLSurfaceViewTest(Context context) {
        super(context);
        init(context, null);
    }

    public GLSurfaceViewTest(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
    }
}
