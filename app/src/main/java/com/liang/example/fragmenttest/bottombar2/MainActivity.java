package com.liang.example.fragmenttest.bottombar2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.liang.example.androidtest.R;
import com.liang.example.fragmenttest.bottombar.ContextFragment;
import com.liang.example.utils.ApiManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "FragmentTest1_Main";

    private Button[] buttons;
    private int lastPosition = 1;
    private List<Fragment> contextFragments;

    private String[] bottomBarTitles = new String[]{"微信", "通信录", "发现", "我",};
    private FragmentBarHelper2<String> fragmentBarHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fragment_bottombar2);
        ApiManager.LOGGER.d(TAG, "onCreate -- start");

        // initButton();
        // initFragment();

        LinearLayout linearLayout = findViewById(R.id.test_fragment_bottombar_buttons);
        List<String> dataSet = Arrays.asList(bottomBarTitles);
        int red = ContextCompat.getColor(this, android.R.color.holo_red_light);  // getColor(android.R.color.holo_red_light) minSdkVersion=21
        int green = ContextCompat.getColor(this, android.R.color.holo_green_light);
        fragmentBarHelper = new FragmentBarHelper2<>(this, R.id.test_fragment_bottombar_container, dataSet,
                (index, context, data) -> {
                    ApiManager.LOGGER.d(TAG, "create controller - %d", index);
                    Button button = new Button(MainActivity.this);
                    button.setText(data);
                    button.setBackgroundResource(R.color.colorTransparent);
                    button.setTextColor(red);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    linearLayout.addView(button, layoutParams);
                    if (index == 0) {
                        button.setTextColor(green);
                    }
                    return button;
                },
                index -> {
                    ApiManager.LOGGER.d(TAG, "create fragment - %d", index);
                    ContextFragment contextFragment = new ContextFragment();
                    String title = bottomBarTitles[index];
                    Bundle bundle = new Bundle();
                    bundle.putString(ContextFragment.TEXT_VIEW_NAME_KEY, title);
                    contextFragment.setArguments(bundle);
                    contextFragment.setOkClickListener((v) -> fragmentBarHelper.switchFragment((index + 1) % dataSet.size()));
                    contextFragment.setCancelClickListener((v) -> fragmentBarHelper.switchFragment((index - 1 + dataSet.size()) % dataSet.size()));
                    return new FragmentBarHelper2.FragmentWrapper(contextFragment, title);
                }, false/*, true*/)
                .setSwitchListener(((lastPos, newPos) -> {
                    ((Button) fragmentBarHelper.getButtons().get(lastPos)).setTextColor(red);
                    ((Button) fragmentBarHelper.getButtons().get(newPos)).setTextColor(green);
                }))
                .setBackListener((lastPos, newPos) -> {
                    fragmentBarHelper.getBackStack().clear();
                    fragmentBarHelper.switchFragment(0, false);
                    if (lastPos == 0) {
                        fragmentBarHelper.backFragment();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        // if (!fragmentBarHelper.backFragment())
        //     super.onBackPressed();
        fragmentBarHelper.backFragment();
    }

    @Deprecated
    private void initButton() {
        LinearLayout linearLayout = findViewById(R.id.test_fragment_bottombar_buttons);
        buttons = new Button[bottomBarTitles.length];
        for (int i = 0; i < bottomBarTitles.length; i++) {
            Button button = new Button(this);
            button.setText(bottomBarTitles[i]);
            button.setTag(i);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            button.setOnClickListener(this);
            button.setBackgroundResource(R.color.colorTransparent);
            button.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            linearLayout.addView(button, layoutParams);
            buttons[i] = button;
        }
        ApiManager.LOGGER.d(TAG, "initButton -- finish");
    }

    @Deprecated
    @Override
    public void onClick(View v) {
        showFragment((int) v.getTag());
    }

    @Deprecated
    private void initFragment() {
        contextFragments = new ArrayList<>(bottomBarTitles.length);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (String title : bottomBarTitles) {
            ContextFragment contextFragment = new ContextFragment();
            transaction.add(R.id.test_fragment_bottombar_container, contextFragment, title).hide(contextFragment);
            Bundle bundle = new Bundle();
            bundle.putString(ContextFragment.TEXT_VIEW_NAME_KEY, title);
            contextFragment.setArguments(bundle);
            contextFragments.add(contextFragment);
        }
        transaction.commit();
        showFragment(0);
        ApiManager.LOGGER.d(TAG, "initFragment -- finish");
    }

    @Deprecated
    private void showFragment(int newPosition) {
        if (newPosition == lastPosition) return;
        ApiManager.LOGGER.d(TAG, "changeStatus(lastPosition: %d, newPosition: %d)", lastPosition, newPosition);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(contextFragments.get(lastPosition));
        buttons[lastPosition].setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));  // getColor(android.R.color.holo_red_light) minSdkVersion=21
        lastPosition = newPosition;
        transaction.show(contextFragments.get(lastPosition));
        buttons[lastPosition].setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        transaction.commit();
    }
}
