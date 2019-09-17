package com.liang.example.utils.process

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper

class ContextApi(context: Context, application: Application) {
    var context: Context? = context
    var application: Application? = application
    var handler: Handler? = null

    init {
        handler = Handler(Looper.getMainLooper())
    }
}