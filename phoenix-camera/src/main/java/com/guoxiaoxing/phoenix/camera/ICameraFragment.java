package com.guoxiaoxing.phoenix.camera;

import android.support.annotation.Nullable;

import com.guoxiaoxing.phoenix.camera.listener.CameraControlListener;
import com.guoxiaoxing.phoenix.camera.listener.OnCameraResultListener;
import com.guoxiaoxing.phoenix.camera.listener.CameraStateListener;
import com.guoxiaoxing.phoenix.camera.listener.CameraVideoRecordTextListener;

public interface ICameraFragment {

    void takePicture(@Nullable String directoryPath, @Nullable String fileName, OnCameraResultListener resultListener);

    void startRecordingVideo(@Nullable String directoryPath, @Nullable String fileName);

    void stopRecordingVideo(OnCameraResultListener callback);

    void openSettingDialog();

    void switchCameraTypeFrontBack();

    void switchCaptureAction(int actionType);

    void toggleFlashMode();

    void setStateListener(CameraStateListener cameraStateListener);

    void setTextListener(CameraVideoRecordTextListener cameraVideoRecordTextListener);

    void setControlsListener(CameraControlListener cameraControlListener);

    void setResultListener(OnCameraResultListener onCameraResultListener);
}
