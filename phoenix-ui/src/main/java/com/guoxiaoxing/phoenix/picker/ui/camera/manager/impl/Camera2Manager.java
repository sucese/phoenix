package com.guoxiaoxing.phoenix.picker.ui.camera.manager.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.guoxiaoxing.phoenix.picker.ui.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.CameraConfigProvider;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.PictureQualityOption;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.VideoQualityOption;
import com.guoxiaoxing.phoenix.picker.ui.camera.listener.OnCameraResultListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.manager.listener.CameraCloseListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.manager.listener.CameraOpenListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.manager.listener.CameraPictureListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.manager.listener.CameraVideoListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.CameraUtils;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.ImageSaver;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.Size;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The camera mCameraManager for camera2
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class Camera2Manager extends BaseCameraManager<String, TextureView.SurfaceTextureListener>
        implements ImageReader.OnImageAvailableListener, TextureView.SurfaceTextureListener {

    private final static String TAG = "Camera2Manager";

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRE_CAPTURE = 2;
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    private CameraOpenListener<String, TextureView.SurfaceTextureListener> mCameraOpenListener;
    private CameraPictureListener mCameraPictureListener;
    private CameraVideoListener mCameraVideoListener;
    private File mOutputPath;

    @CameraPreviewState
    private int mPreviewState = STATE_PREVIEW;

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CaptureRequest mPreviewRequest;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;
    private CameraCharacteristics mFrontCameraCharacteristics;
    private CameraCharacteristics mBackCameraCharacteristics;
    private StreamConfigurationMap mFrontCameraStreamConfigurationMap;
    private StreamConfigurationMap mBackCameraStreamConfigurationMap;

    private Surface mWorkingSurface;
    private ImageReader mImageReader;
    private SurfaceTexture mSurfaceTexture;

    private OnCameraResultListener mOnCameraResultListener;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Camera2Manager.this.mCameraDevice = cameraDevice;
            if (mCameraOpenListener != null) {
                mUiiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(mCameraId) && mPreviewSize != null)
                            mCameraOpenListener.onCameraOpened(mCameraId, mPreviewSize, Camera2Manager.this);
                    }
                });
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            Camera2Manager.this.mCameraDevice = null;
            mUiiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCameraOpenListener.onCameraOpenError();
                }
            });
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            Camera2Manager.this.mCameraDevice = null;
            mUiiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCameraOpenListener.onCameraOpenError();
                }
            });
        }
    };

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            processCaptureResult(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            processCaptureResult(result);
        }

    };

    @Override
    public void initializeCameraManager(CameraConfigProvider cameraConfigProvider, Context context) {
        super.initializeCameraManager(cameraConfigProvider, context);
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        mWindowSize = new Size(size.x, size.y);

        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            final String[] ids = mCameraManager.getCameraIdList();
            mNumberOfCameras = ids.length;
            for (String id : ids) {
                final CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);

                final int orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (orientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    mFaceFrontCameraId = id;
                    mFaceFrontCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    mFrontCameraCharacteristics = characteristics;
                } else {
                    mFaceBackCameraId = id;
                    mFaceBackCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    mBackCameraCharacteristics = characteristics;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during camera initialize");
        }
    }

    @Override
    public void openCamera(String cameraId, final CameraOpenListener<String, TextureView.SurfaceTextureListener> cameraOpenListener) {
        this.mCameraId = cameraId;
        this.mCameraOpenListener = cameraOpenListener;
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mContext == null || cameraConfigProvider == null) {
                    Log.e(TAG, "openCamera: ");
                    if (cameraOpenListener != null) {
                        mUiiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraOpenListener.onCameraOpenError();
                            }
                        });
                    }
                    return;
                }
                prepareCameraOutputs();
                try {
                    mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
                } catch (Exception e) {
                    Log.e(TAG, "openCamera: ", e);
                    if (cameraOpenListener != null) {
                        mUiiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraOpenListener.onCameraOpenError();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void closeCamera(final CameraCloseListener<String> cameraCloseListener) {
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                closeCamera();
                if (cameraCloseListener != null) {
                    mUiiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraCloseListener.onCameraClosed(mCameraId);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void setFlashMode(@CameraConfig.FlashMode int flashMode) {
        setFlashModeAndBuildPreviewRequest(flashMode);
    }

    @Override
    public void takePicture(File photoFile, CameraPictureListener cameraPictureListener, OnCameraResultListener callback) {
        this.mOutputPath = photoFile;
        this.mCameraPictureListener = cameraPictureListener;
        this.mOnCameraResultListener = callback;

        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                lockFocus();
            }
        });

    }

    @Override
    public Size getPictureSizeForQuality(@CameraConfig.MediaQuality int mediaQuality) {
        final StreamConfigurationMap map = mCameraId.equals(mFaceBackCameraId) ? mBackCameraStreamConfigurationMap : mFrontCameraStreamConfigurationMap;
        return CameraUtils.getPictureSize(Size.fromArray2(map.getOutputSizes(ImageFormat.JPEG)), mediaQuality);
    }

    @Override
    public void startVideoRecord(File videoFile, final CameraVideoListener cameraVideoListener) {
        if (mIsVideoRecording || mSurfaceTexture == null) return;

        this.mOutputPath = videoFile;
        this.mCameraVideoListener = cameraVideoListener;

        if (cameraVideoListener != null)
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Tzutalin++ 2017/05. If calling release function, it should not be executed
                    if (mContext == null) return;

                    closePreviewSession();
                    if (prepareVideoRecorder()) {

                        final SurfaceTexture texture = Camera2Manager.this.mSurfaceTexture;
                        texture.setDefaultBufferSize(mVideoSize.getWidth(), mVideoSize.getHeight());

                        try {
                            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                            final List<Surface> surfaces = new ArrayList<>();

                            final Surface previewSurface = mWorkingSurface;
                            surfaces.add(previewSurface);
                            mPreviewRequestBuilder.addTarget(previewSurface);

                            mWorkingSurface = mMediaRecorder.getSurface();
                            surfaces.add(mWorkingSurface);
                            mPreviewRequestBuilder.addTarget(mWorkingSurface);

                            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                    mCaptureSession = cameraCaptureSession;

                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                    try {
                                        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                                    } catch (Exception e) {
                                    }

                                    try {
                                        mMediaRecorder.start();
                                    } catch (Exception ignore) {
                                        Log.e(TAG, "mMediaRecorder.start(): ", ignore);
                                    }

                                    mIsVideoRecording = true;

                                    mUiiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            cameraVideoListener.onVideoRecordStarted(mVideoSize);
                                        }
                                    });
                                }

                                @Override
                                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                    Log.d(TAG, "onConfigureFailed");
                                }
                            }, mBackgroundHandler);
                        } catch (Exception e) {
                            Log.e(TAG, "startVideoRecord: ", e);
                        }
                    }
                }
            });
    }

    @Override
    public void stopVideoRecord(final OnCameraResultListener callback) {
        if (mIsVideoRecording) {
            closePreviewSession();
            if (mMediaRecorder != null) {
                try {
                    mMediaRecorder.stop();
                } catch (Exception ignore) {
                }
            }
            mIsVideoRecording = false;
            releaseVideoRecorder();

            if (mCameraVideoListener != null) {
                mCameraVideoListener.onVideoRecordStopped(mOutputPath, callback);
            }
        }
    }

    private void startPreview(SurfaceTexture texture) {
        try {
            if (texture == null) return;

            this.mSurfaceTexture = texture;

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            mWorkingSurface = new Surface(texture);

            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mWorkingSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(mWorkingSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            updatePreview(cameraCaptureSession);
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.d(TAG, "Fail while starting preview: ");
                        }
                    }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error while preparing surface for preview: ", e);
        }
    }

    //--------------------Internal methods------------------

    @Override
    protected void onMaxDurationReached() {
        stopVideoRecord(mOnCameraResultListener);
    }

    @Override
    protected void onMaxFileSizeReached() {
        stopVideoRecord(mOnCameraResultListener);
    }

    @Override
    protected int getPhotoOrientation(@CameraConfig.SensorPosition int sensorPosition) {
        return getVideoOrientation(sensorPosition);
    }

    @Override
    protected int getVideoOrientation(@CameraConfig.SensorPosition int sensorPosition) {
        final int degrees;
        switch (sensorPosition) {
            case CameraConfig.SENSOR_POSITION_UP:
                degrees = 0;
                break; // Natural orientation
            case CameraConfig.SENSOR_POSITION_LEFT:
                degrees = 90;
                break; // Landscape left
            case CameraConfig.SENSOR_POSITION_UP_SIDE_DOWN:
                degrees = 180;
                break;// Upside down
            case CameraConfig.SENSOR_POSITION_RIGHT:
                degrees = 270;
                break;// Landscape right
            case CameraConfig.SENSOR_POSITION_UNSPECIFIED:
            default:
                degrees = 0;
                break;
        }

        final int rotate;
        if (Objects.equals(mCameraId, mFaceFrontCameraId)) {
            rotate = (360 + mFaceFrontCameraOrientation + degrees) % 360;
        } else {
            rotate = (360 + mFaceBackCameraOrientation - degrees) % 360;
        }
        return rotate;
    }

    private void closeCamera() {
        closePreviewSession();
        releaseTexture();
        closeCameraDevice();
        closeImageReader();
        releaseVideoRecorder();
    }

    private void releaseTexture() {
        if (null != mSurfaceTexture) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    private void closeImageReader() {
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void closeCameraDevice() {
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    protected void prepareCameraOutputs() {
        try {
            final CameraCharacteristics characteristics = mCameraId.equals(mFaceBackCameraId) ? mBackCameraCharacteristics : mFrontCameraCharacteristics;

            if (mCameraId.equals(mFaceFrontCameraId) && mFrontCameraStreamConfigurationMap == null)
                mFrontCameraStreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            else if (mCameraId.equals(mFaceBackCameraId) && mBackCameraStreamConfigurationMap == null)
                mBackCameraStreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            final StreamConfigurationMap map = mCameraId.equals(mFaceBackCameraId) ? mBackCameraStreamConfigurationMap : mFrontCameraStreamConfigurationMap;
            if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_AUTO) {
                mCamcorderProfile = CameraUtils.getCamcorderProfile(mCameraId, cameraConfigProvider.getVideoFileSize(), cameraConfigProvider.getMinimumVideoDuration());
            } else
                mCamcorderProfile = CameraUtils.getCamcorderProfile(cameraConfigProvider.getMediaQuality(), mCameraId);

            mVideoSize = CameraUtils.chooseOptimalSize(Size.fromArray2(map.getOutputSizes(MediaRecorder.class)),
                    mWindowSize.getWidth(), mWindowSize.getHeight(), new Size(mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight));

            if (mVideoSize == null || mVideoSize.getWidth() > mCamcorderProfile.videoFrameWidth
                    || mVideoSize.getHeight() > mCamcorderProfile.videoFrameHeight)
                mVideoSize = CameraUtils.getSizeWithClosestRatio(Size.fromArray2(map.getOutputSizes(MediaRecorder.class)), mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight);
            else if (mVideoSize == null || mVideoSize.getWidth() > mCamcorderProfile.videoFrameWidth
                    || mVideoSize.getHeight() > mCamcorderProfile.videoFrameHeight)
                mVideoSize = CameraUtils.getSizeWithClosestRatio(Size.fromArray2(map.getOutputSizes(MediaRecorder.class)), mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight);

            mPhotoSize = CameraUtils.getPictureSize(Size.fromArray2(map.getOutputSizes(ImageFormat.JPEG)),
                    cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_AUTO
                            ? CameraConfig.MEDIA_QUALITY_HIGHEST : cameraConfigProvider.getMediaQuality());

            mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                    ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(this, mBackgroundHandler);

            if (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED) {

                if (mWindowSize.getHeight() * mWindowSize.getWidth() > mPhotoSize.getWidth() * mPhotoSize.getHeight()) {
                    mPreviewSize = CameraUtils.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mPhotoSize.getWidth(), mPhotoSize.getHeight());
                } else {
                    mPreviewSize = CameraUtils.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mWindowSize.getWidth(), mWindowSize.getHeight());
                }

                if (mPreviewSize == null)
                    mPreviewSize = CameraUtils.chooseOptimalSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mWindowSize.getWidth(), mWindowSize.getHeight(), mPhotoSize);

            } else {
                if (mWindowSize.getHeight() * mWindowSize.getWidth() > mVideoSize.getWidth() * mVideoSize.getHeight()) {
                    mPreviewSize = CameraUtils.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mVideoSize.getWidth(), mVideoSize.getHeight());
                } else {
                    mPreviewSize = CameraUtils.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mWindowSize.getWidth(), mWindowSize.getHeight());
                }

                if (mPreviewSize == null)
                    mPreviewSize = CameraUtils.getSizeWithClosestRatio(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mVideoSize.getWidth(), mVideoSize.getHeight());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while setup camera sizes.", e);
        }
    }

    @Override
    protected boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            mMediaRecorder.setOutputFormat(mCamcorderProfile.fileFormat);
            mMediaRecorder.setVideoFrameRate(mCamcorderProfile.videoFrameRate);
            mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
            mMediaRecorder.setVideoEncodingBitRate(mCamcorderProfile.videoBitRate);
            mMediaRecorder.setVideoEncoder(mCamcorderProfile.videoCodec);

            mMediaRecorder.setAudioEncodingBitRate(mCamcorderProfile.audioBitRate);
            mMediaRecorder.setAudioChannels(mCamcorderProfile.audioChannels);
            mMediaRecorder.setAudioSamplingRate(mCamcorderProfile.audioSampleRate);
            mMediaRecorder.setAudioEncoder(mCamcorderProfile.audioCodec);

            File outputFile = mOutputPath;
            String outputFilePath = outputFile.toString();
            mMediaRecorder.setOutputFile(outputFilePath);

            if (cameraConfigProvider.getVideoFileSize() > 0) {
                mMediaRecorder.setMaxFileSize(cameraConfigProvider.getVideoFileSize());
                mMediaRecorder.setOnInfoListener(this);
            }
            if (cameraConfigProvider.getVideoDuration() > 0) {
                mMediaRecorder.setMaxDuration(cameraConfigProvider.getVideoDuration());
                mMediaRecorder.setOnInfoListener(this);
            }
            mMediaRecorder.setOrientationHint(getVideoOrientation(cameraConfigProvider.getSensorPosition()));

            mMediaRecorder.prepare();

            return true;
        } catch (IllegalStateException error) {
            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + error.getMessage());
        } catch (IOException error) {
            Log.e(TAG, "IOException preparing MediaRecorder: " + error.getMessage());
        } catch (Throwable error) {
            Log.e(TAG, "Error during preparing MediaRecorder: " + error.getMessage());
        }

        releaseVideoRecorder();
        return false;
    }

    private void updatePreview(CameraCaptureSession cameraCaptureSession) {
        if (null == mCameraDevice) {
            return;
        }
        mCaptureSession = cameraCaptureSession;

        setFlashModeAndBuildPreviewRequest(cameraConfigProvider.getFlashMode());
    }

    private void closePreviewSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            try {
                mCaptureSession.abortCaptures();
            } catch (Exception ignore) {
            } finally {
                mCaptureSession = null;
            }
        }
    }

    private void lockFocus() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            mPreviewState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler);
        } catch (Exception ignore) {
        }
    }

    private void processCaptureResult(CaptureResult result) {
        switch (mPreviewState) {
            case STATE_PREVIEW: {
                break;
            }
            case STATE_WAITING_LOCK: {
                final Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                if (afState == null) {
                    captureStillPicture();
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                        || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        mPreviewState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    } else {
                        runPreCaptureSequence();
                    }
                }
                break;
            }
            case STATE_WAITING_PRE_CAPTURE: {
                final Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    mPreviewState = STATE_WAITING_NON_PRE_CAPTURE;
                }
                break;
            }
            case STATE_WAITING_NON_PRE_CAPTURE: {
                final Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    mPreviewState = STATE_PICTURE_TAKEN;
                    captureStillPicture();
                }
                break;
            }
            case STATE_PICTURE_TAKEN:
                break;
        }
    }

    private void runPreCaptureSequence() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mPreviewState = STATE_WAITING_PRE_CAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
        }
    }

    private void captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return;
            }
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getPhotoOrientation(cameraConfigProvider.getSensorPosition()));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.d(TAG, "onCaptureCompleted: ");
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Error during capturing picture");
        }
    }

    private void unlockFocus() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler);
            mPreviewState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, captureCallback, mBackgroundHandler);
        } catch (Exception e) {
            Log.e(TAG, "Error during focus unlocking");
        }
    }

    private void setFlashModeAndBuildPreviewRequest(@CameraConfig.FlashMode int flashMode) {
        try {

            switch (flashMode) {
                case CameraConfig.FLASH_MODE_AUTO:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    break;
                case CameraConfig.FLASH_MODE_ON:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    break;
                case CameraConfig.FLASH_MODE_OFF:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    break;
                default:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    break;
            }

            mPreviewRequest = mPreviewRequestBuilder.build();

            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequest, captureCallback, mBackgroundHandler);
            } catch (Exception e) {
                Log.e(TAG, "Error updating preview: ", e);
            }
        } catch (Exception ignore) {
            Log.e(TAG, "Error setting flash: ", ignore);
        }
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        final File outputFile = mOutputPath;
        mBackgroundHandler.post(new ImageSaver(imageReader.acquireNextImage(), outputFile, new ImageSaver.ImageSaverCallback() {
            @Override
            public void onSuccessFinish(final byte[] bytes) {
                Log.d(TAG, "onPhotoSuccessFinish: ");
                if (mCameraPictureListener != null) {
                    mUiiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCameraPictureListener.onPictureTaken(bytes, mOutputPath, mOnCameraResultListener);
                            mOnCameraResultListener = null;
                        }
                    });
                }
                unlockFocus();
            }

            @Override
            public void onError() {
                Log.d(TAG, "onPhotoError: ");
                mUiiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCameraPictureListener.onPictureTakeError();
                    }
                });
            }
        }));
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (surfaceTexture != null) startPreview(surfaceTexture);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (surfaceTexture != null) startPreview(surfaceTexture);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        final List<CharSequence> videoQualities = new ArrayList<>();

        if (cameraConfigProvider.getMinimumVideoDuration() > 0)
            videoQualities.add(new VideoQualityOption(CameraConfig.MEDIA_QUALITY_AUTO, CameraUtils.getCamcorderProfile(CameraConfig.MEDIA_QUALITY_AUTO, getCameraId()), cameraConfigProvider.getMinimumVideoDuration()));


        CamcorderProfile camcorderProfile = CameraUtils.getCamcorderProfile(CameraConfig.MEDIA_QUALITY_HIGH, mCameraId);
        double videoDuration = CameraUtils.calculateApproximateVideoDuration(camcorderProfile, cameraConfigProvider.getVideoFileSize());
        videoQualities.add(new VideoQualityOption(CameraConfig.MEDIA_QUALITY_HIGH, camcorderProfile, videoDuration));

        camcorderProfile = CameraUtils.getCamcorderProfile(CameraConfig.MEDIA_QUALITY_MEDIUM, mCameraId);
        videoDuration = CameraUtils.calculateApproximateVideoDuration(camcorderProfile, cameraConfigProvider.getVideoFileSize());
        videoQualities.add(new VideoQualityOption(CameraConfig.MEDIA_QUALITY_MEDIUM, camcorderProfile, videoDuration));

        camcorderProfile = CameraUtils.getCamcorderProfile(CameraConfig.MEDIA_QUALITY_LOW, mCameraId);
        videoDuration = CameraUtils.calculateApproximateVideoDuration(camcorderProfile, cameraConfigProvider.getVideoFileSize());
        videoQualities.add(new VideoQualityOption(CameraConfig.MEDIA_QUALITY_LOW, camcorderProfile, videoDuration));

        CharSequence[] array = new CharSequence[videoQualities.size()];
        videoQualities.toArray(array);

        return array;
    }

    @Override
    public CharSequence[] getPictureQualityOptions() {
        final List<CharSequence> photoQualities = new ArrayList<>();
        photoQualities.add(new PictureQualityOption(CameraConfig.MEDIA_QUALITY_HIGHEST, getPictureSizeForQuality(CameraConfig.MEDIA_QUALITY_HIGHEST)));
        photoQualities.add(new PictureQualityOption(CameraConfig.MEDIA_QUALITY_HIGH, getPictureSizeForQuality(CameraConfig.MEDIA_QUALITY_HIGH)));
        photoQualities.add(new PictureQualityOption(CameraConfig.MEDIA_QUALITY_MEDIUM, getPictureSizeForQuality(CameraConfig.MEDIA_QUALITY_MEDIUM)));
        photoQualities.add(new PictureQualityOption(CameraConfig.MEDIA_QUALITY_LOWEST, getPictureSizeForQuality(CameraConfig.MEDIA_QUALITY_LOWEST)));

        final CharSequence[] array = new CharSequence[photoQualities.size()];
        photoQualities.toArray(array);

        return array;
    }

    @IntDef({STATE_PREVIEW, STATE_WAITING_LOCK, STATE_WAITING_PRE_CAPTURE, STATE_WAITING_NON_PRE_CAPTURE, STATE_PICTURE_TAKEN})
    @Retention(RetentionPolicy.SOURCE)
    @interface CameraPreviewState {
    }

}
