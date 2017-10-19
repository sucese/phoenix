package com.guoxiaoxing.phoenix.upload;

import android.content.Context;
import android.util.Log;

import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.listener.OnProcessorListener;
import com.guoxiaoxing.phoenix.core.listener.Processor;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.upload.internal.MediaUploader;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/4 下午1:36
 */
public final class MediaUploadProcessor implements Processor {

    private static final String TAG = "MediaUploadProcessor";

    @Override
    public MediaEntity syncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption) {

        if (mediaEntity == null) {
            Log.d(TAG, "The mediaEntity can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        return MediaUploader.syncUploadMedia(mediaEntity);
    }

    @Override
    public void asyncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption, OnProcessorListener onProcessorListener) {

        if (mediaEntity == null) {
            Log.d(TAG, "The mediaEntity can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        if (onProcessorListener == null) {
            Log.d(TAG, "The onProcessorListener can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }
        MediaUploader.asyncUploadMedia(mediaEntity, onProcessorListener);
    }
}
