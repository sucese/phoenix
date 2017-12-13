package com.guoxiaoxing.phoenix.camera.manager.listener;

import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.util.Size;

import java.io.File;

public interface CameraVideoListener {
    void onVideoRecordStarted(Size videoSize);

    void onVideoRecordStopped(File videoFile, CameraResultListener callback);

    void onVideoRecordError();
}
