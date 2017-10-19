package com.guoxiaoxing.phoenix.compress.picture.util;

import android.text.TextUtils;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/1 下午3:29
 */
public class MimeUtils {

    /**
     * 是否是网络图片
     *
     * @param path path
     * @return boolean
     */
    public static boolean isHttp(String path) {
        if (!TextUtils.isEmpty(path)) {
            if (path.startsWith("http")
                    || path.startsWith("https")) {
                return true;
            }
        }
        return false;
    }
}
