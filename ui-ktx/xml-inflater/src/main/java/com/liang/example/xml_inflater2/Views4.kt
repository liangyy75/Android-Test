package com.liang.example.xml_inflater2

import android.view.ViewStub

open class TableLayoutParser

open class TableRowParser

open class SpinnerParser

open class CardViewParser

open class AppBarLayoutParser

open class BottomAppBarParser

open class NavigationViewParser

open class BottomNavigationViewParser

open class ToolbarParser

open class TabLayoutParser

open class TabItemParser

open class ViewStubParser(viewParser: ViewParser) : BaseViewParser<ViewStub>(
        "ViewStub", ResViewType.VIEW_STUB, mutableListOf(viewParser)) {
    override fun prepare() {
        Attrs.ViewStub.inflatedId
        Attrs.ViewStub.layout
        TODO("not implement")
    }

    override fun makeView(node: Node): ViewStub? = ViewStub(apm.context)
}
