package com.youzan.mobile.lib_common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动注册
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MediatorRegister {
    String pluginName();
}
