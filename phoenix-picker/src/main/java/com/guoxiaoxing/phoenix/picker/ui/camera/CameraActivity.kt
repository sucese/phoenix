package com.guoxiaoxing.phoenix.picker.ui.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picker.rx.permission.RxPermissions
import com.guoxiaoxing.phoenix.picker.ui.BaseActivity

import java.util.ArrayList

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class CameraActivity : BaseActivity() {

    private var maxCanTakePhotoNum = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rxPermissions = RxPermissions(this)
        rxPermissions.request(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(aBoolean: Boolean?) {
                        if (aBoolean!!) {
                            maxCanTakePhotoNum = option.maxSelectNum
                            //full screen
                            setTheme(R.style.style_full_screen)
                            setContentView(R.layout.activity_camera_library)
                            location()
                            setupFragment()
                        } else {
                            showToast(getString(R.string.picture_camera))
                            closeActivity()
                        }
                    }

                    override fun onError(e: Throwable) {
                        showToast(getString(R.string.picture_camera))
                        closeActivity()
                    }

                    override fun onComplete() {

                    }
                })
    }

    override fun onRestart() {
        super.onRestart()
    }

    fun setupFragment() {
        val fragment = CameraFragment.newInstance()
        val bundle = Bundle()
        bundle.putInt(PhoenixConstant.KEY_TAKE_PICTURE_MAX_NUM, maxCanTakePhotoNum)
        fragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_root, fragment)
                .commitAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraList.clear()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (onPickerListener != null) {
            onPickerListener.onPickSuccess(cameraList)
        }
    }

    override fun showToast(msg: String) {
        Toast.makeText(this@CameraActivity, msg, Toast.LENGTH_LONG).show()
    }

    override fun closeActivity() {
        finish()
        overridePendingTransition(0, R.anim.phoenix_activity_out)
    }

    private fun location() {
        val aMapLocationClient = AMapLocationClient(this@CameraActivity)
        val aMapLocationClientOption = AMapLocationClientOption()
        aMapLocationClientOption.locationMode = AMapLocationClientOption.AMapLocationMode.Battery_Saving
        aMapLocationClientOption.isOnceLocation = true
        aMapLocationClient.setLocationOption(aMapLocationClientOption)
        aMapLocationClient.setLocationListener { aMapLocation ->
            if (aMapLocation != null) {
                if (aMapLocation.errorCode == 0) {
                    latitude = aMapLocation.latitude.toString()
                    longitude = aMapLocation.longitude.toString()
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.errorCode + ", errInfo:"
                            + aMapLocation.errorInfo)
                }

            }
            aMapLocationClient.stopLocation()
            aMapLocationClient.onDestroy()
        }
        aMapLocationClient.startLocation()
    }

    companion object {
        var cameraList: MutableList<MediaEntity> = ArrayList()
        lateinit var latitude: String
        lateinit var longitude: String
    }
}
