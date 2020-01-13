package com.liang.example.json_inflater

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import com.liang.example.basic_ktx.KKMap
import java.util.concurrent.atomic.AtomicInteger

abstract class NLayoutManager {
    protected abstract fun getLayouts(): Map<String?, LayoutV?>?
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
        open val parsers: MutableMap<String, ViewTypeParser>,
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
    fun getInflater(idGenerator: ViewIdGenerator<String>? = SimpleIdGenerator()): NInflater {
        if (inflater == null) {
            if (idGenerator == null) {
                throw RuntimeException("first get inflater need idGenerator")
            }
            inflater = SimpleInflater(this, idGenerator)
        }
        return inflater!!
    }

    open class Builder(open val context: Context, open val parsers: MutableMap<String, ViewTypeParser>, open val funcManager: FuncManager) {
        open var callback: NInflater.Callback? = null
        open var loader: NInflater.ImageLoader? = null
        open var layoutManager: NLayoutManager? = null
        open var styleManager: NStyleManager? = null

        open fun build(): NContext = NContext(context, NResources(parsers, funcManager, layoutManager, styleManager), callback, loader)
    }
}

@Suppress("LeakingThis")
open class DataContext {
    // TODO()
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

    open fun createChild(context: NContext, scope: MutableMap<String, Value>, index: Int) = create(context, data, index, scope)
    open fun copy() = DataContext(this)

    companion object {
        fun create(context: NContext, data: ObjectV?, index: Int, scope: MutableMap<String, Value>? = null) =
                DataContext(scope, index).update(context, data)
    }
}

interface NView {
    @SuppressLint("CI_ByteDanceKotlinRules_Class_Camel_Case")
    interface NViewManager {
        fun update(data: ObjectV?)
        fun findViewById(id: String): View?

        val nContext: NContext
        val layoutV: LayoutV
        val dataContext: DataContext
        var extra: Any?
    }

    val view: View
    var nViewManager: NViewManager
    val context: Context
        get() = view.context
}

abstract class ViewTypeParser

interface ViewIdGenerator<T> {
    fun getUniqueViewId(id: T): Int
}

interface NInflater {
    fun inflate(layoutV: LayoutV, data: ObjectV, dataIndex: Int = -1, parent: ViewGroup? = null)
    fun inflate(name: String, data: ObjectV, dataIndex: Int = -1, parent: ViewGroup? = null)
    fun getViewTypeParser(type: String): ViewTypeParser?
    fun getUniqueViewId(id: String): Int
    fun getStrIdGenerator(): ViewIdGenerator<String>

    interface Callback {
        fun onUnknownViewType(nContext: NContext, type: String, layoutV: LayoutV, data: ObjectV, dataIndex: Int)
        fun onEvent(event: String, value: Value?, nView: NView)
    }

    interface ImageLoader {
        fun getBitmap(nView: NView, url: String, callback: DrawableV.AsyncCallback)
        fun getBitmap(nView: NView, url: String, callback: DrawableV.Callback)
    }
}

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

open class SimpleInflater(open val context: NContext, open val idGenerator: ViewIdGenerator<String>) : NInflater {
    override fun inflate(layoutV: LayoutV, data: ObjectV, dataIndex: Int, parent: ViewGroup?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun inflate(name: String, data: ObjectV, dataIndex: Int, parent: ViewGroup?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewTypeParser(type: String): ViewTypeParser? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUniqueViewId(id: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStrIdGenerator(): ViewIdGenerator<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
