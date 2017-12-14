package com.guoxiaoxing.phoenix.camera.manager.listener;

import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;

import java.io.File;

public interface CameraPictureListener {

    void onPictureTaken(byte[] bytes, File photoFile, CameraResultListener callback);

    void onPictureTakeError();
}
