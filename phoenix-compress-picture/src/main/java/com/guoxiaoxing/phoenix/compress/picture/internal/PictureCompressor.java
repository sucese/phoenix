package com.guoxiaoxing.phoenix.compress.picture.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.guoxiaoxing.phoenix.compress.picture.listener.OnCompressListener;

import java.io.File;
import java.io.IOException;

public class PictureCompressor implements Handler.Callback {

    private static final String TAG = "PictureCompressor";
    private static final String DEFAULT_DISK_CACHE_DIR = "cache";

    private static final int MSG_COMPRESS_SUCCESS = 0;
    private static final int MSG_COMPRESS_START = 1;
    private static final int MSG_COMPRESS_ERROR = 2;

    private File mFile;
    private int mFilterSize;
    private String mSavePath;
    private OnCompressListener onCompressListener;
    private Handler mHandler;

    private PictureCompressor(Builder builder) {
        this.mFile = builder.file;
        this.mSavePath = builder.savePath;
        this.mFilterSize = builder.filterSize;
        this.onCompressListener = builder.onCompressListener;
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    /**
     * Returns a mFile with a cache audio name in the private cache directory.
     *
     * @param context A context.
     */
    private File getImageCacheFile(Context context) {
        if (getImageCacheDir(context) != null) {
            return new File(getImageCacheDir(context) + "/" + System.currentTimeMillis() + (int) (Math.random() * 1000) + ".jpg");
        }
        return null;
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved audio.
     *
     * @param context A context.
     * @see #getImageCacheDir(Context, String)
     */
    @Nullable
    private File getImageCacheDir(Context context) {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context   A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getImageCacheDir(Context)
     */
    @Nullable
    private File getImageCacheDir(Context context, String cacheName) {
        File cacheDir = new File(mSavePath);
        File result = new File(cacheDir, cacheName);
        if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
            // File wasn't able to create a directory, or the result exists but not a directory
            return null;
        }
        return result;
    }

    /**
     * start asynchronous compress thread
     */
    @UiThread
    private void launch(final Context context) {
        if (mFile == null && onCompressListener != null) {
            onCompressListener.onError(new NullPointerException("image mFile cannot be null"));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START));

                    File result = new Engine(mFile, getImageCacheFile(context), mFilterSize).compress();
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_SUCCESS, result));
                } catch (IOException e) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_ERROR, e));
                }
            }
        }).start();
    }

    /**
     * start compress and return the mFile
     */
    @WorkerThread
    private File get(final Context context) throws IOException {
        return new Engine(mFile, getImageCacheFile(context), mFilterSize).compress();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (onCompressListener == null) return false;

        switch (msg.what) {
            case MSG_COMPRESS_START:
                onCompressListener.onStart();
                break;
            case MSG_COMPRESS_SUCCESS:
                onCompressListener.onSuccess((File) msg.obj);
                break;
            case MSG_COMPRESS_ERROR:
                onCompressListener.onError((Throwable) msg.obj);
                break;
        }
        return false;
    }

    public static class Builder {

        private Context context;
        private int filterSize;
        private File file;
        private String savePath;
        private OnCompressListener onCompressListener;

        Builder(Context context) {
            this.context = context;
        }

        private PictureCompressor build() {
            return new PictureCompressor(this);
        }

        public Builder load(File file) {
            this.file = file;
            return this;
        }

        public Builder filterSize(int filterSize) {
            this.filterSize = filterSize;
            return this;
        }

        public Builder savePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public Builder setCompressListener(OnCompressListener listener) {
            this.onCompressListener = listener;
            return this;
        }

        public void launch() {
            build().launch(context);
        }

        public File get() throws IOException {
            return build().get(context);
        }
    }
}