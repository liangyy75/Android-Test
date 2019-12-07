package com.example.uilib.block

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

@Suppress("LeakingThis")
open class BlockManager(context: Context) : BlockGroup(context), FragmentLifeCycleInter, ActivityLifeCycleInter {
    companion object {
        const val KEY_FRAGMENT_STATE = "KEY_FRAGMENT_STATE"
        const val KEY_ACTIVITY_STATE = "KEY_ACTIVITY_STATE"
    }

    open var bundle: Bundle? = null

    // init

    init {
        init(WhiteBoard(), WhiteBoard(), RxHandler(), null, this)
    }

    open fun initInActivity(activity: Activity) {
        innerActivity = activity
        inflater = LayoutInflater.from(activity)
        putData(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.ORIGINAL)
    }

    open fun initInFragment(fragment: Fragment) {
        innerActivity = fragment.activity
        innerFragment = fragment
        putData(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.ORIGIN)
    }

    open fun initInBlockManager(blockManager: BlockManager) {
        innerActivity = blockManager.innerActivity
        innerFragment = blockManager.innerFragment
        context = blockManager.context
        if (blockManager.getData(KEY_FRAGMENT_STATE) != null) {
            putData(KEY_FRAGMENT_STATE, blockManager.getData(KEY_FRAGMENT_STATE))
        }
        if (blockManager.getData(KEY_ACTIVITY_STATE) != null) {
            putData(KEY_ACTIVITY_STATE, blockManager.getData(KEY_ACTIVITY_STATE))
        }
    }

    protected var index: Int = -1  // TODO

    override fun onInflateView(context: Context, inflater: LayoutInflater, parent: ViewGroup?): View? {
        view = inflater.inflate(layoutId, null, false)
        parent?.addView(view)
        return view
    }

    open fun build(parent: ViewGroup?, index: Int = -1) {
        // val activityState = getData(KEY_ACTIVITY_STATE)
        // if (activityState != null && (activityState == ActivityLifeCycleInter.ORIGINAL || activityState == ActivityLifeCycleInter.STATE_DESTROY)) {
        //     throw RuntimeException("can't inflate before activity's onCreate or after activity's onDestroy")
        // }
        // val fragmentState = getData(KEY_FRAGMENT_STATE)
        // if (fragmentState != null && (fragmentState == FragmentLifeCycleInter.ORIGIN || fragmentState == FragmentLifeCycleInter.STATE_DETACH)) {
        //     throw RuntimeException("can't inflate before fragment's onAttach or after fragment's onDetach")
        // }
        this.index = index
        inflate(this.context, this.inflater, parent ?: this.parent)
    }

    // activity proxy

    protected var innerActivity: Activity? = null
    protected var innerFragment: Fragment? = null
    override var ai: ActivityInter
        get() = this
        set(_) {
            throw RuntimeException("cann't set ai of blockManager")
        }

    // activity / fragment 的一些功能

    override fun getActivity(): Activity? = if (innerActivity == null && innerFragment != null) {
        innerFragment!!.activity
    } else {
        innerActivity
    }

    override fun getFragment(): Fragment? = innerFragment
    override fun getFragmentManager(): FragmentManager? = getFragmentActivity()?.supportFragmentManager

    override fun getFragmentActivity(): FragmentActivity? = if (innerActivity is FragmentActivity) {
        innerActivity as FragmentActivity
    } else {
        null
    }

    override fun finish() = innerActivity?.finish() ?: Unit
    override fun startActivity(intent: Intent) = innerActivity?.startActivity(intent) ?: Unit
    override fun startActivityForResult(intent: Intent, requestCode: Int) = innerActivity?.startActivityForResult(intent, requestCode)
            ?: Unit

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) = innerActivity?.startActivityForResult(intent, requestCode, options)
            ?: Unit

    // blockGroup -- add / remove / get

    // refresh

    override fun refresh() = refreshManager()
    override fun refreshGroup() = children.forEach { it.refresh() }
    override fun refreshManager() = children.forEach { it.refresh() }

    // lifecycle

    private fun putDataIfExists(key: String, value: Any?) = if (this.swb.getData(key) != null) this.swb.putData(key, value) else Unit

    override fun onAttach(context: Context) {
        this.context = context
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_ATTACH)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onAttach(context) }
    }

    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_CREATE)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onCreate(bundle) }
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_CREATE)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onCreate(bundle) }
    }

    override fun onRestart() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_RESTART)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onRestart() }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View? {
        this.inflater = inflater
        this.bundle = bundle
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_CREATE_VIEW)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onCreateView(inflater, parent, bundle) }
        return this.view
    }

    override fun onActivityCreated(bundle: Bundle?) {
        this.bundle = bundle
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_ACTIVITY_CREATE)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onActivityCreated(bundle) }
    }

    override fun onStart() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_START)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onStart() }
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_START)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onStart() }
    }

    override fun onResume() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_RESUME)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onResume() }
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_RESUME)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onResume() }
    }

    override fun onPause() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_PAUSE)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onPause() }
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_PAUSE)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onPause() }
    }

    override fun onStop() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_STOP)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onStop() }
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_STOP)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onStop() }
    }

    override fun onDestroyView() {
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_DESTROY_VIEW)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDestroyView() }
    }

    override fun onDestroy() {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_DESTROY)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onDestroy() }
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_DESTROY)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDestroy() }
    }

    override fun onDetach() {
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_DETACH)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDetach() }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_SAVE_INSTANCE_STATE)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onSaveInstanceState(bundle) }
        putDataIfExists(KEY_FRAGMENT_STATE, FragmentLifeCycleInter.STATE_SAVE_INSTANCE_STATE)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onSaveInstanceState(bundle) }
    }

    override fun onRestoreInstanceState(bundle: Bundle) {
        this.bundle = bundle
        putDataIfExists(KEY_ACTIVITY_STATE, ActivityLifeCycleInter.STATE_RESTORE_INSTANCE_STATE)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onRestoreInstanceState(bundle) }
    }
}

// block -> fragment
// blockGroup -> fragmentManager
// blockManager -> activity / fragment
// TODO: FragmentBlockManager
// TODO: ActivityBlockManager
