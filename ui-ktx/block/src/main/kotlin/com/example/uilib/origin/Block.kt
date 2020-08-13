@file:Suppress("UNCHECKED_CAST", "unused", "MemberVisibilityCanBePrivate", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "LeakingThis")

package com.example.uilib.origin

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor

open class WhiteBoard {
    private val data: MutableMap<String, Any> = ConcurrentHashMap()
    private val subjectMap: MutableMap<String, Subject<*>?> = ConcurrentHashMap()

    open fun <T> getObservable(key: String): Observable<T?>? {
        val res: Subject<T>
        if (subjectMap.containsKey(key)) {
            res = subjectMap[key] as Subject<T>
        } else {
            res = PublishSubject.create<T>()
            subjectMap[key] = res
        }
        return if (getData(key) != null) {
            res.startWith(getData(key)!! as T)
        } else {
            res
        }
    }

    open fun notifyDataChanged(key: String) {
        if (subjectMap.containsKey(key)) {
            (subjectMap[key] as Subject<Any>).onNext(when {
                getData(key) == null -> sNullObject
                else -> getData(key)!!
            })
        }
    }

    open fun removeData(key: String) {
        data.remove(key)
        notifyDataChanged(key)
    }

    open fun getData(key: String): Any? = data[key]

    open fun putData(key: String, value: Any?) {
        if (value == null) {
            removeData(key)
        } else {
            data[key] = value
            notifyDataChanged(key)
        }
    }

    open fun putDataWithoutNotify(key: String, value: Any?) {
        if (value == null) {
            data.remove(key)
        } else {
            data[key] = value
        }
    }

    open fun putBundle(arguments: Bundle?) {
        if (arguments == null) {
            return
        }
        for (key in arguments.keySet()) {
            putData(key, arguments[key])
        }
    }

    open fun putIntent(intent: Intent?) {
        if (intent == null) {
            return
        }
        putBundle(intent.extras)
    }

    private val classData: MutableMap<Class<*>, Any> = ConcurrentHashMap()
    private val classSubjectMap: MutableMap<Class<*>, Subject<*>?> = ConcurrentHashMap()
    open fun <T> getData(tClass: Class<T>): T? {
        for (s in classData.keys) {
            if (tClass.isAssignableFrom(s)) {
                return classData[s] as T?
            }
        }
        return null
    }

    open fun <T> getObservable(key: Class<T>): Observable<T?>? {
        val res: Subject<T>
        if (classSubjectMap.containsKey(key)) {
            res = classSubjectMap[key] as Subject<T>
        } else {
            res = PublishSubject.create<T>()
            classSubjectMap[key] = res
        }
        return if (getData(key) != null) {
            res.startWith(getData(key))
        } else {
            res
        }
    }

    open fun <T : Any> putData(value: T?) {
        if (value == null) {
            return
        }
        classData[value.javaClass] = value
        notifyDataChanged(value.javaClass)
    }

    open fun <T : Any> putDataWithoutNotify(value: T?) {
        if (value == null) {
            return
        }
        classData[value.javaClass] = value
    }

    open fun removeData(tClass: Class<*>) {
        for (s in classData.keys) {
            if (tClass.isAssignableFrom(s)) {
                classData.remove(s)
            }
        }
        notifyDataChanged(tClass)
    }

    open fun notifyDataChanged(key: Class<*>) {
        val o: Any? = getData(key)
        for (s in classSubjectMap.keys) {
            if (s.isAssignableFrom(key)) {
                (classSubjectMap[s] as Subject<Any>).onNext(o ?: sNullObject)
            }
        }
    }

    companion object {
        var sNullObject = Any()
    }
}

interface Callable<V> {
    fun call(): V
}

interface Consumer<T> {
    fun accept(t: T)
}

abstract class Block : LifecycleOwner {
    var mWhiteBoard: WhiteBoard? = null
    val handler: Handler = Handler(Looper.getMainLooper())
    private val mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    protected var mBlockManager: BlockManager? = null
    var mParent: BlockGroup? = null
    var context: Context? = null
        protected set
    protected var mInflater: LayoutInflater? = null
    var view: View? = null
        protected set
    var left = -1
    var top = -1
    var right = -1
    var bottom = -1
    var backgroundColor = 0
    var isCreated = false
    protected var isResumed = false
    protected var isDestroyed = false

    override fun getLifecycle(): Lifecycle = mLifecycleRegistry

    open fun setBackgroundColor(@ColorInt backgroundColor: Int): Block? {
        this.backgroundColor = backgroundColor
        initOtherUI()
        return this
    }

    open fun setPadding(left: Int, top: Int, right: Int, bottom: Int): Block? {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        initOtherUI()
        return this
    }

    protected open fun initOtherUI() {
        if (view != null) {
            if (left >= 0 && top >= 0 && right >= 0 && bottom >= 0) {
                view!!.setPadding(left, top, right, bottom)
            }
            if (backgroundColor != 0) {
                view!!.setBackgroundColor(backgroundColor)
            }
        }
    }

    open fun blockName(): String? {
        return this.javaClass.simpleName
    }

    protected open fun onCreateView(inflater: LayoutInflater?, parent: ViewGroup?): View? {
        return null
    }

    open fun create(parentView: ViewGroup?, context: Context?, whiteBoard: WhiteBoard?,
                    inflater: LayoutInflater?, blockManager: BlockManager?): Boolean {
        this.context = context
        mInflater = inflater
        mWhiteBoard = whiteBoard
        mBlockManager = blockManager
        if (!onCreate()) {
            return false
        }
        val viewProducer: Callable<View?> = object : Callable<View?> {
            override fun call(): View? {
                Log.d(TAG_VIEW, Thread.currentThread().toString() + "create View -> " + this)
                if (view == null) {
                    return onCreateView(mInflater, parentView)
                }
                return null
            }
        }
        val viewConsumer: Consumer<View?> = object : Consumer<View?> {
            override fun accept(view: View?) {
                Log.d(TAG_VIEW, Thread.currentThread().toString() + "after create view-> " + this)
                if (view != null) {
                    this@Block.view = view
                    if (view.parent == null && !isPendingAdd) {
                        mParent?.addView(this@Block)
                    }
                    initOtherUI()
                }
                isCreated = true
                beforeOnViewCreate()
                onViewCreated()
                mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                onRefresh()
                afterOnViewCreate()
            }
        }
        if (createAsync()) {
            blockManager?.runAsync(viewProducer, viewConsumer)
        } else {
            viewConsumer.accept(viewProducer.call())
        }
        return true
    }

    protected open fun beforeOnViewCreate() {}
    protected open fun afterOnViewCreate() {
        when (mBlockManager?.state) {
            LifecycleFragment.State.Start -> onStart()
            LifecycleFragment.State.Resume -> {
                onStart()
                onResume()
            }
            LifecycleFragment.State.Pause -> {
                onStart()
                onResume()
                onPause()
            }
            LifecycleFragment.State.Stop -> {
                onStart()
                onResume()
                onPause()
                onStop()
            }
            LifecycleFragment.State.Destroy -> {
                onDestroyView()
                onDestroy()
            }
            else -> {
            }
        }
    }

    protected val isPendingAdd: Boolean
        get() = false

    //=========== string类型 start ===========
    private fun getCompatibleClass(clazz: Class<*>?): Class<*>? {
        if (clazz == Boolean::class.javaPrimitiveType) {
            return Boolean::class.java
        }
        if (clazz == Int::class.javaPrimitiveType) {
            return Int::class.java
        }
        if (clazz == Long::class.javaPrimitiveType) {
            return Long::class.java
        }
        if (clazz == Short::class.javaPrimitiveType) {
            return Short::class.java
        }
        if (clazz == Double::class.javaPrimitiveType) {
            return Double::class.java
        }
        if (clazz == Float::class.javaPrimitiveType) {
            return Float::class.java
        }
        if (clazz == Byte::class.javaPrimitiveType) {
            return Byte::class.java
        }
        return if (clazz == Char::class.javaPrimitiveType) {
            Char::class.java
        } else clazz
    }

    open fun getObservable(event: String): Observable<*>? {
        return mWhiteBoard!!.getObservable<Any>(event)?.doOnSubscribe { disposables.add(it) }
    }

    open fun getObservableNotNull(event: String): Observable<*>? {
        return mWhiteBoard!!.getObservable<Any>(event)?.filter { o -> o !== WhiteBoard.sNullObject }?.doOnSubscribe { disposables.add(it) }
    }

    open fun <T : Any> getObservable(@NonNull event: String, @NonNull tClass: Class<T>): Observable<T?>? {
        val compatibleClass: Class<T>? = getCompatibleClass(tClass) as Class<T>
        return mWhiteBoard!!.getObservable<T>(event)
                ?.filter { o: T? -> o != null && compatibleClass!!.isAssignableFrom(o::class.java) }
                ?.doOnSubscribe { disposables.add(it) }
    }

    open fun <T : Any> getObservableNotNull(@NonNull event: String, @NonNull tClass: Class<T>): Observable<T?>? {
        val compatibleClass: Class<T>? = getCompatibleClass(tClass) as Class<T>
        return mWhiteBoard!!.getObservable<T>(event)
                ?.filter {  o: T? -> o != null && o !== WhiteBoard.sNullObject && compatibleClass!!.isAssignableFrom(o.javaClass) }
                ?.doOnSubscribe { disposables.add(it) }
    }

    open fun notifyData(key: String?) {
        mWhiteBoard!!.notifyDataChanged(key!!)
    }

    open fun removeData(key: String?) {
        mWhiteBoard!!.removeData(key!!)
    }

    open fun putData(key: String?, value: Any?) {
        mWhiteBoard!!.putData(key!!, value)
    }

    open fun putDataWithoutNotify(key: String?, value: Any?) {
        mWhiteBoard!!.putDataWithoutNotify(key!!, value)
    }

    open fun <T : Any> getData(key: String, @NonNull defaultValue: T): T? {
        val o = mWhiteBoard!!.getData(key) ?: return defaultValue
        val compatibleClass: Class<T> = getCompatibleClass(defaultValue.javaClass) as Class<T>
        return if (compatibleClass.isAssignableFrom(o.javaClass)) {
            o as T
        } else {
            defaultValue
        }
    }

    open fun <T : Any> getData(key: String, @NonNull tClass: Class<T>): T? {
        val o = mWhiteBoard!!.getData(key) ?: return null
        val compatibleClass: Class<T>? = getCompatibleClass(tClass) as Class<T>
        return if (compatibleClass!!.isAssignableFrom(o.javaClass)) {
            o as T
        } else {
            null
        }
    }

    open fun getData(key: String?): Any? {
        return mWhiteBoard!!.getData(key!!)
    }

    open fun getLong(key: String?): Long {
        val value = mWhiteBoard!!.getData(key!!) ?: return 0
        if (value is String) {
            try {
                return value.toLong()
            } catch (e: NumberFormatException) {
            }
            return 0
        }
        return try {
            value as Long
        } catch (e: ClassCastException) {
            0
        }
    }

    open fun getInt(key: String?): Int {
        val value = mWhiteBoard!!.getData(key!!) ?: return 0
        if (value is String) {
            try {
                return value.toInt()
            } catch (e: NumberFormatException) {
            }
            return 0
        }
        return try {
            value as Int
        } catch (e: ClassCastException) {
            0
        }
    }

    open fun getShort(key: String?): Short {
        val value = mWhiteBoard!!.getData(key!!) ?: return 0
        if (value is String) {
            try {
                return value.toShort()
            } catch (e: NumberFormatException) {
            }
            return 0
        }
        return try {
            value as Short
        } catch (e: ClassCastException) {
            0
        }
    }

    open fun getDouble(key: String?): Double {
        val value = mWhiteBoard!!.getData(key!!) ?: return 0.0
        if (value is String) {
            try {
                return value.toDouble()
            } catch (e: NumberFormatException) {
            }
            return 0.0
        }
        return try {
            (value as Double)
        } catch (e: ClassCastException) {
            0.0
        }
    }

    open fun getFloat(key: String?): Float {
        val value = mWhiteBoard!!.getData(key!!) ?: return 0.0f
        if (value is String) {
            try {
                return value.toFloat()
            } catch (e: NumberFormatException) {
            }
            return 0.0f
        }
        return try {
            value as Float
        } catch (e: ClassCastException) {
            0.0f
        }
    }

    open fun getBoolean(key: String?): Boolean {
        val value = mWhiteBoard!!.getData(key!!) ?: return false
        if (value is String) {
            try {
                return java.lang.Boolean.parseBoolean(value)
            } catch (e: NumberFormatException) {
            }
            return false
        }
        return try {
            value as Boolean
        } catch (e: ClassCastException) {
            false
        }
    }

    open fun getString(key: String): String? {
        return getData(key, String::class.java)
    }

    //=========== string类型 end ===========
    //=========== class 类型 start===========
    open fun notifyData(key: Class<*>?) {
        mWhiteBoard!!.notifyDataChanged(key!!)
    }

    open fun removeData(key: Class<*>?) {
        mWhiteBoard!!.removeData(key!!)
    }

    open fun <T : Any> getObservable(event: Class<T>): Observable<T?>? {
        val compatibleClass: Class<T> = getCompatibleClass(event) as Class<T>
        return mWhiteBoard!!.getObservable(event)?.filter { o: T? -> o != null && compatibleClass.isAssignableFrom(o.javaClass) }
    }

    open fun <T : Any> getObservableNotNull(event: Class<T>): Observable<T?>? {
        val compatibleClass: Class<T> = getCompatibleClass(event) as Class<T>
        return mWhiteBoard!!.getObservable(event)!!.filter { o: T? -> o != null && compatibleClass.isAssignableFrom(o.javaClass) }
    }

    open fun <T> getData(tClass: Class<T?>?): T? {
        return mWhiteBoard!!.getData(tClass!!)
    }

    open fun putData(o: Any?) {
        mWhiteBoard!!.putData(o)
    }

    open fun putDataWithoutNotify(o: Any?) {
        mWhiteBoard!!.putDataWithoutNotify(o)
    }

    //=========== class 类型 end ===========
    //刷新相关
    open fun refreshBlock() {
        onRefresh()
    }

    protected open fun refreshPage() {
        mBlockManager?.refreshBlock()
    }

    protected open fun onRefresh() {}

    //生命周期
    protected open fun onViewCreated() {}
    open fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {}

    @CallSuper
    open fun onStart() {
        Log.d(TAG_VIEW, "onStart $this")
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    @TargetApi(Build.VERSION_CODES.M)
    @CallSuper
    open fun onResume() {
        Log.d(TAG_VIEW, "onResume $this")
        isResumed = true
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    @CallSuper
    open fun onPause() {
        isResumed = false
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    @CallSuper
    open fun onStop() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    @CallSuper
    open fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        disposables.clear()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    @CallSuper
    open fun onDestroy() {
        isDestroyed = true
    }

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    protected open val lifeCyclerOwner: LifecycleOwner?
        get() = this

    open fun <T : FragmentActivity> getActivity(): T? {
        return mBlockManager?.getActivity() as? T
    }

    open fun <T : Fragment> getFragment(): T? {
        return mBlockManager?.getFragment() as? T
    }

    open val fragmentManager: FragmentManager?
        get() = mBlockManager?.fragmentManager

    protected open fun startActivity(intent: Intent?) {
        getActivity<FragmentActivity>()?.startActivity(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    open fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        mBlockManager?.startActivityForResult(intent, requestCode, options)
    }

    open fun startActivityForResult(intent: Intent?, requestCode: Int) {
        mBlockManager?.startActivityForResult(intent, requestCode)
    }

    protected open fun finish() {
        if (getActivity<FragmentActivity>() != null) {
            getActivity<FragmentActivity>()!!.finish()
        }
    }

    protected open fun createAsync(): Boolean {
        return true
    }

    //是否添加到父block中
    @CallSuper
    open fun onCreate(): Boolean {
        Log.d(TAG_VIEW, "onCreate $this")
        return true
    }

    open class BlockAction<T : Any>(private val key: String, private val tClass: Class<T>) {
        open fun getObservableNotNull(block: Block): Observable<T?>? {
            return block.getObservableNotNull(key, tClass)
        }

        open fun getObservable(block: Block): Observable<T?>? {
            return block.getObservable(key, tClass)
        }

        open fun putData(block: Block, t: T?) {
            block.putData(key, t)
        }

        open fun notifyData(block: Block) {
            block.notifyData(key)
        }
    }

    private val disposables: CompositeDisposable = CompositeDisposable()
    protected open fun register(subscription: Disposable?) {
        disposables.add(subscription!!)
    }

    private fun doOnBlockRemoved() {
        when (mLifecycleRegistry.currentState) {
            Lifecycle.State.CREATED -> {
                onDestroyView()
                onDestroy()
            }
            Lifecycle.State.STARTED -> {
                onStop()
                onDestroyView()
                onDestroy()
            }
            Lifecycle.State.RESUMED -> {
                onPause()
                onStop()
                onDestroyView()
                onDestroy()
            }
            else -> {
            }
        }
    }

    open fun destroyBlock() {
        doOnBlockRemoved()
    }

    companion object {
        protected const val TAG_VIEW = "TAG_VIEW"
    }
}

open class BlockGroup : Block {
    private val mChildren = Collections.synchronizedList<Block>(ArrayList<Block>())
    protected var mContainer: ViewGroup? = null
    var mCurrentId = View.NO_ID

    constructor()

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int): BlockGroup {
        super.setPadding(left, top, right, bottom)
        return this
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int): BlockGroup {
        super.setBackgroundColor(backgroundColor)
        return this
    }

    override fun initOtherUI() {
        if (left >= 0 && top >= 0 && right >= 0 && bottom >= 0 && mContainer != null) {
            mContainer!!.setPadding(left, top, right, bottom)
        }
        if (view != null && backgroundColor != 0) {
            view!!.setBackgroundColor(backgroundColor)
        }
    }

    constructor(currentId: Int) {
        mCurrentId = currentId
    }

    open fun getBlock(index: Int): Block {
        return mChildren[index]
    }

    val leafBlocks: List<Block>
        get() {
            val leafBlocks: MutableList<Block> = ArrayList()
            for (child in mChildren) {
                if (child is BlockGroup) {
                    leafBlocks.addAll(child.leafBlocks)
                } else {
                    leafBlocks.add(child)
                }
            }
            return leafBlocks
        }

    override fun onCreateView(inflater: LayoutInflater?, parent: ViewGroup?): View? {
        if (mCurrentId != View.NO_ID) {
            mContainer = parent!!.findViewById<View>(mCurrentId) as ViewGroup
            if (mContainer == null) {
                throw RuntimeException("Split does not has this child:$mCurrentId")
            }
            return mContainer
        }
        return null
    }

    override fun beforeOnViewCreate() {
        if (mContainer == null) {
            mContainer = view as ViewGroup?
        }
        val it = mChildren.iterator()
        while (it.hasNext()) {
            val block = it.next()
            if (block == null || !block.create(mContainer, context, mWhiteBoard, mInflater, mBlockManager)) {
                it.remove()
            }
        }
    }

    open fun addBlockIf(condition: Boolean, block: Block?): BlockGroup {
        if (condition) {
            addBlock(block)
        }
        return this
    }

    open fun addBlock(block: Block?): BlockGroup {
        if (block == null) {
            return this
        }
        block.mParent = this
        //支持创建后添加
        if (isCreated) {
            mChildren.add(block)
            if (!block.create(mContainer, context, mWhiteBoard, mInflater, mBlockManager)) {
                mChildren.remove(block)
            }
        } else {
            mChildren.add(block)
        }
        return this
    }

    open fun addBlocks(blocks: List<Block?>): BlockGroup {
        for (block in blocks) {
            addBlock(block)
        }
        return this
    }

    /**
     * 移除 block
     *
     * @param id: 指定要 remove 的 Block内 view 的 根 id;
     * @return
     */
    open fun removeBlock(id: Int): Boolean {
        if (View.NO_ID == id) {
            return false
        }
        val childCount = mContainer!!.childCount
        var tobeReplacedView: View? = null
        for (i in 0 until childCount) {
            val childView = mContainer!!.getChildAt(i)
            if (childView.id == id) {
                tobeReplacedView = childView
                break
            }
        }
        if (tobeReplacedView == null) {
            return false
        }
        var tobeReplacedBlock: Block? = null
        for (child in mChildren) {
            if (tobeReplacedView === child.view) {
                tobeReplacedBlock = child
                break
            }
        }
        return tobeReplacedBlock?.let { removeBlock(it) } ?: false
    }

    /**
     * 删除 block
     *
     * @param oldBlockRef: 需要传入待删除 block 的引用
     * @return
     */
    open fun removeBlock(oldBlockRef: Block?): Boolean {
        if (oldBlockRef == null) {
            return false
        }
        val position = mChildren.indexOf(oldBlockRef)
        if (position < 0) {
            return false
        }
        mChildren.removeAt(position)
        if (oldBlockRef.view != null) {
            mContainer!!.removeView(oldBlockRef.view)
        }
        oldBlockRef.destroyBlock()
        return true
    }

    /**
     * 替换 Block
     *
     * @param oldBlockRef: 原 block 的引用
     * @param newBlock:    要替换的新 block
     * @return : 如果原 block 在当前 BlockGroup 中不存在,或者新 block 的 create 方法返回 false,表示替换失败;
     * 否则替换成功.
     */
    open fun replaceBlock(oldBlockRef: Block?, newBlock: Block?): Boolean {
        if (oldBlockRef == null || newBlock == null) {
            return false
        }
        val position = mChildren.indexOf(oldBlockRef)
        if (position < 0) {
            return false
        }
        newBlock.mParent = this
        //支持创建后添加
        return if (isCreated) {
            mChildren[position] = newBlock
            if (oldBlockRef.view != null) {
                mContainer!!.removeView(oldBlockRef.view)
            }
            oldBlockRef.destroyBlock()
            newBlock.create(mContainer, context, mWhiteBoard, mInflater, mBlockManager)
        } else {
            // parent 还未创建, child 只创建了 block 对象,还未创建 view,因此直接替换 block 即可
            mChildren[position] = newBlock
            oldBlockRef.destroyBlock()
            true
        }
    }

    @Deprecated("", ReplaceWith("findBlockGroupById(childId)"))
    open fun createBlockById(childId: Int): BlockGroup {
        return findBlockGroupById(childId)
    }

    open fun findBlockGroupById(childId: Int): BlockGroup {
        for (block in mChildren) {
            if (block is BlockGroup && block.mCurrentId == childId) {
                return block
            }
        }
        val moduleGroup = BlockGroup(childId)
        addBlock(moduleGroup)
        return moduleGroup
    }

    override fun refreshBlock() {
        super.refreshBlock()
        for (child in addedChildren) {
            child.refreshBlock()
        }
    }

    @CallSuper
    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        for (child in addedChildren) {
            child.onActivityCreated(savedInstanceState)
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        for (child in addedChildren) {
            child.onStart()
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        for (child in addedChildren) {
            child.onResume()
        }
    }

    @CallSuper
    override fun onPause() {
        for (child in addedChildren) {
            child.onPause()
        }
        super.onPause()
    }

    @CallSuper
    override fun onStop() {
        for (child in addedChildren) {
            child.onStop()
        }
        super.onStop()
    }

    @CallSuper
    override fun onDestroy() {
        for (child in addedChildren) {
            child.onDestroy()
        }
        super.onDestroy()
    }

    @CallSuper
    override fun onDestroyView() {
        for (child in addedChildren) {
            child.onDestroyView()
        }
        super.onDestroyView()
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        for (child in addedChildren) {
            child.onActivityResult(requestCode, resultCode, data)
        }
    }

    @CallSuper
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var res = false
        for (child in addedChildren) {
            res = res or child.onKeyDown(keyCode, event)
        }
        return res
    }

    open val addedChildren: List<Block>
        get() {
            val blocks: MutableList<Block> = ArrayList()
            for (mChild in mChildren) {
                if (mChild.isCreated) {
                    blocks.add(mChild)
                }
            }
            return blocks
        }

    fun addView(block: Block) {
        val view: View = block.view ?: return
        var lastBlock: Block? = null
        for (child in mChildren) {
            if (child === block) {
                break
            }
            if (child.view != null && child.view!!.parent === mContainer) {
                lastBlock = child
            }
        }
        if (lastBlock != null) {
            val position = mContainer!!.indexOfChild(lastBlock.view)
            mContainer!!.addView(view, position + 1)
        } else {
            mContainer!!.addView(view)
        }
    }
}

open class BlockManager : BlockGroup {
    private var mFragment: Fragment? = null
    private var mActivity: Activity? = null
    private var lifecycleFragment: LifecycleFragment? = null
    open var state: LifecycleFragment.State = LifecycleFragment.State.Idle

    constructor(fragment: Fragment) {
        mFragment = fragment
        context = fragment.context
        mWhiteBoard = WhiteBoard()
        if (fragment.activity != null) {
            mWhiteBoard!!.putIntent(fragment.activity!!.intent)
        }
        mWhiteBoard!!.putBundle(fragment.getArguments())
    }

    constructor(activity: FragmentActivity) {
        mActivity = activity
        context = activity
        mWhiteBoard = WhiteBoard()
        mWhiteBoard!!.putIntent(mActivity!!.intent)
    }

    constructor(blockManager: BlockManager) {
        mFragment = blockManager.getFragment()
        mActivity = blockManager.getActivity()
        context = blockManager.context
        mWhiteBoard = blockManager.mWhiteBoard
    }

    //提供给注入用
    open fun putAll(vararg payload: Any?) {
        if (!payload.isNullOrEmpty()) {
            for (element in payload) {
                mWhiteBoard!!.putData(element)
            }
        }
    }

    /**
     * 从资源获得View，并且切割
     *
     *
     * * @param resId the resourceId, either [.LINEAR_LAYOUT_H],
     * [.LINEAR_LAYOUT_V],[.FRAME_LAYOUT],[.SCROLL_VIEW]
     */
    open fun build(resId: Int): View {
        if (view != null) {
            if (view!!.parent != null) {
                (view!!.parent as ViewGroup).removeView(view)
            }
            ensureLifecycle()
            return view!!
        }
        when (resId) {
            LINEAR_LAYOUT_V -> {
                val linearLayoutV = LinearLayout(context)
                linearLayoutV.orientation = LinearLayout.VERTICAL
                initBlock(linearLayoutV, linearLayoutV)
            }
            LINEAR_LAYOUT_H -> {
                val linearLayoutH = LinearLayout(context)
                initBlock(linearLayoutH, linearLayoutH)
            }
            FRAME_LAYOUT -> {
                val frameLayout = FrameLayout(context!!)
                initBlock(frameLayout, frameLayout)
            }
            SCROLL_VIEW -> {
                val scrollView = ScrollView(context)
                val linearLayoutVV = LinearLayout(context)
                linearLayoutVV.orientation = LinearLayout.VERTICAL
                scrollView.addView(linearLayoutVV,
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                mContainer = linearLayoutVV
                initBlock(scrollView, linearLayoutVV)
            }
            CONSTAINT_LAYOUT -> {
                val constraintLayout = ConstraintLayout(context)
                initBlock(constraintLayout, constraintLayout)
            }
            else -> {
                val resView = LayoutInflater.from(context).inflate(resId, null, false) as ViewGroup
                initBlock(resView, resView)
            }
        }
        return view!!
    }

    open fun build(viewGroup: ViewGroup): View {
        if (view != null) {
            if (view!!.parent != null) {
                (view!!.parent as ViewGroup).removeView(view)
            }
            ensureLifecycle()
            return view!!
        }
        initBlock(viewGroup, viewGroup)
        return view!!
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        if (lifecycleFragment != null) {
            lifecycleFragment!!.startActivityForResult(intent, requestCode, options)
        }
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        if (lifecycleFragment != null) {
            lifecycleFragment!!.startActivityForResult(intent, requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (getFragment<Fragment>() != null) {
            getFragment<Fragment>()!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    protected open fun initBlock(rootView: ViewGroup, container: ViewGroup) {
        view = rootView
        mContainer = container
        create(null, context, mWhiteBoard, LayoutInflater.from(context), this)
        ensureLifecycle()
    }

    protected open fun ensureLifecycle() {
        //生命周期
        val lifecycle: FragmentManager? = fragmentManager
        if (lifecycle != null) {
            lifecycleFragment = lifecycle.findFragmentByTag(FRAGMENT_TAG_LIFECYCLE) as LifecycleFragment
            if (lifecycleFragment == null) {
                lifecycleFragment = LifecycleFragment()
                lifecycle.beginTransaction().add(lifecycleFragment!!, FRAGMENT_TAG_LIFECYCLE).commitNowAllowingStateLoss()
            }
            lifecycleFragment!!.addModuleManager(this)
        }
    }

    override fun <T : FragmentActivity> getActivity(): T? {
        if (mActivity == null && mFragment != null) {
            mActivity = mFragment!!.activity
        }
        return if (mActivity == null) null else mActivity as T
    }

    override fun <T : Fragment> getFragment(): T? {
        return mFragment as? T
    }

    override val fragmentManager: FragmentManager?
        get() {
            if (mFragment != null) {
                try {
                    return mFragment!!.childFragmentManager
                } catch (e: java.lang.Exception) {
                }
                return mFragment!!.fragmentManager
            }
            return if (mActivity != null && mActivity is FragmentActivity) {
                (mActivity as FragmentActivity).getSupportFragmentManager()
            } else null
        }

    /**
     * 去掉异步操作
     *
     * @param supplier
     * @param consumer
     */
    @Deprecated("", ReplaceWith("consumer.accept(supplier.call())"))
    open fun runAsync(supplier: Callable<View?>, consumer: Consumer<View?>) {
        consumer.accept(supplier.call())
    }

    /**
     * 去掉异步
     *
     * @param singleThreadExecutor
     */
    @Deprecated("")
    open fun setSingleThreadScheduler(singleThreadExecutor: Executor?) {
        //this.singleThreadExecutor = singleThreadExecutor;
    }

    @CallSuper
    override fun onCreate(): Boolean {
        val result = super.onCreate()
        state = LifecycleFragment.State.Create
        return result
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        state = LifecycleFragment.State.Start
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        state = LifecycleFragment.State.Resume
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        state = LifecycleFragment.State.Pause
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        state = LifecycleFragment.State.Stop
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        state = LifecycleFragment.State.Destroy
    }

    override fun createAsync(): Boolean {
        return false
    }

    companion object {
        private const val FRAGMENT_TAG_LIFECYCLE = "lifecycle"
        const val LINEAR_LAYOUT_H = -1
        const val LINEAR_LAYOUT_V = -2
        const val FRAME_LAYOUT = -3
        const val SCROLL_VIEW = -4
        const val CONSTAINT_LAYOUT = -5
    }
}

open class LifecycleFragment : Fragment() {
    enum class State {
        Idle, Create, Start, Resume, Pause, Stop, Destroy;

        val isAlive: Boolean
            get() = this != Destroy
    }

    private val mModuleManagerSet: MutableSet<BlockManager> = HashSet()
    open fun addModuleManager(manager: BlockManager) {
        mModuleManagerSet.add(manager)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        for (manager in mModuleManagerSet) {
            manager.onStart()
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        for (manager in mModuleManagerSet) {
            manager.onResume()
        }
    }

    @CallSuper
    override fun onPause() {
        for (manager in mModuleManagerSet) {
            manager.onPause()
        }
        super.onPause()
    }

    @CallSuper
    override fun onStop() {
        for (manager in mModuleManagerSet) {
            manager.onStop()
        }
        super.onStop()
    }

    @CallSuper
    override fun onDestroy() {
        for (manager in mModuleManagerSet) {
            manager.onDestroy()
        }
        super.onDestroy()
    }

    @CallSuper
    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        for (manager in mModuleManagerSet) {
            manager.onActivityCreated(savedInstanceState)
        }
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (manager in mModuleManagerSet) {
            manager.onActivityResult(requestCode, resultCode, data)
        }
    }

    @CallSuper
    override fun onDestroyView() {
        for (manager in mModuleManagerSet) {
            manager.onDestroyView()
        }
        super.onDestroyView()
    }
}

abstract class SubBlock : Block() {
    override fun onCreateView(inflater: LayoutInflater?, parent: ViewGroup?): View {
        return parent!!.findViewById(viewID)
    }

    protected abstract val viewID: Int
}
