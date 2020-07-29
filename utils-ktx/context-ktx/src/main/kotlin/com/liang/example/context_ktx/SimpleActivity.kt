package com.liang.example.context_ktx

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

open class SimpleActivity : AppCompatActivity() {
    override fun setContentView(layoutResID: Int) {
        set(window.decorView, this)
        super.setContentView(layoutResID)
    }

    override fun setContentView(view: View?) {
        set(window.decorView, this)
        super.setContentView(view)
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        set(window.decorView, this)
        super.setContentView(view, params)
    }
}
