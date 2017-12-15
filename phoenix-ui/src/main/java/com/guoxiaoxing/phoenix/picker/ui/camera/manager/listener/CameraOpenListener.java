package com.guoxiaoxing.phoenix.picker.ui.camera.manager.listener;

import com.guoxiaoxing.phoenix.picker.ui.camera.util.Size;

public interface CameraOpenListener<CameraId, SurfaceListener> {

    void onCameraOpened(CameraId openedCameraId, Size previewSize, SurfaceListener surfaceListener);

    void onCameraOpenError();
}
