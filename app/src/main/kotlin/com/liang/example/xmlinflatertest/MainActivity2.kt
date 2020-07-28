package com.liang.example.xmlinflatertest

import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.MaskFilter
import android.graphics.NinePatch
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import java.util.*

/**
 * 在Android自定义View使用绘图的裁剪功能的时候,我们最好manifest中的<application/> 或者<activity/>或者<fragment/>标签中添加如下属性: android:hardwareAccelerated="false"
 * 因为裁剪功能是不支持硬件加速的.没有设置的话,有可能绘图裁剪的效果出不来;
 */

// test class
class MainActivity2 : AppCompatActivity() {
    // fun test() {
    //     val paint = Paint()
    //     paint.isAntiAlias = true
    //     paint.color = Color.RED
    //     paint.style = Paint.Style.FILL
    //     paint.strokeWidth = 30f
    //     paint.setShadowLayer(10f, 20f, 20f, Color.RED)
    //     paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    //         paint.blendMode = BlendMode.SRC
    //     }
    //
    //     val canvas = Canvas()
    //     canvas.drawColor(Color.RED)
    //     canvas.drawRGB(Color.red(Color.RED), Color.green(Color.RED), Color.blue(Color.RED))
    //     canvas.drawARGB(Color.alpha(Color.RED), Color.red(Color.RED), Color.green(Color.RED), Color.blue(Color.RED))
    //     canvas.drawText("text", 0, 4, 20f, 20f, paint)
    //     canvas.drawText("text".toCharArray(), 0, 4, 20f, 20f, paint)
    //     canvas.drawPosText("text".toCharArray(), 0, 4, floatArrayOf(/* 应该要有8个float值的 */), paint)
    //     // canvas.drawTextRun()
    //     canvas.drawPath(Path(), paint)
    //     // canvas.drawTextOnPath()
    //     canvas.drawPoint(10f, 10f, paint)
    //     canvas.drawPoints(floatArrayOf(10f, 10f, 20f, 10f, 10f, 20f), paint)
    //     canvas.drawLine(0f, 0f, 300f, 300f, paint)
    //     canvas.drawLines(floatArrayOf(0f, 0f, 100f, 100f, 100f, 100f, 300f, -100f), paint)
    //     canvas.drawRect(0f, 0f, 300f, 200f, paint)
    //     canvas.drawRoundRect(0f, 0f, 300f, 200f, 5f, 5f, paint)
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    //         canvas.drawDoubleRoundRect(RectF(0f, 0f, 200f, 300f), 5f, 5f, RectF(20f, 20f, 280f, 280f), 5f, 5f, paint)
    //     }
    //     canvas.drawCircle(300f, 300f, 200f, paint)
    //     canvas.drawOval(0f, 0f, 300f, 200f, paint)
    //     canvas.drawArc(RectF(0f, 0f, 300f, 200f), 0, 90, true, paint)
    //     canvas.drawBitmap()
    //     canvas.drawPatch(patch: NinePatch, Rect(0, 0, 300, 200), paint)
    //
    //     canvas.drawVertices()
    //     canvas.drawRenderNode()
    //
    //     paint.textAlign = Paint.Align.LEFT
    //     paint.textLocale = Locale.CHINA
    //     paint.textLocales = LocaleList.getDefault()
    //     paint.textSize = 50f
    //     paint.textScaleX = 2f
    //     paint.textSkewX = 2f
    //
    //     paint.isLinearText = false
    //     paint.isSubpixelText = false
    //     paint.isUnderlineText = false
    //     paint.isElegantTextHeight = false
    //     paint.isFakeBoldText = false
    //     paint.isStrikeThruText = false
    // }
}

/**
 * ColorFilter / ColorMatrix / MaskFilter / PathEffect
 *
 * BlendModeColorFilter(color: Int, mode: BlendMode)
 *
 * PorterColorFilter(color: Int, mode: PorterDuff.Mode)  // 将color与bitmap/paint的每个color pixel进行“图片混合操作”
 *
 * ColorMatrixColorFilter  // 色彩矩阵颜色过滤器
 * ColorMatrix  // TODO 深入理解，比如颜色算法之类的
 * 在Android中图片是以RGBA像素点的形式加载到内存中的，修改这些像素信息需要一个叫做ColorMatrix类的支持，这个类其实定义的是一个矩阵，是一个4x5的float[]类型的矩阵
 * R = a*R + b*G + c*B + d*A + e;
 * G = f*R + g*G + h*B + i*A + j;
 * B = k*R + l*G + m*B + n*A + o;
 * A = p*R + q*G + r*B + s*A + t;
 *
 * LightingColorFilter  // 光照颜色过滤器
 * R' = R * colorMultiply.R + colorAdd.R
 * G' = G * colorMultiply.G + colorAdd.G
 * B' = B * colorMultiply.B + colorAdd.B
 *
 * BlurMaskFilter  // 模糊遮罩滤镜 [详解Paint的setMaskFilter(MaskFilter maskfilter)](https://www.cnblogs.com/tianzhijiexian/p/4297734.html)
 * EmbossMaskFilter  // 浮雕遮罩滤镜  TODO
 *
 * DashPathEffect  // 第一个参数是一个浮点型的数组，元素个数大于等于2即可，偶数参数定义了实线的长度，而奇数参数定义虚线长度，如果此时数组后面不再有数据则重复第一个数以此往复循环。
 *     第二个参数则是偏差值，动态改变其值会让路径产生动画的效果。
 * PathDashPathEffect  // 和DashPathEffect是类似的，但可以让我们自己定义路径虚线的样式
 * CornerPathEffect  // CornerPathEffect则可以将路径的转角变得圆滑，CornerPathEffect的构造方法只接受一个参数radius，意思就是转角处的圆滑程度。
 * DiscretePathEffect  // 离散路径效果，相对来说则稍微复杂点，其会在路径上绘制很多“杂点”的突出来模拟一种类似生锈铁丝的效果。
 *     其构造方法有两个参数：指定这些突出的“杂点”的密度，值越小杂点越密集；“杂点”突出的大小，值越大突出的距离越大。
 * SumPathEffect  // 可以用来组合两种路径效果，会把两种路径效果加起来再作用于路径。
 * ComposePathEffect  // 可以用来组合两种路径效果，会先将路径变成innerpe的效果，再去复合outerpe的路径效果，即：outerpe(innerpe(Path))。
 */

/**
 * TODO: Paint
 *
 * paint.shader = Shader(  // TODO
 * paint.typeface = Typeface.create("宋体", Typeface.BOLD)
 *
 * paint.startHyphenEdit  // TODO
 * paint.endHyphenEdit  // TODO
 * paint.wordSpacing  // TODO
 *
 * paint.strokeMiter  // 设置笔画的倾斜度，取值：>=0。如：小时候用的铅笔，削的时候斜与垂直削出来的笔尖效果是不一样的。主要是用来设置笔触的连接处的样式。可以和setStrokeJoin()来比较比较。  TODO
 * paint.strokeCap = Paint.Cap.BUTT / ROUND / SQUARE
 * paint.strokeWidth = 50f  // 当画笔样式（style）为STROKE或FILL_OR_STROKE时(空心样式时)，设置笔刷的粗细度
 * paint.strokeJoin = Paint.Join.MITER / ROUND / BEVEL  TODO
 *
 * paint.isAntiAlias = true  // 抗拒齿
 * paint.isDither = true  // 防抖动
 * paint.isFilterBitmap = true  // 如果该项设置为true，则图像在动画进行中会滤掉对Bitmap图像的优化操作，加快显示速度，本设置项依赖于dither和xfermode的设置。
 * paint.maskFilter = BlurMaskFilter(20, BlurMaskFilter.Blur.SOLID)
 *
 * paint.style = Paint.Style.FILL  // 设置画笔状态(FILL: 填充; STROKE: 描边; FILL_AND_STROKE: 描边和填充(会在半径的基础上增加 StrokeWidth / 2 的stroke 宽度))
 * paint.setShadowLayer(10f /* 模糊半径，越大越模糊 */, 20f /* 阴影离开文字的x横向距离 */, 20f /* Y横向距离 */, Color.RED)
 * paint.fontFeatureSettings = settings  // 设置字体的样式，这个样式和CSS样式很类似。样式可以为null，可以参考：http://dev.w3.org/csswg/css-fonts/#propdef-font-feature-settings  TODO
 * paint.fontVariationSettings = settings: String
 * paint.setHinting(mode: Int)  // 设置画笔的隐藏模式。可以是 HINTING_OFF or HINTING_ON之一
 * paint.pathEffect = SumPathEffect(CornerPathEffect(50f), DashPathEffect(floatArrayOf(10f, 2f), 0))
 *
 * paint.setColor(Color.RED)  // 设置画笔颜色
 * paint.alpha = 255
 * paint.setARGB(a, r, g, b)
 * paint.colorFilter = ColorMatrixColorFilter(floatArrayOf(-1f, 0f, 0f, 1f, 1f, 0f, -1f, 0f, 1f, 1f, 0f, 0f, -1f, 1f, 1f, 0f, 0f, 0f, 1f, 0f))
 * paint.colorFilter = LightingColorFilter(0xFFFFFFFF, 0x00110000)
 *
 * paint.textAlign = Paint.Align.LEFT  // 文本对齐方式
 * paint.textSize = 50f
 * paint.textLocales = LocaleList.getDefault()
 * paint.textLocale = Locale.CHINA
 * paint.textScaleX = 2f  // 字体水平拉伸
 * paint.textSkewX = 0.25f  // 字体水平倾斜度
 *
 * paint.isLinearText = false  // 设置是否打开线性文本标识，这玩意对大多数人来说都很奇怪不知道这玩意什么意思。想要明白这东西你要先知道文本在Android中是如何进行存储和计算的。在Android中文本的绘制需要使用一个bitmap作为单个字符的缓存，既然是缓存必定要使用一定的空间，我们可以通过setLinearText (true)告诉Android我们不需要这样的文本缓存。
 * paint.isSubpixelText = false
 * paint.isUnderlineText = false  // 是否下划线
 * paint.isElegantTextHeight = false  // 设置原始高度 。默认时，字体是压缩之后才显示的，如果想显示原始的高度，就可以设置为true。不过，该方法对于中文没什么用，一般用于特殊的字符
 * paint.isFakeBoldText = false  // 是否粗体
 * paint.isStrikeThruText = false  // 是否删除线
 *
 * paint.setLetterSpacing(letterSpacing: Float)  // 设置行间距，默认是0
 */

/**
 * TODO: Canvas
 *
 * canvas.translate(0, 250)
 * canvas.scale(1, -1)
 *
 * // 整个画布的背景颜色
 * canvas.drawColor(Int/Long, PorterDuff.Mode/BlendMode)
 * canvas.drawRGB(0, 0, 0)
 * canvas.drawARGB(255, 0, 0, 0)
 *
 * // 文本
 * canvas.drawText("text", startIndex = 0, count = "text".length, startX, startY, paint)  // 文本的 x 和 y 是 baseline 的 x 和 y
 * canvas.drawText("text".toCharArray(), startIndex = 0, endIndex = "text".length, startX, startY, paint)
 * canvas.drawPosText(text, startIndex = 0, count = text.length, pos: FloatArray /* 每个character需要描绘的位置(x, y)的数组 */, paint)
 * canvas.drawTextRun(text.toCharArray(), startIndex, count, contextIndex, contextCount, startX, startY, isRtl, paint)  // [Canvas DrawText详解](https://blog.csdn.net/qqqq245425070/article/details/79027979)
 * canvas.drawTextRun(text: CharSequence / MeasuredText, startIndex, endIndex, contextStart, contextEnd, startX, startY, isRtl, paint)  text[startIndex, endIndex] 里面的内容会受到 text[contextStart, contextEnd] 里面的内容的影响，有些语言的同一文字在不同的上下文中写法不同
 * canvas.drawTextOnPath(text, path: Path, horizontalOffset, verticalOffset, paint)  // 沿着 path 绘制文本
 * canvas.drawTextOnPath(text.toCharArray(), startIndex, count, path: Path, horizontalOffset, verticalOffset, paint)
 * // Canvas.drawText() 只能绘制单行的文字，而不能换行。不能在 View 的边缘自动折行。不能在换行符 \n 处换行。如果需要绘制多行的文字，你必须自行把文字切断后分多次使用 drawText() 来绘制，或者——使用 StaticLayout。
 * // StaticLayout 并不是一个 View 或者 ViewGroup ，而是 android.text.Layout 的子类，它是纯粹用来绘制文字的。 StaticLayout 支持换行，它既可以为文字设置宽度上限来让文字自动换行，也会在 \n 处主动换行。
 *
 * // 点
 * canvas.drawPoint(x, y, paint)
 * canvas.drawPoints(points: FloatArray /* 包含每个点的 x, y */, startIndex: 0 /* 注意不是 startPointIndex */, count = points.length, paint)
 *
 * // 直线
 * canvas.drawLine(startX, startY, endX, endY, paint)
 * canvas.drawLines(points: FloatArray /* 包含每条直线的 startX, startY, endX, endY */, startIndex = 0, count = points.length, paint)
 *
 * // 长方形
 * canvas.drawRect(left, top, right, bottom, paint)
 * canvas.drawRect(r: Rect/ RectF, paint)
 * canvas.drawRoundRect(r: RectF, rx: Float, ry: Float, paint)  // 四个边角是圆的
 * canvas.drawRoundRect(left, top, right, bottom, rx, ry, paint)
 * canvas.drawDoubleRoundRect(outer: RectF, outerRx, outerRy, inner: RectF, innerRx, innerRy, paint)
 * canvas.drawDoubleRoundRect(outer: RectF, outerRadii: FloatArray, inner: RectF, innerRadii: FloatArray /* 四个角(tl, tr, br, bl)的 rx, ry */, paint)
 *
 * // 圆
 * canvas.drawCircle(centerX, centerY, radius, paint)
 *
 * // 椭圆
 * canvas.drawOval(left, top, right, bottom, paint)
 * canvas.drawOval(r: Rect/RectF, paint)
 *
 * // 弧
 * canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, useCenter, paint)
 * canvas.drawArc(oval: RectF, startAngle, sweepAngle, useCenter /* 是否有弧的两边，True，还两边，False，只有一条弧 */, paint)
 *
 * // Bitmap
 * canvas.drawBitmap(bitmap, src: Rect, dst: Rect/RectF, paint)
 * canvas.drawBitmap(bitmap, left, top, paint)
 * canvas.drawBitmap(bitmap, matrix, paint)  // TODO
 * canvas.drawBitmap(colors: IntArray, offset: Int, stride: Int, x: Float/Int, y: Float/Int, width: Int, height: Int, hasAlpha: Boolean, paint)  // TODO
 * canvas.drawBitmapMesh(bitmap, meshWidth: Int, meshHeight: Int, verts: FloatArray, vertOffset: Int, colors: IntArray, colorOffset: Int, paint)  // TODO
 *
 * // other
 * canvas.drawPath(path: Path, paint)  // TODO
 * canvas.drawPatch(patch: NinePatch, dst: Rect/RectF, paint)  // TODO
 * canvas.drawPaint(paint)  // TODO
 * canvas.drawVertices(  // TODO
 * canvas.drawRenderNode(renderNode: RenderNode)  // TODO
 */

/**
 * TODO: xfermode
 * paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
 * // [xfermode的基本用法](https://www.jianshu.com/p/4017cb5a0ff3)
 * // [Android Paint&Xfermode总结](https://blog.csdn.net/u014133119/article/details/80769591) -- 图片镜像或倒影 / 圆形(其他形状) / so on
 * // 1 区域指图片A中不相交的地方
 * // 2 区域指图片A与图片B相交的地方
 * // 3 区域指图片B中不相交的地方
 * public enum Mode {
 *     //  [0, 0] 什么都不显示
 *     CLEAR       (0),
 *     //  [Sa, Sc] 仅显示图片A
 *     SRC         (1),
 *     //  [Da, Dc] 仅显示图片B
 *     DST         (2),
 *     //  [Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc] 2中图片A在图片B上面
 *     SRC_OVER    (3),
 *     //  [Sa + (1 - Sa)*Da, Rc = Dc + (1 - Da)*Sc] 2中图片A在图片B下面
 *     DST_OVER    (4),
 *     //  [Sa * Da, Sc * Da] 1和3不显示，2中显示图片A
 *     SRC_IN      (5),
 *     //  [Sa * Da, Sa * Dc] 1和3不显示，2中显示图片B
 *     DST_IN      (6),
 *     //  [Sa * (1 - Da), Sc * (1 - Da)] 2和3不显示
 *     SRC_OUT     (7),
 *     //  [Da * (1 - Sa), Dc * (1 - Sa)] 1和2不显示
 *     DST_OUT     (8),
 *     //  [Da, Sc * Da + (1 - Sa) * Dc] 1不显示，2中A在B上
 *     SRC_ATOP    (9),
 *     //  [Sa, Sa * Dc + Sc * (1 - Da)] 3不显示，2中B在A上
 *     DST_ATOP    (10),
 *     //  [Sa + Da - 2 * Sa * Da, Sc * (1 - Da) + (1 - Sa) * Dc] 2不显示
 *     XOR         (11),
 *     //  [Sa + Da - Sa*Da, Sc*(1 - Da) + Dc*(1 - Sa) + min(Sc, Dc)] 2中颜色混杂，变暗
 *     DARKEN      (12),
 *     //  [Sa + Da - Sa*Da, Sc*(1 - Da) + Dc*(1 - Sa) + max(Sc, Dc)] 2中颜色混杂，变亮
 *     LIGHTEN     (13),
 *     //  [Sa * Da, Sc * Dc] 1和3不显示，2中颜色相乘 ???
 *     MULTIPLY    (14),
 *     //  [Sa + Da - Sa * Da, Sc + Dc - Sc * Dc] 2中颜色均和，保留两个图层中较白的部分，较暗的部分被遮盖 ???
 *     SCREEN      (15),
 *     //  Saturate(S + D)
 *     ADD         (16),  // ???
 *     OVERLAY     (17);  // ???
 * }
 *
 * mode | 效果
 * :-|:-
 * CLEAR | 清除图像（源覆盖的目标像素被清除为0）
 * SRC | 只显示源图像（源像素替换目标像素）
 * DST | 只显示目标图像（源像素被丢弃，使目标保持完整）
 * SRC_OVER | 将源图像放在目标图像上方
 * DST_OVER | 将目标图像放在源图像上方（源像素是在目标像素后面绘制的。）
 * SRC_IN | 只在源图像和目标图像相交的地方绘制【源图像】（保持源像素覆盖目标像素，丢弃剩余的源和目标像素）
 * DST_IN | 只在源图像和目标图像相交的地方绘制【目标图像】，绘制效果受到源图像对应地方透明度影响
 * SRC_OUT | 只在源图像和目标图像不相交的地方绘制【源图像】，相交的地方根据目标图像的对应地方的alpha进行过滤，目标图像完全不透明则完全过滤，完全透明则不过滤
 * DST_OUT | 只在源图像和目标图像不相交的地方绘制【目标图像】，在相交的地方根据源图像的alpha进行过滤，源图像完全不透明则完全过滤，完全透明则不过滤（保持目标像素不被源像素所覆盖。丢弃由源像素覆盖的目标像素。丢弃所有源像素。）
 * SRC_ATOP | 在源图像和目标图像相交的地方绘制【源图像】，在不相交的地方绘制【目标图像】，相交处的效果受到源图像和目标图像alpha的影响
 * DST_ATOP | 在源图像和目标图像相交的地方绘制【目标图像】，在不相交的地方绘制【源图像】，相交处的效果受到源图像和目标图像alpha的影响
 * XOR | 在源图像和目标图像相交的地方之外绘制它们，在相交的地方受到对应alpha和色值影响，如果完全不透明则相交处完全不绘制
 * DARKEN | 变暗,较深的颜色覆盖较浅的颜色，若两者深浅程度相同则混合
 * LIGHTEN | 变亮，与DARKEN相反，DARKEN和LIGHTEN生成的图像结果与Android对颜色值深浅的定义有关
 * MULTIPLY | 正片叠底，源图像素颜色值乘以目标图像素颜色值除以255得到混合后图像像素颜色值
 * SCREEN | 滤色，色调均和,保留两个图层中较白的部分，较暗的部分被遮盖（添加源和目标像素，然后减去源像素乘以目标。）
 * ADD | 饱和相加,对图像饱和度进行相加,不常用
 * OVERLAY | 叠加
 */

/**
 * TODO: BlendMode
 */

/**
 * TODO: Path
 * Path().run {
 *     reset()  // reset不保留内部数据结构，但会保留FillType.
 *     rewind()  // rewind会保留内部的数据结构，但不保留FillType
 *     set(src: Path)
 *     close()  // close的作用是封闭路径，与连接当前最后一个点和第一个点并不等价。如果连接了最后一个点和第一个点仍然无法形成封闭图形，则close什么 也不做。
 *
 *     enum class Op {
 *         DIFFERENCE, /* 减去path1中path1与path2都存在的部分: path1 = (path1 - path1 ∩ path2) */
 *         INTERSECT, /* 保留path1与path2共同的部分: path1 = path1 ∩ path2 */
 *         UNION, /* 取path1与path2的并集: path1 = path1 ∪ path2 */
 *         XOR, /* 去掉path1与path2共同的部分，剩下的保留: path1 = path2 - (path1 ∩ path2) */
 *         REVERSE_DIFFERENCE /* 减去path2中path1与path2都存在的部分: path1 = (path1 ∪ path2) - (path1 ∩ path2) */
 *     }
 *     op(path, op: Op): Boolean
 *     op(path1, path2, op: Op): Boolean
 *
 *     enum class FillType { WINDING / EVEN_ODD / INVERSE_WINDING / INVERSE_EVEN_ODD }  // TODO
 *     getFillType(): FillType
 *     setFillType(ft)
 *     isInverseFillType(): Boolean
 *     toggleInverseFillType()
 *
 *     enum class Direction { CW /* 顺时针: clockwise */ , CCW /* 逆时针: counter-clockwise */ }
 *     detectSimplePath(left, top, right, bottom, direction)
 *
 *     isConvex(): Boolean  // TODO
 *     isEmpty(): Boolean
 *     isRect(rect: RectF): Boolean
 *
 *     moveTo(x, y)
 *     rMoveTo(x, y)
 *     lineTo(x, y)
 *     rLineTo(x, y)
 *     quadTo(x1, y1, x2, y2)
 *     rQuadTo(x1, y1, x2, y2)
 *     cubicTo(x1, y1, x2, y2, x3, y3)
 *     rCubicTo(x1, y1, x2, y2, x3, y3)
 *     arcTo(oval: Rect, startAngle: Float, sweepAngle: Float)
 *     arcTo(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, forceMoveTo: Boolean)
 *
 *     computeBounds(bounds: RectF, extra: Boolean)  // 计算Path的边界
 *     incReserve(extraPtCount: Int)  // 提示Path还有多少个点等待加入**(这个方法貌似会让Path优化存储结构)**
 *
 *     addRect(rect: RectF, direction)
 *     addRect(l, t, r, b, direction)
 *     addOval(rect, direction)
 *     addOval(l, t, r, b, direction)
 *     addCircle(rx, ry, r, dir)
 *     addArc(rect, startAngle, end)
 *     addArc(l, t, r, b, startAngle, end)
 *     addRoundRect(rect, rx, ry, dir)
 *     addRoundRect(rect, radii: FloatArray, dir)
 *     addRoundRect(l, t, r, b, rx, ry, dir)
 *     addRoundRect(l, t, r, b, radii: FloatArray, dir)
 *     addPath(path)
 *     addPath(path, offsetX, offsetY)
 *     addPath(path, matrix: Matrix)  // TODO: Matrix
 *
 *     offset(offsetX, offsetY, path)  // 将当前path平移后的状态存入dst中，不会影响当前path
 *     offset(offsetX, offsetY)  // 平移将作用于当前path，相当于第一种方法
 *     setLastPoint(x, y)  // 重置当前path中最后一个点位置，如果在绘制之前调用，效果和moveTo相同
 *     transform(matrix, path: Path?)
 *     approximate(acceptableError: Float)  // TODO
 * }
 */

/**
 * TODO: Matrix
 */
