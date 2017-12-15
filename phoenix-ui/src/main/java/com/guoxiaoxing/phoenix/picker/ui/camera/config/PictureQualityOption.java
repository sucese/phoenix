package com.guoxiaoxing.phoenix.picker.ui.camera.config;

import com.guoxiaoxing.phoenix.picker.ui.camera.util.Size;

public class PictureQualityOption implements CharSequence {

    @CameraConfig.MediaQuality
    private int mediaQuality;
    private String title;

    public PictureQualityOption(@CameraConfig.MediaQuality int mediaQuality, Size size) {
        this.mediaQuality = mediaQuality;

        title = String.valueOf(size.getWidth()) + " x " + String.valueOf(size.getHeight());
    }

    @CameraConfig.MediaQuality
    public int getMediaQuality() {
        return mediaQuality;
    }

    @Override
    public int length() {
        return title.length();
    }

    @Override
    public char charAt(int index) {
        return title.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return title.subSequence(start, end);
    }

    @Override
    public String toString() {
        return title;
    }
}
