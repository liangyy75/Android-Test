@file:Suppress("unused")

package com.liang.example.xml_inflater.values

import android.content.Context
import android.util.Log
import com.liang.example.xml_inflater.Attr
import com.liang.example.xml_inflater.Attrs
import com.liang.example.xml_inflater.FormatValue
import com.liang.example.xml_inflater.debugFlag
import com.liang.example.xml_inflater.tag
import com.liang.example.xml_inflater.throwFlag
import java.lang.RuntimeException

open class Node(open var name: String) {
    open var parent: Node? = null
    open var text = EMPTY_TEXT
    open var children: MutableList<Node> = EMPTY_CHILDREN
    open var attributes: MutableList<NodeAttr> = EMPTY_ATTRIBUTES

    operator fun get(attrName: String) = attributes.find { it.name == attrName }?.value

    open class NodeAttr(open var name: String, open var value: String)

    companion object {
        val EMPTY_CHILDREN = mutableListOf<Node>()
        val EMPTY_ATTRIBUTES = mutableListOf<NodeAttr>()
        const val EMPTY_TEXT = ""
    }
}

abstract class ResProcessor<T>(open val apm: IAttrProcessorManager, open val type: String) {
    open val attrProcessors = mutableMapOf<Attr, AttrProcessor<*>>()

    open fun register(attr: Attr): Boolean {
        attrProcessors[attr] = when {
            attr.format == null || attr.format == "flag" -> apm["flag"] ?: return false
            attr.format.contains('|') -> apm.getComplex(*attr.format.split('|').toTypedArray()) ?: return false
            else -> apm[attr.format] ?: return false
        }
        return true
    }

    abstract fun prepare()
    abstract fun process(node: Node): NodeValue<T>?

    abstract class NodeValue<T>(open val node: Node, _value: T) : FormatValue<T>(_value) {
        override fun copy(): NodeValue<T> = super.copy() as NodeValue<T>
    }

    open class ListNodeValue(_node: Node, _value: List<NodeValue<*>>) : NodeValue<List<NodeValue<*>>>(_node, _value) {
        override fun innerCopy(): FormatValue<List<NodeValue<*>>> = ListNodeValue(node, value.toList())
    }
}

interface IResProcessorManager {
    var processors: MutableList<ResProcessor<*>>
    // TODO
}

/**
 * values 下面的
 * <resources>
 *     <item name="string" type=["transition" | "animator" | "anim" | "interpolator" | "style" | "string" | "array" | "attr" | "bool" | "color" | "dimen" | "drawable"
 *         | "font" | "fraction" | "id" | "integer" | "layout" | "menu" | "mipmap" | "navigation" | "plurals" | "raw" | "xml"] format=["integer" | "float" | "reference"
 *         | "fraction" | "color" | "enum" | "string" | "boolean" | "dimension" | "flags"] >text</item>
 *     <style name="string" parent="string">
 *         <item name="string">text</item>
 *     </style>
 *     <declare-styleable name="string">
 *         <attr name="string" format="...">
 *             <enum name="string" value="int" />
 *             <flag name="string" value="int" />
 *         </attr>
 *     </declare-styleable>
 *     <drawable name="string">reference</drawable>
 *     <dimen name="string">100dp</dimen>
 *     <color name="string">#rgb</color>
 *     <array name="string">
 *         <item>reference</item>
 *     </array>
 *     <attr .../>
 *     <bool name="string">[true | false]</bool>
 *     <fraction name="string">100%p</fraction>
 *     <!-- eat-comment -->
 *     <integer name="string">100</integer>
 *     <integer-array name="string">
 *         <item>100</item>
 *     </integer-array>
 *     <string name="string" formatted="bool" translatable="bool">string</string>
 *     <string-array name="string">
 *         <item>string</item>
 *     </string-array>
 *     <plurals name="string">
 *         <item quantity=["zero" | "one" | "two" | "few" | "many" | "other"]>string</item>
 *     </plurals>
 * </resources>
 */
open class ValuesResProcessor(open var context: Context, _attrProcessorManager: IAttrProcessorManager) : ResProcessor<Any>(_attrProcessorManager, "values") {
    override fun prepare() {
        register(Attrs.Resources2.name)
        register(Attrs.Resources2.type)  // TODO
        register(Attrs.Resources2.format)  // TODO
        register(Attrs.Resources2.parent)  // TODO
        register(Attrs.Resources2.value)  // TODO
        register(Attrs.Resources2.formatted)  // TODO
        register(Attrs.Resources2.translatable)  // TODO
        register(Attrs.Resources2.quantity)
    }

    override fun process(node: Node): NodeValue<Any>? {
        if (node.name != "resources" || node.children.isEmpty()) {
            return null
        }
        val list = mutableListOf<NodeValue<*>>()
        node.children.forEach {
            val name = attrProcessors[Attrs.Resources2.name]!!.from(it["name"])?.string()
                    ?: return makeFail("resources's element should has name attribute")
            val temp: NodeValue<*> = when (it.name) {
                "item" -> PrimitiveNodeValue(it, TODO(), ITEM, name)
                "style" -> PrimitiveNodeValue(it, TODO(), STYLE, name)
                "attr" -> PrimitiveNodeValue(it, TODO(), ATTR, name)
                "declare-styleable" -> PrimitiveNodeValue(it, TODO(), DECLARE_STYLEABLE, name)
                "drawable" -> PrimitiveNodeValue(it, TODO(), DRAWABLE, name)
                "dimen" -> PrimitiveNodeValue(it, (apm.dimen().from(it.text) as? DimenAttrProcessor.DimenAttrValue)?.apply(context)
                        ?: return makeFail("dimen's text is incorrect"), DIMEN, name)
                "color" -> PrimitiveNodeValue(it, apm.color().from(it.text) ?: return makeFail("color's text is incorrect"), COLOR, name)
                "array" -> PrimitiveNodeValue(it, TODO(), ARRAY, name)
                "bool" -> PrimitiveNodeValue(it, apm.bool().from(it.text)?.value() ?: return makeFail("bool's text is incorrect"), BOOL, name)
                "fraction" -> PrimitiveNodeValue(it, apm.fraction().from(it.text)?.value()
                        ?: return makeFail("fraction's text is incorrect"), FRACTION, name)
                "integer" -> PrimitiveNodeValue(it, apm.int().from(it.text)?.value() ?: return makeFail("integer's text is incorrect"), INTEGER, name)
                "integer-array" -> PrimitiveNodeValue(it, it.children.map { integer ->
                    apm.int().from(integer.text)?.value() ?: return makeFail("integer array's item text is incorrect")
                }.toTypedArray(), INTEGER_ARRAY, name)
                "string" -> PrimitiveNodeValue(it, apm.str().from(it.text) ?: return makeFail("string's text is incorrect"), STRING, name)
                "string-array" -> PrimitiveNodeValue(it, it.children.map { string ->
                    apm.str().from(string.text)?.value() ?: return makeFail("string array's item text is incorrect")
                }.toTypedArray(), STRING_ARRAY, name)
                "plurals" -> PrimitiveNodeValue(it, it.children.map { item ->
                    (apm.enum().from(item["quantity"])?.value() ?: return makeFail("the quantity attribute of plurals's item is incorrect")) to
                            (apm.str().from(item.text)?.value() ?: return makeFail("plurals's item text is incorrect"))
                }.toMap(), STRING_ARRAY, name)
                else -> return makeFail("unknown resources's element: ${it.name}", null)
            }
            list.add(temp)
        }
        return ListNodeValue(node, list) as NodeValue<Any>
    }

    open fun <T> makeFail(reason: String, value: T? = null): T? {
        if (throwFlag) {
            throw RuntimeException(reason)
        }
        if (debugFlag) {
            Log.w(tag, reason)
        }
        return value
    }

    open class PrimitiveNodeValue(_node: Node, _value: Any, open var type: Int, open var name: String) : NodeValue<Any>(_node, _value) {
        override fun innerCopy(): FormatValue<Any> = PrimitiveNodeValue(node, value, type, name)

        open fun dimen(): Float = value as Float
        open fun color(): Long = value as Long
        open fun bool(): Boolean = value as Boolean
        open fun fraction(): Float = value as Float
        open fun int(): Long = (value as IntegerAttrProcessor.IntegerAttrValue).value()
        open fun string2(vararg args: Any): String = String.format(value as String, *args)

        open fun intArray() = value as LongArray
        open fun stringArray() = value as Array<String>

        open fun item(context: Context): Int = value as Int

        open fun style(): String = TODO("not implement")
        open fun declareStyleable(): String = TODO("not implement")
        open fun attr(): String = TODO("not implement")
        open fun array(): String = TODO("not implement")
        open fun drawable(): String = TODO("not implement")

        open fun plurals(num: Int, vararg args: Any): String {
            val temp = value as Map<Int, String>
            return when {
                num == 0 -> temp[PLURALS_ZERO]
                num == 1 -> temp[PLURALS_ONE]
                num == 2 -> temp[PLURALS_TWO]
                num < 100 -> temp[PLURALS_FEW]
                num >= 100 -> temp[PLURALS_MANY]
                else -> temp[PLURALS_OTHER]
            }!!
        }
    }

    companion object {
        const val ITEM = 1  // 注意， string / bool / dimen / ... 这种基本类型会转换为对应的 FormatValue ，其余用 ReferenceAttrValue
        const val STYLE = 2
        const val DECLARE_STYLEABLE = 3
        const val ATTR = 8

        const val DRAWABLE = 4
        const val ARRAY = 7

        const val DIMEN = 5
        const val COLOR = 6
        const val BOOL = 9
        const val FRACTION = 10
        const val INTEGER = 11
        const val INTEGER_ARRAY = 12
        const val STRING = 13
        const val STRING_ARRAY = 14
        const val PLURALS = 15

        const val PLURALS_ZERO = 0
        const val PLURALS_ONE = 1
        const val PLURALS_TWO = 2
        const val PLURALS_FEW = 3
        const val PLURALS_MANY = 4
        const val PLURALS_OTHER = 5
    }
}

/**
 * color @[package:]color/filename
 * <selector>
 *     <item android:color="color" .../>
 * </selector>
 * <gradient
 *     android:centerColor="color"
 *     android:centerX="integer"
 *     android:centerY="integer"
 *     android:endColor="color"
 *     android:endX="integer"
 *     android:endY="integer"
 *     android:gradientRadius="integer"
 *     android:startColor="color"
 *     android:startX="integer"
 *     android:startY="integer"
 *     android:tileMode="mirror"
 *     android:type="linear">
 *     <item android:color="color" android:offset="integer"/>
 * </gradient>
 */


/**
 * animation-list
 * alpha
 * translate
 * rotate
 * scale
 * set
 */

/**
 * style
 */

/**
 * menu
 */

/**
 * layout
 */
