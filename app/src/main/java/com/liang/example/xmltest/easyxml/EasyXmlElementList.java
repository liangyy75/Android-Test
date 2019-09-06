package com.liang.example.xmltest.easyxml;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EasyXmlElementList {
    String name() default "";

    boolean inline() default false;

    Class type() default void.class;
}
