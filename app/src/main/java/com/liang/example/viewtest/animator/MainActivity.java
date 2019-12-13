package com.liang.example.viewtest.animator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Property;
import android.view.View;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.R;
import com.liang.example.utils.ScreenApiKt;
import com.liang.example.utils.view.ToastApiKt;

public class MainActivity extends AppCompatActivity {
    private TextView animObj;
    private boolean alphaFlag = false;
    private boolean scaleFlag = false;
    private boolean rotateFlag = false;
    private boolean translateFlag = false;
    private boolean colorFlag = false;

    private static final boolean useObject = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim_test);
        animObj = findViewById(R.id.test_view_anim_obj);
        animObj.setOnClickListener((v) -> ToastApiKt.showToast("click anim obj"));
        int selfMode = Animation.RELATIVE_TO_SELF;
        int parentMode = Animation.RELATIVE_TO_PARENT;
        int absoluteMode = Animation.ABSOLUTE;
        int translateGap = ScreenApiKt.getScreenWidthPixels(this) - getResources().getDimensionPixelSize(R.dimen.dimen20) * 2 - getResources().getDimensionPixelSize(R.dimen.dimen260);

        findViewById(R.id.test_view_anim_alpha).setOnClickListener((v) -> startAnim(getAlphaAnimator()));
        findViewById(R.id.test_view_anim_scale).setOnClickListener((v) -> startAnim(getScaleAnimator(selfMode)));
        findViewById(R.id.test_view_anim_rotate).setOnClickListener((v) -> startAnim(getRotateAnimator(selfMode)));
        findViewById(R.id.test_view_anim_translate).setOnClickListener((v) -> startAnim(getTranslateAnimator(parentMode, absoluteMode, translateGap)));
        findViewById(R.id.test_view_anim_color).setOnClickListener((v) -> startAnim(getColorAnimator()));

        CheckBox checkAlpha = findViewById(R.id.test_view_anim_check_alpha);
        CheckBox checkScale = findViewById(R.id.test_view_anim_check_scale);
        CheckBox checkTranslate = findViewById(R.id.test_view_anim_check_translate);
        CheckBox checkRotate = findViewById(R.id.test_view_anim_check_rotate);
        CheckBox checkColor = findViewById(R.id.test_view_anim_check_color);
        findViewById(R.id.test_view_anim_set).setOnClickListener((v) -> {
            AnimatorSet animator = new AnimatorSet();
            if (checkAlpha.isChecked()) {
                animator.play(getAlphaAnimator());
            }
            if (checkScale.isChecked()) {
                animator.play(getScaleAnimator(selfMode));
            }
            if (checkRotate.isChecked()) {
                animator.play(getRotateAnimator(selfMode));
            }
            if (checkTranslate.isChecked()) {
                animator.play(getTranslateAnimator(parentMode, absoluteMode, translateGap));
            }
            if (checkColor.isChecked()) {
                animator.play(getColorAnimator());
            }
            startAnim(animator);
        });
    }

    private void startAnim(Animator animator) {
        animator.setDuration(3000);
        if (animator instanceof ValueAnimator) {
            ((ValueAnimator) animator).setRepeatCount(1);
            ((ValueAnimator) animator).setRepeatMode(ValueAnimator.RESTART);
        }
        animator.start();
    }

    private Animator getAlphaAnimator() {
        alphaFlag = !alphaFlag;
        float[] values = !alphaFlag ? new float[]{0f, 1f} : new float[]{1f, 0f};
        if (useObject) {
            return ObjectAnimator.ofFloat(animObj, "alpha", values);
        }
        ValueAnimator animator = ValueAnimator.ofFloat(values);
        animator.addUpdateListener(animation -> animObj.setAlpha((Float) animation.getAnimatedValue()));
        return animator;
    }

    private Animator getScaleAnimator(int selfMode) {
        scaleFlag = !scaleFlag;
        float[] values = !scaleFlag ? new float[]{0f, 1f} : new float[]{1f, 0f};
        // animObj.setPivotX(0.5f);
        // animObj.setPivotY(0.5f);
        if (useObject) {
            return ObjectAnimator.ofPropertyValuesHolder(animObj, PropertyValuesHolder.ofFloat(View.SCALE_X, values), PropertyValuesHolder.ofFloat(View.SCALE_Y, values));
        }
        ValueAnimator animator = ValueAnimator.ofFloat(values);
        // TODO: mode -- absolute / relative to self / relative to parent / ...
        animator.addUpdateListener(animation -> {
            float floatValue = (Float) animation.getAnimatedValue();
            animObj.setScaleX(floatValue);
            animObj.setScaleY(floatValue);
        });
        return animator;
    }

    private Animator getRotateAnimator(int selfMode) {
        rotateFlag = !rotateFlag;
        float[] values = !rotateFlag ? new float[]{0, 180} : new float[]{180, 0};
        // animObj.setPivotX(0.5f);
        // animObj.setPivotY(0.5f);
        if (useObject) {
            return ObjectAnimator.ofFloat(animObj, "rotation", values);
        }
        ValueAnimator animator = ValueAnimator.ofFloat(values);
        // TODO: mode -- absolute / relative to self / relative to parent / ...
        animator.addUpdateListener(animation -> animObj.setRotation((Float) animation.getAnimatedValue()));
        return animator;
    }

    private Animator getTranslateAnimator(int mode1, int mode2, int translateGap) {
        translateFlag = !translateFlag;
        float[] values = !translateFlag ? new float[]{translateGap, 0} : new float[]{0, translateGap};
        if (useObject) {
            return ObjectAnimator.ofFloat(animObj, View.TRANSLATION_X, values);
        }
        ValueAnimator animator = ValueAnimator.ofFloat(values);
        // TODO: mode -- absolute / relative to self / relative to parent / ...
        animator.addUpdateListener(animation -> animObj.setTranslationX((Float) animation.getAnimatedValue()));
        return animator;
    }

    private Animator getColorAnimator() {
        colorFlag = !colorFlag;
        int startColor = !colorFlag ? Color.BLACK : Color.WHITE;
        int endColor = !colorFlag ? Color.WHITE : Color.BLACK;
        if (useObject) {
            // return ObjectAnimator.ofPropertyValuesHolder(animObj,
            //         PropertyValuesHolder.ofInt("backgroundColor", endColor, startColor),
            //         PropertyValuesHolder.ofInt("textColor", startColor, endColor));
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(ObjectAnimator.ofArgb(animObj, "backgroundColor", endColor, startColor))
                    .with(ObjectAnimator.ofArgb(animObj, "textColor", startColor, endColor));
            return animatorSet;
        }
        ValueAnimator animator = ValueAnimator.ofArgb(startColor, endColor);
        animator.addUpdateListener(animation -> {
            int intValue = (int) animation.getAnimatedValue();
            animObj.setTextColor(intValue);
            animObj.setBackgroundColor(Color.argb(0xff, 0xff - Color.red(intValue), 0xff - Color.green(intValue), 0xff - Color.blue(intValue)));
        });
        return animator;
    }
}
