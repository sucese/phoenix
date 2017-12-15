package com.guoxiaoxing.phoenix.picker.ui.camera.manager.listener;

import com.guoxiaoxing.phoenix.picker.ui.camera.listener.OnCameraResultListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.Size;

import java.io.File;

public interface CameraVideoListener {

    void onVideoRecordStarted(Size videoSize);

    void onVideoRecordStopped(File videoFile, OnCameraResultListener callback);

    void onVideoRecordError();
}
