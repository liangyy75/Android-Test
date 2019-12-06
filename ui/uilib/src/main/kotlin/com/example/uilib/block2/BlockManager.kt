package com.example.uilib.block2

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.CallSuper
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.reactivex.Scheduler

@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate", "unused", "LeakingThis")
open class BlockManager : BlockGroup {
    var mFragment: Fragment? = null
    var mActivity: Activity? = null
    val FRAGMENT_TAG_LIFECYCLE = "lifecycle"
    var lifecycleFragment: LifecycleFragment? = null
    var scheduler: Scheduler? = null

    var state: LifecycleFragment.State = LifecycleFragment.State.Idle

    constructor(fragment: Fragment) : super() {
        mFragment = fragment
        mContext = fragment.context
        mWhiteBoard = WhiteBoard()
        if (mFragment!!.activity != null) {
            mWhiteBoard!!.putIntent(mFragment!!.activity?.intent)
        }
        mWhiteBoard!!.putBundle(mFragment!!.arguments)
    }

    constructor(activity: FragmentActivity) : super() {
        mActivity = activity
        mContext = activity
        mWhiteBoard = WhiteBoard()
        mWhiteBoard!!.putIntent(mActivity!!.intent)
    }

    constructor(blockManager: BlockManager) : super() {
        mFragment = blockManager.mFragment
        mActivity = blockManager.mActivity
        mContext = blockManager.mContext
        mWhiteBoard = blockManager.mWhiteBoard
    }

    // 提供给注入用
    fun putAll(vararg payload: Any?) {
        if (mWhiteBoard != null) {
            for (element in payload) {
                mWhiteBoard!!.putData(element)
            }
        }
    }

    val LinearLayout_H = -1
    val LinearLayout_V = -2
    val FrameLayout = -3
    val ScrollView = -4

    /**
     * 从资源获得View，并且切割
     *
     *
     * * @param resId the resourceId, either [.LinearLayout_H],
     * [.LinearLayout_V],[.FrameLayout],[.ScrollView]
     */
    open fun build(resId: Int): View? {
        if (mView != null) {
            if (mView!!.parent != null) {
                (mView!!.parent as ViewGroup).removeView(mView)
            }
            ensureLifecycle()
            return mView!!
        }
        when (resId) {
            LinearLayout_V -> {
                val linearLayoutV = LinearLayout(mContext)
                linearLayoutV.orientation = LinearLayout.VERTICAL
                initBlock(linearLayoutV, linearLayoutV)
            }
            LinearLayout_H -> {
                val linearLayoutH = LinearLayout(mContext)
                initBlock(linearLayoutH, linearLayoutH)
            }
            FrameLayout -> {
                val frameLayout = FrameLayout(mContext!!)
                initBlock(frameLayout, frameLayout)
            }
            ScrollView -> {
                val scrollView = ScrollView(mContext)
                val linearLayout_V = LinearLayout(mContext)
                linearLayout_V.orientation = LinearLayout.VERTICAL
                scrollView.addView(linearLayout_V,
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                mContainer = linearLayout_V
                initBlock(scrollView, linearLayout_V)
            }
            else -> {
                val resView = LayoutInflater.from(mContext).inflate(resId, null, false) as ViewGroup
                initBlock(resView, resView)
            }
        }
        return mView
    }

    open fun build(view: ViewGroup): View? {
        if (mView != null) {
            if (mView!!.parent != null) {
                (mView!!.parent as ViewGroup).removeView(mView)
            }
            ensureLifecycle()
            return mView
        }
        initBlock(view, view)
        return mView
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        lifecycleFragment?.startActivityForResult(intent, requestCode, options)
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        lifecycleFragment?.startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getFragment<Fragment>()?.onActivityResult(requestCode, resultCode, data)
    }

    open fun initBlock(rootView: ViewGroup, container: ViewGroup) {
        mView = rootView
        mContainer = container
        create(null, mContext!!, mWhiteBoard!!, LayoutInflater.from(mContext), this)
        ensureLifecycle()
    }

    open fun ensureLifecycle() { // 生命周期
        val lifecycle: FragmentManager? = fragmentManager
        if (lifecycle != null) {
            lifecycleFragment = lifecycle.findFragmentByTag(FRAGMENT_TAG_LIFECYCLE) as LifecycleFragment
            if (lifecycleFragment == null) {
                lifecycleFragment = LifecycleFragment()
                lifecycle.beginTransaction().add(lifecycleFragment!!, FRAGMENT_TAG_LIFECYCLE).commitNowAllowingStateLoss()
            }
            lifecycleFragment!!.addModuleManager(this)
        }
    }

    override fun <T : FragmentActivity> getActivity(): T? {
        if (mActivity == null && mFragment != null) {
            mActivity = mFragment!!.activity
        }
        return if (mActivity == null) null else mActivity as T
    }

    override fun <T : Fragment> getFragment(): T? {
        return if (mFragment == null) null else mFragment!! as T
    }

    override val fragmentManager: FragmentManager?
        get() {
            if (mFragment != null) {
                try {
                    return mFragment!!.childFragmentManager
                } catch (e: Exception) {
                }
                return mFragment!!.fragmentManager
            }
            return if (mActivity != null && mActivity is FragmentActivity) {
                (mActivity as FragmentActivity).supportFragmentManager
            } else null
        }

    open fun runAsync(supplier: Callable<View?>, consumer: Consumer<View?>) {
        scheduler?.scheduleDirect {
            val view = supplier.call()
            mHandler.post {
                if (state.isAlive) {
                    consumer.accept(view)
                }
            }
        } ?: consumer.accept(supplier.call())
    }

    @CallSuper
    override fun onCreate(): Boolean {
        val result: Boolean = super.onCreate()
        state = LifecycleFragment.State.Create
        return result
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        state = LifecycleFragment.State.Start
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        state = LifecycleFragment.State.Resume
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        state = LifecycleFragment.State.Pause
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        state = LifecycleFragment.State.Stop
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        state = LifecycleFragment.State.Destroy
    }

    override fun createAsync(): Boolean {
        return false
    }
}
