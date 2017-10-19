package com.guoxiaoxing.phoenix.core.listener;

import android.content.Context;

import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/1 下午5:46
 */
public interface Processor {


    /**
     * 同步处理任务
     *
     * @param context      context
     * @param mediaEntity  mediaEntity
     * @param phoenixOption phoenixOption
     */
    MediaEntity syncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption);

    /**
     * 异步处理任务
     *
     * @param context             context
     * @param mediaEntity         mediaEntity
     * @param phoenixOption        option
     * @param onProcessorListener listener
     */
    void asyncProcess(Context context, MediaEntity mediaEntity, PhoenixOption phoenixOption, OnProcessorListener onProcessorListener);
}
