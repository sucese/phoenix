package com.guoxiaoxing.phoenix.camera.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.manager.CameraManager;

import java.io.File;

/**
 * The camera controller for control camera lifecycle
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
public interface CameraController<CameraId> {

    void onCreate(Bundle savedInstanceState);

    void onResume();

    void onPause();

    void onDestroy();

    void takePhoto(CameraResultListener resultListener);

    void takePhoto(CameraResultListener callback, @Nullable String direcoryPath, @Nullable String fileName);

    void startVideoRecord();

    void startVideoRecord(@Nullable String direcoryPath, @Nullable String fileName);

    void stopVideoRecord(CameraResultListener callback);

    boolean isVideoRecording();

    void switchCamera(@CameraConfig.CameraFace int cameraFace);

    void switchQuality();

    void setFlashMode(@CameraConfig.FlashMode int flashMode);

    int getNumberOfCameras();

    @CameraConfig.MediaAction
    int getMediaAction();

    CameraId getCameraId();

    File getOutputFile();

    CameraManager getCameraManager();

    CharSequence[] getVideoQualityOptions();

    CharSequence[] getPhotoQualityOptions();
}
