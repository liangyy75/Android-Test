package com.liang.example.utils.process

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.liang.example.utils.ApiManager

fun isInMainProcess(context: Context?): Boolean = context!!.packageName == getProcessName(context)

@JvmOverloads
fun getProcessName(context: Context, defaultValue: String = ""): String {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    activityManager?.runningAppProcesses?.forEach {
        if (it.pid == Process.myPid()) return it.processName
    }
    return defaultValue
}

fun killProcesses(context: Context) {
    TODO()
}

fun showProcessNames() {
    val activityManager = ApiManager.CONTEXT.context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    activityManager?.runningAppProcesses?.forEach { println(it) }
}