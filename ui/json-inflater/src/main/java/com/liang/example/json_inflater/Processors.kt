@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.liang.example.json_inflater

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.animation.Animation
import androidx.annotation.IntDef

// TODO: 理解
/**
 * precompile / staticPreCompile 是为了方便对 value 的各种 handle ，而 compile 则是直接将 value 转换为目的类型
 */
abstract class NAttributeProcessor<V : NView<*>?> {
    companion object {
        fun evaluate(context: Context, input: Value?, data: Value?, index: Int): Value {
            val processor = DefaultImpl(context, data, index)
            processor.process(null, input)
            return processor.output ?: NullV.nullV
        }

        fun staticPreCompile(value: Value?, context: Context, funcManager: FuncManager?): Value? = when (value) {
            is PrimitiveV -> staticPreCompile(value, context, funcManager)
            is ObjectV -> staticPreCompile(value)
            is BindingV, is ResourceV, is AttributeResourceV, is StyleResourceV -> value
            else -> null
        }

        fun staticPreCompile(value: ObjectV): Value? = value["@"]?.let { BindingV.NestedBinding(it) }

        fun staticPreCompile(value: PrimitiveV, context: Context, funcManager: FuncManager?): Value? {
            val str = value.string()
            return when {
                BindingV.isBindingValue(str) -> BindingV.valueOf(str, context, funcManager)
                ResourceV.isResource(str) -> ResourceV.valueOf(str, null, context)
                AttributeResourceV.isAttributeResource(str) -> AttributeResourceV.valueOf(str, context)
                StyleResourceV.isStyleResource(str) -> StyleResourceV.valueOf(str, context)
                else -> null
            }
        }
    }

    abstract fun handleValue(view: V, value: Value?)
    abstract fun handleResource(view: V, resource: ResourceV)
    abstract fun handleStyleResource(view: V, style: StyleResourceV)
    abstract fun handleAttributeResource(view: V, attribute: AttributeResourceV)

    open fun handleBinding(view: V, binding: BindingV) = if (view != null) {
        val dataContext = view.nManager!!.dataContext
        handleValue(view, evaluate(binding, view.viewContext, dataContext.data, dataContext.index))
    } else Unit

    open fun process(view: V, value: Value?) = when (value) {
        is BindingV -> handleBinding(view, value)
        is ResourceV -> handleResource(view, value)
        is StyleResourceV -> handleStyleResource(view, value)
        is AttributeResourceV -> handleAttributeResource(view, value)
        else -> handleValue(view, value)
    }

    open fun compile(value: Value?, context: Context) = value
    protected fun evaluate(binding: BindingV, context: Context, data: Value?, index: Int) = binding.evaluate(context, data, index)
    open fun precompile(value: Value?, context: Context, funcManager: FuncManager) =
            staticPreCompile(value, context, funcManager) ?: compile(value, context)

    open fun precompile(value: Value?, view: V) =
            if (view != null) {
                staticPreCompile(value, view.viewContext, (view.viewContext as NContext).getFuncManager()) ?: compile(value, view.viewContext)
            } else null

    // 这里的 DefaultImpl 配合 Companion 里面的 evaluate ，方便 Binding 的 arguments 的转换
    class DefaultImpl(private val context: Context, private val data: Value?, private val index: Int) : NAttributeProcessor<NView<*>?>() {
        var output: Value? = null

        private fun innerSetOutput(output: Value?) {
            this.output = output
        }

        private fun innerGetValue(v: String?): Value = when {
            v != null -> PrimitiveV(v)
            else -> NullV.nullV
        }

        override fun handleBinding(view: NView<*>?, binding: BindingV) = innerSetOutput(binding.evaluate(context, data, index))
        override fun handleValue(view: NView<*>?, value: Value?) = innerSetOutput(value)
        override fun handleResource(view: NView<*>?, resource: ResourceV) = innerSetOutput(innerGetValue(ResourceV.getString(resource.resId, context)))
        override fun handleStyleResource(view: NView<*>?, style: StyleResourceV) = innerSetOutput(innerGetValue(style.apply(context).getString(0)))
        override fun handleAttributeResource(view: NView<*>?, attribute: AttributeResourceV) = innerSetOutput(innerGetValue(attribute.apply(context).getString(0)))
    }
}

abstract class NBooleanAttributeProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = when {
        value is PrimitiveV && value.isBoolean() -> setBoolean(view, value.toBoolean())
        else -> process(view, precompile(value, view))
    }

    override fun handleResource(view: V, resource: ResourceV) =
            setBoolean(view, ResourceV.getBoolean(resource.resId, view.viewContext) ?: false)

    override fun handleStyleResource(view: V, style: StyleResourceV) = setBoolean(view, style.apply(view.viewContext).getBoolean(0, false))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setBoolean(view, attribute.apply(view.viewContext).getBoolean(0, false))
    override fun compile(value: Value?, context: Context): Value? = if (parseBoolean(value)) PrimitiveV.TRUE_V else PrimitiveV.FALSE_V

    abstract fun setBoolean(view: V, value: Boolean)

    companion object {
        fun <V : NView<*>> create(action: (view: V, value: Boolean) -> Unit) = object : NBooleanAttributeProcessor<V>() {
            override fun setBoolean(view: V, value: Boolean) = action(view, value)
        }
    }
}

abstract class NColorResourceProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = when (value) {
        is ColorV -> apply(view, value)
        else -> process(view, precompile(value, view))
    }

    override fun handleResource(view: V, resource: ResourceV) {
        val colors = ResourceV.getColorStateList(resource.resId, view.viewContext)
        if (colors != null) {
            setColor(view, colors)
        } else {
            setColor(view, ResourceV.getColor(resource.resId, view.viewContext) ?: ColorV.IntV.BLACK.value)
        }
    }

    override fun handleStyleResource(view: V, style: StyleResourceV) = set(view, style.apply(view.viewContext))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = set(view, attribute.apply(view.viewContext))
    override fun compile(value: Value?, context: Context): Value? = staticCompile(value, context)

    abstract fun setColor(view: V, color: Int)
    abstract fun setColor(view: V, colors: ColorStateList)

    private fun set(view: V, a: TypedArray) {
        val colors = a.getColorStateList(0)
        if (colors != null) {
            setColor(view, colors)
        } else {
            setColor(view, a.getColor(0, ColorV.IntV.BLACK.value))
        }
    }

    private fun apply(view: V, color: ColorV) {
        val result = color.apply(view.viewContext)
        when {
            result.colors != null -> setColor(view, result.colors)
            else -> setColor(view, result.color!!)
        }
    }

    companion object {
        fun staticCompile(value: Value?, context: Context): Value = when (value) {
            null -> ColorV.IntV.BLACK
            is ColorV -> value
            is ObjectV -> ColorV.valueOf(value)
            is PrimitiveV -> staticPreCompile(value, context, null) ?: ColorV.valueOf(value.string())
            else -> ColorV.IntV.BLACK
        }

        fun evaluate(value: Value?, view: NView<*>): ColorV.Result {
            val processor = DefaultColorImpl()
            processor.process(view, value)
            return processor.result
        }

        fun <V : NView<*>> create(action1: (view: V, color: Int) -> Unit, action2: (view: V, color: ColorStateList) -> Unit) = object : NColorResourceProcessor<V>() {
            override fun setColor(view: V, color: Int) = action1(view, color)
            override fun setColor(view: V, colors: ColorStateList) = action2(view, colors)
        }
    }

    class DefaultColorImpl : NColorResourceProcessor<NView<*>>() {
        lateinit var result: ColorV.Result

        override fun setColor(view: NView<*>, color: Int) {
            result = ColorV.Result(color, null)
        }

        override fun setColor(view: NView<*>, colors: ColorStateList) {
            result = ColorV.Result(null, colors)
        }
    }
}

abstract class NDimensionAttributeProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = when (value) {
        is DimensionV -> setDimension(view, value.apply(view.viewContext))
        is PrimitiveV -> process(view, precompile(value, view))
        else -> Unit
    }

    override fun handleResource(view: V, resource: ResourceV) =
            setDimension(view, ResourceV.getDimension(resource.resId, view.viewContext) ?: 0f)

    override fun handleStyleResource(view: V, style: StyleResourceV) = setDimension(view, style.apply(view.viewContext).getDimension(0, 0f))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setDimension(view, attribute.apply(view.viewContext).getDimension(0, 0f))
    override fun compile(value: Value?, context: Context): Value? = staticCompile(value, context)

    abstract fun setDimension(view: V, dimension: Float)

    companion object {
        fun staticCompile(value: Value?, context: Context): Value? = when (value) {
            null -> DimensionV.ZERO
            is DimensionV -> value
            else -> staticPreCompile(value, context, null) ?: DimensionV.valueOf(value.string())
        }

        fun evaluate(value: Value?, view: NView<*>): Float = when (value) {
            null -> DimensionV.ZERO.apply(view.viewContext)
            else -> {
                val processor = DefaultDimenImpl()
                processor.process(view, value)
                processor.result
            }
        }

        fun <V : NView<*>> create(action: (view: V, dimension: Float) -> Unit) = object : NDimensionAttributeProcessor<V>() {
            override fun setDimension(view: V, dimension: Float) = action(view, dimension)
        }
    }

    class DefaultDimenImpl : NDimensionAttributeProcessor<NView<*>>() {
        var result: Float = 0f
        override fun setDimension(view: NView<*>, dimension: Float) {
            result = dimension
        }
    }
}

abstract class NDrawableResourceProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) {
        if (value is DrawableV) {
            val loader = view.nManager!!.nContext.loader ?: return
            value.apply(view, view.viewContext, loader, object : DrawableV.Callback {
                override fun apply(drawable: Drawable?) {
                    if (drawable != null) {
                        setDrawable(view, drawable)
                    }
                }
            })
        } else {
            process(view, precompile(value, view))
        }
    }

    override fun handleResource(view: V, resource: ResourceV) {
        val d = ResourceV.getDrawable(resource.resId, view.viewContext)
        if (d != null) {
            setDrawable(view, d)
        }
    }

    override fun handleStyleResource(view: V, style: StyleResourceV) = set(view, style.apply(view.viewContext))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = set(view, attribute.apply(view.viewContext))
    override fun compile(value: Value?, context: Context): Value? = staticCompile(value, context)

    private fun set(view: V, a: TypedArray) {
        val d = a.getDrawable(0)
        if (d != null) {
            setDrawable(view, d)
        }
    }

    abstract fun setDrawable(view: V, drawable: Drawable)

    companion object {
        fun staticCompile(value: Value?, context: Context): Value = when (value) {
            null -> DrawableV.ColorDrawV.BLACK
            is DrawableV -> value
            is PrimitiveV -> staticPreCompile(value, context, null) ?: DrawableV.valueOf(value.string()) ?: DrawableV.ColorDrawV.BLACK
            is ObjectV -> DrawableV.valueOf(value.string())
            else -> DrawableV.ColorDrawV.BLACK
        }

        fun evaluate(value: Value?, view: NView<*>): Drawable? {
            if (value == null) {
                return null
            }
            val processor = DefaultDrawableImpl()
            processor.process(view, value)
            return processor.drawable
        }

        fun <V : NView<*>> create(action: (view: V, drawable: Drawable) -> Unit) = object : NDrawableResourceProcessor<V>() {
            override fun setDrawable(view: V, drawable: Drawable) = action(view, drawable)
        }
    }

    class DefaultDrawableImpl : NDrawableResourceProcessor<NView<*>>() {
        lateinit var drawable: Drawable

        override fun setDrawable(view: NView<*>, drawable: Drawable) {
            this.drawable = drawable
        }
    }
}

abstract class NEventProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = setOnEventListener(view, value)
    override fun handleResource(view: V, resource: ResourceV) = setOnEventListener(view, resource)
    override fun handleStyleResource(view: V, style: StyleResourceV) = setOnEventListener(view, style)
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setOnEventListener(view, attribute)

    abstract fun setOnEventListener(view: V, value: Value?)

    open fun trigger(event: String, value: Value?, view: NView<*>) = view.nManager!!.nContext.callback?.onEvent(event, value, view) ?: Unit

    companion object {
        fun <V : NView<*>> create(action: (view: V, value: Value?, thisObj: NEventProcessor<V>) -> Unit) = object : NEventProcessor<V>() {
            override fun setOnEventListener(view: V, value: Value?) = action(view, value, this)
        }
    }
}

abstract class NGravityAttributeProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = setGravity(view, when {
        value is PrimitiveV && value.isNumber() -> value.toInt()
        value is PrimitiveV -> Util.parseGravity(value.string())
        else -> android.view.Gravity.NO_GRAVITY
    })

    override fun handleResource(view: V, resource: ResourceV) =
            setGravity(view, ResourceV.getInteger(resource.resId, view.viewContext) ?: android.view.Gravity.NO_GRAVITY)

    override fun handleStyleResource(view: V, style: StyleResourceV) = setGravity(view, style.apply(view.viewContext).getInt(0, android.view.Gravity.NO_GRAVITY))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setGravity(view, attribute.apply(view.viewContext).getInt(0, android.view.Gravity.NO_GRAVITY))
    override fun compile(value: Value?, context: Context): Value? = when (value) {
        null -> NO_GRAVITY
        else -> Util.getGravity(value.string())
    }

    private fun set(view: V, a: TypedArray) = setGravity(view, a.getInt(0, android.view.Gravity.NO_GRAVITY))

    @SuppressLint("RtlHardcoded")
    @IntDef(android.view.Gravity.NO_GRAVITY, android.view.Gravity.TOP, android.view.Gravity.BOTTOM, android.view.Gravity.LEFT, android.view.Gravity.RIGHT,
            android.view.Gravity.START, android.view.Gravity.END, android.view.Gravity.CENTER_VERTICAL, android.view.Gravity.FILL_VERTICAL,
            android.view.Gravity.CENTER_HORIZONTAL, android.view.Gravity.FILL_HORIZONTAL, android.view.Gravity.CENTER, android.view.Gravity.FILL)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER,
            AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.LOCAL_VARIABLE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
    annotation class Gravity

    abstract fun setGravity(view: V, @Gravity gravity: Int)

    companion object {
        val NO_GRAVITY = PrimitiveV(android.view.Gravity.NO_GRAVITY)

        fun <V : NView<*>> create(action: (view: V, gravity: Int) -> Unit) = object : NGravityAttributeProcessor<V>() {
            override fun setGravity(view: V, @Gravity gravity: Int) = action(view, gravity)
        }
    }
}

abstract class NNumberAttributeProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = when (value) {
        is PrimitiveV -> setNumber(view, value.toNumber())
        else -> Unit
    }

    override fun handleResource(view: V, resource: ResourceV) = setNumber(view, ResourceV.getInteger(resource.resId, view.viewContext) ?: 0f)
    override fun handleStyleResource(view: V, style: StyleResourceV) = setNumber(view, style.apply(view.viewContext).getFloat(0, 0f))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setNumber(view, attribute.apply(view.viewContext).getFloat(0, 0f))

    abstract fun setNumber(view: V, value: Number)

    companion object {
        fun <V : NView<*>> create(action: (view: V, value: Number) -> Unit) = object : NNumberAttributeProcessor<V>() {
            override fun setNumber(view: V, value: Number) = action(view, value)
        }
    }
}

abstract class NStringAttributeProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = setString(view, when (value) {
        is PrimitiveV, is NullV -> value.string()
        else -> "[Object]"
    })

    override fun handleResource(view: V, resource: ResourceV) = setString(view, ResourceV.getString(resource.resId, view.viewContext))
    override fun handleStyleResource(view: V, style: StyleResourceV) = setString(view, style.apply(view.viewContext).getString(0))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setString(view, attribute.apply(view.viewContext).getString(0))
    override fun compile(value: Value?, context: Context): Value? = when (value) {
        null, is NullV -> PrimitiveV.EMPTY_STR
        else -> value
    }

    abstract fun setString(view: V, value: String?)

    companion object {
        fun <V : NView<*>> create(action: (view: V, value: String?) -> Unit) = object : NStringAttributeProcessor<V>() {
            override fun setString(view: V, value: String?) = action(view, value)
        }
    }
}

abstract class NTweenAnimResourceProcessor<V : NView<*>> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) {
        val animation = AnimationUtils.loadAnimation(view.viewContext, value)
        if (null != animation) {
            setAnimation(view, animation)
        } else {
            Log.e("NTweenAnimResourceProcessor", "Animation Resource must be a primitive or an object. value -> " + value.toString())
        }
    }

    override fun handleResource(view: V, resource: ResourceV) = TODO("not implemented")
    override fun handleStyleResource(view: V, style: StyleResourceV) = TODO("not implemented")
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = TODO("not implemented")
    override fun compile(value: Value?, context: Context): Value? = TODO("not implemented")

    abstract fun setAnimation(view: V, animation: Animation?)

    companion object {
        fun <V : NView<*>> create(action: (view: V, animation: Animation?) -> Unit) = object : NTweenAnimResourceProcessor<V>() {
            override fun setAnimation(view: V, animation: Animation?) = action(view, animation)
        }
    }
}
