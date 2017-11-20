package com.guoxiaoxing.phoenix.core;

import com.guoxiaoxing.phoenix.core.listener.ImageLoader;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/11/19 下午12:15
 */
public class PhoenixConfig {

    private ImageLoader imageLoader;

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public PhoenixConfig imageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }
}
