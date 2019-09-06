package com.liang.example.apttest.bind;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(OnLongClicks.class)
@EventType(targetType = View.class, listenerSetterName = "setOnLongClickListener", listenerType = View.OnLongClickListener.class, listenerMethodName = "onLongClick")
public @interface OnLongClick {
    int[] value();
    // TODO:
}
