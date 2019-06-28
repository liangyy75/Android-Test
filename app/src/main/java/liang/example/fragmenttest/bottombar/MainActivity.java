package liang.example.fragmenttest.bottombar;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import liang.example.androidtest.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String[] bottomBarTitles = new String[]{"微信", "通信录", "发现", "我",};
    private Button[] buttons;
    int lastPosition = 1;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fragment_bottombar);
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
    }

    @Override
    public void onClick(View v) {
        viewPager.setCurrentItem((int) v.getTag());
    }

    private void initFragment() {
        List<Fragment> contextFragments = new ArrayList<>(bottomBarTitles.length);
        for (String title : bottomBarTitles) {
            ContextFragment contextFragment = new ContextFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ContextFragment.TEXTVIEW_NAME_KEY, title);
            contextFragment.setArguments(bundle);
            contextFragments.add(contextFragment);
        }
        FragmentPagerAdapterTest fragmentPagerAdapter = new FragmentPagerAdapterTest(getSupportFragmentManager(), contextFragments);
        viewPager = findViewById(R.id.test_fragment_bottombar_viewpager);
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            /**
             * 这个方法会在屏幕滚动过程中不断被调用。
             * @param position 当用手指滑动时，如果手指按在页面上不动，position和当前页面index是一致的；
             *                 如果手指向左拖动（相应页面向右翻动），这时候position大部分时间和当前页面是一致的，只有翻页成功的情况下最后一次调用才会变为目标页面；
             *                 如果手指向右拖动（相应页面向左翻动），这时候position大部分时间和目标页面是一致的，只有翻页不成功的情况下最后一次调用才会变为原页面。
             *                 当直接设置setCurrentItem翻页时，如果是相邻的情况，如果页面向右翻动，大部分时间是和当前页面是一致的，只有最后才变成目标页面；
             *                 如果向左翻动，position和目标页面是一致的。这和用手指拖动页面翻动是基本一致的。
             *                 如果不是相邻的情况，比如我从第一个页面跳到第三个页面，position先是0，然后逐步变成1，然后逐步变成2；
             *                 我从第三个页面跳到第一个页面，position先是1，然后逐步变成0，并没有出现为2的情况。
             * @param positionOffset 当前页面滑动比例，如果页面向右翻动，这个值不断变大，最后在趋近1的情况后突变为0。如果页面向左翻动，这个值不断变小，最后变为0。
             * @param positionOffsetPixels 当前页面滑动像素，变化情况和positionOffset一致。
             */
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("OnPageChangeListener onPageScrolled", String.format(
                        "position: %d, positionOffset: %f, positionOffsetPixels: %d", position, positionOffset, positionOffsetPixels));
            }

            /**
             * 当用手指滑动翻页的时候，如果翻动成功了（滑动的距离够长），手指抬起来就会立即执行这个方法。
             * 如果直接setCurrentItem翻页，那position就和setCurrentItem的参数一致，这种情况在onPageScrolled执行方法前就会立即执行。
             * @param position 代表哪个页面被选中。
             */
            @Override
            public void onPageSelected(int position) {
                Log.d("OnPageChangeListener onPageSelected", String.format("position: %d", position));
                if (position == lastPosition) return;
                changeStatus(position);
            }

            /**
             * 此方法是在状态改变的时候调用
             * @param state 表示状态，可选值为0(END)/1(PRESS)/2(UP)，当用手指滑动翻页时，手指按下去的时候会触发这个方法，state值为1，
             *              手指抬起时，如果发生了滑动（即使很小），这个值会变为2，然后最后变为0 。总共执行这个方法三次。
             *              一种特殊情况是手指按下去以后一点滑动也没有发生，这个时候只会调用这个方法两次，state值分别是1,0 。
             *              当setCurrentItem翻页时，会执行这个方法两次，state值分别为2 , 0 。
             */
            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("OnPageChange onPageScrollStateChanged", String.format("state: %d", state));
            }
            /*
             * 三个方法的执行顺序为：用手指拖动翻页时，最先执行一遍onPageScrollStateChanged（1），然后不断执行onPageScrolled，放手指的时候，
             * 直接立即执行一次onPageScrollStateChanged（2），然后立即执行一次onPageSelected，然后再不断执行onPageScrollStateChanged，
             * 最后执行一次onPageScrollStateChanged（0）。
             * https://www.cnblogs.com/Dionexin/p/5727297.html
             */
        });
        viewPager.setCurrentItem(0);
        changeStatus(0);
    }

    private void changeStatus(int position) {
        buttons[lastPosition].setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        lastPosition = position;
        buttons[lastPosition].setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
    }
}
