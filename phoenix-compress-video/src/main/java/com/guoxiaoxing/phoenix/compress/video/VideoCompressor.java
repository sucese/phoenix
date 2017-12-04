package com.guoxiaoxing.phoenix.compress.video;

import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.guoxiaoxing.phoenix.compress.video.engine.MediaTranscoderEngine;
import com.guoxiaoxing.phoenix.compress.video.format.MediaFormatPresets;
import com.guoxiaoxing.phoenix.compress.video.format.MediaFormatStrategy;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class VideoCompressor {
    private static final String TAG = "VideoCompressor";
    private static final int MAXIMUM_THREAD = 1; // TODO
    private static volatile VideoCompressor sVideoCompressor;
    private ThreadPoolExecutor mExecutor;

    private VideoCompressor() {
        mExecutor = new ThreadPoolExecutor(
                0, MAXIMUM_THREAD, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "VideoCompressor-Worker");
                    }
                });
    }

    public static VideoCompressor with() {
        if (sVideoCompressor == null) {
            synchronized (VideoCompressor.class) {
                if (sVideoCompressor == null) {
                    sVideoCompressor = new VideoCompressor();
                }
            }
        }
        return sVideoCompressor;
    }

    /**
     * Transcodes video file ssynchronously.
     * Audio track will be kept unchanged.
     *
     * @param inPath            File path for input.
     * @param outPath           File path for output.
     * @param outFormatStrategy Strategy for output video format.
     * @throws IOException if input file could not be read.
     */
    public String syncTranscodeVideo(final String inPath, final String outPath, final MediaFormatStrategy outFormatStrategy) throws IOException {
        FileInputStream fileInputStream = null;
        FileDescriptor inFileDescriptor;
        try {
            fileInputStream = new FileInputStream(inPath);
            inFileDescriptor = fileInputStream.getFD();
        } catch (IOException e) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException eClose) {
                    Log.e(TAG, "Can't close input stream: ", eClose);
                }
            }
            throw e;
        }
        Exception caughtException = null;
        try {
            MediaTranscoderEngine engine = new MediaTranscoderEngine();
            engine.setDataSource(inFileDescriptor);
            engine.transcodeVideo(outPath, outFormatStrategy);
        } catch (IOException e) {
            Log.w(TAG, "Transcode failed: input file (fd: " + inFileDescriptor.toString() + ") not found"
                    + " or could not open output file ('" + outPath + "') .", e);
            caughtException = e;
        } catch (InterruptedException e) {
            Log.i(TAG, "Cancel transcode video file.", e);
            caughtException = e;
        } catch (RuntimeException e) {
            Log.e(TAG, "Fatal error while transcoding, this might be invalid format or bug in engine or Android.", e);
            caughtException = e;
        }
        final Exception exception = caughtException;
        if (exception == null) {
            try {
                fileInputStream.close();
            } catch (IOException eClose) {
                Log.e(TAG, "Can't close input stream: ", eClose);
            }
            return outPath;
        }
        try {
            fileInputStream.close();
        } catch (IOException eClose) {
            Log.e(TAG, "Can't close input stream: ", eClose);
        }
        return null;
    }

    /**
     * Transcodes video file asynchronously.
     * Audio track will be kept unchanged.
     *
     * @param inFileDescriptor FileDescriptor for input.
     * @param outPath          File path for output.
     * @param listener         Listener instance for callback.
     * @deprecated Use {@link #asyncTranscodeVideo(FileDescriptor, String, MediaFormatStrategy, Listener)} which accepts output video format.
     */
    @Deprecated
    public Future<Void> asyncTranscodeVideo(final FileDescriptor inFileDescriptor, final String outPath, final Listener listener) {
        return asyncTranscodeVideo(inFileDescriptor, outPath, new MediaFormatStrategy() {
            @Override
            public MediaFormat createVideoOutputFormat(MediaFormat inputFormat) {
                return MediaFormatPresets.getExportPreset960x540();
            }

            @Override
            public MediaFormat createAudioOutputFormat(MediaFormat inputFormat) {
                return null;
            }
        }, listener);
    }

    /**
     * Transcodes video file asynchronously.
     * Audio track will be kept unchanged.
     *
     * @param inPath            File path for input.
     * @param outPath           File path for output.
     * @param outFormatStrategy Strategy for output video format.
     * @param listener          Listener instance for callback.
     * @throws IOException if input file could not be read.
     */
    public Future<Void> asyncTranscodeVideo(final String inPath, final String outPath, final MediaFormatStrategy outFormatStrategy, final Listener listener) throws IOException {
        FileInputStream fileInputStream = null;
        FileDescriptor inFileDescriptor;
        try {
            fileInputStream = new FileInputStream(inPath);
            inFileDescriptor = fileInputStream.getFD();
        } catch (IOException e) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException eClose) {
                    Log.e(TAG, "Can't close input stream: ", eClose);
                }
            }
            throw e;
        }
        final FileInputStream finalFileInputStream = fileInputStream;
        return asyncTranscodeVideo(inFileDescriptor, outPath, outFormatStrategy, new Listener() {
            @Override
            public void onTranscodeProgress(double progress) {
                listener.onTranscodeProgress(progress);
            }

            @Override
            public void onTranscodeCompleted() {
                closeStream();
                listener.onTranscodeCompleted();
            }

            @Override
            public void onTranscodeCanceled() {
                closeStream();
                listener.onTranscodeCanceled();
            }

            @Override
            public void onTranscodeFailed(Exception exception) {
                closeStream();
                listener.onTranscodeFailed(exception);
            }

            private void closeStream() {
                try {
                    finalFileInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Can't close input stream: ", e);
                }
            }
        });
    }

    /**
     * Transcodes video file asynchronously.
     * Audio track will be kept unchanged.
     *
     * @param inFileDescriptor  FileDescriptor for input.
     * @param outPath           File path for output.
     * @param outFormatStrategy Strategy for output video format.
     * @param listener          Listener instance for callback.
     */
    public Future<Void> asyncTranscodeVideo(final FileDescriptor inFileDescriptor, final String outPath, final MediaFormatStrategy outFormatStrategy, final Listener listener) {
        Looper looper = Looper.myLooper();
        if (looper == null) looper = Looper.getMainLooper();
        final Handler handler = new Handler(looper);
        final AtomicReference<Future<Void>> futureReference = new AtomicReference<>();
        final Future<Void> createdFuture = mExecutor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Exception caughtException = null;
                try {
                    MediaTranscoderEngine engine = new MediaTranscoderEngine();
                    engine.setProgressCallback(new MediaTranscoderEngine.ProgressCallback() {
                        @Override
                        public void onProgress(final double progress) {
                            handler.post(new Runnable() { // TODO: reuse instance
                                @Override
                                public void run() {
                                    listener.onTranscodeProgress(progress);
                                }
                            });
                        }
                    });
                    engine.setDataSource(inFileDescriptor);
                    engine.transcodeVideo(outPath, outFormatStrategy);
                } catch (IOException e) {
                    Log.w(TAG, "Transcode failed: input file (fd: " + inFileDescriptor.toString() + ") not found"
                            + " or could not open output file ('" + outPath + "') .", e);
                    caughtException = e;
                } catch (InterruptedException e) {
                    Log.i(TAG, "Cancel transcode video file.", e);
                    caughtException = e;
                } catch (RuntimeException e) {
                    Log.e(TAG, "Fatal error while transcoding, this might be invalid format or bug in engine or Android.", e);
                    caughtException = e;
                }

                final Exception exception = caughtException;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (exception == null) {
                            listener.onTranscodeCompleted();
                        } else {
                            Future<Void> future = futureReference.get();
                            if (future != null && future.isCancelled()) {
                                listener.onTranscodeCanceled();
                            } else {
                                listener.onTranscodeFailed(exception);
                            }
                        }
                    }
                });

                if (exception != null) throw exception;
                return null;
            }
        });
        futureReference.set(createdFuture);
        return createdFuture;
    }

    public interface Listener {
        /**
         * Called to notify progress.
         *
         * @param progress Progress in [0.0, 1.0] range, or negative value if progress is unknown.
         */
        void onTranscodeProgress(double progress);

        /**
         * Called when transcode completed.
         */
        void onTranscodeCompleted();

        /**
         * Called when transcode canceled.
         */
        void onTranscodeCanceled();

        /**
         * Called when transcode failed.
         *
         * @param exception Exception thrown from {@link MediaTranscoderEngine#transcodeVideo(String, MediaFormatStrategy)}.
         *                  Note that it IS NOT {@link Throwable}. This means {@link Error} won't be caught.
         */
        void onTranscodeFailed(Exception exception);
    }
}
