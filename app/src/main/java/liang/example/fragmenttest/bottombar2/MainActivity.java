package liang.example.fragmenttest.bottombar2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import liang.example.androidtest.R;
import liang.example.fragmenttest.bottombar.ContextFragment;
import liang.example.utils.ApiManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "FragmentTest1_Main";

    private String[] bottomBarTitles = new String[]{"微信", "通信录", "发现", "我",};
    private Button[] buttons;
    private List<Fragment> contextFragments;
    int lastPosition = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fragment_bottombar2);
        ApiManager.LOGGER.d(TAG, "onCreate -- start");
        initButton();
        initFragment();
    }

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

    @Override
    public void onClick(View v) {
        showFragment((int) v.getTag());
    }

    private void initFragment() {
        contextFragments = new ArrayList<>(bottomBarTitles.length);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (String title : bottomBarTitles) {
            ContextFragment contextFragment = new ContextFragment();
            transaction.add(R.id.test_fragment_bottombar_container, contextFragment, title).hide(contextFragment);
            Bundle bundle = new Bundle();
            bundle.putString(ContextFragment.TEXTVIEW_NAME_KEY, title);
            contextFragment.setArguments(bundle);
            contextFragments.add(contextFragment);
        }
        transaction.commit();
        showFragment(0);
        ApiManager.LOGGER.d(TAG, "initFragment -- finish");
    }

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
