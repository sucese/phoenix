package com.guoxiaoxing.phoenix.picker.rx.bus

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Documented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
annotation class Subscribe(val code: Int = -1, val threadMode: ThreadMode = ThreadMode.CURRENT_THREAD)
