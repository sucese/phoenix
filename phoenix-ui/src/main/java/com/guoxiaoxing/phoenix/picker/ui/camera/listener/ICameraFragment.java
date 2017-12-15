package com.guoxiaoxing.phoenix.picker.ui.camera.listener;

import android.support.annotation.Nullable;

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
