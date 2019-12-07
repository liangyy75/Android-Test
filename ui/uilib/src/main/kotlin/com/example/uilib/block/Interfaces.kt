package com.example.uilib.block

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

interface ActivityInter {
    fun getFragment(): Fragment?
    fun getActivity(): Activity?
    fun getFragmentManager(): FragmentManager?
    fun getFragmentActivity(): FragmentActivity?

    fun finish()
    fun startActivity(intent: Intent)
    fun startActivityForResult(intent: Intent, requestCode: Int)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?)

    fun onNewIntent(intent: Intent)
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    fun onBackPressed()
    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
}

open class ActivityProxy : ActivityInter {
    open lateinit var ai: ActivityInter

    override fun getFragment(): Fragment? = ai.getFragment()
    override fun getActivity(): Activity? = ai.getActivity()
    override fun getFragmentManager(): FragmentManager? = ai.getFragmentManager()
    override fun getFragmentActivity(): FragmentActivity? = ai.getFragmentActivity()

    override fun finish() = ai.finish()
    override fun startActivity(intent: Intent) = ai.startActivity(intent)
    override fun startActivityForResult(intent: Intent, requestCode: Int) = ai.startActivityForResult(intent, requestCode)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) = ai.startActivityForResult(intent, requestCode, options)

    override fun onNewIntent(intent: Intent) = Unit
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) = Unit
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit
    override fun onBackPressed() = Unit  // TODO
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = false
}

interface ActivityLifeCycleInter {
    companion object {
        const val ORIGINAL = 0
        const val STATE_CREATE = 1
        const val STATE_START = 2
        const val STATE_RESUME = 3
        const val STATE_PAUSE = 4
        const val STATE_STOP = 5
        const val STATE_RESTART = 6
        const val STATE_DESTROY = 7
        const val STATE_SAVE_INSTANCE_STATE = 8
        const val STATE_RESTORE_INSTANCE_STATE = 9
    }

    fun onCreate(bundle: Bundle?)
    fun onRestart()
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroy()

    fun onSaveInstanceState(bundle: Bundle)
    fun onRestoreInstanceState(bundle: Bundle)
}

interface FragmentLifeCycleInter {
    companion object {
        const val ORIGIN = 0
        const val STATE_ATTACH = 1
        const val STATE_CREATE = 2
        const val STATE_CREATE_VIEW = 3
        const val STATE_ACTIVITY_CREATE = 4
        const val STATE_START = 5
        const val STATE_RESUME = 6
        const val STATE_PAUSE = 7
        const val STATE_STOP = 8
        const val STATE_DESTROY_VIEW = 9
        const val STATE_DESTROY = 10
        const val STATE_DETACH = 11
        const val STATE_SAVE_INSTANCE_STATE = 12
    }

    fun onAttach(context: Context)
    fun onCreate(bundle: Bundle?)
    fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View?
    fun onActivityCreated(bundle: Bundle?)
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroyView()
    fun onDestroy()
    fun onDetach()

    fun onSaveInstanceState(bundle: Bundle)
}

open class BlockActivity : AppCompatActivity() {
    protected val blockManagers = mutableListOf<BlockManager>()

    open fun getBlockManagerList(): List<BlockManager>? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        getBlockManagerList()?.let { blockManagers.addAll(it) }
        blockManagers.forEachIndexed { index, it ->
            it.initInActivity(this)
            it.build(null, index)
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
    protected val blockManagers = mutableListOf<BlockManager>()

    open fun getBlockManagerList(): List<BlockManager>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getBlockManagerList()?.let { blockManagers.addAll(it) }
        blockManagers.forEach {
            it.initInFragment(this)
            it.build(null)
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
