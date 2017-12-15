package com.guoxiaoxing.phoenix.picker.ui.camera.manager.impl;

import android.content.Context;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.guoxiaoxing.phoenix.picker.ui.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.CameraConfigProvider;
import com.guoxiaoxing.phoenix.picker.ui.camera.manager.CameraManager;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.Size;

/**
 * The base camera manager
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
abstract class BaseCameraManager<CameraId, SurfaceListener> implements CameraManager<CameraId, SurfaceListener>
        , MediaRecorder.OnInfoListener {

    private static final String TAG = "BaseCameraManager";

    Context mContext;
    CameraConfigProvider cameraConfigProvider;

    MediaRecorder mMediaRecorder;
    boolean mIsVideoRecording = false;

    CameraId mCameraId = null;
    CameraId mFaceFrontCameraId = null;
    CameraId mFaceBackCameraId = null;
    int mNumberOfCameras = 0;
    int mFaceFrontCameraOrientation;
    int mFaceBackCameraOrientation;

    Size mPhotoSize;
    Size mVideoSize;
    Size mPreviewSize;
    Size mWindowSize;
    CamcorderProfile mCamcorderProfile;

    HandlerThread mBackgroundThread;
    Handler mBackgroundHandler;
    Handler mUiiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void initializeCameraManager(CameraConfigProvider cameraConfigProvider, Context context) {
        this.mContext = context;
        this.cameraConfigProvider = cameraConfigProvider;
        startBackgroundThread();
    }

    @Override
    public void releaseCameraManager() {
        this.mContext = null;
        stopBackgroundThread();
    }

    protected abstract void prepareCameraOutputs();

    protected abstract boolean prepareVideoRecorder();

    protected abstract void onMaxDurationReached();

    protected abstract void onMaxFileSizeReached();

    protected abstract int getPhotoOrientation(@CameraConfig.SensorPosition int sensorPosition);

    protected abstract int getVideoOrientation(@CameraConfig.SensorPosition int sensorPosition);

    protected void releaseVideoRecorder() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
            }
        } catch (Exception ignore) {

        } finally {
            mMediaRecorder = null;
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (Build.VERSION.SDK_INT > 17) {
            mBackgroundThread.quitSafely();
        } else mBackgroundThread.quit();

        try {
            mBackgroundThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "stopBackgroundThread: ", e);
        } finally {
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
        if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
            onMaxDurationReached();
        } else if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == what) {
            onMaxFileSizeReached();
        }
    }

    public boolean isVideoRecording() {
        return mIsVideoRecording;
    }

    public CameraId getCameraId() {
        return mCameraId;
    }

    public CameraId getFaceFrontCameraId() {
        return mFaceFrontCameraId;
    }

    public CameraId getFaceBackCameraId() {
        return mFaceBackCameraId;
    }

    public int getNumberOfCameras() {
        return mNumberOfCameras;
    }

    public int getFaceFrontCameraOrientation() {
        return mFaceFrontCameraOrientation;
    }

    public int getFaceBackCameraOrientation() {
        return mFaceBackCameraOrientation;
    }

    public void setCameraId(CameraId currentCameraId) {
        this.mCameraId = currentCameraId;
    }
}
