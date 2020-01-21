package com.liang.example.xmlinflatertest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.liang.example.xml_inflater.Attr
import com.liang.example.xml_inflater.AttrProcessorManager
import com.liang.example.xml_inflater.BooleanAttrProcessor
import com.liang.example.xml_inflater.ColorAttrProcessor
import com.liang.example.xml_inflater.DimenAttrProcessor
import com.liang.example.xml_inflater.EnumAttrProcessor
import com.liang.example.xml_inflater.FlagAttrProcessor
import com.liang.example.xml_inflater.FloatAttrProcessor
import com.liang.example.xml_inflater.FractionAttrProcessor
import com.liang.example.xml_inflater.IntegerAttrProcessor
import com.liang.example.xml_inflater.ReferenceAttrProcessor
import com.liang.example.xml_inflater.StringAttrProcessor
import com.liang.example.xml_inflater.ValuesResProcessor
import com.liang.example.xml_inflater.XmlTransformer
import com.liang.example.xml_inflater.debugFlag
import com.liang.example.xml_inflater.throwFlag

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transformer = XmlTransformer()
        val dom = transformer.parser.parseXmlOrThrow("""
            <resources>
                <string name="app_name">AndroidTest</string>
                <string name="recyclerview_test">Test RecyclerView</string>
                <string name="handler_test">Test Handler</string>
                <string name="contentDescription">null</string>
                <string name="fragment_test">Test Fragment</string>
                <string name="fragment_test_bottombar">Test Bottom Bar</string>
                <string name="fragment_test_bottombar2">Test Bottom Bar2</string>
            
                <color name="colorPrimary">#008577</color>
                <color name="colorPrimaryDark">#00574B</color>
                <color name="colorAccent">#D81B60</color>
            
                <dimen name="dimen16">16dp</dimen>
                <dimen name="dimen17">17dp</dimen>
                <dimen name="dimen18">18dp</dimen>
                <dimen name="dimen19">19dp</dimen>
                <dimen name="dimen20">20dp</dimen>
            
                <integer name="int1">1</integer>
                <integer name="int2">2</integer>
                <integer name="int3">3</integer>
                
                <string-array name="str_arr1">
                    <item>text1</item>
                    <item>text2</item>
                    <item>text3</item>
                </string-array>
                
                <integer-array name="int_arr1">
                    <item>100</item>
                    <item>112</item>
                    <item>134</item>
                    <item>156</item>
                    <item>178</item>
                    <item>290</item>
                </integer-array>
                
                <bool name="bool1">false</bool>
                <bool name="bool2">true</bool>
                
                <plurals name="plurals">
                    <item quantity="zero">0</item>
                    <item quantity="one">1</item>
                    <item quantity="two">2</item>
                    <item quantity="many">many</item>
                </plurals>
            </resources>
        """.trimIndent())
        Log.d("Xml Inflater", dom.string())

        val node = transformer.transform(dom)
        val apm = AttrProcessorManager()
        apm + DimenAttrProcessor()
        apm + ColorAttrProcessor()
        apm + FlagAttrProcessor()
        apm + EnumAttrProcessor()
        apm + IntegerAttrProcessor()
        apm + FloatAttrProcessor()
        apm + FractionAttrProcessor()
        apm + StringAttrProcessor()
        apm + BooleanAttrProcessor()
        apm + ReferenceAttrProcessor(Attr.ATTR_EMPTY, this)

        throwFlag = true
        debugFlag = true
        val resProcessor = ValuesResProcessor(this, apm)
        Log.d("Xml Inflater", "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        resProcessor.process(node)?.value()?.forEach {
            when (val value = it.value()) {
                is LongArray -> Log.d("Xml Inflater", "get res -- ${it.name}, ${value.joinToString()}")
                is Array<*> -> Log.d("Xml Inflater", "get res -- ${it.name}, ${value.joinToString()}")
                is Map<*, *> -> Log.d("Xml Inflater", "get res -- ${it.name}, ${value.toList().joinToString { "(${it.first}, ${it.second})" }}")
                else -> Log.d("Xml Inflater", "get res -- ${it.name}, $value")
            }
        } ?: Log.d("Xml Inflater", "null res")
    }
}
