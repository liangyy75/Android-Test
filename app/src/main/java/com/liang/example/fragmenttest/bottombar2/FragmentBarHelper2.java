package com.liang.example.fragmenttest.bottombar2;

import android.content.Context;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedDispatcher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.liang.example.utils.ApiManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentBarHelper2<T> {
    private static final String TAG = "FragmentBarHelper2";
    private static Field field;

    private FragmentActivity activity;
    private int containerId;
    private ControllerCreator<T> controllerCreator;
    private List<T> dataSet;
    private List<View> buttons;
    private FragmentCreator fragmentCreator;
    private List<FragmentWrapper> fragmentWrappers;
    private int lastPosition = -1;
    // private boolean slide;
    private FragmentSwitchListener switchListener;
    private FragmentBackListener backListener;
    private List<Integer> backStack;

    static {
        Class clazz = ComponentActivity.class;
        for (int i = 0; i < 3; i++) {
            try {
                field = clazz.getDeclaredField("mOnBackPressedDispatcher");
                field.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    public FragmentBarHelper2(FragmentActivity activity, int containerId, List<T> dataSet, ControllerCreator<T> controllerCreator, FragmentCreator fragmentCreator, boolean lazy/*, boolean slide*/) {
        this.activity = activity;
        this.backStack = new ArrayList<>();
        this.containerId = containerId;
        this.dataSet = dataSet;
        this.controllerCreator = controllerCreator;
        this.fragmentCreator = fragmentCreator;
        // this.slide = slide;
        if (!lazy) {
            this.initButtons().initFragments();
        } else {
            this.initButtons().fillFragments().switchFragment(0);
        }
    }

    public List<View> getButtons() {
        return buttons;
    }

    public List<FragmentWrapper> getFragmentWrappers() {
        return fragmentWrappers;
    }

    public List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<>(fragmentWrappers.size());
        for (FragmentWrapper fragmentWrapper : fragmentWrappers) {
            fragments.add(fragmentWrapper.fragment);
        }
        return fragments;
    }

    public int getPosition() {
        return lastPosition;
    }

    public FragmentBarHelper2<T> changePosition(int position) {
        this.lastPosition = position;
        return this;
    }

    public List<Integer> getBackStack() {
        return backStack;
    }

    public FragmentBarHelper2<T> initButtons() {
        int len = dataSet.size();
        buttons = new ArrayList<>(len);
        fragmentWrappers = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            View button = controllerCreator.getController(i, activity, dataSet.get(i));
            button.setTag(i);
            button.setOnClickListener((v) -> switchFragment((int) v.getTag()));
            buttons.add(button);
        }
        ApiManager.LOGGER.d(TAG, "initButtons: %d", len);
        return this;
    }

    public FragmentBarHelper2<T> setSwitchListener(FragmentSwitchListener switchListener) {
        this.switchListener = switchListener;
        return this;
    }

    public FragmentBarHelper2<T> setBackListener(FragmentBackListener backListener) {
        this.backListener = backListener;
        return this;
    }

    public FragmentBarHelper2<T> initFragments() {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        int len = buttons.size();
        for (int i = 0; i < len; i++) {
            FragmentWrapper fragmentWrapper = fragmentCreator.getFragment(i);
            Fragment fragment = fragmentWrapper.fragment;
            String tag = fragmentWrapper.tag;
            // if (slide) {
            //     makeSlideFragment(fragment);
            // }
            transaction.add(containerId, fragment, tag);
            if (i != 0) {
                transaction.hide(fragment);
            } else {
                lastPosition = 0;
            }
            fragmentWrappers.add(fragmentWrapper);
        }
        transaction.commit();
        ApiManager.LOGGER.d(TAG, "initFragments");
        return this;
    }

    private void makeSlideFragment(Fragment fragment) {
        // View.OnTouchListener dragListener = new View.OnTouchListener() {
        //     float lastX, lastY;
        //
        //     @Override
        //     public boolean onTouch(View v, MotionEvent event) {
        //         v.performClick();
        //         int action = event.getAction();
        //         if (action == MotionEvent.ACTION_DOWN) {
        //             lastX = event.getX();
        //             lastY = event.getY();
        //             return false;
        //         }
        //         float disX = event.getX() - lastX;
        //         float disY = event.getY() - lastY;
        //         if (action == MotionEvent.ACTION_MOVE) {
        //             // TODO:
        //         } else if (action == MotionEvent.ACTION_UP) {
        //         }
        //         return false;
        //     }
        // };
        // fragment.getViewLifecycleOwner().getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
        //     if (event == Lifecycle.Event.ON_RESUME) {
        //         Objects.requireNonNull(fragment.getView()).setOnTouchListener(dragListener);
        //     } else if (event == Lifecycle.Event.ON_PAUSE) {
        //         Objects.requireNonNull(fragment.getView()).setOnTouchListener(null);
        //     }
        // });
    }

    public FragmentBarHelper2<T> fillFragments() {
        int len = buttons.size();
        for (int i = 0; i < len; i++) {
            fragmentWrappers.add(new FragmentWrapper());
        }
        ApiManager.LOGGER.d(TAG, "fillFragments");
        return this;
    }

    public FragmentBarHelper2<T> switchFragment(int newPosition) {
        return this.switchFragment(newPosition, true);
    }

    public FragmentBarHelper2<T> switchFragment(int newPosition, boolean back) {
        if (newPosition == lastPosition) return this;
        ApiManager.LOGGER.d(TAG, "switchFragment -- newPos: %d, lastPosition: %d", newPosition, lastPosition);
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        if (lastPosition != -1) {
            transaction.hide(fragmentWrappers.get(lastPosition).fragment);
            if (back) {
                backStack.add(lastPosition);
            }
        }
        if (switchListener != null && lastPosition != -1) {
            switchListener.switchFragment(lastPosition, newPosition);
        }
        lastPosition = newPosition;
        FragmentWrapper fragmentWrapper = fragmentWrappers.get(lastPosition);
        if (!fragmentWrapper.isFlag()) {
            FragmentWrapper fragmentWrapper1 = fragmentCreator.getFragment(lastPosition);
            fragmentWrapper.fragment = fragmentWrapper1.fragment;
            fragmentWrapper.tag = fragmentWrapper1.tag;
            // if (slide) {
            //     makeSlideFragment(fragmentWrapper.fragment);
            // }
            transaction.add(containerId, fragmentWrapper.fragment, fragmentWrapper.tag);
        }
        transaction.show(fragmentWrapper.fragment);
        transaction.commit();
        return this;
    }

    public boolean backFragment() {
        if (backStack.isEmpty()) {
            if (field == null) {
                return false;
            } else {
                try {
                    ((OnBackPressedDispatcher) Objects.requireNonNull(field.get(activity))).onBackPressed();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        int newPos = backStack.remove(backStack.size() - 1);
        if (backListener != null) {
            backListener.backFragment(lastPosition, newPos);
        } else {
            switchFragment(newPos, false);
        }
        return true;
    }

    public interface ControllerCreator<T> {
        View getController(int index, Context context, T data);
    }

    public interface FragmentCreator {
        FragmentWrapper getFragment(int index);
    }

    public interface FragmentSwitchListener {
        void switchFragment(int lastPos, int newPos);
    }

    public interface FragmentBackListener {
        void backFragment(int lastPos, int newPos);
    }

    public static class FragmentWrapper {
        public Fragment fragment;
        public String tag;
        private boolean flag;

        public FragmentWrapper() {
            this.flag = false;
        }

        public FragmentWrapper(Fragment fragment, String tag) {
            this.fragment = fragment;
            this.tag = tag;
            this.flag = true;
        }

        public boolean isFlag() {
            return flag;
        }
    }
}
