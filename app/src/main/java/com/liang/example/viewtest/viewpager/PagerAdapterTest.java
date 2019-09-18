package com.liang.example.viewtest.viewpager;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.liang.example.utils.ApiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// TODO: instantiateItem时的Cache
// TODO: subViews在有轮播与没轮播的适配
public class PagerAdapterTest<T> extends PagerAdapter/* implements ViewPager.OnPageChangeListener*/ {
    private static final String TAG = "PagerAdapterTest";

    private List<T> dataSet;
    private SparseArray<View> subViews;
    private boolean carousel;  // 是否轮播
    private long duration;  // 轮播时间间隔
    private ViewPager viewPager;  // 轮播需要的ViewPager
    private volatile Timer timer;  // 轮播的timer
    private PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder;

    // private List<OnPageChangeListener> pageChangeListeners;
    // private List<OnPageScrolledListener> pageScrolledListeners;
    // private List<OnPageSelectedListener> pageSelectedListeners;
    // private List<OnPageScrollStateListener> pageScrollStateListeners;

    public PagerAdapterTest(List<T> dataSet, PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder/*, ViewPager viewPager*/) {
        this(dataSet, pagerAdapterItemHolder, -1, /*viewPager*/null);
    }

    public PagerAdapterTest(List<T> dataSet, PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder, long duration, ViewPager viewPager) {
        this.dataSet = dataSet;
        this.pagerAdapterItemHolder = pagerAdapterItemHolder;
        this.duration = duration;
        this.viewPager = viewPager;
        this.subViews = new SparseArray<>();
        this.carousel = duration > 0;

        // viewPager.addOnPageChangeListener(this);
        // pageChangeListeners = new ArrayList<>();
        // pageScrolledListeners = new ArrayList<>();
        // pageSelectedListeners = new ArrayList<>();
        // pageScrollStateListeners = new ArrayList<>();
    }

    public void startCarousel() {
        if (!carousel || timer != null) return;
        synchronized (this) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int currPos = viewPager.getCurrentItem();
                    viewPager.setCurrentItem(currPos == dataSet.size() ? 0 : currPos + 1);
                }
            }, duration, duration);
        }
    }

    public void stopCarousel() {
        if (!carousel || timer == null) return;
        synchronized (this) {
            timer.cancel();
            timer = null;
        }
    }

    public boolean isCarouselRunning() {
        return timer != null;
    }

    @Override
    public int getCount() {
        return carousel ? dataSet.size() + 2 : dataSet.size();
    }

    private int parsePos(int position) {
        int result = position;
        if (carousel) {
            int size = dataSet.size();
            if (position == 0) result = size - 1;
            else if (position == size + 1) result = 0;
            else result--;
        }
        ApiManager.LOGGER.d(TAG, "position: %d, result: %d", position, result);
        return result;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        int newPos = parsePos(position);
        T data = dataSet.get(newPos);
        View view = pagerAdapterItemHolder.instantiateItem(container, newPos, data);
        subViews.put(newPos, view);
        container.addView(view);
        return data;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return pagerAdapterItemHolder.isViewFromObject(view, object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        position = parsePos(position);
        View view = subViews.remove(position);
        container.removeView(view);
        pagerAdapterItemHolder.destroyItem(container, position, object, view, dataSet.get(position));
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        int newPos = parsePos(position);
        pagerAdapterItemHolder.setPrimaryItem(container, newPos, object, dataSet.get(newPos));
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        pagerAdapterItemHolder.finishUpdate(container, dataSet, subViews);
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        pagerAdapterItemHolder.startUpdate(container, dataSet, subViews);
    }

    public void setDuration(long duration) {
        this.duration = duration;
        this.carousel = true;
    }

    // @Override
    // public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    //     for (OnPageChangeListener pageChangeListener : pageChangeListeners) {
    //         pageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    //     }
    //     for (OnPageScrolledListener pageScrolledListener : pageScrolledListeners) {
    //         pageScrolledListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    //     }
    // }
    //
    // @Override
    // public void onPageSelected(int position) {
    //     for (OnPageChangeListener pageChangeListener : pageChangeListeners) {
    //         pageChangeListener.onPageSelected(position);
    //     }
    //     for (OnPageSelectedListener pageSelectedListener : pageSelectedListeners) {
    //         pageSelectedListener.onPageSelected(position);
    //     }
    // }
    //
    // @Override
    // public void onPageScrollStateChanged(int state) {
    //     for (OnPageChangeListener pageChangeListener : pageChangeListeners) {
    //         pageChangeListener.onPageScrollStateChanged(state);
    //     }
    //     for (OnPageScrollStateListener pageScrollStateListener : pageScrollStateListeners) {
    //         pageScrollStateListener.onPageScrollStateChanged(state);
    //     }
    // }
    //
    // public boolean addPageChangeListener(OnPageChangeListener pageChangeListener) {
    //     return pageChangeListeners.add(pageChangeListener);
    // }
    //
    // public boolean removePageChangeListener(OnPageChangeListener pageChangeListener) {
    //     return pageChangeListeners.remove(pageChangeListener);
    // }
    //
    // public boolean addPageScrolledListener(OnPageScrolledListener pageScrolledListener) {
    //     return pageScrolledListeners.add(pageScrolledListener);
    // }
    //
    // public boolean removePageScrolledListener(OnPageScrolledListener pageScrolledListener) {
    //     return pageScrolledListeners.remove(pageScrolledListener);
    // }
    //
    // public boolean addPageSelectedListener(OnPageSelectedListener pageSelectedListener) {
    //     return pageSelectedListeners.add(pageSelectedListener);
    // }
    //
    // public boolean removePageSelectedListener(OnPageSelectedListener pageSelectedListener) {
    //     return pageSelectedListeners.remove(pageSelectedListener);
    // }
    //
    // public boolean addPageScrollStateListener(OnPageScrollStateListener pageScrollStateListener) {
    //     return pageScrollStateListeners.add(pageScrollStateListener);
    // }
    //
    // public boolean removePageScrollStateListener(OnPageScrollStateListener pageScrollStateListener) {
    //     return pageScrollStateListeners.remove(pageScrollStateListener);
    // }

    public interface PagerAdapterItemHolder<T> {
        View instantiateItem(@NonNull ViewGroup container, int position, T data);

        default boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        default void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object, View view, T data) {
        }

        default void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object, T data) {
        }

        default void finishUpdate(@NonNull ViewGroup container, List<T> dataSet, List<View> subViews) {
        }

        default void startUpdate(@NonNull ViewGroup container, List<T> dataSet, List<View> subViews) {
        }
    }

    // public interface OnPageChangeListener {
    //     default void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    //     }
    //
    //     default void onPageSelected(int position) {
    //     }
    //
    //     default void onPageScrollStateChanged(int state) {
    //     }
    // }
    //
    // public interface OnPageScrolledListener {
    //     void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
    // }
    //
    // public interface OnPageSelectedListener {
    //     void onPageSelected(int position);
    // }
    //
    // public interface OnPageScrollStateListener {
    //     void onPageScrollStateChanged(int state);
    // }
}
