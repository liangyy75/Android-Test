package com.example.uilib.block2

import android.view.View
import android.view.ViewGroup
import com.example.uilib.block.ActivityProxy
import java.lang.Exception
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

open class Block : ActivityProxy()

// 1. TODO: initInContext / initInBlock / initInGroup / initInManager
// 2. TODO: default constructor / constructor(layoutId) / constructor(view)
// 3. TODO: recycle / load / unload
// 4. TODO: refresh / refreshGroup / refreshManager
//
// 5. TODO: inflate / beforeInflateView / onInflateView / afterInflateView --> 因为有了各种init，所以不需要依靠inflate来设置字段了
// 6. TODO: observableData / liveData -- add / remove / get
// 7. TODO: consumer / message / runnable / disposable
// 8. TODO: holderByApp
//
// 9. 字段
//    1. provider: StrongViewModelProvider?
//    2. swb: WhiteBoard<String>
//    3. cwb: WhiteBoard<Class<*>>
//    4. rxHandler: RxHandler
//    5. blockGroup: BlockGroup?
//    6. blockManager: BlockManager?
//
//    7. context: Context?
//    8. inflater: LayoutInflater?
//    9. parent: ViewGroup?
//    10. view: View?
//    11. viewId: Int
//    12. inflated: AtomicBoolean(false)
//    13. layoutId: Int
//    14. inflateViewAsync: Boolean
//    15. inflateViewDelay: Long
//    16. afterInflateListener: Runnable?

open class BlockGroup : Block()

// 1. TODO: initInContext / initInBlock / initInGroup / initInManager
// 2. TODO: default constructor / constructor(layoutId) / constructor(view)
// 3. TODO: recycle / load / unload
// 4. TODO: refresh / refreshGroup / refreshManager
//
// 5. TODO: view 的增、删、改、查
// 6. TODO: block 的增、删、改、查
// 7. TODO: afterInflateView
// 8. TODO: viewBuilder / blockBuilder

open class BlockManager : BlockGroup()
// 1. TODO: initInContext / initInBlock / initInGroup / initInManager
// 2. TODO: default constructor / constructor(layoutId) / constructor(view)
// 3. TODO: recycle / load / unload
// 4. TODO: refresh / refreshGroup / refreshManager
//
// 5. TODO: fragment / activity lifecycle
// 6. TODO: initInActivity / initInFragment
// 7. TODO: activity proxy
// 8. TODO:

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
