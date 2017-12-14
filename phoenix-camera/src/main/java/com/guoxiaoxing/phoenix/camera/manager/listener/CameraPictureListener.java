package com.guoxiaoxing.phoenix.camera.manager.listener;

import com.guoxiaoxing.phoenix.camera.listener.OnCameraResultListener;

import java.io.File;

public interface CameraPictureListener {

    void onPictureTaken(byte[] bytes, File photoFile, OnCameraResultListener callback);

    void onPictureTakeError();
}
