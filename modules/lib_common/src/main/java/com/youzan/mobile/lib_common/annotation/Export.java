package com.youzan.mobile.lib_common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * module api导出
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Export {
}
