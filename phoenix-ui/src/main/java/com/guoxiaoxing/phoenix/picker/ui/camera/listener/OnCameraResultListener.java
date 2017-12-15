package com.guoxiaoxing.phoenix.picker.ui.camera.listener;

public interface OnCameraResultListener {

    //Called when the video record is finished and saved
    void onVideoRecorded(String filePath);

    //called when the photo is taken and saved
    void onPhotoTaken(byte[] bytes, String filePath);
}
