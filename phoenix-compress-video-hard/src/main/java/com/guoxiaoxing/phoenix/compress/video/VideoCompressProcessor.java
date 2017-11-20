package com.guoxiaoxing.phoenix.compress.video;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import com.guoxiaoxing.phoenix.compress.video.internal.VideoCompressor;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.listener.OnProcessorListener;
import com.guoxiaoxing.phoenix.core.listener.Processor;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/4 下午1:44
 */
public class VideoCompressProcessor implements Processor {

    private static final String TAG = "VideoCompressProcessor";

    private VideoCompressor videoCompressor;

    @Override
    public MediaEntity syncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption) {

        if (mediaEntity == null) {
            Log.d(TAG, "The mediaEntity can not be null");
            throw new IllegalArgumentException("The onProcessorListener can not be null");
        }

        if(!TextUtils.isEmpty(mediaEntity.getCompressPath())){
            return mediaEntity;
        }

        if(mediaEntity.getSize() < phoenixOption.getCompressVideoFilterSize() * 1000){
            return mediaEntity;
        }

        if (videoCompressor == null) {
            videoCompressor = VideoCompressor.newBuilder()
                    .context(context)
                    .destinationUri(phoenixOption.getSavePath())
                    .build();

        }

        return videoCompressor.compressVideo(mediaEntity);
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

        if(!TextUtils.isEmpty(mediaEntity.getCompressPath())){
            onProcessorListener.onSuccess(mediaEntity);
            return;
        }

        if(mediaEntity.getSize() < phoenixOption.getCompressVideoFilterSize() * 1000){
            onProcessorListener.onSuccess(mediaEntity);
            return;
        }

        if (videoCompressor == null) {
            videoCompressor = VideoCompressor.newBuilder()
                    .context(context)
                    .destinationUri(phoenixOption.getSavePath())
                    .build();

        }

        videoCompressor.singleAction(mediaEntity)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MediaEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "Video compress start");
                    }

                    @Override
                    public void onNext(MediaEntity mediaEntity) {
                        Log.d(TAG, "Video compress success");
                        onProcessorListener.onSuccess(mediaEntity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Video compress error: " + e.getMessage());
                        onProcessorListener.onFailed(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
