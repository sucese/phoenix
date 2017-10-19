package com.guoxiaoxing.phoenix.picture.edit.operation

/**
 * Mode handler, like change selected bar
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
interface OperationProcessor {

    fun operatePaint(selected: Boolean)

    fun operateStick(selected: Boolean)

    fun operateText(selected: Boolean)

    fun operateBlur(selected: Boolean)

    fun operateCrop(selected: Boolean)
}