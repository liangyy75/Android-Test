package liang.example.viewpagertest;

// import android.support.annotation.NonNull;
// import androidx.core.view.PagerAdapter;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class PagerAdapterTest<T> extends PagerAdapter {
    private List<T> dataSet;
    private boolean carousel;  // 是否轮播
    private long duration = 5000;  // 轮播时间间隔

    public PagerAdapterTest(List<T> dataSet) {
        this.dataSet = dataSet;
        this.carousel = false;
    }

    public PagerAdapterTest(List<T> dataSet, long duration) {
        this.dataSet = dataSet;
        this.duration = duration;
        this.carousel = true;
    }

    @Override
    public int getCount() {
        // TODO
        return 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        // TODO
        return false;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // TODO
        return super.getItemPosition(object);
    }

    // TODO

    public void setDuration(long duration) {
        this.duration = duration;
        this.carousel = true;
    }
}
