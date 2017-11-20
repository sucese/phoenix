package com.guoxiaoxing.phoenix.core.listener;

import android.content.Context;
import android.widget.ImageView;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/11/19 下午12:40
 */
public interface ImageLoader {

    /**
     * Load image
     *
     * @param context   context
     * @param imageView imageView
     * @param imagePath imagePath
     * @param type      the type of handle image, such as rounded corners and so on
     */
    void loadImage(Context context, ImageView imageView, String imagePath, int type);
}
