package com.liang.example.fragmenttest.fragmentbase;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.liang.example.utils.logger.NullableLogger;

public class LoggableFragment extends Fragment {
    public NullableLogger nullableLogger = new NullableLogger();
    public String TAG = "LoggableFragment";

    public LoggableFragment(@LayoutRes int layoutId){
        super(layoutId);
    }

    /**************************************** 生命周期 ****************************************/
    @Override
    public void onAttach(@NonNull Context context) {
        nullableLogger.d(TAG, "onAttach");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        nullableLogger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        nullableLogger.d(TAG, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        nullableLogger.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        nullableLogger.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        nullableLogger.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        nullableLogger.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        nullableLogger.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        nullableLogger.d(TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        nullableLogger.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        nullableLogger.d(TAG, "onDetach");
        super.onDetach();
    }

    /**************************************** 其他监听 ****************************************/
    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        nullableLogger.d(TAG, "onAttachFragment");
        super.onAttachFragment(childFragment);
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        nullableLogger.d(TAG, "onCreateAnimation");
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Nullable
    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        nullableLogger.d(TAG, "onCreateAnimator");
        return super.onCreateAnimator(transit, enter, nextAnim);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        nullableLogger.d(TAG, "onContextItemSelected");
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        nullableLogger.d(TAG, "onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        nullableLogger.d(TAG, "onGetLayoutInflater");
        return super.onGetLayoutInflater(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        nullableLogger.d(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        nullableLogger.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        nullableLogger.d(TAG, "onCreateContextMenu");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        nullableLogger.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyOptionsMenu() {
        nullableLogger.d(TAG, "onDestroyOptionsMenu");
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        nullableLogger.d(TAG, "onHiddenChanged");
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs, @Nullable Bundle savedInstanceState) {
        nullableLogger.d(TAG, "onInflate");
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void onLowMemory() {
        nullableLogger.d(TAG, "onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        nullableLogger.d(TAG, "onMultiWindowModeChanged");
        super.onMultiWindowModeChanged(isInMultiWindowMode);
    }

    @Override
    public void onOptionsMenuClosed(@NonNull Menu menu) {
        nullableLogger.d(TAG, "onOptionsMenuClosed");
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        nullableLogger.d(TAG, "onPictureInPictureModeChanged");
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        nullableLogger.d(TAG, "onPrepareOptionsMenu");
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPrimaryNavigationFragmentChanged(boolean isPrimaryNavigationFragment) {
        nullableLogger.d(TAG, "onPrimaryNavigationFragmentChanged");
        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        nullableLogger.d(TAG, "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        nullableLogger.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nullableLogger.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        nullableLogger.d(TAG, "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
    }
}
