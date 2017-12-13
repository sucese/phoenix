package com.guoxiaoxing.phoenix.camera.manager.listener;

public interface CameraCloseListener<CameraId> {
    void onCameraClosed(CameraId closedCameraId);
}
