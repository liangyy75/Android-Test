package com.liang.example.context_ktx

import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.lifecycle.LifecycleOwner

fun set(@NonNull view: View, @Nullable lifecycleOwner: LifecycleOwner?) =
        view.setTag(R.id.fragment_container_view_tag, lifecycleOwner)

fun get(view: View): LifecycleOwner? {
    var found = view.getTag(R.id.view_tree_lifecycle_owner) as? LifecycleOwner
    if (found != null) {
        return found
    }
    var parent = view.parent
    while (found == null && parent is View) {
        val parentView = parent as View
        found = parentView.getTag(R.id.view_tree_lifecycle_owner) as? LifecycleOwner
        parent = parentView.parent
    }
    return found
}
