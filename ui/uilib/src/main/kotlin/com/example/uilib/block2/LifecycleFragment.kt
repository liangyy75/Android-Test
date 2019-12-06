package com.example.uilib.block2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import java.util.*

class LifecycleFragment : Fragment() {
    @SuppressLint("CI_ByteDanceKotlinRules_Enum_Fields_All_Uppercase")
    enum class State {
        Idle, Create, Start, Resume, Pause, Stop, Destroy;

        val isAlive: Boolean
            get() = this != Destroy
    }

    private val mModuleManagerSet: MutableSet<BlockManager> = HashSet()
    fun addModuleManager(manager: BlockManager) {
        mModuleManagerSet.add(manager)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        for (manager in mModuleManagerSet) {
            manager.onStart()
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        for (manager in mModuleManagerSet) {
            manager.onResume()
        }
    }

    @CallSuper
    override fun onPause() {
        for (manager in mModuleManagerSet) {
            manager.onPause()
        }
        super.onPause()
    }

    @CallSuper
    override fun onStop() {
        for (manager in mModuleManagerSet) {
            manager.onStop()
        }
        super.onStop()
    }

    @CallSuper
    override fun onDestroy() {
        for (manager in mModuleManagerSet) {
            manager.onDestroy()
        }
        super.onDestroy()
    }

    @CallSuper
    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        for (manager in mModuleManagerSet) {
            manager.onActivityCreated(savedInstanceState)
        }
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (manager in mModuleManagerSet) {
            manager.onActivityResult(requestCode, resultCode, data)
        }
    }

    @CallSuper
    override fun onDestroyView() {
        for (manager in mModuleManagerSet) {
            manager.onDestroyView()
        }
        super.onDestroyView()
    }
}
