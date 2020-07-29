package com.liang.example.context_ktx

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment

open class SimpleFragment : Fragment() {
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = set(view, this)
}
