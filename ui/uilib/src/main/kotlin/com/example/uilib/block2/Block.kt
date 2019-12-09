package com.example.uilib.block2

import com.example.uilib.block.ActivityProxy

open class Block : ActivityProxy()

// 1. TODO: initInContext / initInBlock / initInGroup / initInManager
// 2. TODO: default constructor / constructor(layoutId) / constructor(view)
// 3. TODO: inflate / beforeInflateView / onInflateView / afterInflateView --> 因为有了各种init，所以不需要依靠inflate来设置字段了
// 4. TODO: recycle / load / unload
// 5. TODO: refresh / refreshGroup / refreshManager
// 6. TODO: observableData / liveData -- add / remove / get
// 7. TODO: consumer / message / runnable / disposable
// 8. 字段
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

// 1. TODO: view 的增、删、改、查
// 2. TODO: block 的增、删、改、查
// 3. TODO: afterInflateView
// 4. TODO: refresh / refreshGroup / refreshManager
// 5. TODO: recycle / load / unload
// 6. TODO: 

open class BlockManager : BlockGroup()

// 1. TODO:
