package com.guoxiaoxing.phoenix.camera.listener;

public interface CameraVideoRecordTextListener {
    void setRecordSizeText(long size, String text);
    void setRecordSizeTextVisible(boolean visible);

    void setRecordDurationText(String text);
    void setRecordDurationTextVisible(boolean visible);
}
