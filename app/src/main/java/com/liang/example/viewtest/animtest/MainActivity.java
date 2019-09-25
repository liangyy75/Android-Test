package com.liang.example.viewtest.animtest;

import android.os.Bundle;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {
    private TextView animObj;
    private boolean alphaFlag = false;
    private boolean scaleFlag = false;
    private boolean rotateFlag = false;
    private boolean translateFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim_test);
        animObj = findViewById(R.id.test_view_anim_obj);
        animObj.setOnClickListener((v) -> ToastApiKt.showToast("click anim obj"));
        int selfMode = Animation.RELATIVE_TO_SELF;
        int parentMode = Animation.RELATIVE_TO_PARENT;
        int absoluteMode = Animation.ABSOLUTE;
        int translateGap = ScreenApiKt.getScreenWidthPixels(this) - getResources().getDimensionPixelSize(R.dimen.dimen20) * 2
                - getResources().getDimensionPixelSize(R.dimen.dimen300);

        findViewById(R.id.test_view_anim_alpha).setOnClickListener((v) -> startAnim(getAlphaAnimation()));
        findViewById(R.id.test_view_anim_scale).setOnClickListener((v) -> startAnim(getScaleAnimation(selfMode)));
        findViewById(R.id.test_view_anim_rotate).setOnClickListener((v) -> startAnim(getRotateAnimation(selfMode)));
        findViewById(R.id.test_view_anim_translate).setOnClickListener((v) -> startAnim(getTranslateAnimation(parentMode, absoluteMode, translateGap)));

        CheckBox checkAlpha = findViewById(R.id.test_view_anim_check_alpha);
        CheckBox checkScale = findViewById(R.id.test_view_anim_check_scale);
        CheckBox checkTranslate = findViewById(R.id.test_view_anim_check_translate);
        CheckBox checkRotate = findViewById(R.id.test_view_anim_check_rotate);
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
            startAnim(animation);
        });
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
}
