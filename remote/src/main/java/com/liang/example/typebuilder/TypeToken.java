package com.liang.example.typebuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author liangyuyin
 * @since 2019/7/16
 */
public abstract class TypeToken<T> {
    private final Type type;

    public TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new TypeException("No generics found!");
        }
        ParameterizedType type = (ParameterizedType) superclass;
        this.type = type.getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
