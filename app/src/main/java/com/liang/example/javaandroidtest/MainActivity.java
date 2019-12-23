package com.liang.example.javaandroidtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.uilib.block.Block;
import com.example.uilib.block.BlockActivity;
import com.example.uilib.block.BlockManager;
import com.liang.example.androidtest.R;
import com.liang.example.utils.ApiManager;
import com.liang.example.utils.DensityApi;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static com.liang.example.utils.view.ToastApiKt.showToast;

// import java.util.Timer;
// import java.util.TimerTask;

public class MainActivity extends BlockActivity {
    private static final String TAG = "Groovy-Java-Kotlin";

    // private Timer timer;

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    protected List<BlockManager> getBlockManagerList() {
        int dp10 = DensityApi.INSTANCE.dpToPx(this, 10f);
        List<BlockManager> result = new ArrayList<>();
        BlockManager blockManager = new BlockManager(this, R.layout.layout_linear);
        blockManager.setInflateBlocksAsync(false);
        blockManager.setParent(getWindow().getDecorView().findViewById(android.R.id.content));

        blockManager.addBlock(new Block(R.layout.view_button).setInflatedCallback((Function1<Button, Unit>) it -> {
            ApiManager.LOGGER.d(TAG, "block -- button_1 afterInflater");
            it.setId(R.id.button_1);
            it.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            it.setText("go to kotlin");
            it.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, com.liang.example.ktandroidtest.MainActivity.class)));
            return null;
        }));

        blockManager.addBlock(new Block(R.layout.view_button).setInflatedCallback((Function1<Button, Unit>) it -> {
            ApiManager.LOGGER.d(TAG, "block -- button_2 afterInflater");
            it.setId(R.id.button_2);
            it.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            it.setText("go to groovy");
            it.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, com.liang.example.groovyandroidtest.MainActivity.class)));
            return null;
        }));

        result.add(blockManager.setInflatedCallback2((Function1<LinearLayout, Unit>) it -> {
            ApiManager.LOGGER.d(TAG, "blockManager -- linear_layout_1 afterInflater");
            it.setId(R.id.linear_layout_1);
            it.setOrientation(LinearLayout.VERTICAL);
            // it.setGravity(Gravity.CENTER_HORIZONTAL);
            it.setPadding(dp10, dp10, dp10, dp10);
            it.setOnClickListener(v -> showToast("Java Activity is clicked"));
            return null;
        }));

        // timer = new Timer();
        // timer.schedule(new TimerTask() {
        //     @Override
        //     public void run() {
        //         ApiManager.LOGGER.d(TAG, "blockManager -- children: " + blockManager.getChildren().size() + "; views' size: "
        //                 + (blockManager.getViewGroup() != null ? blockManager.getViewGroup().getChildCount() : 0));
        //     }
        // }, 3000, 3000);
        return result;
    }
    //
    // @Override
    // protected void onCreate(@Nullable Bundle bundle) {
    //     super.onCreate(bundle);
    //     this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    // }
    //
    // @Override
    // protected void onDestroy() {
    //     super.onDestroy();
    //     // timer.cancel();
    // }
}
