package com.liang.example.blocktest

import android.annotation.SuppressLint
import android.util.Log
import android.view.Gravity
import android.widget.*
import com.example.uilib.block.Block
import com.example.uilib.block.BlockActivity
import com.example.uilib.block.BlockGroup
import com.example.uilib.block.BlockManager
import com.liang.example.androidtest.R
import com.liang.example.utils.DensityApi
import com.liang.example.utils.view.showToast

class MainActivity : BlockActivity() {
    companion object {
        private const val TAG = "BlockTest"
    }

    @SuppressLint("SetTextI18n")
    override fun getBlockManagerList(): List<BlockManager>? = listOf(
            BlockManager(this).apply {
                val dp10 = DensityApi.dpToPx(this@MainActivity, 10f)

                inflateBlocksAsync = true
                layoutId = R.layout.layout_linear
                parent = this@MainActivity.window.decorView.findViewById(android.R.id.content)
                setInflatedCallback<LinearLayout> {
                    Log.d(TAG, "blockManager -- linear_layout afterInflater")
                    it.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    it.orientation = LinearLayout.VERTICAL
                    it.gravity = Gravity.CENTER_VERTICAL
                    it.setPadding(dp10, dp10, dp10, dp10)
                }

                addBlock(BlockGroup(this).apply {
                    layoutId = R.layout.layout_linear
                    setInflatedCallback<LinearLayout> {
                        Log.d(TAG, "blockGroup -- linear_layout afterInflater")
                        it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    }

                    addBlock(Block().apply {
                        layoutId = R.layout.view_button
                        setInflatedCallback<Button> {
                            Log.d(TAG, "block -- button afterInflater")
                            it.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            it.text = "button1"
                            it.setOnClickListener { showToast("button1 clicked") }
                        }
                    })

                    addBlock(Block().apply {
                        layoutId = R.layout.view_button
                        setInflatedCallback<Button> {
                            Log.d(TAG, "block -- button afterInflater")
                            it.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            it.text = "button2"
                            it.setOnClickListener { showToast("button2 clicked") }
                        }
                    })
                })

                addBlock(Block().apply {
                    layoutId = R.layout.view_text
                    setInflatedCallback<TextView> {
                        Log.d(TAG, "block -- text_view afterInflater")
                        it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        it.text = "Just a test"
                        it.textSize = 16f
                        it.gravity = Gravity.CENTER_HORIZONTAL
                        it.setPadding(0, 0, 0, dp10)
                        it.setTextColor(resources.getColor(android.R.color.holo_orange_light))
                    }
                })

                addBlockLater(BlockGroup(this).apply {
                    layoutId = R.layout.layout_relative
                    setInflatedCallback<RelativeLayout> {
                        Log.d(TAG, "blockGroup -- relative_layout afterInflater")
                        it.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    }

                    addBlock(Block().apply {
                        layoutId = R.layout.view_text
                        setInflatedCallback<TextView> {
                            it.text = "连接wifi时自动下载更新"
                            it.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                addRule(RelativeLayout.CENTER_VERTICAL)
                            }
                        }
                    })

                    addBlock(Block().apply {
                        layoutId = R.layout.view_switch
                        setInflatedCallback<Switch> {
                            it.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                addRule(RelativeLayout.CENTER_VERTICAL)
                            }
                        }
                    })
                })
            }
            // , BlockManager(this).apply {
            // }
    )
}
