package com.guoxiaoxing.phoenix.camera.listener;

/**
 * Convenience implementation of {@link CameraResultListener}. Derive from this and only override what you need.
 * @author Skala
 */
public class CameraResultAdapter implements CameraResultListener {
    @Override
    public void onVideoRecorded(String filePath) {

    }

    @Override
    public void onPhotoTaken(byte[] bytes, String filePath) {

    }
}
