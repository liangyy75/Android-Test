package com.liang.example.viewtest.animation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.R;
import com.liang.example.utils.ScreenApiKt;
import com.liang.example.utils.view.ToastApiKt;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView animObj;
    private boolean alphaFlag = false;
    private boolean scaleFlag = false;
    private boolean rotateFlag = false;
    private boolean translateFlag = false;
    private boolean colorFlag = false;
    private Timer timer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim_test);
        animObj = findViewById(R.id.test_view_anim_obj);
        animObj.setOnClickListener((v) -> ToastApiKt.showToast("click anim obj"));  // translate后点击特定区域可以证明只是改变外形，没有改变属性
        int selfMode = Animation.RELATIVE_TO_SELF;
        int parentMode = Animation.RELATIVE_TO_PARENT;
        int absoluteMode = Animation.ABSOLUTE;
        int translateGap = ScreenApiKt.getScreenWidthPixels(this) - getResources().getDimensionPixelSize(R.dimen.dimen20) * 2 - getResources().getDimensionPixelSize(R.dimen.dimen260);

        findViewById(R.id.test_view_anim_alpha).setOnClickListener((v) -> startAnim(getAlphaAnimation()));
        findViewById(R.id.test_view_anim_scale).setOnClickListener((v) -> startAnim(getScaleAnimation(selfMode)));
        findViewById(R.id.test_view_anim_rotate).setOnClickListener((v) -> startAnim(getRotateAnimation(selfMode)));
        findViewById(R.id.test_view_anim_translate).setOnClickListener((v) -> startAnim(getTranslateAnimation(parentMode, absoluteMode, translateGap)));
        // View colorButton = findViewById(R.id.test_view_anim_color);
        // colorButton.setClickable(false);
        // colorButton.setFocusable(false);
        findViewById(R.id.test_view_anim_color).setOnClickListener((v) -> startColorAnim());

        CheckBox checkAlpha = findViewById(R.id.test_view_anim_check_alpha);
        CheckBox checkScale = findViewById(R.id.test_view_anim_check_scale);
        CheckBox checkTranslate = findViewById(R.id.test_view_anim_check_translate);
        CheckBox checkRotate = findViewById(R.id.test_view_anim_check_rotate);
        CheckBox checkColor = findViewById(R.id.test_view_anim_check_color);
        findViewById(R.id.test_view_anim_set).setOnClickListener((v) -> {
            AnimationSet animation = new AnimationSet(true);
            if (checkAlpha.isChecked()) {
                animation.addAnimation(getAlphaAnimation());
            }
            if (checkScale.isChecked()) {
                animation.addAnimation(getScaleAnimation(selfMode));
            }
            if (checkRotate.isChecked()) {
                animation.addAnimation(getRotateAnimation(selfMode));
            }
            if (checkTranslate.isChecked()) {
                animation.addAnimation(getTranslateAnimation(parentMode, absoluteMode, translateGap));
            }
            if (checkColor.isChecked()) {
                startColorAnim();
            }
            startAnim(animation);
        });
    }

    private void startColorAnim() {
        int d = 50;  // 渲染间隔
        int t = 3000;  // 总耗时时间
        int start = Color.BLACK;
        int end = Color.WHITE;
        timer.schedule(new TimerTask() {
            private int startColor = start, endColor = end;
            private float base1 = Color.blue(start), base2 = Color.green(start), base3 = Color.red(start);
            private float step1 = (float) (Color.blue(start) - Color.blue(end)) * d / t;
            private float step2 = (float) (Color.green(start) - Color.green(end)) * d / t;
            private float step3 = (float) (Color.red(start) - Color.red(end)) * d / t;
            private boolean flag = startColor < endColor;

            @Override
            public void run() {
                if (startColor != endColor) {
                    base1 -= step1;
                    base2 -= step2;
                    base3 -= step3;
                    startColor = Color.argb(0xff, (int) base3, (int) base2, (int) base1);
                    if (flag && startColor > endColor || !flag && startColor < endColor) {
                        startColor = endColor;
                    }
                }
                animObj.post(() -> {
                    // ApiManager.LOGGER.d("Animation_Test", "step1: %f, step2: %f, step3: %f, color: 0x%s", step1, step2, step3, Integer.toHexString(startColor));
                    int revertColor = Color.argb(0xff, 0xff - (int) base3, 0xff - (int) base2, 0xff - (int) base1);
                    if (colorFlag) {
                        animObj.setBackgroundColor(startColor);
                        animObj.setTextColor(revertColor);
                    } else {
                        animObj.setBackgroundColor(revertColor);
                        animObj.setTextColor(startColor);
                    }
                });
                if (startColor == endColor) {
                    this.cancel();
                }
            }
        }, d, d);
        colorFlag = !colorFlag;
    }

    @NotNull
    private TranslateAnimation getTranslateAnimation(int mode1, int mode2, int translateGap) {
        translateFlag = !translateFlag;
        return !translateFlag ? new TranslateAnimation(mode2, translateGap, mode1, 0.0f, mode1, 0.0f, mode1, 0.0f)
                : new TranslateAnimation(mode1, 0.0f, mode2, translateGap, mode1, 0.0f, mode1, 0.0f);
    }

    @NotNull
    private RotateAnimation getRotateAnimation(int mode) {
        rotateFlag = !rotateFlag;
        return !rotateFlag ? new RotateAnimation(0, 180, mode, 0.5f, mode, 0.5f) :
                new RotateAnimation(180, 0, mode, 0.5f, mode, 0.5f);
    }

    @NotNull
    private ScaleAnimation getScaleAnimation(int mode) {
        scaleFlag = !scaleFlag;
        return !scaleFlag ? new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, mode, 0.5f, mode, 0.5f) :
                new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, mode, 0.5f, mode, 0.5f);
    }

    @NotNull
    private AlphaAnimation getAlphaAnimation() {
        alphaFlag = !alphaFlag;
        return !alphaFlag ? new AlphaAnimation(0.0f, 1.0f) : new AlphaAnimation(1.0f, 0.0f);
    }

    private void startAnim(Animation animation) {
        animation.setDuration(3000);
        animation.setFillEnabled(false);
        animation.setFillAfter(true);
        animation.setFillBefore(false);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.RESTART);
        animObj.startAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer = null;
    }
}
