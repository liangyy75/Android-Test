package com.liang.example.viewtest.animator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.Animation;
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

        // findViewById(R.id.test_view_anim_alpha).setOnClickListener((v) -> startAnim(getAlphaAnimation()));
        // findViewById(R.id.test_view_anim_scale).setOnClickListener((v) -> startAnim(getScaleAnimation(selfMode)));
        // findViewById(R.id.test_view_anim_rotate).setOnClickListener((v) -> startAnim(getRotateAnimation(selfMode)));
        // findViewById(R.id.test_view_anim_translate).setOnClickListener((v) -> startAnim(getTranslateAnimation(parentMode, absoluteMode, translateGap)));
        // findViewById(R.id.test_view_anim_color).setOnClickListener((v) -> );
    }
}
