package com.liang.example.viewtest.viewpager;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.liang.example.androidtest.R;
import com.liang.example.utils.ApiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 1. 功能
 * 1.1 支持数据集
 * 1.2 支持轮播
 * 1.3 支持动态扩展和删除  TODO: 动态修改与轮播兼容
 * 1.4 支持缓存  TODO: 动态修改时不需要重新生成一遍收到影响的所有page
 * 1.5 支持懒加载与预加载(PagerAdapter特性)
 * 1.6 支持Controller：一个Pager对应一个Controller
 * 2. bug
 * 2.1 2019-09-22: TODO subViews出错
 *
 * @param <T> 数据集中数据的类型，不需要时可以用 {@link Void}
 */
@SuppressWarnings("unused")
public class PagerAdapterTest<T> extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private static final String TAG = "PagerAdapterTest";

    private List<T> dataSet;
    private SparseArray<View> subViews;
    private PagerAdapterHolder<T> pagerAdapterHolder;

    private boolean carousel;  // 是否轮播
    private long duration;  // 轮播时间间隔
    private ViewPager viewPager;  // 轮播需要的ViewPager
    private volatile Timer timer;  // 轮播的timer
    private int pageChangePos = -1;  // pager当前位置

    private boolean canChanged;
    private HashMap<Integer, Boolean> changedMap;

    private boolean useCache;
    private SparseArray<View> cachedViews;

    private boolean useController;
    private ControllerHolder<T> controllerHolder;
    private List<View> controllers;

    /**************************************** 构造函数 ****************************************/
    /* 最基本的PagerAdapter */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterHolder<T> pagerAdapterHolder) {
        this(dataSet, pagerAdapterHolder, -1, null, false, false, false, null);
    }

    /* 支持数据集修改 */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterHolder<T> pagerAdapterHolder, ViewPager viewPager, boolean canChanged) {
        this(dataSet, pagerAdapterHolder, -1, viewPager, canChanged, false, false, null);
    }

    /* 支持轮播 */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterHolder<T> pagerAdapterHolder, long duration, ViewPager viewPager) {
        this(dataSet, pagerAdapterHolder, duration, viewPager, false, false, false, null);
    }

    /* 支持缓存 */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterHolder<T> pagerAdapterHolder, boolean useCache) {
        this(dataSet, pagerAdapterHolder, -1, null, false, useCache, false, null);
    }

    /* 支持控制器 */
    public PagerAdapterTest(List<T> dataSet, PagerAdapterHolder<T> pagerAdapterHolder, ViewPager viewPager, boolean useController, ControllerHolder<T> controllerHolder) {
        this(dataSet, pagerAdapterHolder, -1, viewPager, false, false, useController, controllerHolder);
    }

    /* 支持轮播/数据集修改/缓存/控制器 */
    @SuppressLint("UseSparseArrays")
    public PagerAdapterTest(List<T> dataSet, PagerAdapterHolder<T> pagerAdapterHolder, long duration, ViewPager viewPager, boolean canChanged, boolean useCache,
                            boolean useController, ControllerHolder<T> controllerHolder) {
        this.dataSet = dataSet;
        this.pagerAdapterHolder = pagerAdapterHolder;
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
        setUseCache(useCache);
        setUseController(useController, controllerHolder);
    }

    /**************************************** page的生成与销毁 ****************************************/
    @Override
    public int getCount() {
        return carousel ? dataSet.size() + 2 : dataSet.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        int newPos = parsePos(position, "instantiateItem");
        T data = dataSet.get(newPos);
        View view;
        if (useCache && cachedViews.get(position) != null) {
            view = cachedViews.get(position);
            ApiManager.LOGGER.d(TAG, "instantiateItem -- use cachedView");
        } else {
            view = pagerAdapterHolder.instantiateItem(container, newPos, data);
            ApiManager.LOGGER.d(TAG, "instantiateItem -- create newView");
            if (useCache) {
                cachedViews.put(position, view);
            }
        }
        view.setTag(R.id.PAGE_TAG_KEY, data);
        subViews.put(position, view);
        container.addView(view);
        return data;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        ApiManager.LOGGER.d(TAG, "isViewFromObject");
        return pagerAdapterHolder.isViewFromObject(view, object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        int newPos = parsePos(position, "destroyItem");
        View view = subViews.get(position);
        subViews.remove(position);
        container.removeView(view);
        pagerAdapterHolder.destroyItem(container, newPos, object, view);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        int newPos = parsePos(position, "setPrimaryItem");
        pagerAdapterHolder.setPrimaryItem(container, newPos, object, subViews.get(position));
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        ApiManager.LOGGER.d(TAG, "finishUpdate");
        pagerAdapterHolder.finishUpdate(container, dataSet, subViews);
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        ApiManager.LOGGER.d(TAG, "startUpdate");
        pagerAdapterHolder.startUpdate(container, dataSet, subViews);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        int newPos = parsePos(position, "getPageTitle");
        return pagerAdapterHolder.getPageTitle(position, subViews.get(position), dataSet.get(newPos));
    }

    @Override
    public float getPageWidth(int position) {
        int newPos = parsePos(position, "getPageWidth");
        return pagerAdapterHolder.getPageWidth(position, subViews.get(position), dataSet.get(newPos));
    }

    @Nullable
    @Override
    public Parcelable saveState() {
        ApiManager.LOGGER.d(TAG, "saveState");
        return pagerAdapterHolder.saveState(dataSet);
    }

    @Override
    public void restoreState(@Nullable Parcelable state, @Nullable ClassLoader loader) {
        ApiManager.LOGGER.d(TAG, "restoreState");
        pagerAdapterHolder.restoreState(state, loader, dataSet);
    }

    /**************************************** 数据集动态修改相关 ****************************************/
    public boolean addItem(T data) {
        return addItem(data, dataSet.size());
    }

    public boolean addItem(T data, int position) {
        if (carousel || !canChanged) {
            return false;
        }
        ApiManager.LOGGER.d(TAG, "addItem(data: %s, position: %d)", data, position);
        if (useController) {
            addController(position);
        }
        int oldPos = viewPager.getCurrentItem();
        int size = dataSet.size();
        dataSet.add(position, data);
        // for (int i = position; i < size; i++) {
        //     changedMap.put(i, true);
        //     cachedViews.put(i + 1, cachedViews.get(i));
        // }
        // cachedViews.remove(position);
        // changedMap.put(size, true);
        if (useCache) {
            for (int i = position; i < size; i++) {
                changedMap.put(i, true);
                cachedViews.remove(i);
            }
            changedMap.put(size, true);
        } else {
            for (int i = position; i <= size; i++) {
                changedMap.put(i, true);
            }
        }
        notifyDataSetChanged();
        viewPager.setCurrentItem(oldPos >= position ? oldPos + 1 : oldPos, false);
        return true;
    }

    public boolean removeItem(T data) {
        int position = dataSet.indexOf(data);
        return position != -1 && removeItem(position).equals(data);
    }

    public T removeItem(int position) {
        if (carousel || !canChanged) {
            return null;
        }
        ApiManager.LOGGER.d(TAG, "removeItem(position: %d)", position);
        if (useController) {
            removeController(position);
        }
        int oldPos = viewPager.getCurrentItem();
        T result = dataSet.remove(position);
        // int size = dataSet.size();
        // for (int i = position; i < size; i++) {
        //     changedMap.put(i, true);
        //     cachedViews.put(i, cachedViews.get(i + 1));
        // }
        // cachedViews.remove(size);
        int size = dataSet.size();
        if (useCache) {
            for (int i = position; i < size; i++) {
                changedMap.put(i, true);
                cachedViews.remove(i);
            }
            cachedViews.remove(size);
        } else {
            for (int i = position; i < size; i++) {
                changedMap.put(i, true);
            }
        }
        notifyDataSetChanged();
        if (position == oldPos && oldPos > 0 || oldPos > position) {
            viewPager.setCurrentItem(oldPos - 1, false);
        } else if (oldPos < position) {
            viewPager.setCurrentItem(oldPos, false);
        } else {
            viewPager.setCurrentItem(0, false);
        }
        return result;
    }

    public boolean setItem(T data, int position) {
        if (carousel || !canChanged) {
            return false;
        }
        dataSet.set(position, data);
        if (useCache) {
            cachedViews.remove(position);
        }
        changedMap.put(position, true);
        notifyDataSetChanged();
        return true;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        int result = POSITION_UNCHANGED;
        int index = dataSet.indexOf(object);
        if (dataSet.size() == 0 || index == -1) {
            result = POSITION_NONE;
        } else {
            Boolean flag = changedMap.get(index);
            if (flag != null) {
                changedMap.remove(index);
                result = flag ? POSITION_NONE : POSITION_UNCHANGED;
            }
        }
        if (result == POSITION_UNCHANGED) {
            result = pagerAdapterHolder.getItemPosition(object, dataSet, subViews);
        }
        ApiManager.LOGGER.d(TAG, "getItemPosition(object: %s) -- result: %d", object, result);
        return result;
    }

    public int getSize() {
        return dataSet.size();
    }

    /**************************************** 缓存相关 ****************************************/
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
        ApiManager.LOGGER.d(TAG, "setUseCache(useCache: %s)", String.valueOf(useCache));
        if (!useCache && cachedViews != null) {
            cachedViews.clear();
            cachedViews = null;
        } else {
            cachedViews = new SparseArray<>();
        }
    }

    public void clearCache() {
        if (useCache) {
            cachedViews.clear();
        }
    }

    public SparseArray<View> getCachedViews() {
        return cachedViews;
    }

    public SparseArray<View> getSubViews() {
        return subViews;
    }

    /**************************************** 轮播相关 ****************************************/
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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        pageChangePos = position;
    }

    @Override
    public void onPageSelected(int position) {
        pageChangePos = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
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

    /**************************************** 控制器相关 ****************************************/
    public void setUseController(boolean useController, ControllerHolder<T> controllerHolder) {
        ApiManager.LOGGER.d(TAG, "setUseController(useController: %s)", String.valueOf(useController));
        this.useController = useController;
        this.controllerHolder = controllerHolder;
        if (this.useController) {
            controllers = new ArrayList<>();
            int size = dataSet.size();
            int key = R.id.CONTROLLER_TAG_KEY;
            for (int i = 0; i < size; i++) {
                View controller = this.controllerHolder.getController(i, dataSet.get(i));
                controller.setTag(key, carousel ? i + 1 : i);
                controller.setOnClickListener((v) -> viewPager.setCurrentItem((Integer) v.getTag(key)));
                this.controllers.add(controller);
            }
        } else {
            clearControllers();
        }
    }

    public void clearControllers() {
        if (controllers != null) {
            ApiManager.LOGGER.d(TAG, "clearControllers");
            int size = dataSet.size();
            int key = R.id.CONTROLLER_TAG_KEY;
            for (int i = 0; i < size; i++) {
                View view = controllers.get(i);
                view.setOnClickListener(null);
                view.setTag(key, null);
                this.controllerHolder.removeController(i, dataSet.get(i), view);
            }
            controllers.clear();
            controllers = null;
            useController = false;
        }
    }

    private void addController(int position) {
        int size = dataSet.size();
        int key = R.id.CONTROLLER_TAG_KEY;
        for (int i = position; i < size; i++) {
            controllers.get(i).setTag(key, i + 1);
        }
        View controller = controllerHolder.getController(position, dataSet.get(position));
        controller.setTag(key, carousel ? position + 1 : position);
        controller.setOnClickListener((v) -> viewPager.setCurrentItem((Integer) v.getTag(key)));
        controllers.add(position, controller);
    }

    private void removeController(int position) {
        int size = dataSet.size();
        int key = R.id.CONTROLLER_TAG_KEY;
        for (int i = position + 1; i < size; i++) {
            controllers.get(i).setTag(key, i - 1);
        }
        View view = controllers.remove(position);
        view.setOnClickListener(null);
        view.setTag(key, null);
        controllerHolder.removeController(position, dataSet.get(position), view);
    }

    public interface ControllerHolder<T> {
        View getController(int index, T data);

        default void removeController(int index, T data, View view) {
        }
    }

    /**************************************** 委托接口 ****************************************/
    public interface PagerAdapterHolder<T> {
        View instantiateItem(@NonNull ViewGroup container, int position, T data);

        default boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            int key = R.id.PAGE_TAG_KEY;
            return view == object || (view.getTag(key) != null && view.getTag(key).equals(object));
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

        default CharSequence getPageTitle(int position, View view, T data) {
            return null;
        }

        default float getPageWidth(int position, View view, T data) {
            return 1.f;
        }

        default Parcelable saveState(List<T> dataSet) {
            return null;
        }

        default void restoreState(@Nullable Parcelable state, @Nullable ClassLoader loader, List<T> dataSet) {
        }
    }
}
