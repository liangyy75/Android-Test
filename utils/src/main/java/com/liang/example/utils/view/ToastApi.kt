package com.liang.example.utils.view

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.liang.example.utils.ApiManager

@JvmOverloads
fun showToast(msg: String, c: Context? = ApiManager.CONTEXT.context, duration: Int = Toast.LENGTH_LONG) {
    if (c != null) Toast.makeText(c, msg, duration).show()
}

@JvmOverloads
fun showToastOrLog(msg: String, c: Context? = ApiManager.CONTEXT.context, duration: Int = Toast.LENGTH_LONG) {
    if (c != null) Toast.makeText(c, msg, duration).show() else ApiManager.LOGGER.d(ApiManager.DEFAULT_TAG, msg)
}

@JvmOverloads
fun showToastWithLog(msg: String, c: Context? = ApiManager.CONTEXT.context, duration: Int = Toast.LENGTH_LONG) {
    if (c != null) Toast.makeText(c, msg, duration).show()
    ApiManager.LOGGER.d(ApiManager.DEFAULT_TAG, msg)
}

// fun Activity.showToast(msg: String, duration: Int = Toast.LENGTH_LONG) = Toast.makeText(this, msg, duration).show()
