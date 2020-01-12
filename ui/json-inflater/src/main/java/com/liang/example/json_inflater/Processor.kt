package com.liang.example.json_inflater

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.view.View

// TODO: 理解
/**
 * precompile / staticPreCompile 是为了方便对 value 的各种 handle ，而 compile 则是直接将 value 转换为目的类型
 */
abstract class NAttributeProcessor<V : View?> {
    companion object {
        fun evaluate(context: Context, input: Value?, data: Value?, index: Int): Value {
            val processor = DefaultImpl(context, data, index)
            processor.process(null, input)
            return processor.output ?: NullV.nullV
        }

        fun staticPreCompile(value: Value?, context: Context, funcManager: FuncManager?): Value? = when (value) {
            is PrimitiveV -> staticPreCompile(value, context, funcManager)
            is ObjectV -> staticPreCompile(value, context, funcManager)
            is BindingV, is ResourceV, is AttributeResourceV, is StyleResourceV -> value
            else -> null
        }

        fun staticPreCompile(value: ObjectV, context: Context, funcManager: FuncManager?): Value? = value["@"]?.let { BindingV.NestedBinding(it) }

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

    open fun handleBinding(view: V, binding: BindingV) = if (view is NView) {
        val dataContext = view.nViewManager.dataContext
        handleValue(view, evaluate(binding, view.context, dataContext.data, dataContext.index))
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

    // 这里的 DefaultImpl 配合 Companion 里面的 evaluate ，方便 Binding 的 arguments 的转换
    class DefaultImpl(private val context: Context, private val data: Value?, private val index: Int) : NAttributeProcessor<View?>() {
        var output: Value? = null

        private fun innerSetOutput(output: Value?) {
            this.output = output
        }

        private fun innerGetValue(v: String?): Value = when {
            v != null -> PrimitiveV(v)
            else -> NullV.nullV
        }

        override fun handleBinding(view: View?, binding: BindingV) = innerSetOutput(binding.evaluate(context, data, index))
        override fun handleValue(view: View?, value: Value?) = innerSetOutput(value)
        override fun handleResource(view: View?, resource: ResourceV) = innerSetOutput(innerGetValue(ResourceV.getString(resource.resId, context)))
        override fun handleStyleResource(view: View?, style: StyleResourceV) = innerSetOutput(innerGetValue(style.apply(context).getString(0)))
        override fun handleAttributeResource(view: View?, attribute: AttributeResourceV) = innerSetOutput(innerGetValue(attribute.apply(context).getString(0)))
    }
}

abstract class NBooleanResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = when {
        value is PrimitiveV && value.isBoolean() -> setBoolean(view, value.toBoolean())
        else -> process(view, precompile(value, view.context, (view.context as NContext).getFuncManager()))
    }

    override fun handleResource(view: V, resource: ResourceV) =
            setBoolean(view, ResourceV.getBoolean(resource.resId, view.context) ?: false)

    override fun handleStyleResource(view: V, style: StyleResourceV) = setBoolean(view, style.apply(view.context).getBoolean(0, false))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setBoolean(view, attribute.apply(view.context).getBoolean(0, false))
    override fun compile(value: Value?, context: Context): Value? = if (parseBoolean(value)) PrimitiveV.TRUE_V else PrimitiveV.FALSE_V

    abstract fun setBoolean(view: V, value: Boolean)
}

abstract class NColorResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = when (value) {
        is ColorV -> apply(view, value)
        else -> process(view, precompile(value, view.context, (view.context as NContext).getFuncManager()))
    }

    override fun handleResource(view: V, resource: ResourceV) {
        val colors = ResourceV.getColorStateList(resource.resId, view.context)
        if (colors != null) {
            setColor(view, colors)
        } else {
            setColor(view, ResourceV.getColor(resource.resId, view.context) ?: ColorV.IntV.BLACK.value)
        }
    }

    override fun handleStyleResource(view: V, style: StyleResourceV) = set(view, style.apply(view.context))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = set(view, attribute.apply(view.context))
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
        val result = color.apply(view.context)
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

        fun evaluate(value: Value?, view: View): ColorV.Result {
            val processor = DefaultColorImpl()
            processor.process(view, value)
            return processor.result
        }
    }

    class DefaultColorImpl : NColorResourceProcessor<View>() {
        lateinit var result: ColorV.Result

        override fun setColor(view: View, color: Int) {
            result = ColorV.Result(color, null)
        }

        override fun setColor(view: View, colors: ColorStateList) {
            result = ColorV.Result(null, colors)
        }
    }
}

abstract class NDimensionResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = when (value) {
        is DimensionV -> setDimension(view, value.apply(view.context))
        is PrimitiveV -> process(view, precompile(value, view.context, (view.context as NContext).getFuncManager()))
        else -> Unit
    }

    override fun handleResource(view: V, resource: ResourceV) =
            setDimension(view, ResourceV.getDimension(resource.resId, view.context) ?: 0f)

    override fun handleStyleResource(view: V, style: StyleResourceV) = setDimension(view, style.apply(view.context).getDimension(0, 0f))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setDimension(view, attribute.apply(view.context).getDimension(0, 0f))
    override fun compile(value: Value?, context: Context): Value? = staticCompile(value, context)

    abstract fun setDimension(view: V, dimension: Float)

    companion object {
        fun staticCompile(value: Value?, context: Context): Value = when (value) {
            is DimensionV -> value
            null, !is PrimitiveV -> DimensionV.ZERO
            else -> staticPreCompile(value, context, null) ?: DimensionV.valueOf(value.string())
        }

        fun evaluate(value: Value?, view: View): Float = when (value) {
            null -> DimensionV.ZERO.apply(view.context)
            else -> {
                val processor = DefaultDimenImpl()
                processor.process(view, value)
                processor.result
            }
        }
    }

    class DefaultDimenImpl : NDimensionResourceProcessor<View>() {
        var result: Float = 0f
        override fun setDimension(view: View, dimension: Float) {
            result = dimension
        }
    }
}

abstract class NDrawableResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = TODO("not implemented")
    override fun handleResource(view: V, resource: ResourceV) = TODO("not implemented")
    override fun handleStyleResource(view: V, style: StyleResourceV) = TODO("not implemented")
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = TODO("not implemented")
    override fun compile(value: Value?, context: Context): Value? = TODO("not implemented")
}

abstract class NEventResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = TODO("not implemented")
    override fun handleResource(view: V, resource: ResourceV) = TODO("not implemented")
    override fun handleStyleResource(view: V, style: StyleResourceV) = TODO("not implemented")
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = TODO("not implemented")
    override fun compile(value: Value?, context: Context): Value? = TODO("not implemented")
}

abstract class NGravityResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = TODO("not implemented")
    override fun handleResource(view: V, resource: ResourceV) = TODO("not implemented")
    override fun handleStyleResource(view: V, style: StyleResourceV) = TODO("not implemented")
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = TODO("not implemented")
    override fun compile(value: Value?, context: Context): Value? = TODO("not implemented")
}

abstract class NNumberResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = when (value) {
        is PrimitiveV -> setNumber(view, value.toNumber())
        else -> Unit
    }

    override fun handleResource(view: V, resource: ResourceV) = setNumber(view, ResourceV.getInteger(resource.resId, view.context))
    override fun handleStyleResource(view: V, style: StyleResourceV) = setNumber(view, style.apply(view.context).getFloat(0, 0f))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setNumber(view, attribute.apply(view.context).getFloat(0, 0f))

    abstract fun setNumber(view: V, value: Number)
}

abstract class NStringResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = setString(view, when (value) {
        is PrimitiveV, is NullV -> value.string()
        else -> "[Object]"
    })

    override fun handleResource(view: V, resource: ResourceV) = setString(view, ResourceV.getString(resource.resId, view.context))
    override fun handleStyleResource(view: V, style: StyleResourceV) = setString(view, style.apply(view.context).getString(0))
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = setString(view, attribute.apply(view.context).getString(0))
    override fun compile(value: Value?, context: Context): Value? = when (value) {
        null, is NullV -> PrimitiveV.EMPTY_STR
        else -> value
    }

    abstract fun setString(view: V, value: String?)
}

abstract class NTweenAnimResourceProcessor<V : View> : NAttributeProcessor<V>() {
    override fun handleValue(view: V, value: Value?) = TODO("not implemented")
    override fun handleResource(view: V, resource: ResourceV) = TODO("not implemented")
    override fun handleStyleResource(view: V, style: StyleResourceV) = TODO("not implemented")
    override fun handleAttributeResource(view: V, attribute: AttributeResourceV) = TODO("not implemented")
    override fun compile(value: Value?, context: Context): Value? = TODO("not implemented")
}
