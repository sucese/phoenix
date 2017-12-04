package com.guoxiaoxing.phoenix.compress.picture;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.guoxiaoxing.phoenix.compress.picture.internal.PictureCompressor;
import com.guoxiaoxing.phoenix.compress.picture.listener.OnCompressListener;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.listener.OnProcessorListener;
import com.guoxiaoxing.phoenix.core.listener.Processor;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;

import java.io.File;
import java.io.IOException;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/2 下午2:51
 */
public final class PictureCompressProcessor implements Processor {

    private static final String TAG = "CompressProcessor";

    @Override
    public MediaEntity syncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption) {

        if (mediaEntity == null) {
            Log.d(TAG, "The mediaEntity can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        if (mediaEntity.getSize() < phoenixOption.getCompressPictureFilterSize() * 1000) {
            return mediaEntity;
        }

        if (!TextUtils.isEmpty(mediaEntity.getCompressPath())) {
            return mediaEntity;
        }

        String path;
        if(!TextUtils.isEmpty(mediaEntity.getEditPath())){
            path = mediaEntity.getEditPath();
        }else {
            path = mediaEntity.getLocalPath();
        }
        File file = new File(path);
        try {
            File compressFIle = PictureCompressor.with(context)
                    .savePath(context.getCacheDir().getAbsolutePath())
                    .load(file)
                    .get();
            if (compressFIle != null) {
                mediaEntity.setCompressed(true);
                mediaEntity.setCompressPath(compressFIle.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaEntity;
    }

    @Override
    public void asyncProcess(Context context, final MediaEntity mediaEntity, PhoenixOption phoenixOption, final OnProcessorListener onProcessorListener) {

        if (mediaEntity == null) {
            Log.d(TAG, "The mediaEntity can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        if (onProcessorListener == null) {
            Log.d(TAG, "The onProcessorListener can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        if (!TextUtils.isEmpty(mediaEntity.getCompressPath())) {
            onProcessorListener.onSuccess(mediaEntity);
            return;
        }

        if (mediaEntity.getSize() < phoenixOption.getCompressPictureFilterSize() * 1000) {
            onProcessorListener.onSuccess(mediaEntity);
            return;
        }

        String path;
        if(!TextUtils.isEmpty(mediaEntity.getEditPath())){
            path = mediaEntity.getEditPath();
        }else {
            path = mediaEntity.getLocalPath();
        }

        File file = new File(path);
        PictureCompressor.with(context)
                .load(file)
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "Picture compress onStart");
                        onProcessorListener.onStart(mediaEntity);
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.d(TAG, "Picture compress onSuccess");
                        mediaEntity.setCompressed(true);
                        mediaEntity.setCompressPath(file.getAbsolutePath());
                        onProcessorListener.onSuccess(mediaEntity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Picture compress onError : " + e.getMessage());
                        onProcessorListener.onFailed("Picture compress onError : " + e.getMessage());
                    }
                }).launch();
    }
}
