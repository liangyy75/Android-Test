package liang.example.apttest.bind;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@EventType(targetType = View.class, listenerSetterName = "setOnClickListener", listenerType = View.OnClickListener.class, listenerMethodName = "onClick")
public @interface OnClick {
    int[] value();
}
