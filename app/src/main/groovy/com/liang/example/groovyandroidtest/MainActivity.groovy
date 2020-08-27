package com.liang.example.groovyandroidtest

import android.content.Intent
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.uilib.block.Block
import com.example.uilib.block.BlockActivity
import com.example.uilib.block.BlockManager
import com.liang.example.androidtest.R
import com.liang.example.utils.ApiManager
import com.liang.example.utils.r.ResApiKt
import groovy.io.FileType

import static com.liang.example.utils.view.ToastApiKt.showToast

class MainActivity extends BlockActivity {
    private static final String TAG = "Groovy-Java-Kotlin"

    @Override
    protected List<BlockManager> getBlockManagerList() {
        int dp10 = ResApiKt.dp2px(10f, this)
        BlockManager blockManager = new BlockManager(this, R.layout.layout_linear)
        blockManager.inflateBlocksAsync = false
        blockManager.setParent window.decorView.findViewById(android.R.id.content)

        blockManager.addBlock(new Block(R.layout.view_button).setInflatedCallback { Button it ->
            ApiManager.LOGGER.d(TAG, "block -- button_1 afterInflater")
            it.id = R.id.button_1
            it.layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.text = "go to java"
            it.setOnClickListener {
                startActivity(new Intent(this, com.liang.example.javaandroidtest.MainActivity.class))
            }
            null
        })

        blockManager.addBlock(new Block(R.layout.view_button).setInflatedCallback { Button it ->
            ApiManager.LOGGER.d(TAG, "block -- button_2 afterInflater")
            it.id = R.id.button_2
            it.layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.text = "go to kotlin"
            it.setOnClickListener {
                startActivity(new Intent(this, com.liang.example.ktandroidtest.MainActivity.class))
            }
            null
        })

        blockManager.addBlock(new Block(R.layout.view_edit).setInflatedCallback { EditText it ->
            ApiManager.LOGGER.d(TAG, "block -- edit_view_1 afterInflater")
            it.id = R.id.edit_view_1
            it.layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.hint = "execute groovy code"
            null
        })

        blockManager.addBlock(new Block(R.layout.view_button).setInflatedCallback { Button it ->
            ApiManager.LOGGER.d(TAG, "block -- button_3 afterInflater")
            it.id = R.id.button_3
            it.layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.text = "execute groovy code"
            it.setOnClickListener {
                String shellText = it.rootView.<EditText>findViewById(R.id.edit_view_1).text.toString()
                // Binding bind = new Binding()
                // bind.setVariable("blockManager", blockManager)
                // bind.setVariable("this", this)
                // GroovyShell shell = new GroovyShell(bind)
                // Object object = shell.evaluate(shellText)
                it.rootView.<TextView>findViewById(R.id.text_view_1).text = object.toString()
                // GroovyScriptEngine engine = new GroovyScriptEngine()
                // engine.run()  // 失败了，groovy动态化失败 😭
            }
            null
        })

        blockManager.addBlock(new Block(R.layout.view_text).setInflatedCallback { TextView it ->
            ApiManager.LOGGER.d(TAG, "block -- text_view_1 afterInflater")
            it.id = R.id.text_view_1
            it.layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.hint = "result"
            null
        })

        [blockManager.setInflatedCallback2({ LinearLayout it ->
            ApiManager.LOGGER.d(TAG, "blockManager -- linear_layout_1 afterInflater")
            it.id = R.id.linear_layout_1
            it.orientation = LinearLayout.VERTICAL
            // it.gravity = Gravity.CENTER
            it.setPadding dp10, dp10, dp10, dp10
            it.setOnClickListener { showToast("Groovy Activity is clicked") }
            null
        })]
    }

    void test() {
        new File("").eachFileRecurse(FileType.FILES) {  }
    }
}

// https://stackoverflow.com/questions/9663817/how-to-create-a-layoutinflater-given-xmlpullparser-as-input
// // byte[] data = ...
// // bytes of compiled xml (unzip the apk, get the bytes from res/layout*/*.xml)
//
// // XmlBlock block = new XmlBlock(data);
// Class<?> clazz = Class.forName("android.content.res.XmlBlock");
// Constructor<?> constructor = clazz.getDeclaredConstructor(byte[].class);
// constructor.setAccessible(true);
// Object block = constructor.newInstance(data);
//
// // XmlPullParser parser = block.newParser();
// Method method = clazz.getDeclaredMethod("newParser");
// method.setAccessible(true);
// XmlPullParser parser = method.invoke(block);
