package com.liang.example.blocktest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.uilib.block.Block
import com.example.uilib.block.BlockGroup
import com.liang.example.androidtest.R
import com.liang.example.utils.ApiManager
import com.liang.example.utils.DensityApi

class MainActivity3 : AppCompatActivity() {
    companion object {
        private const val TAG = "BlockTest3"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dp30 = DensityApi.dpToPx(this, 30f)
        BlockGroup(this, R.layout.layout_relative).setInflatedCallback<RelativeLayout> {
            ApiManager.LOGGER.d(TAG, "blockGroup inflated")
        }.addBlock(Block(R.layout.view_text).setInflatedCallback<TextView> {
            ApiManager.LOGGER.d(TAG, "block -- textView inflated")
            it.text = "left and top"
            it.layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
        }).addBlock(Block(R.layout.view_button).setInflatedCallback<Button> {
            ApiManager.LOGGER.d(TAG, "block -- button inflated")
            it.layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
        }).addBlock(Block(R.layout.view_switch).setInflatedCallback<Button> {
            ApiManager.LOGGER.d(TAG, "block -- switch inflated")
            it.text = "button"
            it.layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }).insertBlock(Block(R.layout.view_progress).setInflatedCallback<ProgressBar> {
            ApiManager.LOGGER.d(TAG, "block -- progressBar inflated")
            it.layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }, 0).build(this.window.decorView.findViewById(android.R.id.content))
                .apply {
                    post(Runnable {
                        insertBlock(Block(R.layout.view_image).setInflatedCallback<ImageView> {
                            ApiManager.LOGGER.d(TAG, "block -- imageView inflated")
                            it.layoutParams = RelativeLayout.LayoutParams(dp30, dp30).apply {
                                addRule(RelativeLayout.CENTER_IN_PARENT)
                            }
                            it.setBackgroundResource(R.drawable.more_right)
                        }, 1)
                    }, 3000)

                    post(Runnable { removeBlock(1) }, 6000)

                    post(Runnable {
                        replaceBlock(Block(R.layout.view_edit).setInflatedCallback<EditText> {
                            it.hint = "i'm a edit text"
                            it.layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                addRule(RelativeLayout.ALIGN_PARENT_TOP)
                            }
                        }, findBlockById(R.id.view_text)!!)
                    }, 9000)
                }
    }
}
