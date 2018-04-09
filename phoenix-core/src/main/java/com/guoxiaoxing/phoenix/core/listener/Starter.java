package com.guoxiaoxing.phoenix.core.listener;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.guoxiaoxing.phoenix.core.PhoenixOption;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/14 上午10:17
 */
public interface Starter {
    String BUNDLE_KEY_FUTURE_ACTION = "future_action";

    void start(Fragment fragment, PhoenixOption option, int type, int requestCode);
    void start(Activity activity, PhoenixOption option, int type, int requestCode);
    void start(Fragment fragment, PhoenixOption option, int type, String futureAction);
    void start(Activity activity, PhoenixOption option, int type, String futureAction);
}
