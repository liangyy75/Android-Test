@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS", "DEPRECATION")

package com.liang.example.xml_inflater2

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.annotation.RequiresApi
import com.liang.example.view_ktx.getIntIdByStrId
import com.liang.example.view_ktx.getStrIdByIntId
import com.liang.example.xml_inflater2.AtomicType.Companion.RES_TYPE
import com.liang.example.xml_inflater2.ResStore.getRes

abstract class ResProcessor<T>(
        open val rootName: String, open val careChild: Boolean, open val careNameAttr: Boolean, open val resIntType: Int, vararg _otherRootNames: String
) {
    open val otherRootNames = _otherRootNames.toMutableList()
    open val apm: IAttrProcessorManager = ManagerHolder.apm
    open val rpm: IResProcessorManager = ManagerHolder.rpm

    open fun process(node: Node?, careNameAttr: Boolean? = null): NodeValue<T>? {
        if (node == null) {
            return makeFail("node is null")
        }
        if (node.name != rootName && node.name !in otherRootNames) {
            return makeFail("element's nodeName is incorrect: ${node.name}, [$rootName, ${otherRootNames.joinToString()}]")
        }
        if (careChild && node.children.isEmpty()) {
            return makeFail("element should have children")
        }
        val resName = if (careNameAttr == true || careNameAttr == null && this.careNameAttr) {
            node[Attrs.FreeRes.name] ?: return makeFail("element should have name attribute")
        } else null
        val resType = RES_TYPE.typeMaps.getVK(resIntType) ?: return makeFail("no such resType")
        return if (resName != null) {
            var result = getRes(resType, resName)
            if (result == null) {
                result = NodeValue(node, innerProcess(node) ?: return makeFail("innerProcess error"), resIntType, resName)
                ResStore.saveRes(resType, resName, result)
            } else if (result.type != resIntType) {
                return makeFail("ResStore have stored a res whose name is same as current res: $resName, " +
                        "but type is different: (store: ${result.type}, current: $resIntType)")
            }
            result as? NodeValue<T>
        } else {
            NodeValue(node, innerProcess(node) ?: return makeFail("innerProcess error"), resIntType, "")
        }
    }

    abstract fun innerProcess(node: Node): T?

    open class NodeValue<T>(_node: Node, _value: T, open var type: Int, open var name: String?) : FormatValue<T>(_value) {
        open val node: Node = _node.copy()
        override fun copy(): NodeValue<T> = super.copy() as NodeValue<T>
        override fun innerCopy(): NodeValue<T> = NodeValue(node, value, type, name)
    }

    open class ListNodeValue(_node: Node, _value: List<NodeValue<*>>, _type: Int, _name: String? = null)
        : NodeValue<List<NodeValue<*>>>(_node, _value, _type, _name)

    open fun dimen(s: String?, attr: Attr? = null): Float? =
            (apm["dimen"] as DimenAttrProcessor).attr(attr).from(s)?.let { DimenAttrProcessor.staticApply(apm.context, it as DimenAttrProcessor.DimenAttrValue) }

    open fun fractionValue(s: String?): Pair<Int, Float>? {
        val temp = (apm["fraction"] as FractionAttrProcessor).from(s) as? FractionAttrProcessor.FractionAttrValue ?: return null
        return Pair(temp.mode, temp.value())
    }

    open fun fraction(s: String?, base: Int = 1, pBase: Int = 1, rBase: Int = 1): Float? {
        val temp = fractionValue(s) ?: return null
        val value = temp.second.div(100)
        return when (temp.first) {
            FractionAttrProcessor.RELATIVE_TO_SELF -> value * base
            FractionAttrProcessor.RELATIVE_TO_PARENT -> value * pBase
            FractionAttrProcessor.RELATIVE_TO_ROOT -> value * rBase
            else -> null
        }
    }

    open fun refer(s: String?): Int? = (apm["refer"] as ReferenceAttrProcessor).from(s)?.value()
    open fun color(s: String?): Int? = (apm["color"] as ColorAttrProcessor).from(s)?.value()?.toInt()
    open fun str(s: String?): String? = (apm["str"] as StringAttrProcessor).from(s)?.value()
    open fun bool(s: String?): Boolean? = (apm["bool"] as BooleanAttrProcessor).from(s)?.value()
    open fun int(s: String?, attr: Attr? = null): Int? = (apm["int"] as IntegerAttrProcessor).attr(attr).from(s)?.value()?.toInt()
    open fun long(s: String?, attr: Attr? = null): Long? = (apm["int"] as IntegerAttrProcessor).attr(attr).from(s)?.value()
    open fun float(s: String?): Float? = (apm["float"] as FloatAttrProcessor).from(s)?.value()
    open fun enum(s: String?, attr: Attr): Int? = (apm["enum"] as EnumAttrProcessor).attr(attr).from(s)?.value()?.toInt()
    open fun flag(s: String?, attr: Attr): Int? = (apm["flag"] as FlagAttrProcessor).attr(attr).from(s)?.value()?.toInt()
    open fun enum(node: Node, attr: Attr): Int? = (apm["enum"] as EnumAttrProcessor).attr(attr).from(node[attr])?.value()?.toInt()
    open fun flag(node: Node, attr: Attr): Int? = (apm["flag"] as FlagAttrProcessor).attr(attr).from(node[attr])?.value()?.toInt()

    open fun dimen2(s: String?, attr: Attr? = null): Float? {
        return handleStr(s, { ResStore.loadDimen(it, rpm.context) }) ?: dimen(s, attr)
    }

    open fun fraction2(s: String?, base: Int = 1, pBase: Int = 1, rBase: Int = 1): Float? {
        return handleStr(s, { ResStore.loadFraction(it, rpm.context, base, pBase, rBase) }) ?: fraction(s, base, pBase, rBase)
    }

    open fun fractionValue2(s: String?): Pair<Int, Float>? = handleStr(s, { ResStore.loadFractionValue(it) }) ?: fractionValue(s)

    open fun color2(s: String?, attr: Attr? = null): Int? {
        return handleStr(s, { ResStore.loadColor(it, rpm.context) }, 0) ?: (apm["color"] as ColorAttrProcessor).from(s)?.value()?.toInt()
    }

    open fun str2(s: String?, vararg args: Any, attr: Attr? = null): String? {
        return handleStr(s, { ResStore.loadString(it, rpm.context, false, *args) })
                ?: (apm["str"] as StringAttrProcessor).attr(attr).from(s)?.value()?.format(*args)
    }

    open fun bool2(s: String?, attr: Attr? = null): Boolean? {
        return handleStr(s, { ResStore.loadBool(it, rpm.context) }) ?: (apm["bool"] as BooleanAttrProcessor).attr(attr).from(s)?.value()
    }

    open fun int2(s: String?, attr: Attr? = null): Int? {
        return handleStr(s, { ResStore.loadInt(it, rpm.context) }) ?: (apm["int"] as IntegerAttrProcessor).attr(attr).from(s)?.value()?.toInt()
    }

    open fun long2(s: String?, attr: Attr? = null): Long? {
        return handleStr(s, { ResStore.loadLong(it, rpm.context) }) ?: (apm["int"] as IntegerAttrProcessor).attr(attr).from(s)?.value()
    }

    open fun float2(s: String?, attr: Attr? = null): Float? {
        return handleStr(s, { ResStore.loadFloat(it, rpm.context) }) ?: (apm["float"] as FloatAttrProcessor).attr(attr).from(s)?.value()
    }

    open fun <T> handleStr(s: String?, action: (resIntKey: Int) -> T?, invalid: T? = null): T? {
        if (s == null) {
            return null
        }
        if (s.length >= 3 && (s[0] == '@' || s[0] == '?')) {
            var referId = refer(s) ?: 0
            while (referId != 0) {
                val tryResult = action(referId)
                if (tryResult != invalid) {
                    return tryResult
                }
                referId = ResStore.loadRefer(referId) ?: 0
            }
        }
        return null
    }

    companion object {
        val digital = '0'..'9'
    }
}

object ResStore {
    val CACHE: MutableMap<String, ResProcessor.NodeValue<*>> = LinkedHashMap()
    val rpm: IResProcessorManager = ManagerHolder.rpm

    fun saveRes(resType: String, resName: String, value: ResProcessor.NodeValue<*>): Int {
        val resNameKey = makeResNameKey(resType, resName)
        if (resNameKey in CACHE) {
            makeFail<Unit>("res cache have stored this res: $resNameKey, and now it's covered")
        }
        CACHE[resNameKey] = value
        return getIntIdByStrId(resNameKey)
    }

    fun getRes(resType: String, resName: String, reget: Boolean = false): ResProcessor.NodeValue<*>? {
        val saved = CACHE[makeResNameKey(resType, resName)] ?: return null
        return if (!reget) saved else rpm.process2(saved.node, false)
    }

    fun getRes(resIntKey: Int, reget: Boolean = false): ResProcessor.NodeValue<*>? {
        val saved = CACHE[getStrIdByIntId(resIntKey)] ?: return null
        return if (!reget) saved else rpm.process2(saved.node, false)
    }

    fun getRes(resNameKey: String, reget: Boolean = false): ResProcessor.NodeValue<*>? {
        val saved = CACHE[resNameKey] ?: return null
        return if (!reget) saved else rpm.process2(saved.node, false)
    }

    fun makeResNameKey(resType: String, resName: String) = "<${handleResType(resType)}_$resName>"

    fun handleResType(resType: String) = when (resType) {
        "dimension" -> "dimen"
        "boolean" -> "bool"
        "string" -> "str"
        "integer" -> "int"
        RES_TYPE.typeMaps.getVK(ResType.COLOR_STATE_LIST) -> "color"
        RES_TYPE.typeMaps.getVK(ResType.PROPERTY_VALUES_HOLDER),
        RES_TYPE.typeMaps.getVK(ResType.ANIMATOR),
        RES_TYPE.typeMaps.getVK(ResType.OBJECT_ANIMATOR),
        RES_TYPE.typeMaps.getVK(ResType.ANIMATOR_SET),
        RES_TYPE.typeMaps.getVK(ResType.ANIMATOR_STATE_LIST) -> "animator"
        RES_TYPE.typeMaps.getVK(ResType.ANIMATION_ALPHA),
        RES_TYPE.typeMaps.getVK(ResType.ANIMATION_SCALE),
        RES_TYPE.typeMaps.getVK(ResType.ANIMATION_ROTATE),
        RES_TYPE.typeMaps.getVK(ResType.ANIMATION_TRANSLATE),
        RES_TYPE.typeMaps.getVK(ResType.ANIMATOR_STATE_LIST),
        RES_TYPE.typeMaps.getVK(ResType.LAYOUT_ANIMATION),
        RES_TYPE.typeMaps.getVK(ResType.GRID_LAYOUT_ANIMATION),
        RES_TYPE.typeMaps.getVK(ResType.ANIMATION_SET) -> "anim"
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_ADAPTIVE_ICON),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_ANIMATED_ROTATE),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_ANIMATED_SELECTOR),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_ANIMATED_VECTOR),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_BITMAP),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_CLIP),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_COLOR),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_DRAWABLE),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_GRADIENT),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_INSET),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_LAYER_LIST),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_LEVEL_LIST),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_MASKABLE_ICON),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_NINE_PATCH),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_RIPPLE),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_ROTATE),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_SCALE),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_SELECTOR),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_SHAPE),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_TRANSITION),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_VECTOR),
        RES_TYPE.typeMaps.getVK(ResType.DRAWABLE_ANIMATED_IMAGE) -> "drawable"
        else -> resType
    }

    fun loadRefer(value: Int) = getRes(value)?.value() as? Int

    fun loadBool(value: Int, context: Context, reget: Boolean = false) = getRes(value, reget)?.value() as? Boolean ?: try {
        context.resources.getBoolean(value)
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadInt(value: Int, context: Context, reget: Boolean = false) = getRes(value, reget)?.value() as? Int ?: try {
        context.resources.getInteger(value)
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadLong(value: Int, context: Context, reget: Boolean = false) = (getRes(value, reget)?.value() as? Int)?.toLong() ?: try {
        context.resources.getInteger(value).toLong()
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadFloat(value: Int, context: Context, reget: Boolean = false) = getRes(value, reget)?.value() as? Float ?: when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> try {
            context.resources.getFloat(value)
        } catch (e: Resources.NotFoundException) {
            null
        }
        else -> 0f
    }

    fun loadQString(value: Int, quantity: Int, context: Context, reget: Boolean = false, vararg args: Any) =
            (getRes(value, reget)?.value() as? Map<Int, String>)?.get(quantity)?.format(*args) ?: try {
                context.resources.getQuantityString(value, quantity, *args)
            } catch (e: Resources.NotFoundException) {
                null
            }

    fun loadString(value: Int, context: Context, reget: Boolean = false, vararg args: Any) =
            (getRes(value, reget)?.value() as? String)?.format(*args) ?: try {
                context.resources.getString(value, *args)
            } catch (e: Resources.NotFoundException) {
                null
            }

    fun loadIntArray(value: Int, context: Context, reget: Boolean = false) = getRes(value, reget)?.value() as? IntArray ?: try {
        context.resources.getIntArray(value)
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadStringArray(value: Int, context: Context, reget: Boolean = false): Array<String>? = getRes(value, reget)?.value() as? Array<String> ?: try {
        context.resources.getStringArray(value)
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadDimen(value: Int, context: Context, reget: Boolean = false) = getRes(value, reget)?.value() as? Float ?: try {
        context.resources.getDimension(value)
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadFraction(value: Int, context: Context, base: Int, pBase: Int, rBase: Int = 1, reget: Boolean = false): Float? {
        val temp = getRes(value, reget) as? Pair<Int, Float> ?: return try {
            context.resources.getFraction(value, base, pBase)
        } catch (e: Resources.NotFoundException) {
            null
        }
        return temp.second * when (temp.first) {
            FractionAttrProcessor.RELATIVE_TO_SELF -> base
            FractionAttrProcessor.RELATIVE_TO_PARENT -> pBase
            else -> rBase
        }
    }

    fun loadFractionValue(value: Int, reget: Boolean = false): Pair<Int, Float>? {
        return getRes(value, reget) as? Pair<Int, Float> ?: return try {
            TODO("not implemented")
        } catch (e: Resources.NotFoundException) {
            null
        }
    }

    fun loadColor(value: Int, context: Context, reget: Boolean = false) = getRes(value, reget)?.value() as? Int ?: try {
        context.resources.getColor(value)
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadColorStateList(value: Int, context: Context, reget: Boolean = false) = getRes(value, reget)?.value() as? ColorStateList
            ?: (getRes(value)?.value() as? Int)?.let { ColorStateList.valueOf(it) } ?: try {
                context.resources.getColorStateList(value)
            } catch (e: Resources.NotFoundException) {
                null
            }

    fun loadAnimation(value: Int, context: Context, reget: Boolean = false): Animation? = getRes(value, reget)?.value() as? Animation ?: try {
        AnimationUtils.loadAnimation(context, value)
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadAnimator(value: Int, context: Context, reget: Boolean = false): Animator? = getRes(value, reget)?.value() as? Animator ?: try {
        AnimatorInflater.loadAnimator(context, value)
    } catch (e: Resources.NotFoundException) {
        null
    }

    fun loadLayoutAnimationController(value: Int, context: Context, reget: Boolean = false): LayoutAnimationController? =
            getRes(value, reget)?.value() as? LayoutAnimationController ?: try {
                AnimationUtils.loadLayoutAnimation(context, value)
            } catch (e: Resources.NotFoundException) {
                null
            }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun loadStateListAnimator(value: Int, context: Context, reget: Boolean = false): StateListAnimator? {
        val temp = getRes(value) ?: return try {
            AnimatorInflater.loadStateListAnimator(context, value)
        } catch (e: Resources.NotFoundException) {
            null
        }
        return when {
            reget -> rpm.get<StateListAnimatorResProcessor>(StateListAnimatorResProcessor::class.java)?.process(temp.node, false)?.value()
            else -> temp.value() as? StateListAnimator
        }
    }

    fun loadDrawable(value: Int, context: Context, reget: Boolean = false): Drawable? {
        val temp = getRes(value) ?: return try {
            context.resources.getDrawable(value)
        } catch (e: Resources.NotFoundException) {
            null
        }
        val node = temp.node
        return when {
            reget -> when (node.name) {
                "rotate" -> rpm.get<RotateDrawResProcessor>(RotateDrawResProcessor::class.java)?.process(node, false)?.value()
                "scale" -> rpm.get<ScaleDrawResProcessor>(ScaleDrawResProcessor::class.java)?.process(node, false)?.value()
                "color" -> rpm.get<ColorDrawableResProcessor>(ColorDrawableResProcessor::class.java)?.process(node, false)?.value()
                "selector" -> rpm.get<StateListDrawableResProcessor>(StateListDrawableResProcessor::class.java)?.process(temp.node, false)?.value()
                else -> temp.value() as? Drawable
            }
            else -> temp.value() as? Drawable ?: (temp.value() as? Int)?.let { ColorDrawable(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadFont(value: Int, context: Context, reget: Boolean = false) = getRes(value, reget)?.value() as? Typeface ?: try {
        context.resources.getFont(value)
    } catch (e: Resources.NotFoundException) {
        null
    }
}

object ResType {
    val NOT_CARED = RES_TYPE.inc("NOT_CARED")

    val VALUES = RES_TYPE.inc("values")

    val ITEM = RES_TYPE.inc("ITEM")
    val STYLE = RES_TYPE.inc("STYLE")
    val DECLARE_STYLEABLE = RES_TYPE.inc("DECLARE_STYLEABLE")
    val ATTR = RES_TYPE.inc("ATTR")

    val DRAWABLE = RES_TYPE.inc("DRAWABLE")
    val ARRAY = RES_TYPE.inc("ARRAY")

    val DIMEN = RES_TYPE.inc("DIMEN")
    val COLOR = RES_TYPE.inc("COLOR")
    val BOOL = RES_TYPE.inc("BOOL")
    val FRACTION = RES_TYPE.inc("FRACTION")
    val INTEGER = RES_TYPE.inc("INTEGER")
    val INTEGER_ARRAY = RES_TYPE.inc("INTEGER_ARRAY")
    val STRING = RES_TYPE.inc("STRING")
    val STRING_ARRAY = RES_TYPE.inc("STRING_ARRAY")
    val PLURALS = RES_TYPE.inc("PLURALS")

    val COLOR_STATE_LIST = RES_TYPE.inc("COLOR_STATE_LIST")

    val ANIMATOR = RES_TYPE.inc("ANIMATOR")
    val PROPERTY_VALUES_HOLDER = RES_TYPE.inc("PROPERTY_VALUES_HOLDER")
    val OBJECT_ANIMATOR = RES_TYPE.inc("OBJECT_ANIMATOR")
    val ANIMATOR_SET = RES_TYPE.inc("ANIMATOR_SET")
    val ANIMATOR_STATE_LIST = RES_TYPE.inc("ANIMATOR_STATE_LIST")

    val ANIMATION_SET = RES_TYPE.inc("ANIMATION_SET")
    val ANIMATION_ROTATE = RES_TYPE.inc("ROTATE")
    val ANIMATION_SCALE = RES_TYPE.inc("SCALE")
    val ANIMATION_ALPHA = RES_TYPE.inc("ALPHA")
    val ANIMATION_TRANSLATE = RES_TYPE.inc("TRANSLATE")
    val LAYOUT_ANIMATION = RES_TYPE.inc("LAYOUT_ANIMATION")
    val GRID_LAYOUT_ANIMATION = RES_TYPE.inc("GRID_LAYOUT_ANIMATION")
    val ANIMATION_DRAWABLE = RES_TYPE.inc("ANIMATION_DRAWABLE")

    val DRAWABLE_SHAPE = RES_TYPE.inc("DRAWABLE_SHAPE")
    val DRAWABLE_SCALE = RES_TYPE.inc("DRAWABLE_SCALE")
    val DRAWABLE_ROTATE = RES_TYPE.inc("DRAWABLE_ROTATE")
    val DRAWABLE_CLIP = RES_TYPE.inc("DRAWABLE_CLIP")
    val DRAWABLE_INSET = RES_TYPE.inc("DRAWABLE_INSET")
    val DRAWABLE_LAYER_LIST = RES_TYPE.inc("DRAWABLE_LAYER_LIST")
    val DRAWABLE_LEVEL_LIST = RES_TYPE.inc("DRAWABLE_LEVEL_LIST")
    val DRAWABLE_VECTOR = RES_TYPE.inc("DRAWABLE_VECTOR")
    val DRAWABLE_COLOR = RES_TYPE.inc("DRAWABLE_COLOR")
    val DRAWABLE_SELECTOR = RES_TYPE.inc("DRAWABLE_SELECTOR")
    val DRAWABLE_GRADIENT = RES_TYPE.inc("DRAWABLE_GRADIENT")
    val DRAWABLE_ADAPTIVE_ICON = RES_TYPE.inc("DRAWABLE_ADAPTIVE_ICON")
    val DRAWABLE_ANIMATED_ROTATE = RES_TYPE.inc("DRAWABLE_ANIMATED_ROTATE")
    val DRAWABLE_ANIMATED_SELECTOR = RES_TYPE.inc("DRAWABLE_ANIMATED_SELECTOR")
    val DRAWABLE_ANIMATED_VECTOR = RES_TYPE.inc("DRAWABLE_ANIMATED_VECTOR")
    val DRAWABLE_ANIMATED_IMAGE = RES_TYPE.inc("DRAWABLE_ANIMATED_IMAGE")
    val DRAWABLE_BITMAP = RES_TYPE.inc("DRAWABLE_BITMAP")
    val DRAWABLE_DRAWABLE = RES_TYPE.inc("DRAWABLE_DRAWABLE")
    val DRAWABLE_RIPPLE = RES_TYPE.inc("DRAWABLE_RIPPLE")
    val DRAWABLE_NINE_PATCH = RES_TYPE.inc("DRAWABLE_NINE_PATCH")
    val DRAWABLE_MASKABLE_ICON = RES_TYPE.inc("DRAWABLE_MASKABLE_ICON")
    val DRAWABLE_TRANSITION = RES_TYPE.inc("DRAWABLE_TRANSITION")

    val MENU = RES_TYPE.inc("MENU")
    val FONT = RES_TYPE.inc("FONT")
    val LAYOUT = RES_TYPE.inc("LAYOUT")
}

interface IResProcessorManager {
    var processors: MutableList<ResProcessor<*>>
    var context: Context

    operator fun plus(processor: ResProcessor<*>): Boolean
    operator fun plus(processors: Collection<ResProcessor<*>>): Boolean

    operator fun minus(processor: ResProcessor<*>): Boolean
    operator fun minus(index: Int): ResProcessor<*>
    operator fun minus(processors: Collection<ResProcessor<*>>): Boolean

    operator fun get(index: Int): ResProcessor<*>
    operator fun get(rootName: String): ResProcessor<*>?
    fun <T : ResProcessor<*>> get(clazz: Class<*>): T?

    fun getAll(format: String): List<ResProcessor<*>>
    fun <T : AttrProcessor<*>> getAll(clazz: Class<*>): List<T>

    fun <T> process(node: Node?, careNameAttr: Boolean? = null): T? {
        if (node == null) {
            return null
        }
        return processors.find { it.rootName == node.name }?.process(node, careNameAttr)?.value() as T?
    }

    fun process2(node: Node?, careNameAttr: Boolean? = null): ResProcessor.NodeValue<*>? {
        if (node == null) {
            return null
        }
        return processors.find { it.rootName == node.name }?.process(node, careNameAttr)
    }

    fun inflate(activity: Activity, node: Node?, careNameAttr: Boolean? = null): Boolean {
        if (node == null) {
            return false
        }
        val processor = processors.find { it.rootName == node.name } as BaseViewParser<View>? ?: return false
        val parent = activity.findViewById<ViewGroup>(android.R.id.content)
        processor.p = parent
        val view = processor.process(node, careNameAttr)?.value() ?: return false
        if (parent != null) {
            view.layoutParams = ViewParserHelper.generateDefaultLayoutParams(parent)
            processor.notifyWithState(view, BaseViewParser.AFTER_LAYOUT_PARAMS)
        }
        activity.setContentView(view)
        if (parent == null) {
            processor.notifyWithState(view, BaseViewParser.AFTER_LAYOUT_PARAMS)
        }
        processor.notifyWithState(view, BaseViewParser.AFTER_ADDED)
        return true
    }

    fun inflater(parent: ViewGroup, node: Node?, careNameAttr: Boolean? = null): Boolean {
        if (node == null) {
            return false
        }
        val processor = processors.find { it.rootName == node.name } as BaseViewParser<View>? ?: return false
        processor.p = parent
        val view = processor.process(node, careNameAttr)?.value() ?: return false
        view.layoutParams = ViewParserHelper.generateDefaultLayoutParams(parent)
        processor.notifyWithState(view, BaseViewParser.AFTER_LAYOUT_PARAMS, parent)
        parent.addView(view)
        processor.notifyWithState(view, BaseViewParser.AFTER_ADDED, parent)
        return true
    }
}

open class ResProcessorManager(
        override var context: Context, override var processors: MutableList<ResProcessor<*>> = mutableListOf()
) : IResProcessorManager {
    override fun plus(processor: ResProcessor<*>): Boolean = processors.add(processor)
    override fun plus(processors: Collection<ResProcessor<*>>): Boolean = this.processors.addAll(processors)

    override fun minus(processor: ResProcessor<*>): Boolean = processors.remove(processor)
    override fun minus(index: Int): ResProcessor<*> = processors.removeAt(index)
    override fun minus(processors: Collection<ResProcessor<*>>): Boolean = this.processors.removeAll(processors)

    override fun get(index: Int): ResProcessor<*> = processors[index]
    override fun get(rootName: String): ResProcessor<*>? = processors.find { it.rootName == rootName }
    override fun <T : ResProcessor<*>> get(clazz: Class<*>): T? = processors.filterIsInstance(clazz).getOrNull(0) as? T

    override fun getAll(format: String): List<ResProcessor<*>> = processors.toList()
    override fun <T : AttrProcessor<*>> getAll(clazz: Class<*>): List<T> = processors.filterIsInstance(clazz) as List<T>
}

object ManagerHolder {
    lateinit var apm: IAttrProcessorManager
    lateinit var rpm: IResProcessorManager
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
open class ValuesResProcessor : ResProcessor<List<ResProcessor.NodeValue<*>>>(
        "resources", true, false, ResType.VALUES) {
    open val itemResProcessor = ItemResProcessor()
    open val styleResProcessor = StyleResProcessor()
    open val attrResProcessor = AttrResProcessor()
    open val declareStyleResProcessor = DeclareStyleResProcessor()
    open val drawableResProcessor = DrawableResProcessor()
    open val arrayResProcessor = ArrayResProcessor()
    open val dimenResProcessor = DimenResProcessor()
    open val colorResProcessor = ColorResProcessor()
    open val boolResProcessor = BoolResProcessor()
    open val fractionResProcessor = FractionResProcessor()
    open val intResProcessor = IntResProcessor()
    open val intArrayResProcessor = IntArrResProcessor()
    open val strResProcessor = StrResProcessor()
    open val strArrResProcessor = StrArrResProcessor()
    open val pluralsResProcessor = PluralsResProcessor()

    override fun innerProcess(node: Node): List<NodeValue<*>>? {
        val list = mutableListOf<NodeValue<*>>()
        node.children.forEach {
            val temp: NodeValue<*> = when (it.name) {
                "item" -> itemResProcessor.process(it)
                "style" -> styleResProcessor.process(it)
                "attr" -> attrResProcessor.process(it)
                "declare-styleable" -> declareStyleResProcessor.process(it)
                "drawable" -> drawableResProcessor.process(it)
                "array" -> arrayResProcessor.process(it)
                "dimen", "dimension" -> dimenResProcessor.process(it)
                "color" -> colorResProcessor.process(it)
                "bool", "boolean" -> boolResProcessor.process(it)
                "fraction" -> fractionResProcessor.process(it)
                "integer", "int" -> intResProcessor.process(it)
                "integer-array" -> intArrayResProcessor.process(it)
                "string", "str" -> strResProcessor.process(it)
                "string-array" -> strArrResProcessor.process(it)
                "plurals" -> pluralsResProcessor.process(it)
                else -> return makeFail("unknown resources's element: ${it.name}", null)
            } ?: return null
            list.add(temp)
        }
        return list
    }

    open class ItemResProcessor : ResProcessor<Any>("item", false, true, ResType.ITEM) {
        override fun innerProcess(node: Node): Any? {
            TODO("注意 reference") //To change body of created functions use File | Settings | File Templates.
        }
    }

    open class StyleResProcessor : ResProcessor<Any>("style", true, true, ResType.STYLE) {
        override fun innerProcess(node: Node): Any? {
            TODO("注意 reference") //To change body of created functions use File | Settings | File Templates.
        }
    }

    open class AttrResProcessor : ResProcessor<Any>("attr", false, true, ResType.ATTR) {
        override fun innerProcess(node: Node): Any? {
            TODO("注意 reference") //To change body of created functions use File | Settings | File Templates.
        }
    }

    open class DeclareStyleResProcessor : ResProcessor<Any>("declare-styleable", true, true, ResType.DECLARE_STYLEABLE) {
        override fun innerProcess(node: Node): Any? {
            TODO("注意 reference") //To change body of created functions use File | Settings | File Templates.
        }
    }

    open class DrawableResProcessor : ResProcessor<Int>("drawable", false, true, ResType.DRAWABLE) {
        override fun innerProcess(node: Node): Int? = refer(node.text)
    }

    open class ArrayResProcessor : ResProcessor<Any>("attr", true, true, ResType.ARRAY) {
        override fun innerProcess(node: Node): Any? {
            TODO("注意 reference") //To change body of created functions use File | Settings | File Templates.
        }
    }

    open class DimenResProcessor : ResProcessor<Float>("dimen", false, true, ResType.DIMEN, "dimension") {
        override fun innerProcess(node: Node): Float? = dimen2(node.text)
    }

    open class ColorResProcessor : ResProcessor<Int>("color", false, true, ResType.COLOR) {
        override fun innerProcess(node: Node): Int? = color2(node.text)
    }

    open class BoolResProcessor : ResProcessor<Boolean>("bool", false, true, ResType.BOOL, "boolean") {
        override fun innerProcess(node: Node): Boolean? = bool2(node.text)
    }

    open class FractionResProcessor : ResProcessor<Pair<Int, Float>>("fraction", false, true, ResType.FRACTION) {
        override fun innerProcess(node: Node): Pair<Int, Float>? = fractionValue2(node.text)
    }

    open class IntResProcessor : ResProcessor<Int>("integer", false, true, ResType.INTEGER, "int") {
        override fun innerProcess(node: Node): Int? = int2(node.text)
    }

    open class IntArrResProcessor : ResProcessor<IntArray>("integer-array", true, true, ResType.INTEGER_ARRAY) {
        override fun innerProcess(node: Node): IntArray? {
            return node.children.map { int2(it.text) ?: return makeFail("integer array's item text is incorrect") }.toIntArray()
        }
    }

    open class StrResProcessor : ResProcessor<String>("string", false, true, ResType.STRING, "str") {
        override fun innerProcess(node: Node): String? = str2(node.text)
    }

    open class StrArrResProcessor : ResProcessor<Array<String>>("string-array", true, true, ResType.STRING_ARRAY) {
        override fun innerProcess(node: Node): Array<String>? {
            return node.children.map { str2(it.text) ?: return makeFail("string array's item text is incorrect") }.toTypedArray()
        }
    }

    open class PluralsResProcessor : ResProcessor<Map<Int, String>>("plurals", true, true, ResType.PLURALS) {
        override fun innerProcess(node: Node): Map<Int, String>? {
            return node.children.map { item ->
                (enum(item, Attrs.Resources2.quantity) ?: return makeFail(
                        "the quantity attribute of plurals's item is incorrect: ${item[Attrs.Resources2.quantity]}, ${Attrs.Resources2.quantity}")) to
                        (str2(item.text) ?: return makeFail("plurals's item text is incorrect: ${item.text}"))
            }.toMap()
        }
    }

    companion object {
        const val PLURALS_ZERO = 0L
        const val PLURALS_ONE = 1L
        const val PLURALS_TWO = 2L
        const val PLURALS_FEW = 3L
        const val PLURALS_MANY = 4L
        const val PLURALS_OTHER = 5L
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
open class ColorSelectorResProcessor : ResProcessor<ColorStateList>(
        "selector", true, false, ResType.COLOR_STATE_LIST) {
    override fun innerProcess(node: Node): ColorStateList? {
        val stateSpecs = mutableListOf<IntArray>()
        val colors = mutableListOf<Int>()
        node.children.forEach { item ->
            if (item.name == "item") {
                var color = color2(item[Attrs.Color.color])
                        ?: return makeFail("color selector's item' color attribute should has color value")
                val alpha = float2(item[Attrs.Color.alpha])
                if (alpha != null) {
                    color = Color.argb((alpha * 255).toInt(), Color.red(color), Color.green(color), Color.blue(color))
                }
                stateSpecs.add((getStates(item, this, arrayOf(Attrs.Color.color, Attrs.Color.alpha)) ?: return@forEach).toIntArray())
                colors.add(color)
            }
        }
        return ColorStateList(stateSpecs.toTypedArray(), colors.toIntArray())
    }

    companion object {
        val states = mutableMapOf(
                Attrs.Color.state_accelerated.name to android.R.attr.state_accelerated,
                Attrs.Color.state_activated.name to android.R.attr.state_activated,
                Attrs.Color.state_active.name to android.R.attr.state_active,
                Attrs.Color.state_checkable.name to android.R.attr.state_checkable,
                Attrs.Color.state_checked.name to android.R.attr.state_checked,
                Attrs.Color.state_drag_can_accept.name to android.R.attr.state_drag_can_accept,
                Attrs.Color.state_drag_hovered.name to android.R.attr.state_drag_hovered,
                Attrs.Color.state_enabled.name to android.R.attr.state_enabled,
                Attrs.Color.state_first.name to android.R.attr.state_first,
                Attrs.Color.state_focused.name to android.R.attr.state_focused,
                Attrs.Color.state_hovered.name to android.R.attr.state_hovered,
                Attrs.Color.state_last.name to android.R.attr.state_last,
                Attrs.Color.state_middle.name to android.R.attr.state_middle,
                Attrs.Color.state_pressed.name to android.R.attr.state_pressed,
                Attrs.Color.state_selected.name to android.R.attr.state_selected,
                Attrs.Color.state_single.name to android.R.attr.state_single,
                Attrs.Color.state_window_focused.name to android.R.attr.state_window_focused
        )

        fun getStates(node: Node, resProcessor: ResProcessor<*>, otherAttrs: Array<Attr>): MutableList<Int>? {
            val otherAttrNames = otherAttrs.map { it.name }
            val itemStates = mutableListOf<Int>()
            node.attributes.forEach { attr ->
                val attrName = attr.name
                if (states.containsKey(attrName)) {
                    val value = resProcessor.bool2(attr.value)
                            ?: return makeFail("color selector's item's attribute's value is incorrect: ${attr.value}")
                    itemStates.add(when {
                        value -> states[attrName]!!
                        else -> -states[attrName]!!
                    })
                } else if (attrName !in otherAttrNames) {
                    makeFail<Unit>("color selector's item has incorrect attribute: $attrName")!!
                }
            }
            return itemStates
        }
    }
}

// open class GradientColorResProcessor(_attrProcessorManager: IAttrProcessorManager) : ResProcessor<android.content.res.GradientColor>(_attrProcessorManager, "color")
// android.content.res.GradientColor is hidden

/**
 * menu @[package:]menu.filename
 * <?xml version="1.0" encoding="utf-8"?>
 * <menu xmlns:android="http://schemas.android.com/apk/res/android">
 *     <item android:id="@[+][package:]id/resource_name"
 *           android:title="string"
 *           android:titleCondensed="string"
 *           android:icon="@[package:]drawable/drawable_resource_name"
 *           android:onClick="method name"
 *           android:showAsAction=["ifRoom" | "never" | "withText" | "always" | "collapseActionView"]
 *           android:actionLayout="@[package:]layout/layout_resource_name"
 *           android:actionViewClass="class name"
 *           android:actionProviderClass="class name"
 *           android:alphabeticShortcut="string"
 *           android:alphabeticModifiers=["META" | "CTRL" | "ALT" | "SHIFT" | "SYM" | "FUNCTION"]
 *           android:numericShortcut="string"
 *           android:numericModifiers=["META" | "CTRL" | "ALT" | "SHIFT" | "SYM" | "FUNCTION"]
 *           android:checkable=["true" | "false"]
 *           android:visible=["true" | "false"]
 *           android:enabled=["true" | "false"]
 *           android:menuCategory=["container" | "system" | "secondary" | "alternative"]
 *           android:orderInCategory="integer" />
 *     <group android:id="@[+][package:]id/resource name"
 *            android:checkableBehavior=["none" | "all" | "single"]
 *            android:visible=["true" | "false"]
 *            android:enabled=["true" | "false"]
 *            android:menuCategory=["container" | "system" | "secondary" | "alternative"]
 *            android:orderInCategory="integer" >
 *         <item />
 *     </group>
 *     <item >
 *         <menu>
 *           <item />
 *         </menu>
 *     </item>
 * </menu>
 */
open class MenuResProcessor : ResProcessor<Menu>(
        "menu", true, true, ResType.MENU) {
    override fun innerProcess(node: Node): Menu? {
        TODO("注意 reference") //To change body of created functions use File | Settings | File Templates.
    }
} // TODO

/**
 * font @[package:]font/font_name
 * <font-family>
 *     <font
 *         android:font="@[package:]font/font_to_include"
 *         android:fontStyle=["normal" | "italic"]
 *         android:fontWeight="weight_value" />
 * </font-family>
 */
open class FontResProcessor : ResProcessor<Typeface>(
        "font", true, true, ResType.FONT) {
    override fun innerProcess(node: Node): Typeface? {
        TODO("注意 reference") //To change body of created functions use File | Settings | File Templates.
    }
}

/**
 * custom
 */
