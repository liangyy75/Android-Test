package com.liang.example.viewtest_kt.page_indicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewConfiguration
import androidx.viewpager.widget.ViewPager
import com.liang.example.androidtest.R

/*
```xml
<resources>
    <!-- 这个应该很常见不用解释 -->
    <declare-styleable name="MView">
        <attr name="mview_1" format="string"/>
        <attr name="mview_2" format="string"/>
        <attr name="mview_3" format="string"/>
        <attr name="mview_4" format="string"/>
        <attr name="mview_5" format="string"/>
    </declare-styleable>
    <!-- 这个属性下面有解释-->
    <attr name="StyleInTheme" format="reference"/>
</resources>
```
==> 生成
```java
public static final class attr {
    public static final int StyleInTheme=0x7f010000;
    public static final int mview_1=0x7f0100d9;
    public static final int mview_2=0x7f0100da;
    ......
}
public static final class styleable {
    public static final int[] MView = { 0x7f0100d9, 0x7f0100da, 0x7f0100db, 0x7f0100dc, 0x7f0100dd };
    public static final int MView_mview_1 = 0;
    public static final int MView_mview_2 = 1;
    ......
}
```
先做个说明，StyleInTheme就是为了让我们能在Activity的Theme中使用我们自定义的属性而定义的，这也对应着我们的defStyleAttr，接着看。
```xml
<resources>
    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <item name="StyleInTheme">@style/StyleForTheme</item>
        <item name="mview_1">declare in base theme</item>
        <item name="mview_2">declare in base theme</item>
        <item name="mview_3">declare in base theme</item>
        <item name="mview_4">declare in base theme</item>
        <item name="mview_5">declare in base theme</item>
    </style>
    <style name="StyleForTheme">
        <item name="mview_1">declare in theme by style</item>
        <item name="mview_2">declare in theme by style</item>
        <item name="mview_3">declare in theme by style</item>
    </style>
    <style name="MViewStyle">
        <item name="mview_1">declare in xml by style</item>
        <item name="mview_2">declare in xml by style</item>
    </style>
    <style name="StyleForDefStyleRes">
        <item name="mview_1">declare in style for defStyleRes</item>
        <item name="mview_2">declare in style for defStyleRes</item>
        <item name="mview_3">declare in style for defStyleRes</item>
        <item name="mview_4">declare in style for defStyleRes</item>
    </style>
</resources>
```
先看AppTheme，里面就定义了一个名为StyleInTheme的item,他的属性是一个style中的引用。要明白，我们在attr.xml中可以不定义StyleInTheme这个attr,但如果这样做，那么在theme中设置StyleInTheme就变得没有意义，
因为你无法在代码中获取theme的StyleInTheme属性。
```xml
<android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.testattr.MainActivity">

    <com.example.testattr.MView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:mview_1="Direct declare in xml"
        style="@style/MViewStyle" />
</android.support.constraint.ConstraintLayout>
```
 */

/*
context.obtainStyledAttributes
1、任何在AttributeSet中给出的；
2、在AttributeSet中的style属性中设置的；
3、从defStyleAttr和defStyleRes中设置的；
4、在Theme中直接设置的属性。
*/

/*
getDimension：返回类型为float，
getDimensionPixelSize：返回类型为int，由浮点型转成整型时，采用四舍五入原则。
getDimensionPixelOffset：返回类型为int，由浮点型转成整型时，原则是忽略小数点部分。
*/

/*
Paint.ANTI_ALIAS_FLAG ：抗锯齿标志 0x01
Paint.FILTER_BITMAP_FLAG : 使位图过滤的位掩码标志 0x02
Paint.DITHER_FLAG : 使位图进行有利的抖动的位掩码标志 0x04
Paint.UNDERLINE_TEXT_FLAG : 下划线 0x08
Paint.STRIKE_THRU_TEXT_FLAG : 中划线 0x10
Paint.FAKE_BOLD_TEXT_FLAG : 加粗 0x20
Paint.LINEAR_TEXT_FLAG : 使文本平滑线性扩展的油漆标志 0x40
Paint.SUBPIXEL_TEXT_FLAG : 使文本的亚像素定位的绘图标志 0x80
Paint.DEV_KERN_TEXT_FLAG : 0x100 @deperated
Paint.LCD_RENDER_TEXT_FLAG : 0x200 @hide
Paint.EMBEDDED_BITMAP_TEXT_FLAG : 绘制文本时允许使用位图字体的绘图标志 0x400
Paint.AUTO_HINTING_TEXT_FLAG : 0x800 @hide
Paint.VERTICAL_TEXT_FLAG : 0x1000 @hide
*/

// TODO: 自定义view -- https://www.jianshu.com/p/f1fd2d8d5536
open class PagerIndicatorImpl(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
    : View(context, attrs, defStyleAttr, defStyleRes), PagerIndicator {

    companion object {
        fun isColorType(type: Int): Boolean = type >= TypedValue.TYPE_FIRST_COLOR_INT && type <= TypedValue.TYPE_LAST_COLOR_INT
    }

    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    private val paintSelected = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintUnselected = Paint(1)
    private val paintStroke = Paint(1)
    private var viewPager: ViewPager? = null
    private var mListener: ViewPager.OnPageChangeListener? = null
    private var currentItem: Int = 0
    private var snapItem: Int = 0
    private var pageOffset: Float = 0f
    private var scollState: Int = 0
    private var touchSlop: Int = ViewConfiguration.get(context).scaledPagingTouchSlop
    private var lastMotionX: Float = -1f
    private var activePointerId: Int = -1
    private var isDragging: Boolean = false

    var orientation: Int = 0
        set(value) {
            field = value
            requestLayout()
        }
    var center: Boolean = true
        set(value) {
            field = value
            invalidate()
        }
    var snap: Boolean = false
        set(value) {
            field = value
            invalidate()
        }
    var unselectedColor: Int = 0xe0e0e0
        set(value) {
            field = value
            paintUnselected.color = value
            unselectedType = TypedValue.TYPE_INT_COLOR_RGB8
            invalidate()
        }
    var unselectedDrawable: Int = -1
        set(value) {
            field = value
            unselectedType = TypedValue.TYPE_REFERENCE
            invalidate()
        }
    private var unselectedType: Int = TypedValue.TYPE_INT_COLOR_RGB8
    var selectedColor: Int = 0xff4e33
        set(value) {
            field = value
            paintSelected.color = value
            selectedType = TypedValue.TYPE_INT_COLOR_RGB8
            invalidate()
        }
    var selectedDrawable: Int = -1
        set(value) {
            field = value
            selectedType = TypedValue.TYPE_REFERENCE
            invalidate()
        }
    private var selectedType: Int = TypedValue.TYPE_INT_COLOR_RGB8
    var strokeColor: Int = 0xaaaaaa
        set(value) {
            field = value
            paintStroke.color = value
            invalidate()
        }
    var strokeWidth: Float = 0f
        set(value) {
            field = value
            paintStroke.strokeWidth = value
            invalidate()
        }
    var dividerWidth: Float = 10f
        set(value) {
            field = value
            invalidate()
        }
    var radius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    var radiusType: Int = TypedValue.TYPE_NULL
        set(value) {
            field = value
            invalidate()
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PagerIndicatorImpl, defStyleAttr, defStyleRes)
        val res = resources
        orientation = a.getInteger(R.styleable.PagerIndicatorImpl_orientation2,
                res?.getInteger(R.integer.PagerIndicatorImpl_default_orientation2) ?: 0)
        center = a.getBoolean(R.styleable.PagerIndicatorImpl_center,
                res?.getBoolean(R.bool.PagerIndicatorImpl_default_center) ?: true)
        snap = a.getBoolean(R.styleable.PagerIndicatorImpl_snap,
                res?.getBoolean(R.bool.PagerIndicatorImpl_default_snap) ?: false)
        var tv = a.peekValue(R.styleable.PagerIndicatorImpl_unselectedSrc)
        tv?.let {
            unselectedType = it.type
            when {
                it.type == TypedValue.TYPE_REFERENCE -> unselectedDrawable = it.resourceId
                isColorType(it.type) -> unselectedColor = it.data
            }
        }
        tv = a.peekValue(R.styleable.PagerIndicatorImpl_selectedSrc)
        tv?.let {
            selectedType = it.type
            when {
                it.type == TypedValue.TYPE_REFERENCE -> selectedDrawable = it.resourceId
                isColorType(it.type) -> selectedColor = it.data
            }
        }
        strokeColor = a.getColor(R.styleable.PagerIndicatorImpl_strokeColor,
                res?.getColor(R.color.PagerIndicatorImpl_default_strokeColor) ?: 0xaaaaaa)
        strokeWidth = a.getDimension(R.styleable.PagerIndicatorImpl_strokeWidth,
                res?.getDimension(R.dimen.PagerIndicatorImpl_default_strokeWidth) ?: 0f)
        dividerWidth = a.getDimension(R.styleable.PagerIndicatorImpl_dividerWidth,
                res?.getDimension(R.dimen.PagerIndicatorImpl_default_dividerWidth) ?: dividerWidth)
        tv = a.peekValue(R.styleable.PagerIndicatorImpl_radius)
        tv?.let {
            radiusType = it.type
            when (it.type) {
                TypedValue.TYPE_FRACTION -> radius = tv.getFraction(1f, 1f)
                TypedValue.TYPE_DIMENSION -> radius = tv.getDimension(res.displayMetrics)
            }
        }
        paintUnselected.style = Paint.Style.FILL
        paintUnselected.color = unselectedColor
        paintSelected.style = Paint.Style.FILL
        paintSelected.color = selectedColor
        paintStroke.style = Paint.Style.STROKE
        paintStroke.color = strokeColor
        paintStroke.strokeWidth = strokeWidth
        a.recycle()
    }

    open protected fun drawIndicator(dx: Int, dy: Int) {
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        val vp = viewPager ?: return
        val count = vp.adapter?.count ?: return
        if (count > 0) {
            if (currentItem >= count) {
                setCurrentItem(count - 1)
            } else {
                // data prepare
                val longSize: Int
                val longPaddingBefore: Int
                val longPaddingAfter: Int
                val shortPaddingBefore: Int = if (orientation == 0) {
                    longSize = this.width
                    longPaddingBefore = this.paddingLeft
                    longPaddingAfter = this.paddingRight
                    this.paddingTop
                } else {
                    longSize = this.height
                    longPaddingBefore = this.paddingTop
                    longPaddingAfter = this.paddingBottom
                    this.paddingLeft
                }
                val radius = when (radiusType) {
                    TypedValue.TYPE_DIMENSION -> this.radius
                    TypedValue.TYPE_FRACTION -> this.radius * longSize
                    else -> return
                }
                val threeRadius: Float = radius * 2.0f + dividerWidth
                val shortOffset: Float = shortPaddingBefore + radius
                var longOffset: Float = longPaddingBefore + radius
                if (center) {
                    longOffset += (longSize - longPaddingBefore - longPaddingAfter - count * threeRadius + dividerWidth) / 2f
                }
                // unselected indicator's drawing
                var indicatorFillRadius: Float = radius
                if (paintStroke.strokeWidth > 0.0f) {
                    indicatorFillRadius -= paintStroke.strokeWidth / 2.0f
                }
                var dX: Float
                var dY: Float
                for (iLoop in 0 until count) {
                    val drawLong = longOffset + iLoop * threeRadius
                    if (orientation == 0) {
                        dX = drawLong
                        dY = shortOffset
                    } else {
                        dX = shortOffset
                        dY = drawLong
                    }
                    if (paintUnselected.alpha > 0) {
                        canvas.drawCircle(dX, dY, indicatorFillRadius, paintUnselected)
                    }
                    if (indicatorFillRadius != radius) {
                        canvas.drawCircle(dX, dY, radius, paintStroke)
                    }
                }
                // selected indicator's drawing
                var cx = when {
                    snap -> snapItem
                    else -> currentItem
                } * threeRadius
                if (!this.snap) {
                    cx += pageOffset * threeRadius
                }
                if (orientation == 0) {
                    dX = longOffset + cx
                    dY = shortOffset
                } else {
                    dX = shortOffset
                    dY = longOffset + cx
                }
                canvas.drawCircle(dX, dY, radius, paintSelected)
            }
        }
    }

    override fun setViewPager(vp: ViewPager) {
        this.viewPager = vp
    }

    override fun setViewPager(vp: ViewPager, currentItem: Int) {
        TODO("Not yet implemented")
    }

    override fun setCurrentItem(currentItem: Int) {
        checkNotNull(viewPager) { "ViewPager has not been bound." }
        viewPager!!.currentItem = currentItem
        this.currentItem = currentItem
        invalidate()
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        TODO("Not yet implemented")
    }

    override fun notifyDataSetChanged() {
        TODO("Not yet implemented")
    }

    override fun onPageScrollStateChanged(state: Int) {
        TODO("Not yet implemented")
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        TODO("Not yet implemented")
    }

    override fun onPageSelected(position: Int) {
        TODO("Not yet implemented")
    }
}
