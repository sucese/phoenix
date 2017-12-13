package com.guoxiaoxing.phoenix.camera.controller.view;

import android.support.annotation.Nullable;
import android.view.View;

import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.util.Size;

public interface CameraView {

    void updateCameraPreview(Size size, View cameraPreview);

    void updateUiForMediaAction(@CameraConfig.MediaAction int mediaAction);

    void updateCameraSwitcher(int numberOfCameras);

    void onPhotoTaken(byte[] bytes, @Nullable CameraResultListener callback);

    void onVideoRecordStart(int width, int height);

    void onVideoRecordStop(@Nullable CameraResultListener callback);

    void releaseCameraPreview();
}
