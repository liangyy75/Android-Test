package com.liang.example.ktandroidtest

import android.annotation.SuppressLint
import android.content.Intent
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.example.uilib.block.Block
import com.example.uilib.block.BlockActivity
import com.example.uilib.block.BlockManager
import com.liang.example.androidtest.R
import com.liang.example.javaandroidtest.Helper.groovyTargetActivityClass
import com.liang.example.utils.ApiManager
import com.liang.example.utils.r.dp2px
import com.liang.example.utils.view.showToast

class MainActivity : BlockActivity() {
    companion object {
        private const val TAG = "Groovy-Java-Kotlin"
    }

    @SuppressLint("CI_ByteDanceKotlinRules_Not_Allow_findViewById_Invoked_In_UI", "SetTextI18n")
    override fun getBlockManagerList(): List<BlockManager>? {
        val dp10 = dp2px(10f, this)
        return listOf(BlockManager(this, R.layout.layout_linear).apply {
            inflateBlocksAsync = false
            parent = this@MainActivity.window.decorView.findViewById(android.R.id.content)
            setInflatedCallback<LinearLayout> {
                ApiManager.LOGGER.d(TAG, "blockManager -- linear_layout_1 afterInflater")
                it.id = R.id.linear_layout_1
                it.orientation = LinearLayout.VERTICAL
                // it.gravity = Gravity.CENTER
                it.setPadding(dp10, dp10, dp10, dp10)
                it.setOnClickListener { showToast("Kotlin Activity is clicked") }
            }

            addBlock(Block(R.layout.view_button).setInflatedCallback<Button> {
                ApiManager.LOGGER.d(TAG, "block -- button_1 afterInflater")
                it.id = R.id.button_1
                it.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                it.text = "go to java"
                it.setOnClickListener { startActivity(Intent(this@MainActivity, com.liang.example.javaandroidtest.MainActivity::class.java)) }
            })

            addBlock(Block(R.layout.view_button).setInflatedCallback<Button> {
                ApiManager.LOGGER.d(TAG, "block -- button_2 afterInflater")
                it.id = R.id.button_2
                it.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                it.text = "go to groovy"
                it.setOnClickListener { startActivity(Intent(this@MainActivity, groovyTargetActivityClass)) }
            })
        })
    }
}
