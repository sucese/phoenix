package com.guoxiaoxing.phoenix.picker.ui.camera.manager.listener;

import com.guoxiaoxing.phoenix.picker.ui.camera.listener.OnCameraResultListener;

import java.io.File;

public interface CameraPictureListener {

    void onPictureTaken(byte[] bytes, File photoFile, OnCameraResultListener callback);

    void onPictureTakeError();
}
