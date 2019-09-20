package com.liang.example.viewtest.viewpager;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.liang.example.utils.ApiManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

// TODO: instantiateItem时的Cache

/**
 * 1. 支持轮播
 * 2. 支持动态扩展和删除，但与轮播不兼容，后期考虑兼容实现，即动态轮播，必要时轮播，不轮播时可以动态扩展和删除
 * 3. 支持缓存
 *
 * @param <T>
 */
@SuppressWarnings("unused")
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
    private boolean canChanged;
    private boolean haveChanged;
    private HashMap<Integer, Boolean> changedMap;
    private boolean useCache;

    /* 最基本的PagerAdapter */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder) {
        this(dataSet, pagerAdapterItemHolder, -1, null, false, false);
    }

    /* 支持数据集修改 */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder, ViewPager viewPager, boolean canChanged) {
        this(dataSet, pagerAdapterItemHolder, -1, viewPager, canChanged, false);
    }

    /* 支持轮播 */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder, long duration, ViewPager viewPager) {
        this(dataSet, pagerAdapterItemHolder, duration, viewPager, false, false);
    }

    /* 支持缓存 */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder, boolean useCache) {
        this(dataSet, pagerAdapterItemHolder, -1, null, false, useCache);
    }

    /* 支持轮播/数据集修改/缓存 */
    @SuppressLint("UseSparseArrays")
    public PagerAdapterTest(List<T> dataSet, PagerAdapterTest.PagerAdapterItemHolder<T> pagerAdapterItemHolder, long duration, ViewPager viewPager, boolean canChanged, boolean useCache) {
        this.dataSet = dataSet;
        this.pagerAdapterItemHolder = pagerAdapterItemHolder;
        this.duration = duration;
        this.viewPager = viewPager;
        this.subViews = new SparseArray<>();
        boolean flag = viewPager != null;
        if (flag) {
            this.carousel = duration > 0;
            this.viewPager.addOnPageChangeListener(this);
        }
        this.canChanged = !this.carousel && flag && canChanged;
        if (this.canChanged) {
            this.changedMap = new HashMap<>();
        }
        this.useCache = useCache;
    }

    public void startCarousel() {
        if (!carousel || timer != null) {
            return;
        }
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
        if (!carousel || timer == null) {
            return;
        }
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
            if (position == 0) {
                result = size - 1;
            } else if (position == size + 1) {
                result = 0;
            } else {
                result--;
            }
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
        pagerAdapterItemHolder.destroyItem(container, newPos, object, view);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        int newPos = parsePos(position, "setPrimaryItem");
        pagerAdapterItemHolder.setPrimaryItem(container, newPos, object, subViews.get(position));
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
        return addItem(data, dataSet.size());
    }

    public boolean addItem(T data, int position) {
        if (carousel || !canChanged) {
            return false;
        }
        int oldPos = viewPager.getCurrentItem();
        dataSet.add(position, data);
        for (int i = position; i < dataSet.size(); i++) {
            changedMap.put(i, true);
        }
        notifyDataSetChanged();
        viewPager.setCurrentItem(oldPos >= position ? oldPos + 1 : oldPos, false);
        return true;
    }

    public boolean removeItem(T data) {
        if (carousel || !canChanged) {
            return false;
        }
        int position = dataSet.indexOf(data);
        return position != -1 && removeItem(position);
    }

    public boolean removeItem(int position) {
        if (carousel || !canChanged) {
            return false;
        }
        int oldPos = viewPager.getCurrentItem();
        T result = dataSet.remove(position);
        for (int i = position; i < dataSet.size(); i++) {
            changedMap.put(i, true);
        }
        notifyDataSetChanged();
        if (position == oldPos && oldPos > 0 || oldPos > position) {
            viewPager.setCurrentItem(oldPos - 1, false);
        } else if (oldPos < position) {
            viewPager.setCurrentItem(oldPos, false);
        } else {
            viewPager.setCurrentItem(0, false);
        }
        return true;
    }

    public boolean setItem(T data, int position) {
        if (carousel || !canChanged) {
            return false;
        }
        dataSet.set(position, data);
        changedMap.put(position, true);
        notifyDataSetChanged();
        return true;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        int index = dataSet.indexOf(object);
        if (dataSet.size() == 0 || index == -1) {
            return POSITION_NONE;  // remove
        }
        Boolean flag = changedMap.get(index);
        if (flag != null) {
            changedMap.remove(index);
            return flag ? POSITION_NONE : POSITION_UNCHANGED;
        }
        return pagerAdapterItemHolder.getItemPosition(object, dataSet, subViews);
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

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public void clearCache() {
        subViews.clear();
        changedMap.clear();
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

    public interface PagerAdapterItemHolder<T> {
        View instantiateItem(@NonNull ViewGroup container, int position, T data);

        default boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        default void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object, View vie) {
        }

        default void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object, View view) {
        }

        default void finishUpdate(@NonNull ViewGroup container, List<T> dataSet, SparseArray<View> subViews) {
        }

        default void startUpdate(@NonNull ViewGroup container, List<T> dataSet, SparseArray<View> subViews) {
        }

        default int getItemPosition(@NonNull Object object, List<T> dataSet, SparseArray<View> subViews) {
            return PagerAdapter.POSITION_UNCHANGED;
        }
    }
}
