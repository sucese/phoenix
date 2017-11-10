package com.guoxiaoxing.phoenix.compress.video.soft;


import com.guoxiaoxing.phoenix.compress.video.soft.model.MediaObject;

/**
 * 视频录制接口
 */
public interface IMediaRecorder {

    /**
     * 开始录制
     *
     * @return 录制失败返回null
     */
    MediaObject.MediaPart startRecord();

    /**
     * 停止录制
     */
    void stopRecord();

    /**
     * 音频错误
     *
     * @param what    错误类型
     * @param message
     */
    void onAudioError(int what, String message);

    /**
     * 接收音频数据
     *
     * @param sampleBuffer 音频数据
     * @param len
     */
    void receiveAudioData(byte[] sampleBuffer, int len);
}
