package com.guoxiaoxing.phoenix.picker.rx.bus

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class SubscriberMethod(var subscriber: Any, var method: Method, var eventType: Class<*>, var code: Int, var threadMode: ThreadMode) {

    /**
     * 调用方法

     * @param o 参数
     */
    operator fun invoke(o: Any) {
        try {
            val parameterType = method.parameterTypes
            if (parameterType != null && parameterType.size == 1) {
                method.invoke(subscriber, o)
            } else if (parameterType == null || parameterType.size == 0) {
                method.invoke(subscriber)
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}
