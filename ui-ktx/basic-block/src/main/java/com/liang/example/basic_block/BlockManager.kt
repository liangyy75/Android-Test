package com.liang.example.basic_block

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

@Suppress("LeakingThis")
open class BlockManager : BlockGroup, FragmentLifeCycleInter, ActivityLifeCycleInter {
    open var bundle: Bundle? = null
    open var innerActivity: Activity? = null
    open var innerFragment: Fragment? = null
    override var ai: ActivityInter? = this
    open val activityStateKey: String
        get() = hashCode().toString() + "_activity"
    open val fragmentStateKey: String
        get() = hashCode().toString() + "_fragment"

    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(viewGroup: ViewGroup, strId: String?) : super(viewGroup, strId)

    open fun withActivity(activity: Activity): BlockManager {
        innerActivity = activity
        init(activity, true)
        this.sdc?.putData(activityStateKey, ActivityLifeCycleInter.ORIGINAL)
        return this
    }

    open fun withFragment(fragment: Fragment): BlockManager {
        innerActivity = fragment.activity
        innerFragment = fragment
        init(fragment.context!!, true)
        this.sdc?.putData(fragmentStateKey, FragmentLifeCycleInter.ORIGIN)
        return this
    }

    open fun withBlockManager(blockManager: BlockManager): BlockManager {
        innerActivity = blockManager.innerActivity
        innerFragment = blockManager.innerFragment
        init(blockManager as Block)
        if (blockManager.sdc?.getData(fragmentStateKey) != null) {
            this.sdc?.putData(fragmentStateKey, blockManager.sdc?.getData(fragmentStateKey))
        }
        if (blockManager.sdc?.getData(activityStateKey) != null) {
            this.sdc?.putData(activityStateKey, blockManager.sdc?.getData(activityStateKey))
        }
        return this
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
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) =
            innerActivity?.startActivityForResult(intent, requestCode, options) ?: Unit

    // refresh

    override fun refresh() = refreshManager()
    override fun refreshGroup() = refreshManager()
    override fun refreshManager() {
        refreshTask?.run()
        children.forEach { it.refresh() }
    }

    private fun putDataIfExists(key: String, value: Any?) {
        val data = this.sdc?.getData(key)
        if (data != null && data != value) {
            this.sdc?.putData(key, value, this)
        }
    }

    override fun onAttach(context: Context) {
        this.context = context
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_ATTACH)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onAttach(context) }
    }

    override fun onCreate(bundle: Bundle?) {
        this.bundle = bundle
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_CREATE)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onCreate(bundle) }
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_CREATE)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onCreate(bundle) }
    }

    override fun onRestart() {
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_RESTART)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onRestart() }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View? {
        this.inflater = inflater
        this.bundle = bundle
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_CREATE_VIEW)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onCreateView(inflater, parent, bundle) }
        return this.view
    }

    override fun onActivityCreated(bundle: Bundle?) {
        this.bundle = bundle
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_ACTIVITY_CREATE)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onActivityCreated(bundle) }
    }

    override fun onStart() {
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_START)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onStart() }
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_START)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onStart() }
    }

    override fun onResume() {
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_RESUME)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onResume() }
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_RESUME)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onResume() }
    }

    override fun onPause() {
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_PAUSE)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onPause() }
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_PAUSE)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onPause() }
    }

    override fun onStop() {
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_STOP)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onStop() }
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_STOP)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onStop() }
    }

    override fun onDestroyView() {
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_DESTROY_VIEW)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDestroyView() }
    }

    override fun onDestroy() {
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_DESTROY)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onDestroy() }
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_DESTROY)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDestroy() }
    }

    override fun onDetach() {
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_DETACH)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onDetach() }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_SAVE_INSTANCE_STATE)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onSaveInstanceState(bundle) }
        putDataIfExists(fragmentStateKey, FragmentLifeCycleInter.STATE_SAVE_INSTANCE_STATE)
        children.filterIsInstance<FragmentLifeCycleInter>().forEach { it.onSaveInstanceState(bundle) }
    }

    override fun onRestoreInstanceState(bundle: Bundle) {
        this.bundle = bundle
        putDataIfExists(activityStateKey, ActivityLifeCycleInter.STATE_RESTORE_INSTANCE_STATE)
        children.filterIsInstance<ActivityLifeCycleInter>().forEach { it.onRestoreInstanceState(bundle) }
    }

    // xml / json

    override fun toJson(): String {
        return super.toJson()
    }

    override fun fromJson(jsonStr: String): BlockManager {
        TODO()
        return this
    }

    override fun toXml(): String {
        return super.toXml()
    }

    override fun fromXml(xmlStr: String): BlockManager {
        TODO()
        return this
    }
}

open class BlockActivity : AppCompatActivity() {
    protected open val useSameRes = true
    protected open val blockManagers = mutableListOf<BlockManager>()

    protected open fun getBlockManagerList(): List<BlockManager>? = null

    @SuppressLint("CI_ByteDanceKotlinRules_Not_Allow_findViewById_Invoked_In_UI")
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        val container = window.decorView.findViewById<FrameLayout>(android.R.id.content)
        getBlockManagerList()?.let { blockManagers.addAll(it) }
        blockManagers.forEachIndexed { index, it ->
            if (useSameRes) {
                if (index == 0) {
                    it.withActivity(this)
                } else {
                    it.withBlockManager(blockManagers[0])
                }
            } else {
                it.withActivity(this)
            }
            it.inflate(container)
            it.onCreate(bundle)
        }
    }

    override fun onRestart() {
        super.onRestart()
        blockManagers.forEach { it.onRestart() }
    }

    override fun onStart() {
        super.onStart()
        blockManagers.forEach { it.onStart() }
    }

    override fun onResume() {
        super.onResume()
        blockManagers.forEach { it.onResume() }
    }

    override fun onPause() {
        super.onPause()
        blockManagers.forEach { it.onPause() }
    }

    override fun onStop() {
        super.onStop()
        blockManagers.forEach { it.onStop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        blockManagers.forEach { it.onDestroy() }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        blockManagers.forEach { it.onSaveInstanceState(bundle) }
    }

    override fun onRestoreInstanceState(bundle: Bundle) = blockManagers.forEach { it.onRestoreInstanceState(bundle) }
}

open class BlockFragment : Fragment() {
    protected open val useSameRes = true
    protected open val blockManagers = mutableListOf<BlockManager>()

    protected open fun getBlockManagerList(): List<BlockManager>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getBlockManagerList()?.let { blockManagers.addAll(it) }
        blockManagers.forEachIndexed { index, it ->
            if (useSameRes) {
                if (index == 0) {
                    it.withFragment(this)
                } else {
                    it.withBlockManager(blockManagers[0])
                }
            } else {
                it.withFragment(this)
            }
            it.inflate(null)
            it.onAttach(context)
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        blockManagers.forEach { it.onCreate(bundle) }
    }

    open fun onCreateView2(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        blockManagers.forEach { it.onCreateView(inflater, container, savedInstanceState) }
        return onCreateView2(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)
        blockManagers.forEach { it.onActivityCreated(bundle) }
    }

    override fun onStart() {
        super.onStart()
        blockManagers.forEach { it.onStart() }
    }

    override fun onResume() {
        super.onResume()
        blockManagers.forEach { it.onResume() }
    }

    override fun onPause() {
        super.onPause()
        blockManagers.forEach { it.onPause() }
    }

    override fun onStop() {
        super.onStop()
        blockManagers.forEach { it.onStop() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        blockManagers.forEach { it.onDestroyView() }
    }

    override fun onDestroy() {
        super.onDestroy()
        blockManagers.forEach { it.onDestroy() }
    }

    override fun onDetach() {
        super.onDetach()
        blockManagers.forEach { it.onDetach() }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        blockManagers.forEach { it.onSaveInstanceState(bundle) }
    }
}

// 1. initInContext / initInBlock / initInGroup / initInManager
// 2. TODO: default constructor / constructor(layoutId) / constructor(view)
// 3. recycle / load / unload / copy
// 4. refresh / refreshGroup / refreshManager
// 5. TODO: parseFromXml / parseToXml / parseFromJson / parseToJson
//
// 6. fragment / activity lifecycle
// 7. initInActivity / initInFragment
// 8. activity proxy
// 9. TODO:
