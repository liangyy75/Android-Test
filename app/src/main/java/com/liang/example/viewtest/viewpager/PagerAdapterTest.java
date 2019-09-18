package com.liang.example.viewtest.viewpager;

import android.database.DataSetObserver;
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
// TODO: add/remove以及instantiateItem/destroyItem的配合
public class PagerAdapterTest<T> extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private static final String TAG = "PagerAdapterTest";

    private List<T> dataSet;
    private SparseArray<View> subViews;
    private boolean carousel;  // 是否轮播
    private long duration;  // 轮播时间间隔
    private ViewPager viewPager;  // 轮播需要的ViewPager
    private volatile Timer timer;  // 轮播的timer
    private PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder;
    private int pageChangePos = -1;

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
        if (viewPager != null) {
            this.viewPager.addOnPageChangeListener(this);
        }
        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                // TODO: add/remove以及instantiateItem/destroyItem的配合
            }
        });

        // pageChangeListeners = new ArrayList<>();
        // pageScrolledListeners = new ArrayList<>();
        // pageSelectedListeners = new ArrayList<>();
        // pageScrollStateListeners = new ArrayList<>();
    }

    public void startCarousel() {
        if (!carousel || timer != null) return;
        ApiManager.LOGGER.d(TAG, "startCarousel begin");
        synchronized (this) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    viewPager.post(() -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
                }
            }, duration, duration);
        }
        ApiManager.LOGGER.d(TAG, "startCarousel end");
    }

    public void stopCarousel() {
        if (!carousel || timer == null) return;
        ApiManager.LOGGER.d(TAG, "stopCarousel start");
        synchronized (this) {
            timer.cancel();
            timer = null;
        }
        ApiManager.LOGGER.d(TAG, "stopCarousel stop");
    }

    public boolean isCarouselRunning() {
        return timer != null;
    }

    public int getFirstItemPos() {
        return carousel ? 1 : 0;
    }

    @Override
    public int getCount() {
        return carousel ? dataSet.size() + 2 : dataSet.size();
    }

    private int parsePos(int position, String tag) {
        int result = position;
        if (carousel) {
            int size = dataSet.size();
            if (position == 0) result = size - 1;
            else if (position == size + 1) result = 0;
            else result--;
        }
        ApiManager.LOGGER.d(TAG, "parse position: %d, result: %d -- %s", position, result, tag);
        return result;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        int newPos = parsePos(position, "instantiateItem");
        T data = dataSet.get(newPos);
        View view = pagerAdapterItemHolder.instantiateItem(container, newPos, data);
        subViews.put(position, view);
        container.addView(view);
        return data;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        ApiManager.LOGGER.d(TAG, "isViewFromObject");
        return pagerAdapterItemHolder.isViewFromObject(view, object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        int newPos = parsePos(position, "destroyItem");
        View view = subViews.get(position);
        subViews.remove(position);
        container.removeView(view);
        pagerAdapterItemHolder.destroyItem(container, newPos, object, view, dataSet.get(newPos));
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        int newPos = parsePos(position, "setPrimaryItem");
        pagerAdapterItemHolder.setPrimaryItem(container, newPos, object, subViews.get(position), dataSet.get(newPos));
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        ApiManager.LOGGER.d(TAG, "finishUpdate");
        pagerAdapterItemHolder.finishUpdate(container, dataSet, subViews);
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        ApiManager.LOGGER.d(TAG, "startUpdate");
        pagerAdapterItemHolder.startUpdate(container, dataSet, subViews);
    }

    public int getSize() {
        return dataSet.size();
    }

    public boolean addItem(T data) {
        boolean result = dataSet.add(data);
        notifyDataSetChanged();
        return result;
    }

    public void addItem(T data, int position) {
        dataSet.add(position, data);
        notifyDataSetChanged();
    }

    public boolean removeItem(T data) {
        boolean result = dataSet.remove(data);
        notifyDataSetChanged();
        return result;
    }

    public T removeItem(int position) {
        T result = dataSet.remove(position);
        notifyDataSetChanged();
        return result;
    }

    public void setDuration(long duration, boolean restart) {
        if (!carousel) {
            ApiManager.LOGGER.d(TAG, "setDuration: %d -- failed", duration);
            return;
        }
        ApiManager.LOGGER.d(TAG, "setDuration: %d -- successfully", duration);
        this.duration = duration;
        carousel = true;
        if (restart) {
            stopCarousel();
            startCarousel();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // for (OnPageChangeListener pageChangeListener : pageChangeListeners) {
        //     pageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        // }
        // for (OnPageScrolledListener pageScrolledListener : pageScrolledListeners) {
        //     pageScrolledListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        // }
        pageChangePos = position;
    }

    @Override
    public void onPageSelected(int position) {
        // for (OnPageChangeListener pageChangeListener : pageChangeListeners) {
        //     pageChangeListener.onPageSelected(position);
        // }
        // for (OnPageSelectedListener pageSelectedListener : pageSelectedListeners) {
        //     pageSelectedListener.onPageSelected(position);
        // }
        pageChangePos = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // for (OnPageChangeListener pageChangeListener : pageChangeListeners) {
        //     pageChangeListener.onPageScrollStateChanged(state);
        // }
        // for (OnPageScrollStateListener pageScrollStateListener : pageScrollStateListeners) {
        //     pageScrollStateListener.onPageScrollStateChanged(state);
        // }
        if (carousel && state == ViewPager.SCROLL_STATE_IDLE) {
            int count = dataSet.size();
            ApiManager.LOGGER.d(TAG, "adjust pageChangePos: %d", pageChangePos);
            if (pageChangePos == count + 1) {
                viewPager.setCurrentItem(1, false);
            } else if (pageChangePos == 0) {
                viewPager.setCurrentItem(count, false);
            }
        }
    }

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

        default void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object, View view, T data) {
        }

        default void finishUpdate(@NonNull ViewGroup container, List<T> dataSet, SparseArray<View> subViews) {
        }

        default void startUpdate(@NonNull ViewGroup container, List<T> dataSet, SparseArray<View> subViews) {
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
