package com.liang.example.xmltest.easyxml;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EasyXmlElement {
    String name() default "";
}
