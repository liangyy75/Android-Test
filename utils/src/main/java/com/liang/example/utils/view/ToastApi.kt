package com.liang.example.utils.view

import android.content.Context
import android.widget.Toast
import com.liang.example.utils.ApiManager

@JvmOverloads
fun showToast(msg: String, c: Context? = ApiManager.CONTEXT.context, duration: Int = Toast.LENGTH_LONG) =
        c ?: Toast.makeText(c, msg, duration).show()
