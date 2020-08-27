package com.liang.example.xmlinflatertest

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.liang.example.androidtest.R
import com.liang.example.utils.getScreenWidthPixels
import com.liang.example.view_ktx.findViewById
import com.liang.example.view_ktx.setDrawableLeft
import com.liang.example.view_ktx.setDrawableRight
import com.liang.example.xml_inflater2.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Xml-Inflater"
    }

    private lateinit var animObj: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transformer = XmlTransformer()
        val apm = AttrProcessorManager(this)
        apm + DimenAttrProcessor(apm)
        apm + ColorAttrProcessor(apm)
        apm + FlagAttrProcessor(apm)
        apm + EnumAttrProcessor(apm)
        apm + IntegerAttrProcessor(apm)
        apm + FloatAttrProcessor(apm)
        apm + FractionAttrProcessor(apm)
        apm + StringAttrProcessor(apm)
        apm + BooleanAttrProcessor(apm)
        apm + ReferenceAttrProcessor(apm)

        throwFlag = true
        debugFlag = true

        val rpm = ResProcessorManager(this)
        ManagerHolder.rpm = rpm
        ManagerHolder.apm = apm

        Log.d(TAG,
                "@drawable/icon_left: ${apm.refer("@drawable/icon_left")}, ${R.drawable.icon_left}, " +
                        "result: ${apm.refer("@drawable/icon_left") == R.drawable.icon_left}\n" +
                        "@android:color/holo_blue_dark: ${apm.refer("@android:color/holo_blue_dark")}, ${android.R.color.holo_blue_dark}, " +
                        "result: ${apm.refer("@android:color/holo_blue_dark") == android.R.color.holo_blue_dark}\n" +
                        "@dimen/dimen1000: ${apm.refer("@dimen/dimen1000")}, ${R.dimen.dimen1000}, " +
                        "result: ${apm.refer("@dimen/dimen1000") == R.dimen.dimen1000}\n" +
                        "@color/color000000: ${apm.refer("@color/color000000")}, ${R.color.color000000}, " +
                        "result: ${apm.refer("@color/color000000") == R.color.color000000}\n" +
                        "@id/absolute_layout_1: ${apm.refer("@id/absolute_layout_1")}, ${R.id.absolute_layout_1}, " +
                        "result: ${apm.refer("@id/absolute_layout_1") == R.id.absolute_layout_1}"
        )

        val resProcessor1 = ValuesResProcessor()
        val resProcessor2 = ColorSelectorResProcessor()

        val resProcessor3 = AnimatorResProcessor()
        val resProcessor4 = ObjAnimatorResProcessor(resProcessor3)
        val resProcessor5 = AnimatorSetResProcessor(resProcessor3, resProcessor4)
        val resProcessor6 = StateListAnimatorResProcessor()

        val resProcessor7 = AlphaResProcessor()
        val resProcessor8 = ScaleResProcessor()
        val resProcessor9 = RotateResProcessor()
        val resProcessor10 = TranslateResProcessor()
        val resProcessor11 = AnimationSetResProcessor(resProcessor9, resProcessor8, resProcessor7, resProcessor10)
        val resProcessor12 = LayoutAnimationResProcessor()
        val resProcessor13 = GridLayoutAnimationResProcessor()
        val resProcessor14 = AnimationListResProcessor()

        val resProcessor15 = ColorDrawableResProcessor()
        val resProcessor16 = ShapeResProcessor()
        val resProcessor17 = RippleResProcessor()
        val resProcessor18 = StateListDrawableResProcessor()
        val resProcessor19 = InsetResProcessor()
        val resProcessor20 = ClipResProcessor()
        val resProcessor21 = RotateDrawResProcessor()
        val resProcessor22 = ScaleDrawResProcessor()
        val resProcessor23 = LayerListResProcessor()
        val resProcessor24 = LevelListResProcessor()
        val resProcessor25 = TransitionResProcessor()
        val resProcessor26 = AnimatedStateListResProcessor()
        val resProcessor27 = VectorResProcessor()
        val resProcessor28 = AnimatedVectorResProcessor()
        val resProcessor29 = AnimatedImageResProcessor()

        val resProcessor30 = ViewParser()
        val resProcessor31 = ViewGroupParser(resProcessor30)
        val resProcessor32 = TextViewParser(resProcessor30)
        val resProcessor33 = ButtonParser(resProcessor32)
        val resProcessor34 = EditTextParser(resProcessor32)
        val resProcessor35 = ImageViewParser(resProcessor30)
        val resProcessor36 = ImageButtonParser(resProcessor35)
        val resProcessor37 = FrameLayoutParser(resProcessor31)
        val resProcessor38 = LinearLayoutParser(resProcessor31)
        val resProcessor39 = RelativeLayoutParser(resProcessor31)
        val resProcessor40 = ScrollViewParser(resProcessor37)
        val resProcessor41 = CompoundButtonParser(resProcessor33)
        val resProcessor42 = SwitchViewParser(resProcessor41)
        val resProcessor43 = CheckBoxParser(resProcessor41)
        val resProcessor44 = RadioButtonParser(resProcessor41)
        val resProcessor45 = RadioGroupParser(resProcessor38)
        val resProcessor46 = ToggleButtonParser(resProcessor41)
        val resProcessor47 = AbsListViewParser(resProcessor31)
        val resProcessor48 = ListViewParser(resProcessor47)
        val resProcessor49 = ExpandableListViewParser(resProcessor48)
        val resProcessor50 = GridViewParser(resProcessor47)
        val resProcessor51 = RecyclerViewParser(resProcessor31)
        val resProcessor52 = ViewPagerParser(resProcessor31)
        val resProcessor53 = CoordinatorLayoutParser(resProcessor31)
        val resProcessor54 = ConstraintLayoutParser(resProcessor31)
        val resProcessor55 = PlaceholderParser(resProcessor30)
        val resProcessor56 = GuidelineParser(resProcessor30)
        val resProcessor57 = ConstraintHelperParser(resProcessor30)
        val resProcessor58 = GroupParser(resProcessor30)
        val resProcessor59 = BarrierParser(resProcessor30)
        val resProcessor60 = ConstraintsParser(resProcessor30)
        val resProcessor61 = GridLayoutParser(resProcessor31)
        val resProcessor62 = WebViewParser(resProcessor31)
        val resProcessor63 = SurfaceViewParser(resProcessor30)
        val resProcessor64 = TextureViewParser(resProcessor30)
        val resProcessor65 = VideoViewParser(resProcessor63)
        val resProcessor66 = CalendarViewParser(resProcessor37)
        val resProcessor67 = DatePickerParser(resProcessor37)
        val resProcessor68 = NumberPickerParser(resProcessor38)
        // val resProcessor69 = TimePickerParser(resProcessor37)
        val resProcessor70 = ProgressBarParser(resProcessor30)
        val resProcessor71 = AbsSeekBarParser(resProcessor70)
        val resProcessor72 = SeekBarParser(resProcessor71)
        val resProcessor73 = RatingBarParser(resProcessor71)
        val resProcessor74 = SearchViewParser(resProcessor38)
        val resProcessor75 = ViewAnimatorParser(resProcessor37)
        val resProcessor76 = ViewFlipperParser(resProcessor75)
        val resProcessor77 = ViewSwitcherParser(resProcessor75)
        val resProcessor78 = TextSwitcherParser(resProcessor77)
        val resProcessor79 = ImageSwitcherParser(resProcessor77)

        rpm.plus(resProcessor1)
        rpm.plus(resProcessor2)
        rpm.plus(resProcessor3)
        rpm.plus(resProcessor4)
        rpm.plus(resProcessor5)
        rpm.plus(resProcessor6)
        rpm.plus(resProcessor7)
        rpm.plus(resProcessor8)
        rpm.plus(resProcessor9)
        rpm.plus(resProcessor10)
        rpm.plus(resProcessor11)
        rpm.plus(resProcessor12)
        rpm.plus(resProcessor13)
        rpm.plus(resProcessor14)
        rpm.plus(resProcessor15)
        rpm.plus(resProcessor16)
        rpm.plus(resProcessor17)
        rpm.plus(resProcessor18)
        rpm.plus(resProcessor19)
        rpm.plus(resProcessor20)
        rpm.plus(resProcessor21)
        rpm.plus(resProcessor22)
        rpm.plus(resProcessor23)
        rpm.plus(resProcessor24)
        rpm.plus(resProcessor25)
        rpm.plus(resProcessor26)
        rpm.plus(resProcessor27)
        rpm.plus(resProcessor28)
        rpm.plus(resProcessor29)

        rpm.plus(resProcessor30)
        rpm.plus(resProcessor31)
        rpm.plus(resProcessor32)
        rpm.plus(resProcessor33)
        rpm.plus(resProcessor34)
        rpm.plus(resProcessor35)
        rpm.plus(resProcessor36)
        rpm.plus(resProcessor37)
        rpm.plus(resProcessor38)
        rpm.plus(resProcessor39)
        rpm.plus(resProcessor40)
        rpm.plus(resProcessor41)
        rpm.plus(resProcessor42)
        rpm.plus(resProcessor43)
        rpm.plus(resProcessor44)
        rpm.plus(resProcessor45)
        rpm.plus(resProcessor46)
        rpm.plus(resProcessor47)
        rpm.plus(resProcessor48)
        rpm.plus(resProcessor49)
        rpm.plus(resProcessor50)
        rpm.plus(resProcessor51)
        rpm.plus(resProcessor52)
        rpm.plus(resProcessor53)
        rpm.plus(resProcessor54)
        rpm.plus(resProcessor55)
        rpm.plus(resProcessor56)
        rpm.plus(resProcessor57)
        rpm.plus(resProcessor58)
        rpm.plus(resProcessor59)
        rpm.plus(resProcessor60)
        rpm.plus(resProcessor61)
        rpm.plus(resProcessor62)
        rpm.plus(resProcessor63)
        rpm.plus(resProcessor64)
        rpm.plus(resProcessor65)
        rpm.plus(resProcessor66)
        rpm.plus(resProcessor67)
        rpm.plus(resProcessor68)
        // rpm.plus(resProcessor69)
        rpm.plus(resProcessor70)
        rpm.plus(resProcessor71)
        rpm.plus(resProcessor72)
        rpm.plus(resProcessor73)
        rpm.plus(resProcessor74)
        rpm.plus(resProcessor75)
        rpm.plus(resProcessor76)
        rpm.plus(resProcessor77)
        rpm.plus(resProcessor78)
        rpm.plus(resProcessor79)

        val contentNode = transformer.run("""
            <?xml version="1.0" encoding="UTF-8"?>
            <ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="@dimen/dimen20">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical">

                    <TextView
                        android:id="@+id/test_view_anim_obj"
                        android:layout_width="@dimen/dimen260"
                        android:layout_height="@dimen/dimen260"
                        android:background="@android:color/black"
                        android:gravity="center"
                        android:text="@string/text"
                        android:textColor="@android:color/white"
                        android:textSize="@dimen/font100" />

                    <Button
                        android:id="@+id/test_view_anim_scale"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="@dimen/dimen10"
                        android:text="@string/scale"
                        android:textAllCaps="false" />

                    <Button
                        android:id="@+id/test_view_anim_alpha"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="@dimen/dimen10"
                        android:text="@string/alpha"
                        android:textAllCaps="false" />

                    <Button
                        android:id="@+id/test_view_anim_translate"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="@dimen/dimen10"
                        android:text="@string/translate"
                        android:textAllCaps="false" />

                    <Button
                        android:id="@+id/test_view_anim_rotate"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="@dimen/dimen10"
                        android:text="@string/rotate"
                        android:textAllCaps="false" />

                    <Button
                        android:id="@+id/test_view_anim_color"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="@dimen/dimen10"
                        android:text="@string/color"
                        android:textAllCaps="false" />

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="@dimen/dimen10"
                        android:orientation="horizontal">

                        <CheckBox
                            android:id="@+id/test_view_anim_check_scale"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:checked="true"
                            android:text="@string/scale" />

                        <CheckBox
                            android:id="@+id/test_view_anim_check_alpha"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:checked="true"
                            android:text="@string/alpha" />

                        <CheckBox
                            android:id="@+id/test_view_anim_check_translate"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:checked="true"
                            android:text="@string/translate" />

                        <CheckBox
                            android:id="@+id/test_view_anim_check_rotate"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:checked="true"
                            android:text="@string/rotate" />

                        <CheckBox
                            android:id="@+id/test_view_anim_check_color"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_weight="1"
                            android:checked="true"
                            android:text="@string/color" />
                    </LinearLayout>

                    <Button
                        android:id="@+id/test_view_anim_set"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="@dimen/dimen10"
                        android:text="@string/set"
                        android:textAllCaps="false" />

                    <TextView
                        android:id="@+id/item_select"
                        android:layout_width="match_parent"
                        android:layout_height="40dp"
                        android:layout_marginTop="@dimen/dimen10"
                        android:gravity="center_vertical"
                        android:text="地磁" />
                </LinearLayout>
            </ScrollView>
        """.trimIndent())
        if (!rpm.inflate(this, contentNode, careNameAttr = false)) {
            return makeFail("create ScrollView failed", Unit)!!
        }
        animObj = findViewById("test_view_anim_obj")

        testValues(transformer, resProcessor1)  // has tested
        testSelectorColor(transformer, resProcessor2)  // has tested

        // testAnimator(transformer, resProcessor3)  // has tested
        // testObjectAnimator(transformer, resProcessor4)  // has tested
        // testAnimatorSet(transformer, resProcessor5)  // has tested
        // testStateListAnimator(transformer, resProcessor6)  // has tested

        testAlphaAnimation(transformer, resProcessor7)  // has tested
        testScaleAnimation(transformer, resProcessor8)  // has tested
        testRotateAnimation(transformer, resProcessor9)  // has tested
        testTranslateAnimation(transformer, resProcessor10)  // has tested
        testAnimationSet(transformer, resProcessor11)  // has tested
        // testLayoutAnimation(transformer, resProcessor12)
        // testGridLayoutAnimation(transformer, resProcessor13)
        // testAnimationList(transformer, resProcessor14)

        // testColorDrawable(transformer, resProcessor15)  // has tested
        // testShapeDrawable(transformer, resProcessor16)  // has tested
        // testRippleDrawable(transformer, resProcessor17)  // has tested
        // testStateListDrawable(transformer, resProcessor18)  // has tested
        // testInsetDrawable(transformer, resProcessor19)  // has tested
        // testClipDrawable(transformer, resProcessor20)  // has tested
        // testRotateDrawable(transformer, resProcessor21)  // has tested
        // testScaleDrawable(transformer, resProcessor22)  // has tested
        // testLayerListDrawable(transformer, resProcessor23)  // has tested
        // testLevelListDrawable(transformer, resProcessor24)  // has tested
        // testTransitionDrawable(transformer, resProcessor25)  // has tested
        // testAnimatedStateList(transformer, resProcessor26)
        // testVectorDrawable(transformer, resProcessor27)  // has tested
        // testAnimatedVectorDrawable(transformer, resProcessor28)
        // testAnimatedImageDrawable(transformer, resProcessor29)
    }

    private fun testAnimator(transformer: XmlTransformer, resProcessor: AnimatorResProcessor) {
        val translateGap = getScreenWidthPixels(this) - resources.getDimensionPixelSize(
                R.dimen.dimen20) * 2 - resources.getDimensionPixelSize(R.dimen.dimen260)
        val dom = transformer.parser.parseXmlOrThrow("""
            <animator xmlns:android="http://schemas.android.com/apk/res/android"
                android:duration="@integer/int3000"
                android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                android:repeatCount="@integer/int0"
                android:startOffset="@integer/int300" >
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="alpha_test"
                    android:valueFrom="1"
                    android:valueTo="0" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="alpha_test_reverse"
                    android:valueFrom="0"
                    android:valueTo="1" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="scale_test"
                    android:valueFrom="1"
                    android:valueTo="0" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="scale_test_reverse"
                    android:valueFrom="0"
                    android:valueTo="1" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="rotate_test"
                    android:valueFrom="0"
                    android:valueTo="180" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="rotate_test_reverse"
                    android:valueFrom="180"
                    android:valueTo="360" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="translate_test"
                    android:valueFrom="0"
                    android:valueTo="$translateGap" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="translate_test_reverse"
                    android:valueFrom="$translateGap"
                    android:valueTo="0" />
                <propertyValuesHolder
                    android:valueType="colorType"
                    android:propertyName="color_test"
                    android:valueFrom="@android:color/black"
                    android:valueTo="@android:color/white" />
                <propertyValuesHolder
                    android:valueType="colorType"
                    android:propertyName="color_test_reverse"
                    android:valueFrom="@android:color/white"
                    android:valueTo="@android:color/black" />
            </animator>
        """.trimIndent())
        Log.d(TAG, dom.string())

        val node = transformer.transform(dom)
        node["name"] = "animator_test"
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        val animator = resProcessor.process(node)?.value() ?: return
        val alphaUpdater = NamedAnimatorUpdateListener("") { n, a -> animObj.alpha = a.getAnimatedValue(n) as Float }
        val scaleUpdater = NamedAnimatorUpdateListener("") { n, a ->
            val value = a.getAnimatedValue(n) as Float
            animObj.scaleX = value
            animObj.scaleY = value
        }
        val rotateUpdater = NamedAnimatorUpdateListener("") { n, a -> animObj.rotation = a.getAnimatedValue(n) as Float }
        val translateUpdater = NamedAnimatorUpdateListener("") { n, a -> animObj.translationX = a.getAnimatedValue(n) as Float }
        val colorUpdater = NamedAnimatorUpdateListener("") { n, a ->
            val colorInt = a.getAnimatedValue(n) as Int
            animObj.setTextColor(colorInt)
            animObj.setBackgroundColor(Color.argb(0xff, 0xff - Color.red(colorInt), 0xff - Color.green(colorInt), 0xff - Color.blue(colorInt)))
        }

        findViewById<Button>("test_view_anim_alpha").setOnClickListener {
            resetAnimObjAndAnimator(animator)
            alphaUpdater.propertyName = if (animObj.alpha == 0f) "alpha_test_reverse" else "alpha_test"
            animator.addUpdateListener(alphaUpdater)
            animator.start()
        }

        findViewById<Button>("test_view_anim_scale").setOnClickListener {
            resetAnimObjAndAnimator(animator)
            scaleUpdater.propertyName = if (animObj.scaleX == 0f) "scale_test_reverse" else "scale_test"
            animator.addUpdateListener(scaleUpdater)
            animator.start()
        }

        findViewById<Button>("test_view_anim_rotate").setOnClickListener {
            resetAnimObjAndAnimator(animator)
            rotateUpdater.propertyName = if (animObj.rotation != 0f) "rotate_test_reverse" else "rotate_test"
            animator.addUpdateListener(rotateUpdater)
            animator.start()
        }

        findViewById<Button>("test_view_anim_translate").setOnClickListener {
            resetAnimObjAndAnimator(animator)
            translateUpdater.propertyName = if (animObj.translationX != 0f) "translate_test_reverse" else "translate_test"
            animator.addUpdateListener(translateUpdater)
            animator.start()
        }

        findViewById<Button>("test_view_anim_color").setOnClickListener {
            resetAnimObjAndAnimator(animator)
            colorUpdater.propertyName = if (animObj.currentTextColor == Color.WHITE) "color_test_reverse" else "color_test"
            animator.addUpdateListener(colorUpdater)
            animator.start()
        }

        val checkAlpha = findViewById<CheckBox>("test_view_anim_check_alpha")
        val checkScale = findViewById<CheckBox>("test_view_anim_check_scale")
        val checkTranslate = findViewById<CheckBox>("test_view_anim_check_translate")
        val checkRotate = findViewById<CheckBox>("test_view_anim_check_rotate")
        val checkColor = findViewById<CheckBox>("test_view_anim_check_color")
        findViewById<Button>("test_view_anim_set").setOnClickListener {
            resetAnimObjAndAnimator(animator)
            if (checkAlpha.isChecked) {
                alphaUpdater.propertyName = if (animObj.alpha == 0f) "alpha_test_reverse" else "alpha_test"
                animator.addUpdateListener(alphaUpdater)
            }
            if (checkScale.isChecked) {
                scaleUpdater.propertyName = if (animObj.scaleX == 0f) "scale_test_reverse" else "scale_test"
                animator.addUpdateListener(scaleUpdater)
            }
            if (checkRotate.isChecked) {
                rotateUpdater.propertyName = if (animObj.rotation != 0f) "rotate_test_reverse" else "rotate_test"
                animator.addUpdateListener(rotateUpdater)
            }
            if (checkTranslate.isChecked) {
                translateUpdater.propertyName = if (animObj.translationX != 0f) "translate_test_reverse" else "translate_test"
                animator.addUpdateListener(translateUpdater)
            }
            if (checkColor.isChecked) {
                colorUpdater.propertyName = if (animObj.currentTextColor == Color.WHITE) "color_test_reverse" else "color_test"
                animator.addUpdateListener(colorUpdater)
            }
            animator.start()
        }
    }

    open class NamedAnimatorUpdateListener(open var propertyName: String, open var action: (String, ValueAnimator) -> Unit)
        : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) = action(propertyName, animation)
    }

    private fun resetAnimObjAndAnimator(animator: Animator) {
        animator.end()
        if (animator is ValueAnimator) {
            animator.removeAllUpdateListeners()
        }
        animator.removeAllListeners()
    }

    private fun testObjectAnimator(transformer: XmlTransformer, resProcessor: ObjAnimatorResProcessor) {
        val translateGap = getScreenWidthPixels(this) - resources.getDimensionPixelSize(
                R.dimen.dimen20) * 2 - resources.getDimensionPixelSize(R.dimen.dimen260)
        val dom = transformer.parser.parseXmlOrThrow("""
            <objectAnimator xmlns:android="http://schemas.android.com/apk/res/android"
                android:duration="@integer/int3000"
                android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                android:repeatCount="@integer/int0"
                android:startOffset="@integer/int300" >
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="alpha"
                    android:valueFrom="1"
                    android:valueTo="0" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="scaleX"
                    android:valueFrom="1"
                    android:valueTo="0" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="scaleY"
                    android:valueFrom="1"
                    android:valueTo="0" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="rotation"
                    android:valueFrom="0"
                    android:valueTo="180" />
                <propertyValuesHolder
                    android:valueType="floatType"
                    android:propertyName="translateX"
                    android:valueFrom="0"
                    android:valueTo="$translateGap" />
                <propertyValuesHolder
                    android:valueType="colorType"
                    android:propertyName="backgroundColor"
                    android:valueFrom="@android:color/black"
                    android:valueTo="@android:color/white" />
                <propertyValuesHolder
                    android:valueType="colorType"
                    android:propertyName="textColor"
                    android:valueFrom="@android:color/white"
                    android:valueTo="@android:color/black" />
            </objectAnimator>
        """.trimIndent())
        Log.d(TAG, dom.string())

        val node = transformer.transform(dom)
        node["name"] = "objAnimator_test"
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        val animator = resProcessor.process(node)?.value() ?: return
        animator.target = animObj

        findViewById<Button>("test_view_anim_set").setOnClickListener {
            resetAnimObjAndAnimator(animator)
            animator.start()
        }
    }

    private fun testAnimatorSet(transformer: XmlTransformer, resProcessor: AnimatorSetResProcessor) {
        val translateGap = getScreenWidthPixels(this) - resources.getDimensionPixelSize(
                R.dimen.dimen20) * 2 - resources.getDimensionPixelSize(R.dimen.dimen260)
        val dom = transformer.parser.parseXmlOrThrow("""
            <set xmlns:android="http://schemas.android.com/apk/res/android"
                android:ordering="together">
                <objectAnimator
                    android:duration="@integer/int3000"
                    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                    android:propertyName="alpha"
                    android:startOffset="@integer/int300"
                    android:valueFrom="1"
                    android:valueTo="0"
                    android:valueType="floatType" />
                <objectAnimator
                    android:duration="@integer/int3000"
                    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                    android:propertyName="scaleX"
                    android:startOffset="@integer/int300"
                    android:valueFrom="1"
                    android:valueTo="0"
                    android:valueType="floatType" />
                <objectAnimator
                    android:duration="@integer/int3000"
                    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                    android:propertyName="scaleY"
                    android:startOffset="@integer/int300"
                    android:valueFrom="1"
                    android:valueTo="0"
                    android:valueType="floatType" />
                <objectAnimator
                    android:duration="@integer/int3000"
                    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                    android:propertyName="rotation"
                    android:startOffset="@integer/int300"
                    android:valueFrom="0"
                    android:valueTo="180"
                    android:valueType="floatType" />
                <objectAnimator
                    android:duration="@integer/int3000"
                    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                    android:propertyName="translateX"
                    android:startOffset="@integer/int300"
                    android:valueFrom="0"
                    android:valueTo="$translateGap"
                    android:valueType="floatType" />
                <objectAnimator
                    android:duration="@integer/int3000"
                    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                    android:propertyName="backgroundColor"
                    android:startOffset="@integer/int300"
                    android:valueFrom="@android:color/black"
                    android:valueTo="@android:color/white"
                    android:valueType="colorType" />
                <objectAnimator
                    android:duration="@integer/int3000"
                    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
                    android:propertyName="textColor"
                    android:startOffset="@integer/int300"
                    android:valueFrom="@android:color/white"
                    android:valueTo="@android:color/black"
                    android:valueType="colorType" />
            </set>
        """.trimIndent())
        Log.d(TAG, dom.string())

        val node = transformer.transform(dom)
        node["name"] = "animatorSet_test"
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        val animator: AnimatorSet = resProcessor.process(node)?.value() ?: return
        animator.childAnimations.forEach {
            if (it is ObjectAnimator) {
                it.target = animObj
            }
        }

        findViewById<Button>("test_view_anim_set").setOnClickListener {
            resetAnimObjAndAnimator(animator)
            animator.start()
        }
    }

    private fun testStateListAnimator(transformer: XmlTransformer, resProcessor: StateListAnimatorResProcessor) {
        val prepareDom = transformer.parser.parseXmlOrThrow("""
            <set android:name="prepare_anim_test">
                <objectAnimator
                    android:duration="@android:integer/config_shortAnimTime"
                    android:propertyName="scaleX"
                    android:valueTo="1.0"
                    android:valueType="floatType" />
                <objectAnimator
                    android:duration="@android:integer/config_shortAnimTime"
                    android:propertyName="scaleY"
                    android:valueTo="1.0"
                    android:valueType="floatType" />
                <objectAnimator
                    android:duration="@android:integer/config_shortAnimTime"
                    android:propertyName="translationZ"
                    android:valueTo="0dp"
                    android:valueType="floatType" />
            </set>
        """.trimIndent())
        val node2 = transformer.transform(prepareDom)
        Log.d(TAG, "name: ${node2.name}, attributes-size: ${node2.attributes.size}, children-size: ${node2.children.size}")
        resProcessor.rpm.get<AnimatorSetResProcessor>(AnimatorSetResProcessor::class.java)?.process(node2)
                ?: return makeFail("prepareDom parse error!", Unit)!!

        val dom = transformer.parser.parseXmlOrThrow("""
            <selector xmlns:android="http://schemas.android.com/apk/res/android">
                <item android:state_pressed="true">
                    <set>
                        <objectAnimator
                            android:duration="@android:integer/config_shortAnimTime"
                            android:propertyName="scaleX"
                            android:valueTo="1.025"
                            android:valueType="floatType" />
                        <objectAnimator
                            android:duration="@android:integer/config_shortAnimTime"
                            android:propertyName="scaleY"
                            android:valueTo="1.025"
                            android:valueType="floatType" />
                        <objectAnimator
                            android:duration="@android:integer/config_shortAnimTime"
                            android:propertyName="translationZ"
                            android:valueTo="4dp"
                            android:valueType="floatType" />
                    </set>
                </item>
                <item android:animation="@animator/prepare_anim_test"/>
             </selector>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        node["name"] = "stateListAnimator_test"
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        val animator: StateListAnimator = resProcessor.process(node)?.value()
                ?: return makeFail("dom parse error!", Unit)!!
        findViewById<Button>("test_view_anim_set").stateListAnimator = animator
    }

    private fun testAlphaAnimation(transformer: XmlTransformer, resProcessor: AlphaResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <alpha xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="alpha_test"
                android:duration="3000"
                android:fillAfter="true"
                android:fromAlpha="1"
                android:toAlpha="0" />
        """.trimIndent())
        val dom2 = transformer.parser.parseXmlOrThrow("""
            <alpha xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="alpha_test_reverse"
                android:duration="3000"
                android:fillAfter="true"
                android:fromAlpha="0"
                android:toAlpha="1" />
        """.trimIndent())
        Log.d(TAG, dom.string())
        Log.d(TAG, dom2.string())
        val node = transformer.transform(dom)
        val node2 = transformer.transform(dom2)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        Log.d(TAG, "name: ${node2.name}, attributes-size: ${node2.attributes.size}, children-size: ${node2.children.size}")

        val animation = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
        val animation2 = resProcessor.process(node2)?.value() ?: return makeFail("${node2["name"]} create failed", Unit)!!
        var flag = false
        findViewById<Button>("test_view_anim_alpha").setOnClickListener {
            if (flag) {
                animObj.startAnimation(animation2)
            } else {
                animObj.startAnimation(animation)
            }
            flag = !flag
        }
    }

    private fun testScaleAnimation(transformer: XmlTransformer, resProcessor: ScaleResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <scale xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="scale_test"
                android:duration="3000"
                android:fillAfter="true"
                android:pivotX="50%"
                android:pivotY="50%"
                android:fromXScale="1"
                android:fromYScale="1"
                android:toXScale="0"
                android:toYScale="0" />
        """.trimIndent())
        val dom2 = transformer.parser.parseXmlOrThrow("""
            <scale xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="scale_test_reverse"
                android:duration="3000"
                android:fillAfter="true"
                android:pivotX="50%"
                android:pivotY="50%"
                android:fromXScale="0"
                android:fromYScale="0"
                android:toXScale="1"
                android:toYScale="1" />
        """.trimIndent())
        Log.d(TAG, dom.string())
        Log.d(TAG, dom2.string())
        val node = transformer.transform(dom)
        val node2 = transformer.transform(dom2)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        Log.d(TAG, "name: ${node2.name}, attributes-size: ${node2.attributes.size}, children-size: ${node2.children.size}")

        val animation = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
        val animation2 = resProcessor.process(node2)?.value() ?: return makeFail("${node2["name"]} create failed", Unit)!!
        var flag = false
        findViewById<Button>("test_view_anim_scale").setOnClickListener {
            if (flag) {
                animObj.startAnimation(animation2)
            } else {
                animObj.startAnimation(animation)
            }
            flag = !flag
        }
    }

    private fun testRotateAnimation(transformer: XmlTransformer, resProcessor: RotateResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <rotate xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="rotate_test"
                android:duration="3000"
                android:fillAfter="true"
                android:pivotX="50%"
                android:pivotY="50%"
                android:fromDegrees="0"
                android:toDegrees="180" />
        """.trimIndent())
        val dom2 = transformer.parser.parseXmlOrThrow("""
            <rotate xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="rotate_test_reverse"
                android:duration="3000"
                android:fillAfter="true"
                android:pivotX="50%"
                android:pivotY="50%"
                android:fromDegrees="180"
                android:toDegrees="360" />
        """.trimIndent())
        Log.d(TAG, dom.string())
        Log.d(TAG, dom2.string())
        val node = transformer.transform(dom)
        val node2 = transformer.transform(dom2)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        Log.d(TAG, "name: ${node2.name}, attributes-size: ${node2.attributes.size}, children-size: ${node2.children.size}")

        val animation = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
        val animation2 = resProcessor.process(node2)?.value() ?: return makeFail("${node2["name"]} create failed", Unit)!!
        var flag = false
        findViewById<Button>("test_view_anim_rotate").setOnClickListener {
            if (flag) {
                animObj.startAnimation(animation2)
            } else {
                animObj.startAnimation(animation)
            }
            flag = !flag
        }
    }

    private fun testTranslateAnimation(transformer: XmlTransformer, resProcessor: TranslateResProcessor) {
        val translateGap = getScreenWidthPixels(this) - resources.getDimensionPixelSize(
                R.dimen.dimen20) * 2 - resources.getDimensionPixelSize(R.dimen.dimen260)
        val dom = transformer.parser.parseXmlOrThrow("""
            <translate xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="translate_test"
                android:duration="3000"
                android:fillAfter="true"
                android:fromXDelta="0px"
                android:toXDelta="${translateGap}px" />
        """.trimIndent())
        val dom2 = transformer.parser.parseXmlOrThrow("""
            <translate xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="translate_test_reverse"
                android:duration="3000"
                android:fillAfter="true"
                android:fromXDelta="${translateGap}px"
                android:toXDelta="0px" />
        """.trimIndent())
        Log.d(TAG, dom.string())
        Log.d(TAG, dom2.string())
        val node = transformer.transform(dom)
        val node2 = transformer.transform(dom2)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        Log.d(TAG, "name: ${node2.name}, attributes-size: ${node2.attributes.size}, children-size: ${node2.children.size}")

        val animation = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
        val animation2 = resProcessor.process(node2)?.value() ?: return makeFail("${node2["name"]} create failed", Unit)!!
        var flag = false
        findViewById<Button>("test_view_anim_translate").setOnClickListener {
            if (flag) {
                animObj.startAnimation(animation2)
            } else {
                animObj.startAnimation(animation)
            }
            flag = !flag
        }
    }

    private fun testAnimationSet(transformer: XmlTransformer, resProcessor: AnimationSetResProcessor) {
        val translateGap = getScreenWidthPixels(this) - resources.getDimensionPixelSize(
                R.dimen.dimen20) * 2 - resources.getDimensionPixelSize(R.dimen.dimen260)
        val dom = transformer.parser.parseXmlOrThrow("""
            <set xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="set_test"
                android:duration="3000"
                android:fillAfter="true" >
                <alpha
                    android:fromAlpha="1"
                    android:toAlpha="0" />
                <scale
                    android:pivotX="50%"
                    android:pivotY="50%"
                    android:fromXScale="1"
                    android:fromYScale="1"
                    android:toXScale="0"
                    android:toYScale="0" />
                <rotate
                    android:pivotX="50%"
                    android:pivotY="50%"
                    android:fromDegrees="0"
                    android:toDegrees="180" />
                <translate
                    android:fromXDelta="0px"
                    android:toXDelta="${translateGap}px" />
            </set>
        """.trimIndent())
        val dom2 = transformer.parser.parseXmlOrThrow("""
            <set xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="set_test_reverse"
                android:duration="3000"
                android:fillAfter="true" >
                <alpha
                    android:fromAlpha="0"
                    android:toAlpha="1" />
                <scale
                    android:pivotX="50%"
                    android:pivotY="50%"
                    android:fromXScale="0"
                    android:fromYScale="0"
                    android:toXScale="1"
                    android:toYScale="1" />
                <rotate
                    android:pivotX="50%"
                    android:pivotY="50%"
                    android:fromDegrees="180"
                    android:toDegrees="360" />
                <translate
                    android:fromXDelta="${translateGap}px"
                    android:toXDelta="0px" />
            </set>
        """.trimIndent())
        Log.d(TAG, dom.string())
        Log.d(TAG, dom2.string())
        val node = transformer.transform(dom)
        val node2 = transformer.transform(dom2)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        Log.d(TAG, "name: ${node2.name}, attributes-size: ${node2.attributes.size}, children-size: ${node2.children.size}")

        val animation = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
        val animation2 = resProcessor.process(node2)?.value() ?: return makeFail("${node2["name"]} create failed", Unit)!!
        var flag = false
        findViewById<Button>("test_view_anim_set").setOnClickListener {
            if (flag) {
                animObj.startAnimation(animation2)
            } else {
                animObj.startAnimation(animation)
            }
            flag = !flag
        }
    }

    private fun testLayoutAnimation(transformer: XmlTransformer, resProcessor: LayoutAnimationResProcessor) {
        TODO("今晚")
    }

    private fun testGridLayoutAnimation(transformer: XmlTransformer, resProcessor: GridLayoutAnimationResProcessor) {
        TODO("今晚")
    }

    private fun testAnimationList(transformer: XmlTransformer, resProcessor: AnimationListResProcessor) {
        TODO("今晚")
    }

    private fun testColorDrawable(transformer: XmlTransformer, resProcessor: ColorDrawableResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <color xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="color_drawable_test"
                android:color="#ff0000" />
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        animObj.background = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
    }

    private fun testShapeDrawable(transformer: XmlTransformer, resProcessor: ShapeResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <shape name="shape_drawable_test"
                shape="rectangle">
                <solid color="@color/colorPrimary" />
                <corners radius="@dimen/dimen10" />
                <stroke
                    width="@dimen/dimen20"
                    color="@android:color/white" />
            </shape>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        animObj.background = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
    }

    private fun testRippleDrawable(transformer: XmlTransformer, resProcessor: RippleResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <ripple
                color="#ffff0000"
                name="ripple_drawable_test" >
                <item>
                    <shape
                        shape="rectangle">
                        <solid color="@color/colorPrimary" />
                        <corners radius="@dimen/dimen3" />
                        <stroke
                            width="@dimen/dimen3"
                            color="@android:color/white" />
                    </shape>
                </item>
            </ripple>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        findViewById<Button>("test_view_anim_alpha").background = resProcessor.process(node)?.value()
                ?: return makeFail("${node["name"]} create failed", Unit)!!
    }

    private fun testStateListDrawable(transformer: XmlTransformer, resProcessor: StateListDrawableResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <selector xmlns:android="http://schemas.android.com/apk/res/android"
                android:name="state_list_drawable_test">
                <item android:state_selected="false">
                    <layer-list>
                        <item>
                            <shape android:shape="oval">
                                <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                                <solid android:color="@color/drawable_select_selected_false" />
                                <stroke android:width="@dimen/drawable_select_inner_stroke" android:color="@android:color/transparent" />
                            </shape>
                        </item>
                        <item>
                            <shape android:shape="oval">
                                <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                                <solid android:color="@android:color/transparent" />
                                <stroke android:width="@dimen/drawable_select_outer_stroke" android:color="@color/drawable_select_selected_false" />
                            </shape>
                        </item>
                    </layer-list>
                </item>
                <item android:state_selected="true">
                    <layer-list>
                        <item>
                            <shape android:shape="oval">
                                <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                                <solid android:color="@color/drawable_select_selected_true" />
                                <stroke android:width="@dimen/drawable_select_inner_stroke" android:color="@android:color/transparent" />
                            </shape>
                        </item>
                        <item>
                            <shape android:shape="oval">
                                <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                                <solid android:color="@android:color/transparent" />
                                <stroke android:width="@dimen/drawable_select_outer_stroke" android:color="@color/drawable_select_selected_true" />
                            </shape>
                        </item>
                    </layer-list>
                </item>
            </selector>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        findViewById<TextView>("item_select").run {
            setDrawableRight(resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!)
            setOnClickListener { this.isSelected = !this.isSelected }
        }
    }

    private fun testInsetDrawable(transformer: XmlTransformer, resProcessor: InsetResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <inset  name="inset_drawable_test"
                inset="20dp" >
                <shape
                    shape="rectangle">
                    <solid color="@color/colorPrimary" />
                    <corners radius="@dimen/dimen10" />
                    <stroke
                        width="@dimen/dimen20"
                        color="@android:color/darker_gray" />
                </shape>
            </inset>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        animObj.background = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
    }

    private fun testClipDrawable(transformer: XmlTransformer, resProcessor: ClipResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <clip name="clip_drawable_test"
                clipOrientation="vertical"
                gravity="left|top"
                level="60%" >
                <shape
                    shape="rectangle">
                    <solid color="@color/colorPrimary" />
                    <corners radius="@dimen/dimen10" />
                    <stroke
                        width="@dimen/dimen20"
                        color="@android:color/darker_gray" />
                </shape>
            </clip>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        animObj.background = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
    }

    private fun testRotateDrawable(transformer: XmlTransformer, resProcessor: RotateDrawResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <rotate name="rotate_drawable_test"
                pivotX="50%"
                pivotY="50%"
                fromDegrees="0"
                toDegrees="360"
                level="25%" >
                <clip clipOrientation="vertical"
                    gravity="left|top"
                    level="60%" >
                    <shape shape="rectangle">
                        <solid color="@color/colorPrimary" />
                        <corners radius="@dimen/dimen10" />
                        <stroke width="@dimen/dimen20"
                            color="@android:color/darker_gray" />
                    </shape>
                </clip>
            </rotate>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        animObj.background = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
        // rotateDrawable里面不应该包含有哪些可以设置level的drawable，因为会被覆盖
    }

    private fun testScaleDrawable(transformer: XmlTransformer, resProcessor: ScaleDrawResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <scale name="scale_drawable_test"
                scaleWidth="50%"
                scaleHeight="50%"
                level="50%" >
                <shape shape="rectangle">
                    <solid color="@color/colorPrimary" />
                    <corners radius="@dimen/dimen10" />
                    <stroke width="@dimen/dimen20"
                        color="@android:color/darker_gray" />
                </shape>
            </scale>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        animObj.background = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
    }

    private fun testLayerListDrawable(transformer: XmlTransformer, resProcessor: LayerListResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <layer-list android:name="layer_list_drawable_test">
                <item>
                    <shape android:shape="oval">
                        <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                        <solid android:color="@color/drawable_select_selected_false" />
                        <stroke android:width="@dimen/drawable_select_inner_stroke" android:color="@android:color/transparent" />
                    </shape>
                </item>
                <item>
                    <shape android:shape="oval">
                        <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                        <solid android:color="@android:color/transparent" />
                        <stroke android:width="@dimen/drawable_select_outer_stroke" android:color="@color/drawable_select_selected_false" />
                    </shape>
                </item>
            </layer-list>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        findViewById<TextView>("item_select").apply {
            setDrawableRight(resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!)

            val name2 = "layer_list_drawable_test2"
            val drawableLeft = resProcessor.process(transformer.run("""
                <layer-list
                    xmlns:android="http://schemas.android.com/apk/res/android" >
                    <item>
                        <shape android:shape="rectangle">
                            <size
                                android:width="16dp"
                                android:height="16dp" />
                            <stroke
                                android:width="3dp"
                                android:color="#ffffbb33" />
                        </shape>
                    </item>
                    <item android:bottom="4dp">
                        <shape android:shape="line">
                            <stroke
                                android:width="2dp"
                                android:color="#ffffbb33" />
                        </shape>
                    </item>
                    <item android:top="4dp">
                        <shape android:shape="line">
                            <stroke
                                android:width="2dp"
                                android:color="#ffffbb33" />
                        </shape>
                    </item>
                    <item
                        android:left="4dp"
                        android:top="0.25dp">
                        <rotate
                            android:fromDegrees="90"
                            android:pivotX="50%"
                            android:pivotY="50%"
                            android:toDegrees="90">
                            <shape android:shape="line">
                                <stroke
                                    android:width="2dp"
                                    android:color="#ffffbb33" />
                            </shape>
                        </rotate>
                    </item>
                    <item
                        android:right="4dp"
                        android:top="0.25dp">
                        <rotate
                            android:fromDegrees="90"
                            android:pivotX="50%"
                            android:pivotY="50%"
                            android:toDegrees="90">
                            <shape android:shape="line">
                                <stroke
                                    android:width="2dp"
                                    android:color="#ffffbb33" />
                            </shape>
                        </rotate>
                    </item>
                </layer-list>
            """.trimIndent(), name = name2))?.value() ?: return makeFail("$name2 create failed", Unit)!!
            setDrawableLeft(drawableLeft)
        }
    }

    private fun testLevelListDrawable(transformer: XmlTransformer, resProcessor: LevelListResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <level-list name="level_list_drawable_test" android:level="0" >
                <item android:minLevel="0" android:maxLevel="0">
                    <layer-list>
                        <item>
                            <shape android:shape="oval">
                                <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                                <solid android:color="@color/drawable_select_selected_false" />
                                <stroke android:width="@dimen/drawable_select_inner_stroke" android:color="@android:color/transparent" />
                            </shape>
                        </item>
                        <item>
                            <shape android:shape="oval">
                                <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                                <solid android:color="@android:color/transparent" />
                                <stroke android:width="@dimen/drawable_select_outer_stroke" android:color="@color/drawable_select_selected_false" />
                            </shape>
                        </item>
                    </layer-list>
                </item>
                <item android:minLevel="1" android:maxLevel="1">
                    <layer-list>
                        <item>
                            <shape android:shape="oval">
                                <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                                <solid android:color="@color/drawable_select_selected_true" />
                                <stroke android:width="@dimen/drawable_select_inner_stroke" android:color="@android:color/transparent" />
                            </shape>
                        </item>
                        <item>
                            <shape android:shape="oval">
                                <size android:width="@dimen/drawable_select_size" android:height="@dimen/drawable_select_size" />
                                <solid android:color="@android:color/transparent" />
                                <stroke android:width="@dimen/drawable_select_outer_stroke" android:color="@color/drawable_select_selected_true" />
                            </shape>
                        </item>
                    </layer-list>
                </item>
            </level-list>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        findViewById<TextView>("item_select").apply {
            val drawable = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
            setDrawableRight(drawable)
            setOnClickListener { drawable.level = 1 - drawable.level }
        }
    }

    private fun testTransitionDrawable(transformer: XmlTransformer, resProcessor: TransitionResProcessor) {
        resProcessor.rpm.get<ColorDrawableResProcessor>(ColorDrawableResProcessor::class.java)?.process(transformer.run(
                """<color android:name="holo_blue_light" android:color="@android:color/holo_blue_light" />"""))
        resProcessor.rpm.get<ColorDrawableResProcessor>(ColorDrawableResProcessor::class.java)?.process(transformer.run(
                """<color android:name="holo_green_light" android:color="@android:color/holo_green_light" />"""))
        val dom = transformer.parser.parseXmlOrThrow("""
            <transition name="transition_drawable_test">
                <item android:drawable="@drawable/holo_blue_light"/>
                <item android:drawable="@android:color/holo_green_light"/>
            </transition>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        val transitionDrawable = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
        animObj.background = transitionDrawable
        var flag = false
        animObj.setOnClickListener {
            if (flag) {
                transitionDrawable.startTransition(3000)
            } else {
                transitionDrawable.reverseTransition(3000)
            }
            flag = !flag
        }
    }

    private fun testAnimatedStateList(transformer: XmlTransformer, resProcessor: AnimatedStateListResProcessor) {
        TODO("not implemented")
    }

    private fun testVectorDrawable(transformer: XmlTransformer, resProcessor: VectorResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <vector
                name="vector_drawable_test_1"
                width="108dp"
                height="108dp"
                viewportWidth="100"
                viewportHeight="100">
                <path
                    fillColor="@color/colorTheme"
                    pathData="M0, 50c0, -67, 100, -67, 100, 0c0, 67, -100, 67, -100, 0" />
                <path
                    pathData="M30, 66.6c-21.44, 0, -21.44, -32, 0, -32c0, -26.8, 40, -26.8, 40, 0c21.44, 0, 21.44, 32, 0, 32v-15l-20, 3l-20, -3v30l20, 3l20, -3v-15m-20,-12v30"
                    strokeWidth="6"
                    strokeColor="@android:color/white"
                    strokeLineCap="round"
                    strokeLineJoin="round" />
            </vector>
        """.trimIndent())
        Log.d(TAG, dom.string())
        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")
        animObj.background = resProcessor.process(node)?.value() ?: return makeFail("${node["name"]} create failed", Unit)!!
    }

    private fun testAnimatedVectorDrawable(transformer: XmlTransformer, resProcessor: AnimatedVectorResProcessor) {
        TODO("not implemented")
    }

    private fun testAnimatedImageDrawable(transformer: XmlTransformer, resProcessor: AnimatedImageResProcessor) {
        TODO("not implemented")
    }

    // private fun testBitmapDrawable() {}
    // private fun testDrawableDrawable() {}
    // private fun testNinePatchDrawable() {}
    // private fun testGradientDrawable() {}
    // private fun testMaskableIconDrawable() {}

    private fun testSelectorColor(transformer: XmlTransformer, resProcessor: ColorSelectorResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
            <selector xmlns:android="http://schemas.android.com/apk/res/android" android:name="selector_color1">
                 <item android:state_pressed="true" android:color="#000000" />
                 <item android:state_focused="true" android:color="#000000" />
                 <item android:color="#FFFFFF"/>
            </selector>
        """.trimIndent())
        Log.d(TAG, dom.string())

        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")

        val colorStateList = resProcessor.process(node)?.value()
        if (colorStateList == null) {
            Log.d(TAG, "get res -- null")
            return
        }
        findViewById<Button>("test_view_anim_set").setTextColor(colorStateList)
    }

    private fun testValues(transformer: XmlTransformer, resProcessor: ValuesResProcessor) {
        val dom = transformer.parser.parseXmlOrThrow("""
                <resources>
                    <string name="app_name">AndroidTest</string>
                    <string name="recyclerview_test">Test RecyclerView</string>
                    <string name="handler_test">Test Handler</string>
                    <string name="contentDescription">null</string>
                    <string name="fragment_test">Test Fragment</string>
                    <string name="fragment_test_bottombar">Test Bottom Bar</string>
                    <string name="fragment_test_bottombar2">Test Bottom Bar2</string>
                
                    <color name="colorPrimary">#008577</color>
                    <color name="colorPrimaryDark">#00574B</color>
                    <color name="colorAccent">#D81B60</color>
                
                    <dimen name="dimen16">16dp</dimen>
                    <dimen name="dimen17">17dp</dimen>
                    <dimen name="dimen18">18dp</dimen>
                    <dimen name="dimen19">19dp</dimen>
                    <dimen name="dimen20">20dp</dimen>
                
                    <integer name="int0">0</integer>
                    <integer name="int1">1</integer>
                    <integer name="int2">2</integer>
                    <integer name="int3">3</integer>
                    <integer name="int300">300</integer>
                    <integer name="int3000">3000</integer>
                    
                    <string-array name="str_arr1">
                        <item>text1</item>
                        <item>text2</item>
                        <item>text3</item>
                    </string-array>
                    
                    <integer-array name="int_arr1">
                        <item>100</item>
                        <item>112</item>
                        <item>134</item>
                        <item>156</item>
                        <item>178</item>
                        <item>290</item>
                    </integer-array>
                    
                    <bool name="bool1">false</bool>
                    <bool name="bool2">true</bool>
                    
                    <plurals name="plurals">
                        <item quantity="zero">0</item>
                        <item quantity="one">1</item>
                        <item quantity="two">2</item>
                        <item quantity="many">many</item>
                    </plurals>
                    
                    <dimen name="drawable_select_size">30dp</dimen>
                    <dimen name="drawable_select_inner_stroke">8dp</dimen>
                    <dimen name="drawable_select_outer_stroke">2dp</dimen>
                    <color name="drawable_select_selected_false">#dddddd</color>
                    <color name="drawable_select_selected_true">#ffff4444</color>
                </resources>
            """.trimIndent())
        Log.d(TAG, dom.string())

        val node = transformer.transform(dom)
        Log.d(TAG, "name: ${node.name}, attributes-size: ${node.attributes.size}, children-size: ${node.children.size}")

        resProcessor.process(node)?.value()?.forEach {
            when (val value = it.value()) {
                is LongArray -> Log.d(TAG, "get res -- ${it.name}, ${value.joinToString()}")
                is Array<*> -> Log.d(TAG, "get res -- ${it.name}, ${value.joinToString()}")
                is Map<*, *> -> Log.d(TAG, "get res -- ${it.name}, ${value.toList().joinToString { pair -> "(${pair.first}, ${pair.second})" }}")
                else -> Log.d(TAG, "get res -- ${it.name}, $value")
            }
        } ?: Log.d(TAG, "null res")

        Log.d(TAG, "cache: ${ResStore.CACHE.toList().joinToString(", ") { "(${it.first} -- ${it.second.value()})" }}")
    }
}
