package liang.example.apttest.bind;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@EventType(targetType = View.class, listenerSetterName = "setOnLongClickListener", listenerType = View.OnLongClickListener.class, listenerMethodName = "onLongClick")
public @interface OnLongClick {
    int[] value();
}
