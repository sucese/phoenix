package com.guoxiaoxing.phoenix.camera;

import android.support.annotation.Nullable;

import com.guoxiaoxing.phoenix.camera.listener.CameraControlListener;
import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.listener.CameraStateListener;
import com.guoxiaoxing.phoenix.camera.listener.CameraVideoRecordTextListener;

public interface CameraFragmentApi {

    void takePhotoOrCaptureVideo(CameraResultListener resultListener, @Nullable String directoryPath, @Nullable String fileName);

    void openSettingDialog();

    void switchCameraTypeFrontBack();

    void switchActionPhotoVideo();

    void toggleFlashMode();

    void setStateListener(CameraStateListener cameraStateListener);

    void setTextListener(CameraVideoRecordTextListener cameraVideoRecordTextListener);

    void setControlsListener(CameraControlListener cameraControlListener);

    void setResultListener(CameraResultListener cameraResultListener);
}
