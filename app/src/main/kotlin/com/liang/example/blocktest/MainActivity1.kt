package com.liang.example.blocktest

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import com.example.uilib.block.*
import com.liang.example.androidtest.R
import com.liang.example.utils.ApiManager
import com.liang.example.utils.r.dp2px
import com.liang.example.utils.view.showToast

class MainActivity1 : BlockActivity() {
    companion object {
        private const val TAG = "BlockTest"
    }

    @SuppressLint("SetTextI18n")
    override fun getBlockManagerList(): List<BlockManager>? {
        val dp20 = dp2px(20f, this)
        val dp10 = dp2px(10f, this)
        val blockManager1 =
                BlockManager(this, R.layout.layout_linear).apply {
                    inflateBlocksAsync = true
                    parent = this@MainActivity1.window.decorView.findViewById(android.R.id.content)
                    setInflatedCallback<LinearLayout> {
                        ApiManager.LOGGER.d(TAG, "blockManager -- linear_layout_1 afterInflater")
                        it.id = R.id.linear_layout_1
                        it.orientation = LinearLayout.VERTICAL
                        it.gravity = Gravity.CENTER_VERTICAL
                        it.setPadding(dp10, dp10, dp10, dp10)
                    }

                    addBlock(BlockGroup(this, R.layout.layout_linear).setInflatedCallback<LinearLayout> {
                        ApiManager.LOGGER.d(TAG, "blockGroup -- linear_layout_2 afterInflater")
                        it.id = R.id.linear_layout_2
                        it.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    }.addBlock(Block(R.layout.view_button).setInflatedCallback<Button> {
                        ApiManager.LOGGER.d(TAG, "block -- button_1 afterInflater")
                        it.id = R.id.button_1
                        it.layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                        it.text = "button1"
                        it.setOnClickListener { showToast("button1 clicked") }
                    }).addBlock(Block(R.layout.view_button).setInflatedCallback<Button> {
                        ApiManager.LOGGER.d(TAG, "block -- button_2 afterInflater")
                        it.id = R.id.button_2
                        it.layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                        it.text = "button2"
                        it.setOnClickListener { showToast("button2 clicked") }
                    }))

                    addBlock(Block(R.layout.view_text).setInflatedCallback<TextView> {
                        ApiManager.LOGGER.d(TAG, "block -- text_view_1 afterInflater")
                        it.id = R.id.text_view_1
                        it.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        it.text = "Just a test"
                        it.textSize = 16f
                        it.gravity = Gravity.CENTER_HORIZONTAL
                        it.setPadding(0, 0, 0, dp10)
                        it.setTextColor(resources.getColor(android.R.color.holo_orange_light))

                        register(getObservable(BlockManager.KEY_ACTIVITY_STATE).subscribe { state -> ApiManager.LOGGER.d(TAG, "activity state: $state") })
                        register(getObservable(BlockManager.KEY_FRAGMENT_STATE).subscribe { state -> ApiManager.LOGGER.d(TAG, "activity state: $state") })
                    })

                    addBlockLater(BlockGroup(this, R.layout.layout_relative).setInflatedCallback<RelativeLayout> {
                        ApiManager.LOGGER.d(TAG, "blockGroup -- relative_layout_1 afterInflater")
                        it.id = R.id.relative_layout_1
                        it.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    }.addBlock(Block(R.layout.view_text).setInflatedCallback<TextView> {
                        ApiManager.LOGGER.d(TAG, "block -- text_view_2 afterInflater")
                        it.id = R.id.text_view_2
                        it.text = "连接wifi时自动下载更新"
                        it.layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                            addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                            addRule(RelativeLayout.CENTER_VERTICAL)
                        }
                    }).addBlock(Block(R.layout.view_switch).setInflatedCallback<Switch> {
                        ApiManager.LOGGER.d(TAG, "block -- switch_1 afterInflater")
                        it.id = R.id.switch_1
                        it.layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                            addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                            addRule(RelativeLayout.CENTER_VERTICAL)
                        }
                    }))

                    addBlock(Block(R.layout.layout_frame).setInflatedCallback<FrameLayout> {
                        ApiManager.LOGGER.d(TAG, "block -- frame_layout_1 afterInflater")
                        it.id = R.id.frame_layout_1
                        it.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        putData("frame_layout_1_inflated", true)
                    })
                }
        val blockManager2 =
                BlockManager(blockManager1, R.layout.layout_linear).apply {
                    inflateBlocksAsync = true
                    setInflatedCallback<LinearLayout> {
                        ApiManager.LOGGER.d(TAG, "blockManager -- linear_layout_3 afterInflater")
                        it.id = R.id.linear_layout_3
                        it.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        val task = Runnable { findViewById<FrameLayout>(R.id.frame_layout_1).addView(it) }
                        if (getData("frame_layout_1_inflated") as? Boolean == true) {
                            post(task, type = RxHandler.TYPE_MAIN_THREAD)
                        } else {
                            register(getObservable("frame_layout_1_inflated").subscribe { inflated ->
                                if (inflated != WhiteBoard.NULL_OBJECT && inflated as Boolean) {
                                    post(task, type = RxHandler.TYPE_MAIN_THREAD)
                                }
                            })
                        }
                    }

                    addBlock(Block(R.layout.view_text).setInflatedCallback<TextView> {
                        ApiManager.LOGGER.d(TAG, "block -- text_view_3 afterInflater")
                        it.id = R.id.text_view_3
                        it.layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                        it.text = "更多设置"
                    })

                    addBlock(Block(R.layout.view_image).setInflatedCallback<ImageView> {
                        ApiManager.LOGGER.d(TAG, "block -- image_view_1 afterInflater")
                        it.id = R.id.image_view_1
                        it.layoutParams = LinearLayout.LayoutParams(dp20, dp20)
                        it.setBackgroundResource(R.drawable.more_right)
                    })
                }
        return listOf(blockManager1, blockManager2)
    }
}
