package com.liang.example.basic_block

import android.view.ViewGroup
import androidx.annotation.LayoutRes

open class BlockManager : BlockGroup {
    constructor(@LayoutRes layoutId: Int, strId: String?) : super(layoutId, strId)
    constructor(viewGroup: ViewGroup, strId: String?) : super(viewGroup, strId)
}

// 1. TODO: initInContext / initInBlock / initInGroup / initInManager
// 2. TODO: default constructor / constructor(layoutId) / constructor(view)
// 3. TODO: recycle / load / unload / copy
// 4. TODO: refresh / refreshGroup / refreshManager
// 5. TODO: parseFromXml / parseToXml / parseFromJson / parseToJson
//
// 6. TODO: fragment / activity lifecycle
// 7. TODO: initInActivity / initInFragment
// 8. TODO: activity proxy
// 9. TODO:
