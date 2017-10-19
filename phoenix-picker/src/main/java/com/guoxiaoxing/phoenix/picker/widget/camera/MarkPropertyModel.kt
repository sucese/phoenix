package com.guoxiaoxing.phoenix.picker.widget.camera

import java.io.Serializable

class MarkPropertyModel : Serializable {

    //贴纸id
    private var stickerId: Long = 0
    //x坐标
    private var xLocation: Float = 0.toFloat()
    //y坐标
    private var yLocation: Float = 0.toFloat()
    //角度
    var degree: Float = 0.toFloat()
    //缩放值
    var scaling: Float = 0.toFloat()

    //水平镜像 1镜像 2未镜像
    private var horizonMirror: Int = 0

    fun setHorizonMirror(horizonMirror: Int) {
        this.horizonMirror = horizonMirror
    }

    fun setStickerId(stickerId: Long) {
        this.stickerId = stickerId
    }

    fun getxLocation(): Float {
        return xLocation
    }

    fun setxLocation(xLocation: Float) {
        this.xLocation = xLocation
    }

    fun getyLocation(): Float {
        return yLocation
    }

    fun setyLocation(yLocation: Float) {
        this.yLocation = yLocation
    }

    companion object {

        private const val serialVersionUID = 3800737478616389410L
    }
}
