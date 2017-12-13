package com.guoxiaoxing.phoenix.camera.manager.listener;

import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;

import java.io.File;

public interface CameraPhotoListener {
    void onPhotoTaken(byte[] bytes, File photoFile, CameraResultListener callback);

    void onPhotoTakeError();
}
