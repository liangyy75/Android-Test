package com.liang.example.utils.view

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.liang.example.utils.ApiManager

@JvmOverloads
fun showToast(msg: String, c: Context? = ApiManager.CONTEXT.appContext, duration: Int = Toast.LENGTH_SHORT) {
    if (c != null) Toast.makeText(c, msg, duration).show()
}

@JvmOverloads
fun showToastOrLog(msg: String, c: Context? = ApiManager.CONTEXT.appContext, duration: Int = Toast.LENGTH_SHORT) {
    if (c != null) Toast.makeText(c, msg, duration).show() else ApiManager.LOGGER.d(ApiManager.DEFAULT_TAG, msg)
}

@JvmOverloads
fun showToastWithLog(msg: String, c: Context? = ApiManager.CONTEXT.appContext, duration: Int = Toast.LENGTH_SHORT) {
    if (c != null) Toast.makeText(c, msg, duration).show()
    ApiManager.LOGGER.d(ApiManager.DEFAULT_TAG, msg)
}

// fun Activity.showToast(msg: String, duration: Int = Toast.LENGTH_LONG) = Toast.makeText(this, msg, duration).show()

@JvmOverloads
fun showSnackbar(msg: String, a: Activity, duration: Int = Snackbar.LENGTH_SHORT, log: Boolean = false) {
    Snackbar.make(a.window.decorView.rootView, msg, duration).show()
    if (log) ApiManager.LOGGER.d(ApiManager.DEFAULT_TAG, msg)
}

@JvmOverloads
fun showSnackbarWithAction(msg: String, a: Activity, text: String, listener: ((v: View) -> Unit), duration: Int = Snackbar.LENGTH_SHORT, log: Boolean = false) {
    Snackbar.make(a.window.decorView.rootView, msg, duration).setAction(text, listener).show()
    if (log) ApiManager.LOGGER.d(ApiManager.DEFAULT_TAG, msg)
}

@JvmOverloads
fun showSnackbar(msg: String, v: View, duration: Int = Snackbar.LENGTH_SHORT, log: Boolean = false) {
    Snackbar.make(v, msg, duration).show()
    if (log) ApiManager.LOGGER.d(ApiManager.DEFAULT_TAG, msg)
}

@JvmOverloads
fun showSnackbarWithAction(msg: String, v: View, text: String, listener: ((v: View) -> Unit), duration: Int = Snackbar.LENGTH_SHORT, log: Boolean = false) {
    Snackbar.make(v, msg, duration).setAction(text, listener).show()
    if (log) ApiManager.LOGGER.d(ApiManager.DEFAULT_TAG, msg)
}

// Dialog