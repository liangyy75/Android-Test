@file:Suppress("unused")

package com.liang.example.json_inflater

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.RelativeLayout
import com.liang.example.basic_ktx.ReflectHelper
import com.liang.example.view_ktx.setMargin
import com.liang.example.view_ktx.setMarginBottom
import com.liang.example.view_ktx.setMarginHorizontal
import com.liang.example.view_ktx.setMarginLeft
import com.liang.example.view_ktx.setMarginRight
import com.liang.example.view_ktx.setMarginTop
import com.liang.example.view_ktx.setMarginVertical
import com.liang.example.view_ktx.setPadding
import com.liang.example.view_ktx.setPaddingBottom
import com.liang.example.view_ktx.setPaddingHorizontal
import com.liang.example.view_ktx.setPaddingLeft
import com.liang.example.view_ktx.setPaddingRight
import com.liang.example.view_ktx.setPaddingTop
import com.liang.example.view_ktx.setPaddingVertical
import java.lang.RuntimeException

const val VIEW = "View"
const val INCLUDE = "include"
const val VIEW_GROUP = "ViewGroup"
const val TEXT_VIEW = "TextView"
const val BUTTON = "Button"
const val EDIT_TEXT = "EditText"

open class NViewHolder<V : View>(override val view: V, override var nManager: NView.NManager? = null) : NView<V>

@Suppress("MemberVisibilityCanBePrivate")
open class NViewParser<V : NView<View>> : ViewTypeParser<V>() {
    companion object {
        const val TAG = "ViewParser"
        const val ID_STRING_START_PATTERN = "@+id/"
        const val ID_STRING_START_PATTERN1 = "@id/"
        const val ID_STRING_NORMALIZED_PATTERN = ":id/"

        // RequiresFadingEdge
        private const val NONE = "none"
        private const val BOTH = "both"
        private const val VERTICAL = "vertical"
        private const val HORIZONTAL = "horizontal"
    }

    override fun getType(): String = VIEW
    override fun getParentType(): String? = null

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> = NViewHolder(View(context))

    override fun handleChildren(view: V, children: Value?): Boolean = false
    override fun addView(parent: NView<*>?, view: NView<*>): Boolean = false

    override fun addAttributeProcessors() {
        addAttributeProcessor(Attributes.View.Activated, NBooleanAttributeProcessor.create { view, value -> view.view.isActivated = value })
        addAttributeProcessor(Attributes.View.Clickable, NBooleanAttributeProcessor.create { view, value -> view.view.isClickable = value })
        addAttributeProcessor(Attributes.View.Enabled, NBooleanAttributeProcessor.create { view, value -> view.view.isEnabled = value })
        addAttributeProcessor(Attributes.View.Selected, NBooleanAttributeProcessor.create { view, value -> view.view.isSelected = value })
        addAttributeProcessor(Attributes.View.SaveEnabled, NBooleanAttributeProcessor.create { view, value -> view.view.isSaveEnabled = value })
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            addAttributeProcessor(Attributes.View.Focusable, NNumberAttributeProcessor.create { view, value -> view.view.focusable = value.toInt() })  // TODO()
        }
        addAttributeProcessor(Attributes.View.Tag, NStringAttributeProcessor.create { view, value -> view.view.tag = value })
        addAttributeProcessor(Attributes.View.ContentDescription, NStringAttributeProcessor.create { view, value -> view.view.contentDescription = value })

        addAttributeProcessor(Attributes.View.OnClick, NEventProcessor.create { view, value, thisObj ->
            view.view.setOnClickListener { thisObj.trigger(Attributes.View.OnClick, value, view) }
        })
        addAttributeProcessor(Attributes.View.OnLongClick, NEventProcessor.create { view, value, thisObj ->
            view.view.setOnLongClickListener { thisObj.trigger(Attributes.View.OnLongClick, value, view); return@setOnLongClickListener true }
        })
        addAttributeProcessor(Attributes.View.OnTouch, NEventProcessor.create { view, value, thisObj ->
            view.view.setOnTouchListener { _, _ -> thisObj.trigger(Attributes.View.OnTouch, value, view); return@setOnTouchListener true }
        })

        addAttributeProcessor(Attributes.View.Background, NDrawableResourceProcessor.create { view, drawable ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                view.view.setBackgroundDrawable(drawable)
            } else {
                view.view.background = drawable
            }
        })
        addAttributeProcessor(Attributes.View.Width, NDimensionAttributeProcessor.create { view, dimension ->
            val params = view.view.layoutParams
            if (params != null) {
                params.width = dimension.toInt()
                view.view.layoutParams = params
            }
        })
        addAttributeProcessor(Attributes.View.Height, NDimensionAttributeProcessor.create { view, dimension ->
            val params = view.view.layoutParams
            if (params != null) {
                params.height = dimension.toInt()
                view.view.layoutParams = params
            }
        })
        addAttributeProcessor(Attributes.View.MinWidth, NDimensionAttributeProcessor.create { view, dimension -> view.view.minimumWidth = dimension.toInt() })
        addAttributeProcessor(Attributes.View.MinHeight, NDimensionAttributeProcessor.create { view, dimension -> view.view.minimumHeight = dimension.toInt() })
        addAttributeProcessor(Attributes.View.Weight, NDimensionAttributeProcessor.create { view, dimension ->
            ReflectHelper.setFloat("weight", view.view.layoutParams, dimension)
            /*val params = view.view.layoutParams
        if (params is LinearLayout.LayoutParams) {
            params.weight = dimension
            view.view.layoutParams = params
        } else if (debug) {
            Log.e(TAG, "'weight' is only supported for LinearLayouts")
        }*/
        })
        addAttributeProcessor(Attributes.View.LayoutGravity, NGravityAttributeProcessor.create { view, gravity ->
            ReflectHelper.setInt("gravity", view.view.layoutParams, gravity)
            /*val params = view.view.layoutParams
            when {
                params is LinearLayout.LayoutParams -> {
                    params.gravity = gravity
                    view.view.layoutParams = params
                }
                params is FrameLayout.LayoutParams -> {
                    params.gravity = gravity
                    view.view.layoutParams = params
                }
                debug -> Log.e(TAG, "'layout_gravity' is only supported for LinearLayout and FrameLayout")
            }*/
        })

        addAttributeProcessor(Attributes.View.Padding, NDimensionAttributeProcessor.create { view, d -> view.view.setPadding(d.toInt()) })
        addAttributeProcessor(Attributes.View.PaddingLeft, NDimensionAttributeProcessor.create { view, d -> view.view.setPaddingLeft(d.toInt()) })
        addAttributeProcessor(Attributes.View.PaddingRight, NDimensionAttributeProcessor.create { view, d -> view.view.setPaddingRight(d.toInt()) })
        addAttributeProcessor(Attributes.View.PaddingTop, NDimensionAttributeProcessor.create { view, d -> view.view.setPaddingTop(d.toInt()) })
        addAttributeProcessor(Attributes.View.PaddingBottom, NDimensionAttributeProcessor.create { view, d -> view.view.setPaddingBottom(d.toInt()) })
        addAttributeProcessor(Attributes.View.PaddingStart, NDimensionAttributeProcessor.create { view, d -> view.view.setPaddingLeft(d.toInt()) })
        addAttributeProcessor(Attributes.View.PaddingEnd, NDimensionAttributeProcessor.create { view, d -> view.view.setPaddingRight(d.toInt()) })
        addAttributeProcessor(Attributes.View.PaddingHorizontal, NDimensionAttributeProcessor.create { view, d -> view.view.setPaddingHorizontal(d.toInt()) })
        addAttributeProcessor(Attributes.View.PaddingVertical, NDimensionAttributeProcessor.create { view, d -> view.view.setPaddingVertical(d.toInt()) })

        addAttributeProcessor(Attributes.View.Margin, NDimensionAttributeProcessor.create { view, d -> view.view.setMargin(d.toInt()) })
        addAttributeProcessor(Attributes.View.MarginLeft, NDimensionAttributeProcessor.create { view, d -> view.view.setMarginLeft(d.toInt()) })
        addAttributeProcessor(Attributes.View.MarginRight, NDimensionAttributeProcessor.create { view, d -> view.view.setMarginRight(d.toInt()) })
        addAttributeProcessor(Attributes.View.MarginTop, NDimensionAttributeProcessor.create { view, d -> view.view.setMarginTop(d.toInt()) })
        addAttributeProcessor(Attributes.View.MarginBottom, NDimensionAttributeProcessor.create { view, d -> view.view.setMarginBottom(d.toInt()) })
        addAttributeProcessor(Attributes.View.MarginStart, NDimensionAttributeProcessor.create { view, d -> view.view.setMarginLeft(d.toInt()) })
        addAttributeProcessor(Attributes.View.MarginEnd, NDimensionAttributeProcessor.create { view, d -> view.view.setMarginRight(d.toInt()) })
        addAttributeProcessor(Attributes.View.MarginHorizontal, NDimensionAttributeProcessor.create { view, d -> view.view.setMarginHorizontal(d.toInt()) })
        addAttributeProcessor(Attributes.View.MarginVertical, NDimensionAttributeProcessor.create { view, d -> view.view.setMarginVertical(d.toInt()) })

        addAttributeProcessor(Attributes.View.Elevation, NDimensionAttributeProcessor.create { view, dimension ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.view.elevation = dimension
            }
        })

        addAttributeProcessor(Attributes.View.Alpha, NStringAttributeProcessor.create { view, value ->
            view.view.alpha = value?.toFloatOrNull() ?: 1.0f
        })
        addAttributeProcessor(Attributes.View.Visibility, object : NAttributeProcessor<V>() {
            override fun handleValue(view: V, value: Value?) = when {
                value is PrimitiveV && value.isNumber() -> view.view.visibility = value.toInt()
                else -> process(view, precompile(value, view))
            }

            override fun handleResource(view: V, resource: ResourceV) {
                view.view.visibility = ResourceV.getInteger(resource.resId, view.viewContext) ?: View.GONE
            }

            override fun handleStyleResource(view: V, style: StyleResourceV) {
                view.view.visibility = style.apply(view.viewContext).getInt(0, View.GONE)
            }

            override fun handleAttributeResource(view: V, attribute: AttributeResourceV) {
                view.view.visibility = attribute.apply(view.viewContext).getInt(0, View.GONE)
            }

            override fun compile(value: Value?, context: Context): Value? = Util.getVisibility(Util.parseVisibility(value))
        })

        addAttributeProcessor(Attributes.View.Id, NStringAttributeProcessor.create { view, value ->
            view.view.id = view.nManager!!.nContext.getInflater().getUniqueViewId(value!!)
            // set view id resource name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                view.view.accessibilityDelegate = object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            info.viewIdResourceName = when {
                                !TextUtils.isEmpty(value) -> view.viewContext.packageName + ID_STRING_NORMALIZED_PATTERN + when {
                                    value.startsWith(ID_STRING_START_PATTERN) -> value.substring(ID_STRING_START_PATTERN.length)
                                    value.startsWith(ID_STRING_START_PATTERN1) -> value.substring(ID_STRING_START_PATTERN1.length)
                                    else -> value
                                }
                                else -> ""
                            }
                        }
                    }
                }
            }
        })

        addAttributeProcessor(Attributes.View.Style, NStringAttributeProcessor.create { view, value ->
            val manager = view.nManager!!
            val context = manager.nContext
            @Suppress("UNCHECKED_CAST") val handler = context.getParser(manager.layoutV.type) as? ViewTypeParser<V> ?: this@NViewParser
            value?.split("\\.")?.forEach { context.getStyle(it)?.forEach { handler.handleAttribute(view, handler.getAttributeId(it.key), it.value!!) } }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addAttributeProcessor(Attributes.View.TransitionName, NStringAttributeProcessor.create { view, value -> view.view.transitionName = value })
        }
        addAttributeProcessor(Attributes.View.Animation, NTweenAnimResourceProcessor.create { view, value -> view.view.animation = value })

        addAttributeProcessor(Attributes.View.RequiresFadingEdge, NStringAttributeProcessor.create { nView, value ->
            val view = nView.view
            when (value) {
                NONE -> {
                    view.isVerticalFadingEdgeEnabled = false
                    view.isHorizontalFadingEdgeEnabled = false
                }
                BOTH -> {
                    view.isVerticalFadingEdgeEnabled = true
                    view.isHorizontalFadingEdgeEnabled = true
                }
                VERTICAL -> {
                    view.isVerticalFadingEdgeEnabled = true
                    view.isHorizontalFadingEdgeEnabled = false
                }
                HORIZONTAL -> {
                    view.isVerticalFadingEdgeEnabled = false
                    view.isHorizontalFadingEdgeEnabled = true
                }
                else -> {
                    view.isVerticalFadingEdgeEnabled = false
                    view.isHorizontalFadingEdgeEnabled = false
                }
            }
        })

        addAttributeProcessor(Attributes.View.FadingEdgeLength, NStringAttributeProcessor.create { view, value ->
            view.view.setFadingEdgeLength(if (value == "null") 0 else value?.toIntOrNull() ?: 0)
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            addAttributeProcessor(Attributes.View.TextAlignment, NStringAttributeProcessor.create { view, value ->
                val textAlignment = Util.sTextAlignment[value]
                if (textAlignment != null) {
                    view.view.textAlignment = textAlignment
                }
            })
        }

        addAttributeProcessor(Attributes.View.Above, createRelativeLayoutRuleProcessor(RelativeLayout.ABOVE))
        addAttributeProcessor(Attributes.View.AlignBaseline, createRelativeLayoutRuleProcessor(RelativeLayout.ALIGN_BASELINE))
        addAttributeProcessor(Attributes.View.AlignBottom, createRelativeLayoutRuleProcessor(RelativeLayout.ALIGN_BOTTOM))
        addAttributeProcessor(Attributes.View.AlignLeft, createRelativeLayoutRuleProcessor(RelativeLayout.ALIGN_LEFT))
        addAttributeProcessor(Attributes.View.AlignRight, createRelativeLayoutRuleProcessor(RelativeLayout.ALIGN_RIGHT))
        addAttributeProcessor(Attributes.View.AlignTop, createRelativeLayoutRuleProcessor(RelativeLayout.ALIGN_TOP))
        addAttributeProcessor(Attributes.View.Below, createRelativeLayoutRuleProcessor(RelativeLayout.BELOW))
        addAttributeProcessor(Attributes.View.ToLeftOf, createRelativeLayoutRuleProcessor(RelativeLayout.LEFT_OF))
        addAttributeProcessor(Attributes.View.ToRightOf, createRelativeLayoutRuleProcessor(RelativeLayout.RIGHT_OF))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            addAttributeProcessor(Attributes.View.AlignEnd, createRelativeLayoutRuleProcessor(RelativeLayout.ALIGN_END))
            addAttributeProcessor(Attributes.View.AlignStart, createRelativeLayoutRuleProcessor(RelativeLayout.ALIGN_START))
            addAttributeProcessor(Attributes.View.ToEndOf, createRelativeLayoutRuleProcessor(RelativeLayout.END_OF))
            addAttributeProcessor(Attributes.View.ToStartOf, createRelativeLayoutRuleProcessor(RelativeLayout.START_OF))
        }

        addAttributeProcessor(Attributes.View.AlignParentTop, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.ALIGN_PARENT_TOP))
        addAttributeProcessor(Attributes.View.AlignParentRight, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.ALIGN_PARENT_RIGHT))
        addAttributeProcessor(Attributes.View.AlignParentBottom, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.ALIGN_PARENT_BOTTOM))
        addAttributeProcessor(Attributes.View.AlignParentLeft, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.ALIGN_PARENT_LEFT))
        addAttributeProcessor(Attributes.View.CenterHorizontal, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.CENTER_HORIZONTAL))
        addAttributeProcessor(Attributes.View.CenterVertical, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.CENTER_VERTICAL))
        addAttributeProcessor(Attributes.View.CenterInParent, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.CENTER_IN_PARENT))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            addAttributeProcessor(Attributes.View.AlignParentStart, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.ALIGN_PARENT_START))
            addAttributeProcessor(Attributes.View.AlignParentEnd, createRelativeLayoutBooleanRuleProcessor(RelativeLayout.ALIGN_PARENT_END))
        }
    }

    protected fun createRelativeLayoutRuleProcessor(rule: Int): NAttributeProcessor<V> = object : NStringAttributeProcessor<V>() {
        override fun setString(view: V, value: String?) = addRelativeLayoutRule(view.view, rule, view.nManager!!.nContext.getInflater().getUniqueViewId(value!!))
    }

    protected fun createRelativeLayoutBooleanRuleProcessor(rule: Int): NAttributeProcessor<V> = object : NBooleanAttributeProcessor<V>() {
        override fun setBoolean(view: V, value: Boolean) = addRelativeLayoutRule(view.view, rule, parseRelativeLayoutBoolean(value))
    }
}

// open class NViewGroupParser<V : NView<Button>> : ViewTypeParser<V>() {
//     override fun getType(): String = VIEW_GROUP
//     override fun getParentType(): String? = VIEW
//     override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> = NViewHolder(ViewGroup(context))
//     override fun addAttributeProcessors() {
//         TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//     }
// }

open class NIncludeParser<V : NView<View>> : ViewTypeParser<V>() {
    override fun getType(): String = INCLUDE
    override fun getParentType(): String? = VIEW

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        requireNotNull(layoutV.extra) { "required attribute 'layout' missing." }
        val type = layoutV.extra!!["layout"]
        if (null == type || type !is PrimitiveV) {
            throw RuntimeException("required attribute 'layout' missing or is not a string")
        }
        val layout = context.getLayout(type.string()) ?: throw RuntimeException("Layout '$type' not found")
        return context.getInflater().inflate(layout.merge(layoutV), data, parent, dataIndex)
    }

    override fun addAttributeProcessors() = Unit
}

open class NButtonParser<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String = BUTTON
    override fun getParentType(): String? = TEXT_VIEW

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> = NButton(context)

    override fun addAttributeProcessors() = Unit
}

open class NTextViewParser<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String = TEXT_VIEW
    override fun getParentType(): String? = VIEW

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NEditTextParser<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NCheckBoxGroupParser<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NScrollViewParser<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NLinearLayoutGroupParser<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NFrameLayoutParser<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class NRelativeLayoutParser<V : NView<Button>> : ViewTypeParser<V>() {
    override fun getType(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParentType(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createView(context: NContext, layoutV: LayoutV, data: ObjectV, parent: ViewGroup?, dataIndex: Int): NView<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAttributeProcessors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
