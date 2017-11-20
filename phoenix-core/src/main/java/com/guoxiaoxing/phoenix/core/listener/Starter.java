package com.guoxiaoxing.phoenix.core.listener;

import android.app.Activity;

import com.guoxiaoxing.phoenix.core.PhoenixOption;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/14 上午10:17
 */
public interface Starter {
    void start(Activity activity, PhoenixOption option, int type, int requestCode);
}
