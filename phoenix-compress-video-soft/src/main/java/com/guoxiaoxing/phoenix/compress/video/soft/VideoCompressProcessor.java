package com.guoxiaoxing.phoenix.compress.video.soft;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.guoxiaoxing.phoenix.compress.video.soft.model.AutoVBRMode;
import com.guoxiaoxing.phoenix.compress.video.soft.model.LocalMediaConfig;
import com.guoxiaoxing.phoenix.compress.video.soft.model.OnlyCompressOverBean;
import com.guoxiaoxing.phoenix.compress.video.soft.util.DeviceUtils;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.listener.OnProcessorListener;
import com.guoxiaoxing.phoenix.core.listener.Processor;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;


import java.io.File;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/4 下午1:44
 */
public class VideoCompressProcessor implements Processor {

    private static final String TAG = "VideoCompressProcessor";

    static {
        // 设置拍摄视频缓存路径
        File dcim = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                SCCamera.setVideoCachePath(dcim + "/souche/");
            } else {
                SCCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/",
                        "/sdcard-ext/")
                        + "/souche/");
            }
        } else {
            SCCamera.setVideoCachePath(dcim + "/souche/");
        }
        // 初始化拍摄
        SCCamera.initialize(false, null);
    }

    @Override
    public MediaEntity syncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption) {

        if (mediaEntity == null) {
            Log.d(TAG, "The mediaEntity can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        if (!TextUtils.isEmpty(mediaEntity.getCompressPath())) {
            return mediaEntity;
        }

        LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
        final LocalMediaConfig config = buidler
                .setVideoPath(mediaEntity.getLocalPath())
                .captureThumbnailsTime(1)
                .doH264Compress(new AutoVBRMode())
//                .setFramerate(iRate)
                .setScale(0.5F)
                .build();
        OnlyCompressOverBean onlyCompressOverBean = new LocalMediaCompress(config).startCompress();
        String compressPath = onlyCompressOverBean.getVideoPath();
        String thumbnailPath = onlyCompressOverBean.getPicPath();
        mediaEntity.setCompressed(true);
        mediaEntity.setCompressPath(compressPath);
        mediaEntity.setLocalThumbnailPath(thumbnailPath);
        return mediaEntity;
    }

    @Override
    public void asyncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption, final OnProcessorListener onProcessorListener) {

        if (mediaEntity == null) {
            Log.d(TAG, "The mediaEntity can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        if (onProcessorListener == null) {
            Log.d(TAG, "The onProcessorListener can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }
    }
}