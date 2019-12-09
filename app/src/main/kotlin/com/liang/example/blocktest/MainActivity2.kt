package com.liang.example.blocktest

import android.os.Bundle
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.uilib.block.Block
import com.liang.example.androidtest.R
import com.liang.example.utils.ApiManager

class MainActivity2 : AppCompatActivity() {
    companion object {
        private const val TAG = "BlockTest2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Block(this, R.layout.layout_list).apply {
            inflateViewAsync = true
        }.setInflatedCallback<ListView> {
            ApiManager.LOGGER.d(TAG, "layout_list_inflated")
            it.adapter = SimpleAdapter(this@MainActivity2, (0 until 50).toList().map { num -> mapOf("text" to "text -- $num") },
                    R.layout.view_text, arrayOf("text"), intArrayOf(R.id.view_text))
        }.build(this.window.decorView.findViewById(android.R.id.content))
    }
}
