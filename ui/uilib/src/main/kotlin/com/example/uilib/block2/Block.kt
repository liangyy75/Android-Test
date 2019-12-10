package com.example.uilib.block2

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelStoreOwner
import java.util.concurrent.atomic.AtomicBoolean

open class Block() : ActivityProxy() {
    protected open var provider: StrongViewModelProvider? = null
    protected open var swb: WhiteBoard<String>? = null
    protected open var cwb: WhiteBoard<Class<*>>? = null
    protected open var rxHandler: RxHandler? = null

    protected open var blockGroup: BlockGroup? = null
    protected open var blockManager: BlockManager? = null

    protected open var context: Context? = null
    protected open var inflater: LayoutInflater? = null
    open var layoutId: Int = 0
    open var inflated = AtomicBoolean(false)
    open var inflateViewAsync: Boolean = false
    open var inflateViewDelay: Long = 0L
    open var afterInflateListener: Runnable? = null

    open var view: View? = null
    open var parent: ViewGroup? = null
    open var viewId: Int
        get() = view?.id ?: View.NO_ID
        set(value) {
            view?.id = value
        }

    // constructor

    constructor(layoutId: Int) : this() {
        this.layoutId = layoutId
    }

    constructor(view: View) : this() {
        this.view = view
    }

    // init

    open fun initInContext(context: Context): Block {
        if (context is ViewModelStoreOwner) {
            this.provider = StrongViewModelProvider(context)
            this.swb = WhiteBoard.of(this.provider!!)
            this.cwb = WhiteBoard.of(this.provider!!)
        } else {
            this.swb = WhiteBoard.create()
            this.cwb = WhiteBoard.create()
        }
        this.rxHandler = RxHandler()

        this.context = context
        this.inflater = LayoutInflater.from(context)
        return this
    }

    open fun initInBlock(block: Block, sameView: Boolean = false): Block {
        this.provider = block.provider
        this.swb = block.swb
        this.cwb = block.cwb
        this.rxHandler = block.rxHandler

        this.context = block.context
        this.inflater = block.inflater
        this.layoutId = block.layoutId
        this.inflateViewAsync = block.inflateViewAsync
        this.inflateViewDelay = block.inflateViewDelay
        this.afterInflateListener = block.afterInflateListener

        if (sameView) {
            this.parent = block.parent
            this.view = block.view
            this.inflated.set(block.inflated.get())
        }
        return this
    }

    open fun initInGroup(blockGroup: BlockGroup): Block {
        return this
    }

    open fun initInManager(blockManager: BlockManager): Block {
        return this
    }
}

// 1. TODO: initInContext / initInBlock / initInGroup / initInManager
// 2. TODO: default constructor / constructor(layoutId) / constructor(view)
// 3. TODO: recycle / load / unload / copy
// 4. TODO: refresh / refreshGroup / refreshManager
// 5. TODO: parseFromXml(inflate) / parseToXml / parseFromJson / parseToJson
//
// 6. TODO: inflate / beforeInflateView / onInflateView / afterInflateView --> 因为有了各种init，所以不需要依靠inflate来设置字段了
// 7. TODO: observableData / liveData -- add / remove / get
// 8. TODO: consumer / message / runnable / disposable
// 9. TODO: holderByApp
//
// 10. 字段
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
//
//    17.