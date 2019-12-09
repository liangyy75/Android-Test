package com.liang.example.blocktest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.uilib.block.Block
import com.example.uilib.block.BlockGroup

class MainActivity4 : AppCompatActivity() {
    @SuppressLint("CI_ByteDanceKotlinRules_Not_Allow_findViewById_Invoked_In_UI")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BlockGroup(this).apply {
            inflated.compareAndSet(false, true)
            view = LinearLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }
            parent = this@MainActivity4.window.decorView.findViewById<FrameLayout>(android.R.id.content)
            parent!!.addView(view)

            addBlock(createTextBlock("text1"))
            addBlock(createTextBlock("text2"))
            addBlock(createTextBlock("text3"))
        }
    }

    fun createTextBlock(t: String) = Block().init(this).apply {
        inflated.compareAndSet(false, true)
        view = TextView(context).apply {
            text = t
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.holo_orange_light))
        }
    }
}