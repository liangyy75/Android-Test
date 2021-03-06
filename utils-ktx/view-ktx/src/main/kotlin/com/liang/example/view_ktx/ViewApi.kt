package com.liang.example.view_ktx

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.liang.example.context_ktx.ContextApi
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

const val INVALID_LAYOUT_PARAMS = -10

fun View.setPadding(p: Int) = this.setPadding(p, p, p, p)
fun View.setPaddingLeft(p: Int) = this.setPadding(p, this.paddingTop, this.paddingRight, this.paddingBottom)
fun View.setPaddingRight(p: Int) = this.setPadding(this.paddingLeft, this.paddingTop, p, this.paddingBottom)
fun View.setPaddingTop(p: Int) = this.setPadding(this.paddingLeft, p, this.paddingRight, this.paddingBottom)
fun View.setPaddingBottom(p: Int) = this.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, p)
fun View.setPaddingHorizontal(p: Int) = this.setPadding(p, this.paddingTop, p, this.paddingBottom)
fun View.setPaddingVertical(p: Int) = this.setPadding(this.paddingLeft, p, this.paddingRight, p)

var View.layoutWidth: Int
    get() = this.layoutParams?.width ?: INVALID_LAYOUT_PARAMS
    set(value) {
        val lp = this.layoutParams ?: return
        lp.width = value
        this.layoutParams = lp
    }

var View.layoutHeight: Int
    get() = this.layoutParams?.height ?: INVALID_LAYOUT_PARAMS
    set(value) {
        val lp = this.layoutParams ?: return
        lp.height = value
        this.layoutParams = lp
    }

fun View.setMargin(m: Int) {
    val params = this.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
        params.setMargins(m, m, m, m)
        this.layoutParams = params
    }
}

fun View.setMarginLeft(m: Int) {
    val params = this.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
        params.leftMargin = m
        this.layoutParams = params
    }
}

fun View.setMarginRight(m: Int) {
    val params = this.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
        params.rightMargin = m
        this.layoutParams = params
    }
}

fun View.setMarginTop(m: Int) {
    val params = this.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
        params.topMargin = m
        this.layoutParams = params
    }
}

fun View.setMarginBottom(m: Int) {
    val params = this.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
        params.bottomMargin = m
        this.layoutParams = params
    }
}

fun View.setMarginHorizontal(m: Int) {
    val params = this.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
        params.leftMargin = m
        params.rightMargin = m
        this.layoutParams = params
    }
}

fun View.setMarginVertical(m: Int) {
    val params = this.layoutParams
    if (params is ViewGroup.MarginLayoutParams) {
        params.topMargin = m
        params.bottomMargin = m
        this.layoutParams = params
    }
}

fun TextView.setDrawableLeft(@DrawableRes d: Int) = setDrawableLeft(ContextApi.app.resources.getDrawable(d))
fun TextView.setDrawableRight(@DrawableRes d: Int) = setDrawableRight(ContextApi.app.resources.getDrawable(d))
fun TextView.setDrawableTop(@DrawableRes d: Int) = setDrawableTop(ContextApi.app.resources.getDrawable(d))
fun TextView.setDrawableBottom(@DrawableRes d: Int) = setDrawableBottom(ContextApi.app.resources.getDrawable(d))

fun TextView.setDrawableLeft(d: Drawable) = setDrawableByIndex(d, 0)
fun TextView.setDrawableRight(d: Drawable) = setDrawableByIndex(d, 2)
fun TextView.setDrawableTop(d: Drawable) = setDrawableByIndex(d, 1)
fun TextView.setDrawableBottom(d: Drawable) = setDrawableByIndex(d, 3)

fun TextView.setDrawableByIndex(d: Drawable, index: Int) {
    val drawables = compoundDrawables
    drawables[index] = d
    setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3])  // left, top, right, bottom
}

fun TextView.setDrawableHorizontal(@DrawableRes d: Int) = setDrawableHorizontal(ContextApi.app.resources.getDrawable(d))
fun TextView.setDrawableHorizontal(d: Drawable) {
    val drawables = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(d, drawables[1], d, drawables[3])
}

fun TextView.setDrawableVertical(@DrawableRes d: Int) = setDrawableVertical(ContextApi.app.resources.getDrawable(d))
fun TextView.setDrawableVertical(d: Drawable) {
    val drawables = compoundDrawables
    setCompoundDrawablesWithIntrinsicBounds(drawables[0], d, drawables[2], d)
}

private val sNextGeneratedId = AtomicInteger(1)
fun generateViewId(): Int {
    while (true) {
        val result: Int = sNextGeneratedId.get()
        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
        var newValue = result + 1
        if (newValue > 0x00FFFFFF) newValue = 1 // Roll over to 1, not 0.
        if (sNextGeneratedId.compareAndSet(result, newValue)) {
            return result
        }
    }
}

val strToIdMaps = ConcurrentHashMap<String, Int>()
val idToStrMaps = ConcurrentHashMap<Int, String>()

var View.strId: String?
    set(value) {
        if (value == null) {
            val id = this.id
            if (idToStrMaps.containsKey(id)) {
                strToIdMaps.remove(idToStrMaps[id])
                idToStrMaps.remove(id)
            }
        } else {
            this.id = getIntIdByStrId(value)
        }
    }
    get() = getStrIdByIntId(this.id)

fun getIntIdByStrId(strId: String?): Int {
    if (strId == null) {
        return View.NO_ID
    }
    var id = strToIdMaps[strId]
    if (id == null) {
        id = generateViewId()
        strToIdMaps[strId] = id
        idToStrMaps[id] = strId
    }
    return id
}

fun getStrIdByIntId(id: Int?): String? {
    if (id == View.NO_ID || !idToStrMaps.containsKey(id)) {
        return null
    }
    return idToStrMaps[id]!!
}

fun <T : View> Activity.findViewByStrId2(strId: String): T? = when (val intId = strToIdMaps[strId]) {
    null, View.NO_ID -> null
    else -> findViewById(intId)
}

fun <T : View> Activity.findViewByStrId(strId: String): T = findViewById(strToIdMaps[strId]!!)
fun <T : View> Activity.findViewById(strId: String): T = findViewById(strToIdMaps[strId]!!)
