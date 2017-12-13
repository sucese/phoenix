package com.guoxiaoxing.phoenix.camera.manager.listener;

import com.guoxiaoxing.phoenix.camera.util.Size;

public interface CameraOpenListener<CameraId, SurfaceListener> {
    void onCameraOpened(CameraId openedCameraId, Size previewSize, SurfaceListener surfaceListener);

    void onCameraOpenError();
}
