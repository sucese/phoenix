package com.guoxiaoxing.phoenix.camera.controller.view;

import android.support.annotation.Nullable;
import android.view.View;

import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.listener.OnCameraResultListener;
import com.guoxiaoxing.phoenix.camera.util.Size;

/**
 * The camera manager for camera1
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
public interface CameraView {

    void updateCameraPreview(Size size, View cameraPreview);

    void updateUiForMediaAction(@CameraConfig.MediaAction int mediaAction);

    void updateCameraSwitcher(int numberOfCameras);

    void onPictureTaken(byte[] bytes, @Nullable OnCameraResultListener callback);

    void onVideoRecordStart(int width, int height);

    void onVideoRecordStop(@Nullable OnCameraResultListener callback);

    void releaseCameraPreview();
}
