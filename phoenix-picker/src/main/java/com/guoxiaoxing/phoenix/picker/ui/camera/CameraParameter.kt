package com.guoxiaoxing.phoenix.picker.ui.camera

import android.os.Parcel
import android.os.Parcelable

class CameraParameter : Parcelable {
    private var mIsPortrait: Boolean = false
    private var mDisplayOrientation: Int = 0
    private var mLayoutOrientation: Int = 0
    var mPreviewHeight: Int = 0
    var mPreviewWidth: Int = 0

    constructor() {}

    protected constructor(`in`: Parcel) {
        mIsPortrait = `in`.readByte().toInt() == 1
        mDisplayOrientation = `in`.readInt()
        mLayoutOrientation = `in`.readInt()
        mPreviewHeight = `in`.readInt()
        mPreviewWidth = `in`.readInt()
    }

    fun crateCopy(): CameraParameter {
        val cameraParameter = CameraParameter()
        cameraParameter.mIsPortrait = mIsPortrait
        cameraParameter.mDisplayOrientation = mDisplayOrientation
        cameraParameter.mLayoutOrientation = mLayoutOrientation
        cameraParameter.mPreviewHeight = mPreviewHeight
        cameraParameter.mPreviewWidth = mPreviewWidth
        return cameraParameter
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (mIsPortrait) 1 else 0).toByte())
        dest.writeInt(mDisplayOrientation)
        dest.writeInt(mLayoutOrientation)
        dest.writeInt(mPreviewHeight)
        dest.writeInt(mPreviewWidth)
    }

    fun getmDisplayOrientation(): Int {
        return mDisplayOrientation
    }

    fun setmDisplayOrientation(mDisplayOrientation: Int) {
        this.mDisplayOrientation = mDisplayOrientation
    }

    fun ismIsPortrait(): Boolean {
        return mIsPortrait
    }

    fun setmIsPortrait(mIsPortrait: Boolean) {
        this.mIsPortrait = mIsPortrait
    }

    fun getmLayoutOrientation(): Int {
        return mLayoutOrientation
    }

    fun setmLayoutOrientation(mLayoutOrientation: Int) {
        this.mLayoutOrientation = mLayoutOrientation
    }

    fun getmPreviewHeight(): Int {
        return mPreviewHeight
    }

    fun setmPreviewHeight(mPreviewHeight: Int) {
        this.mPreviewHeight = mPreviewHeight
    }

    fun getmPreviewWidth(): Int {
        return mPreviewWidth
    }

    fun setmPreviewWidth(mPreviewWidth: Int) {
        this.mPreviewWidth = mPreviewWidth
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        val creator: Parcelable.Creator<CameraParameter> = object : Parcelable.Creator<CameraParameter> {
            override fun createFromParcel(`in`: Parcel): CameraParameter {
                return CameraParameter(`in`)
            }

            override fun newArray(size: Int): Array<CameraParameter?> {
                return arrayOfNulls(size)
            }
        }
    }
}
