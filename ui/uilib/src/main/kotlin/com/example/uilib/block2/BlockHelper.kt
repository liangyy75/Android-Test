package com.example.uilib.block2

import android.view.View

var View.padding: Int
    set(value) = setPadding(value, value, value, value)
    get() = throw RuntimeException("can't get padding of view, because it's only an extension by kotlin")

// 注意，反射起码要慢四倍，慎用
// val vgLPMap: MutableMap<String, Method> = ConcurrentHashMap()
//
// inline fun <reified T : ViewGroup> T.lpPrepare() =
//         vgLPMap.put(T::class.java.name, T::class.java.getDeclaredMethod("generateDefaultLayoutParams").apply { isAccessible = true })
//
// inline fun <reified T : ViewGroup> T.lp(w: Int, h: Int) =
//         (vgLPMap[T::class.java.name]!!.invoke(this) as ViewGroup.LayoutParams).apply {
//             width = w
//             height = h
//         }
//
// open class Holder<T : ViewGroup> {
//     open lateinit var vg: T
//     open fun build(view: View, w: Int, h: Int) {
//         view.layoutParams = vg.lp(w, h)
//     }
// }

// TODO:
//  BG<LinearLayout>({
//      param = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
//      gravity = Gravity.CENTER_HORIZONTAL
//      padding = dp20
//  }) {
//      add(B<TextView> {
//          ...
//      })
//      add(BG<LinearLayout> {
//          add(B<Button>{
//              ...
//          })
//          add(B<Button>{
//              ...
//          })
//      })
//  }