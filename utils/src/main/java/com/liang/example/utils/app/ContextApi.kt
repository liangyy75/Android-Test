package com.liang.example.utils.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object ContextApi {
    lateinit var app: Application
    lateinit var appContext: Application
    lateinit var handler: Handler
    var activity: Activity? = null
    var fragment: Fragment? = null
    val activityStack: MutableList<Activity> = mutableListOf()
    val fragmentStack: MutableList<Fragment> = mutableListOf()

    fun init(application: Application): ContextApi {
        this.app = application
        this.handler = Handler(Looper.getMainLooper())
        this.app.registerActivityLifecycleCallbacks(object : LogActivityLifecycleCallbacks() {
            private val fragmentLifecycleCallbacks = object : LogFragmentLifecycleCallbacks() {
                // TODO()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                this@ContextApi.activityStack.add(activity)
                if (activity is FragmentActivity) {
                    activity.supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                this@ContextApi.activity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                this@ContextApi.activity = null
            }

            override fun onActivityDestroyed(activity: Activity) {
                this@ContextApi.activityStack.remove(activity)
                if (activity is FragmentActivity) {
                    activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
                }
            }
        })
        return this
    }
}
