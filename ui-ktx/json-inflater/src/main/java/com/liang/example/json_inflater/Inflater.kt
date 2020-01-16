@file:Suppress("unused", "MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package com.liang.example.json_inflater

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.XmlResourceParser
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.liang.example.basic_ktx.KKMap
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

open class FuncManager(open val cache: MutableMap<String, Func>) {
    abstract class Func {
        abstract fun getName(): String
        abstract fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value

        companion object {
            val INVALID = object : Func() {
                override fun getName(): String = "invalid"
                override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value = PrimitiveV.EMPTY_STR
            }

            val SLICE = object : Func() {
                override fun getName(): String = "slice"
                override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value {
                    val pSize = values.size
                    if (pSize != 2 || pSize != 3) {
                        return ArrayV()
                    }

                    val inArr = values[0] as ArrayV
                    val size = inArr.size()
                    if (size == 0) {
                        return ArrayV()
                    }

                    val start = handleInt(size, (values[1] as PrimitiveV).toInt())
                    val end = when (pSize) {
                        2 -> size
                        3 -> handleInt(size, (values[2] as PrimitiveV).toInt())
                        else -> throw RuntimeException("slice's parameters' number is incorrect")
                    }
                    val out = ArrayV()
                    (start..end).forEach { out.add(inArr[it]) }
                    return out
                }

                private fun handleInt(size: Int, rightIndex: Int): Int {
                    val leftIndex = size - rightIndex
                    return when {
                        leftIndex < 0 -> 0
                        leftIndex > size -> size
                        else -> leftIndex
                    }
                }
            }

            val MIN = object : Func() {
                override fun getName(): String = "min"
                override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value = PrimitiveV(values.map { (it as PrimitiveV).toDouble() }.min()!!)
            }

            val MAX = object : Func() {
                override fun getName(): String = "max"
                override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value = PrimitiveV(values.map { (it as PrimitiveV).toDouble() }.max()!!)
            }

            val TRIM = object : Func() {
                override fun getName(): String = "trim"
                override fun call(context: Context, data: Value?, dataIndex: Int, vararg values: Value): Value = PrimitiveV((values[0] as PrimitiveV).string().trim())
            }

            // TODO: 真的有必要吗？？？
        }
    }

    operator fun get(name: String) = cache[name] ?: Func.INVALID
    operator fun set(name: String, value: Func) = cache.put(name, value)
}

abstract class NLayoutManager {
    protected abstract fun getLayouts(): Map<String, LayoutV?>?
    open operator fun get(name: String) = getLayouts()?.get(name)
}

abstract class NStyleManager {
    open class Styles : HashMap<String, MutableMap<String, Value?>?>() {
        open fun getStyle(name: String) = get(name)
        open fun contains(name: String) = containsKey(name)
    }

    abstract fun getStyles(): Styles?
    open operator fun get(name: String) = getStyles()?.get(name)
}

open class NResources(
        open val parsers: MutableMap<String, ViewTypeParser<*>>,
        open val funcManager: FuncManager,
        open val layoutManager: NLayoutManager?,
        open val styleManager: NStyleManager?
) {
    open fun getFunc(name: String) = funcManager[name]
    open fun getLayout(name: String) = layoutManager?.get(name)
    open fun getStyle(name: String) = styleManager?.get(name)
}

open class NContext(
        context: Context,
        open val resources: NResources,
        open val callback: NInflater.Callback?,
        open val loader: NInflater.ImageLoader?
) : ContextWrapper(context) {
    private var inflater: NInflater? = null

    fun getFuncManager() = resources.funcManager
    fun getFunc(name: String) = resources.getFunc(name)
    fun getLayout(name: String) = resources.getLayout(name)
    fun getParser(type: String) = resources.parsers[type]
    fun getStyle(name: String) = resources.getStyle(name)
    fun getInflater(idGenerator: ViewIdGenerator<String>? = ViewIdGenerator.SimpleIdGenerator()): NInflater {
        if (inflater == null) {
            if (idGenerator == null) {
                throw RuntimeException("first get inflater need idGenerator")
            }
            inflater = SimpleInflater(this, idGenerator)
        }
        return inflater!!
    }

    open class Builder(open val context: Context, open val parsers: MutableMap<String, ViewTypeParser<*>>, open val funcManager: FuncManager) {
        open var callback: NInflater.Callback? = null
        open var loader: NInflater.ImageLoader? = null
        open var layoutManager: NLayoutManager? = null
        open var styleManager: NStyleManager? = null

        open fun build(): NContext = NContext(context, NResources(parsers, funcManager, layoutManager, styleManager), callback, loader)
    }

    @SuppressLint("CI_ByteDanceKotlinRules_Class_Camel_Case")
    open class NContextWrapper(context: NContext) : NContext(context, context.resources, context.callback, context.loader)
}

@Suppress("LeakingThis")
open class DataContext {
    open val hasOwnProperties: Boolean
    open val scope: MutableMap<String, Value>?
    open var data: ObjectV? = null
    open val index: Int

    constructor(scope: MutableMap<String, Value>?, index: Int) {
        this.scope = scope
        this.index = index
        this.hasOwnProperties = scope != null
    }

    constructor(dataContext: DataContext) {
        this.scope = dataContext.scope
        this.index = dataContext.index
        this.data = dataContext.data
        this.hasOwnProperties = false
    }

    open fun update(context: NContext, data: ObjectV?): DataContext {
        val data2 = data ?: ObjectV()
        if (scope == null) {
            this.data = data2
            return this
        }
        this.data = ObjectV()
        scope!!.forEach { (key, value) ->
            this.data!![key] = when (value) {
                is BindingV -> value.evaluate(context, this.data!!, index) ?: value.evaluate(context, data2, index)
                else -> value
            }
        }
        return this
    }

    open fun createChild(context: NContext, scope: MutableMap<String, Value>, index: Int) = DataContext(scope, index).update(context, data)
    open fun copy() = DataContext(this)

    companion object {
        fun create(context: NContext, data: ObjectV?, index: Int, scope: MutableMap<String, Value>? = null) = DataContext(scope, index).update(context, data)
    }
}

interface NView<V : View> {
    @SuppressLint("CI_ByteDanceKotlinRules_Class_Camel_Case")
    interface NManager {
        fun update(data: ObjectV?)
        fun findViewById(id: String): View?

        val nContext: NContext
        val layoutV: LayoutV
        val dataContext: DataContext
        var extra: Any?
    }

    val view: V
    var nManager: NManager?
    val viewContext: Context
        get() = view.context
}

@Suppress("ImplicitThis", "LeakingThis")
open class NViewManager(
        override val nContext: NContext,
        open val viewTypeParser: ViewTypeParser<*>,
        open val view: NView<*>,
        override val layoutV: LayoutV,
        override val dataContext: DataContext
) : NView.NManager {
    protected val boundAttributes: List<BoundAttribute>?
    override var extra: Any? = null

    init {
        if (!layoutV.attributes.isNullOrEmpty()) {
            val boundAttributes = layoutV.attributes!!.filter { it is BindingV }.map { BoundAttribute(it.id, it.value as BindingV) }
            this.boundAttributes = when {
                boundAttributes.isEmpty() -> null
                else -> boundAttributes
            }
        } else {
            this.boundAttributes = null
        }
    }

    override fun update(data: ObjectV?) {
        if (data != null) {
            updateDataContext(data)
        }
        this.boundAttributes?.forEach { handleBinding(it) }
    }

    override fun findViewById(id: String): View? = view.view.findViewById(nContext.getInflater().getUniqueViewId(id))

    open fun updateDataContext(data: ObjectV) = when {
        dataContext.hasOwnProperties -> {
            dataContext.update(nContext, data)
            Unit
        }
        else -> dataContext.data = data
    }

    open fun handleBinding(boundAttribute: BoundAttribute) = (viewTypeParser as ViewTypeParser<NView<*>>).handleAttribute(view, boundAttribute.id, boundAttribute.bindingV)

    data class BoundAttribute(val id: Int, val bindingV: BindingV)

    @SuppressLint("CI_ByteDanceKotlinRules_Class_Camel_Case")
    open class NViewManagerWrapper(val manager: NView.NManager) : NView.NManager {
        override fun update(data: ObjectV?) = manager.update(data)
        override fun findViewById(id: String): View? = manager.findViewById(id)

        override val nContext: NContext
            get() = manager.nContext
        override val layoutV: LayoutV
            get() = manager.layoutV
        override val dataContext: DataContext
            get() = manager.dataContext
        override var extra: Any?
            get() = manager.extra
            set(value) {
                manager.extra = value
            }
    }
}

open class NViewGroupManager(nContext: NContext, viewTypeParser: ViewTypeParser<*>, view: NView<*>, layoutV: LayoutV, dataContext: DataContext)
    : NViewManager(nContext, viewTypeParser, view, layoutV, dataContext) {
    var hasDataBoundChildren = false

    override fun update(data: ObjectV?) {
        super.update(data)
        updateChildren()
    }

    protected open fun updateChildren() {
        if (!hasDataBoundChildren && view is ViewGroup) {
            val parent = view as ViewGroup
            val count = parent.childCount
            var child: View?
            for (index in 0 until count) {
                child = parent.getChildAt(index)
                if (child is NView<*>) {
                    child.nManager!!.update(dataContext.data)
                }
            }
        }
    }
}

open class AdapterBasedViewManager(nContext: NContext, viewTypeParser: ViewTypeParser<*>, view: NView<*>, layoutV: LayoutV, dataContext: DataContext)
    : NViewGroupManager(nContext, viewTypeParser, view, layoutV, dataContext) {
    override fun updateChildren() = Unit
}

// TODO: ViewTypeParser<V2 : View, V : NView<V2>>
abstract class ViewTypeParser<V : NView<*>> {
    open var parent: ViewTypeParser<*>? = null
    open var processors: MutableList<NAttributeProcessor<V>> = mutableListOf()
    open var attributes: MutableMap<String, Attribute> = mutableMapOf()
    private var offset: Int = 0
    protected var attributeSet: AttributeSet? = null

    abstract fun getType(): String
    abstract fun getParentType(): String?
    abstract fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*>
    protected abstract fun addAttributeProcessors()

    open fun createViewManager(context: NContext, view: NView<*>, layoutV: LayoutV, data: ObjectV, caller: ViewTypeParser<*>?, parent: ViewGroup?, dataIndex: Int): NView.NManager =
            when {
                this.parent != null && caller != this.parent -> this.parent!!.createViewManager(context, view, layoutV, data, caller, parent, dataIndex)
                else -> NViewManager(context, caller ?: this, view, layoutV, createDataContext(context, layoutV, data, parent, dataIndex))
            }

    protected fun createDataContext(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): DataContext {
        val parentDataContext = when (parent) {
            is NView<*> -> parent.nManager!!.dataContext
            else -> null
        }
        return when {
            parentDataContext == null -> DataContext.create(context, data, dataIndex, layoutV.data)
            layoutV.data == null -> parentDataContext.copy()
            else -> parentDataContext.createChild(context, layoutV.data!!, dataIndex)
        }
    }

    open fun onAfterCreateView(view: NView<*>, parent: ViewGroup?, dataIndex: Int) {
        if (view.view.layoutParams == null) {
            view.view.layoutParams = when {
                parent != null -> generateDefaultLayoutParams(parent)
                else -> ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    protected fun generateDefaultLayoutParams(parent: ViewGroup): ViewGroup.LayoutParams {
        if (sparser == null) {
            synchronized(ViewTypeParser::class.java) {
                initializeAttributeSet(parent)
            }
        }
        return parent.generateLayoutParams(sparser)
    }

    protected fun initializeAttributeSet(parent: ViewGroup) {
        sparser = parent.resources.getLayout(R.layout.layout_params_hack)
        try {
            while (sparser!!.nextToken() != XmlPullParser.START_TAG) {
                // Skip everything until the view tag.
            }
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    open fun handleAttribute(view: V, id: Int, value: Value): Boolean {
        val pos = id + offset
        if (pos < 0) {
            return this.parent != null && (this.parent!! as ViewTypeParser<NView<*>>).handleAttribute(view, id, value)
        }
        processors[pos].process(view, value)
        return true
    }

    open fun prepare(parent: ViewTypeParser<*>?, extras: Map<String, NAttributeProcessor<V>>?): AttributeSet {
        this.parent = parent
        this.offset = parent?.attributeSet?.offset ?: 0
        addAttributeProcessors()
        if (extras != null) {
            addAttributeProcessors(extras)
        }
        this.attributeSet = AttributeSet(if (attributes.isNotEmpty()) attributes else null, parent?.attributeSet, processors.size)
        return attributeSet!!
    }

    open fun handleChildren(view: V, children: Value?): Boolean = this.parent != null && (this.parent!! as ViewTypeParser<NView<*>>).handleChildren(view, children)
    open fun addView(parent: NView<*>?, view: NView<*>): Boolean = this.parent != null && this.parent!!.addView(parent, view)
    open fun getAttributeId(name: String) = attributeSet!![name]?.id ?: -1

    protected fun addAttributeProcessors(map: Map<String, NAttributeProcessor<V>>) = map.forEach { addAttributeProcessor(it.key, it.value) }
    protected fun addAttributeProcessor(name: String, processor: NAttributeProcessor<V>) {
        processors.add(processor)
        attributes[name] = Attribute(processors.size - 1 - offset, processor)
    }

    companion object {
        var sparser: XmlResourceParser? = null
    }

    data class Attribute(val id: Int, val processor: NAttributeProcessor<*>)

    open class AttributeSet(private val attributes: Map<String, Attribute>?, private val parent: AttributeSet?, offset: Int) {
        val offset: Int = (parent?.offset ?: 0) - offset
        open operator fun get(name: String): Attribute? = attributes?.get(name) ?: parent?.get(name)
    }
}

interface ViewIdGenerator<T> {
    fun getUniqueViewId(id: T): Int

    @SuppressLint("CI_ByteDanceKotlinRules_Parcelable_Annotation")
    open class SimpleIdGenerator : ViewIdGenerator<String>, Parcelable {
        protected val idMap: KKMap<String, Int>
        protected val sNextGeneratedId: AtomicInteger

        constructor() {
            sNextGeneratedId = AtomicInteger(1)
            idMap = KKMap()
        }

        @SuppressLint("UseSparseArrays")
        constructor(parcel: Parcel) {
            sNextGeneratedId = AtomicInteger(parcel.readInt())
            val kvMap = HashMap<String, Int>()
            val vkMap = HashMap<Int, String>()
            parcel.readMap(kvMap as Map<*, *>, null)
            parcel.readMap(vkMap as Map<*, *>, null)
            idMap = KKMap(kvMap, vkMap)
        }

        override fun getUniqueViewId(id: String): Int {
            var result = idMap[id]
            if (result == null) {
                result = generateViewId()
                idMap[id] = result
            }
            return result
        }

        open fun generateViewId(): Int {
            while (true) {
                val result = sNextGeneratedId.get()
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF) {
                    newValue = 1 // Roll over to 1, not 0.
                }
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result
                }
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(sNextGeneratedId.get())
            parcel.writeMap(idMap.kvMap as Map<*, *>)
            parcel.writeMap(idMap.vkMap as Map<*, *>)
        }

        override fun describeContents(): Int = 0

        @SuppressLint("CI_ByteDanceKotlinRules_Class_Camel_Case")
        companion object CREATOR : Parcelable.Creator<SimpleIdGenerator> {
            override fun createFromParcel(parcel: Parcel): SimpleIdGenerator = SimpleIdGenerator(parcel)
            override fun newArray(size: Int): Array<SimpleIdGenerator?> = arrayOfNulls(size)
        }
    }
}

interface NInflater {
    fun inflate(layoutV: LayoutV, data: ObjectV, parent: ViewGroup? = null, dataIndex: Int = -1): NView<*>
    fun inflate(name: String, data: ObjectV, parent: ViewGroup? = null, dataIndex: Int = -1): NView<*>
    fun getViewTypeParser(type: String): ViewTypeParser<*>?
    fun getUniqueViewId(id: String): Int
    fun getStrIdGenerator(): ViewIdGenerator<String>

    interface Callback {
        fun onUnknownViewType(nContext: NContext, type: String, layoutV: LayoutV, data: ObjectV, dataIndex: Int): NView<*>?
        fun onEvent(event: String, value: Value?, nView: NView<*>)
    }

    interface ImageLoader {
        fun getBitmap(nView: NView<*>, url: String, callback: DrawableV.AsyncCallback)
        fun getBitmap(nView: NView<*>, url: String, callback: DrawableV.Callback)
    }
}

open class SimpleInflater(open val context: NContext, open val idGenerator: ViewIdGenerator<String>) : NInflater {
    protected fun createView(parser: ViewTypeParser<*>, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int) =
            parser.createView(context, layoutV, data, parent, dataIndex)

    protected fun createViewManager(parser: ViewTypeParser<*>, view: NView<*>, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int) =
            parser.createViewManager(context, view, layoutV, data, parser, parent, dataIndex)

    protected fun onAfterCreateView(parser: ViewTypeParser<*>, view: NView<*>, parent: ViewGroup?, dataIndex: Int) = parser.onAfterCreateView(view, parent, dataIndex)

    protected fun onUnknownViewEncountered(type: String, layoutV: LayoutV, data: ObjectV, dataIndex: Int): NView<*> {
        if (debug) {
            Log.d("SimpleInflater", "No ViewTypeParser for: $type")
        }
        if (context.callback != null) {
            return context.callback!!.onUnknownViewType(context, type, layoutV, data, dataIndex)
                    ?: throw RuntimeException("inflater Callback#onUnknownViewType() must not return null")
        }
        throw RuntimeException("Layout contains type: 'include' but inflater callback is null")
    }

    protected fun handleAttribute(parser: ViewTypeParser<*>, view: NView<*>, attribute: Int, value: Value): Boolean {
        if (debug) {
            Log.d("SimpleInflater", "Handle '$attribute' : $value")
        }
        return (parser as ViewTypeParser<NView<*>>).handleAttribute(view, attribute, value)
    }

    override fun inflate(layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        val parser = getViewTypeParser(layoutV.type) ?: return onUnknownViewEncountered(layoutV.type, layoutV, data, dataIndex)
        val view = createView(parser, layoutV, data, parent, dataIndex)
        if (view.nManager != null) {
            onAfterCreateView(parser, view, parent, dataIndex)
            view.nManager = createViewManager(parser, view, layoutV, data, parent, dataIndex)
        }
        layoutV.attributes?.forEach { handleAttribute(parser, view, it.id, it.value) }
        return view
    }

    override fun inflate(name: String, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> =
            inflate(context.getLayout(name) ?: throw RuntimeException("layout '$name' not found"), data, parent, dataIndex)

    override fun getViewTypeParser(type: String): ViewTypeParser<*>? = context.getParser(type)
    override fun getUniqueViewId(id: String): Int = idGenerator.getUniqueViewId(id)
    override fun getStrIdGenerator(): ViewIdGenerator<String> = idGenerator
}
