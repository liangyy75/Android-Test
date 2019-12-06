@file:Suppress("unused")

package com.example.uilib.block2

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
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.example.uilib.block2.LifecycleFragment.State.*
import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import io.reactivex.annotations.Nullable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

@Suppress("UNCHECKED_CAST")
abstract class Block {
    open var mWhiteBoard: WhiteBoard? = null
    open val mHandler: Handler = Handler(Looper.getMainLooper())
    open var mBlockManager: BlockManager? = null
    open var mParent: BlockGroup? = null
    open var mContext: Context? = null
    open var mInflater: LayoutInflater? = null
    open var mView: View? = null
    open var left = -1
    open var top = -1
    open var right = -1
    open var bottom = -1
    open var backgroundColor = 0
    open var isCreated = false
    open var isResumed = false
    open var isDestroyed = false

    open fun setBackgroundColor(@ColorInt backgroundColor: Int): Block {
        this.backgroundColor = backgroundColor
        initOtherUI()
        return this
    }

    open fun setPadding(left: Int, top: Int, right: Int, bottom: Int): Block {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        initOtherUI()
        return this
    }

    open fun initOtherUI() {
        if (mView != null) {
            if (left >= 0 && top >= 0 && right >= 0 && bottom >= 0) {
                mView!!.setPadding(left, top, right, bottom)
            }
            if (backgroundColor != 0) {
                mView!!.setBackgroundColor(backgroundColor)
            }
        }
    }

    val context: Context?
        get() = mContext

    val view: View?
        get() = mView

    open fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?): View? {
        return null
    }

    open fun create(parent: ViewGroup?, context: Context, whiteBoard: WhiteBoard, inflater: LayoutInflater, blockManager: BlockManager): Boolean {
        mContext = context
        mInflater = inflater
        mWhiteBoard = whiteBoard
        mBlockManager = blockManager
        if (!onCreate()) {
            return false
        }

        val supplier: Callable<View?> = object : Callable<View?> {
            override fun call(): View? {
                Log.d("Luo", Thread.currentThread().toString() + "create View -> " + this@Block)
                if (mView == null) {
                    return onCreateView(mInflater!!, parent)
                }
                return null
            }
        }
        val consumer: Consumer<View?> = object : Consumer<View?> {
            override fun accept(t: View?) {
                Log.d("Luo", Thread.currentThread().toString() + "after create view-> " + this@Block)
                if (t != null) {
                    mView = t
                    if (t.parent == null && !isPendingAdd) {
                        mParent!!.addView(this@Block)
                    }
                    initOtherUI()
                }
                isCreated = true
                beforeOnViewCreate()
                onViewCreated()
                onRefresh()
                afterOnViewCreate()
            }
        }
        Log.d("Luo", Thread.currentThread().toString() + "create -> " + this)

        if (createAsync()) {
            blockManager.runAsync(supplier, consumer)
        } else {
            consumer.accept(supplier.call())
        }
        return true
    }

    open fun beforeOnViewCreate() {}

    open fun afterOnViewCreate() {
        when (mBlockManager!!.state) {
            Start -> {
                onStart()
            }
            Resume -> {
                onStart()
                onResume()
            }
            Pause -> {
                onStart()
                onResume()
                onPause()
            }
            Stop -> {
                onStart()
                onResume()
                onPause()
                onStop()
            }
            Destroy -> {
                onDestroyView()
                onDestroy()
            }
            else -> {
            }
        }
    }

    val isPendingAdd: Boolean
        get() = false

    open fun createAsync(): Boolean {
        return true
    }

    // 是否添加到父block中

    @CallSuper
    open fun onCreate(): Boolean {
        return true
    }

    // =========== string类型 start ===========

    private fun getCompatibleClass(clazz: Class<*>): Class<*> {
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

    open fun getObservable(event: String): Observable<Any?> {
        return mWhiteBoard!!.getObservable(event)
    }

    open fun getObservableNotNull(event: String): Observable<Any?> {
        val observable: Observable<Any> = mWhiteBoard!!.getObservable(event)
        return observable.filter { o -> o !== WhiteBoard.NULL_OBJECT }
    }

    open fun <T : Any> getObservable(@NonNull event: String, @NonNull tClass: Class<T>): Observable<T> {
        val compatibleClass: Class<T> = getCompatibleClass(tClass) as Class<T>
        return mWhiteBoard!!.getObservable<T>(event).filter { o -> compatibleClass.isAssignableFrom(o.javaClass) }
    }

    open fun <T : Any> getObservableNotNull(@NonNull event: String, @NonNull tClass: Class<T>): Observable<T> {
        val compatibleClass = getCompatibleClass(tClass) as Class<T>
        return mWhiteBoard!!.getObservable<T>(event).filter { o -> o !== WhiteBoard.NULL_OBJECT && compatibleClass.isAssignableFrom(o.javaClass) }
    }

    open fun notifyData(key: String?) {
        mWhiteBoard!!.notifyDataChanged(key)
    }

    open fun removeData(key: String?) {
        mWhiteBoard!!.removeData(key)
    }

    open fun putData(key: String, value: Any?) {
        mWhiteBoard!!.putData(key, value)
    }

    open fun putDataWithoutNotify(key: String, value: Any?) {
        mWhiteBoard!!.putDataWithoutNotify(key, value)
    }

    open fun <T : Any> getData(key: String?, @NonNull defaultValue: T): T {
        val o = mWhiteBoard!!.getData(key) ?: return defaultValue
        val compatibleClass: Class<T> = getCompatibleClass(defaultValue.javaClass) as Class<T>
        return if (compatibleClass.isAssignableFrom(o.javaClass)) {
            o as T
        } else {
            defaultValue
        }
    }

    open fun <T> getData(key: String?, @NonNull tClass: Class<T>): T? {
        val o = mWhiteBoard!!.getData(key) ?: return null
        val compatibleClass = getCompatibleClass(tClass) as Class<T>
        return if (compatibleClass.isAssignableFrom(o.javaClass)) {
            o as T
        } else {
            null
        }
    }

    open fun getData(key: String?): Any? {
        return mWhiteBoard!!.getData(key)
    }

    open fun getLong(key: String?): Long {
        val value = mWhiteBoard!!.getData(key) ?: return 0
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
        val value = mWhiteBoard!!.getData(key) ?: return 0
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
        val value = mWhiteBoard!!.getData(key) ?: return 0
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
        val value = mWhiteBoard!!.getData(key) ?: return 0.0
        if (value is String) {
            try {
                return value.toDouble()
            } catch (e: NumberFormatException) {
            }
            return 0.0
        }
        return try {
            value as Double
        } catch (e: ClassCastException) {
            0.0
        }
    }

    open fun getFloat(key: String?): Float {
        val value = mWhiteBoard!!.getData(key) ?: return 0.0f
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
        val value = mWhiteBoard!!.getData(key) ?: return false
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

    open fun getString(key: String?): String {
        return getData(key, String::class.java)!!
    }

    // =========== string类型 end ===========

    // =========== class 类型 start===========

    open fun notifyData(key: Class<*>) {
        mWhiteBoard!!.notifyDataChanged(key)
    }

    open fun removeData(key: Class<*>) {
        mWhiteBoard!!.removeData(key)
    }

    open fun <T : Any> getObservable(event: Class<T>): Observable<T> {
        val compatibleClass: Class<T> = getCompatibleClass(event) as Class<T>
        return mWhiteBoard!!.getObservable(event).filter { o -> compatibleClass.isAssignableFrom(o.javaClass) }
    }

    open fun <T : Any> getObservableNotNull(event: Class<T>): Observable<T> {
        val compatibleClass: Class<T> = getCompatibleClass(event) as Class<T>
        return mWhiteBoard!!.getObservable(event).filter { o -> compatibleClass.isAssignableFrom(o.javaClass) }
    }

    open fun <T> getData(tClass: Class<T>): T? {
        return mWhiteBoard!!.getData(tClass)
    }

    open fun putData(o: Any?) {
        mWhiteBoard!!.putData(o)
    }

    open fun putDataWithoutNotify(o: Any?) {
        mWhiteBoard!!.putDataWithoutNotify(o)
    }

    // =========== class 类型 end ===========

    // 刷新相关

    open fun refreshBlock() {
        onRefresh()
    }

    open fun refreshPage() {
        mBlockManager!!.refreshBlock()
    }

    open fun onRefresh() {}

    // 生命周期

    open fun onViewCreated() {}

    open fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {}

    @CallSuper
    open fun onStart() {
    }

    @CallSuper
    open fun onResume() {
        isResumed = true
    }

    @CallSuper
    open fun onPause() {
        isResumed = false
    }

    @CallSuper
    open fun onStop() {
    }

    @CallSuper
    open fun onDestroyView() {
        mHandler.removeCallbacksAndMessages(null)
        disposables.clear()
    }

    val handler: Handler
        get() = mHandler

    @CallSuper
    open fun onDestroy() {
        isDestroyed = true
    }

    // Activity / Fragment 的一些方法

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    val lifeCyclerOwner: LifecycleOwner
        get() {
            return getFragment() ?: getActivity()!!
        }

    open fun <T : FragmentActivity> getActivity(): T? {
        return mBlockManager!!.getActivity()
    }

    open fun <T : Fragment> getFragment(): T? {
        return mBlockManager!!.getFragment()
    }

    open val fragmentManager: FragmentManager?
        get() = mBlockManager!!.fragmentManager

    open fun startActivity(intent: Intent?) {
        getActivity<FragmentActivity>()?.startActivity(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    open fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        mBlockManager!!.startActivityForResult(intent, requestCode, options)
    }

    open fun startActivityForResult(intent: Intent?, requestCode: Int) {
        mBlockManager!!.startActivityForResult(intent, requestCode)
    }

    open fun finish() {
        getActivity<FragmentActivity>()?.finish()
    }

    // 注册 dispose ，然后集中释放的方法

    private val disposables = CompositeDisposable()

    open fun register(subscription: Disposable) {
        disposables.add(subscription)
    }

    companion object {
        const val FRAGMENT_USE_VISIBLE_HINT = "FRAGMENT_USE_VISIBLE_HINT"
    }
}
