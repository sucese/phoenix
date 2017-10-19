package com.guoxiaoxing.phoenix.core.listener;

import com.guoxiaoxing.phoenix.core.model.MediaEntity;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/2 下午6:07
 */
public interface OnProcessorListener {

    /**
     * Call when operation is on start
     *
     * @param mediaEntity mediaEntity
     */
    void onStart(MediaEntity mediaEntity);

    /**
     * Call when operation is on progress
     *
     * @param progress progress
     */
    void onProgress(int progress);

    /**
     * Call when operation is on success
     *
     * @param mediaEntity mediaEntity
     */
    void onSuccess(MediaEntity mediaEntity);

    /**
     * Call when operation is on failed
     *
     * @param errorMessage errorMessage
     */
    void onFailed(String errorMessage);
}
