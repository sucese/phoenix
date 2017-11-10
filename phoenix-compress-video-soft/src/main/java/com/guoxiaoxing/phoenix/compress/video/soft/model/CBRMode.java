package com.guoxiaoxing.phoenix.compress.video.soft.model;

public class CBRMode extends BaseMediaBitrateConfig {

    /**
     * @param bufSize bufSize
     * @param bitrate 固定码率值
     */
    public CBRMode(int bufSize, int bitrate) {
        if (bufSize <= 0 || bitrate <= 0) {
            throw new IllegalArgumentException("bufSize or bitrate value error!");
        }
        this.bufSize = bufSize;
        this.bitrate = bitrate;
        this.mode = MODE.CBR;
    }
}
