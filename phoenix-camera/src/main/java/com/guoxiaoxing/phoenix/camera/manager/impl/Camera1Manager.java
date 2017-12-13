package com.guoxiaoxing.phoenix.camera.manager.impl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.config.CameraConfigProvider;
import com.guoxiaoxing.phoenix.camera.config.PictureQualityOption;
import com.guoxiaoxing.phoenix.camera.config.VideoQualityOption;
import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraCloseListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraOpenListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraPhotoListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraVideoListener;
import com.guoxiaoxing.phoenix.camera.util.CameraHelper;
import com.guoxiaoxing.phoenix.camera.util.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The camera manager for camera1
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
@SuppressWarnings("deprecation")
public class Camera1Manager extends BaseCameraManager<Integer, SurfaceHolder.Callback> {

    private static final String TAG = "Camera1Manager";

    private Camera camera;
    private Surface surface;

    private int orientation;
    private int displayRotation = 0;

    private File outputPath;
    private CameraVideoListener videoListener;
    private CameraPhotoListener photoListener;

    private Integer futurFlashMode;

    @Override
    public void openCamera(final Integer cameraId,
                           final CameraOpenListener<Integer, SurfaceHolder.Callback> cameraOpenListener) {
        this.currentCameraId = cameraId;
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    camera = Camera.open(cameraId);
                    prepareCameraOutputs();
                    if (futurFlashMode != null) {
                        setFlashMode(futurFlashMode);
                        futurFlashMode = null;
                    }
                    if (cameraOpenListener != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraOpenListener.onCameraOpened(cameraId, previewSize, new SurfaceHolder.Callback() {
                                    @Override
                                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                                        if (surfaceHolder.getSurface() == null) {
                                            return;
                                        }

                                        surface = surfaceHolder.getSurface();

                                        try {
                                            camera.stopPreview();
                                        } catch (Exception ignore) {
                                        }

                                        startPreview(surfaceHolder);
                                    }

                                    @Override
                                    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                                        if (surfaceHolder.getSurface() == null) {
                                            return;
                                        }

                                        surface = surfaceHolder.getSurface();

                                        try {
                                            camera.stopPreview();
                                        } catch (Exception ignore) {
                                        }

                                        startPreview(surfaceHolder);
                                    }

                                    @Override
                                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                                    }
                                });
                            }
                        });
                    }
                } catch (Exception error) {
                    Log.d(TAG, "Can't open camera: " + error.getMessage());
                    if (cameraOpenListener != null) {
                        uiHandler.post(new Runnable() {
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
    public void closeCamera(final CameraCloseListener<Integer> cameraCloseListener) {
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.release();
                    camera = null;
                    if (cameraCloseListener != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraCloseListener.onCameraClosed(currentCameraId);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void setFlashMode(@CameraConfig.FlashMode final int flashMode) {
        if (camera != null) {
            setFlashMode(camera, camera.getParameters(), flashMode);
        } else {
            futurFlashMode = flashMode;
        }
    }

    @Override
    public void takePicture(File photoFile, CameraPhotoListener cameraPhotoListener, final CameraResultListener callback) {
        this.outputPath = photoFile;
        this.photoListener = cameraPhotoListener;
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                setCameraPhotoQuality(camera);
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        Camera1Manager.this.onPictureTaken(bytes, camera, callback);
                    }
                });
            }
        });
    }

    @Override
    public void startVideoRecord(final File videoFile, CameraVideoListener cameraVideoListener) {
        if (isVideoRecording) return;

        this.outputPath = videoFile;
        this.videoListener = cameraVideoListener;

        if (videoListener != null)
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Tzutalin++ 2017/05. If calling release function, it should not be executed
                    if (context == null) return;

                    if (prepareVideoRecorder()) {
                        videoRecorder.start();
                        isVideoRecording = true;
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                videoListener.onVideoRecordStarted(videoSize);
                            }
                        });
                    }
                }
            });
    }

    @Override
    public void stopVideoRecord(@Nullable final CameraResultListener callback) {
        if (isVideoRecording)
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {

                    try {
                        if (videoRecorder != null) videoRecorder.stop();
                    } catch (Exception ignore) {
                        // ignore illegal state.
                        // appear in case time or file size reach limit and stop already called.
                    }

                    isVideoRecording = false;
                    releaseVideoRecorder();

                    if (videoListener != null) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                videoListener.onVideoRecordStopped(outputPath, callback);
                            }
                        });
                    }
                }
            });
    }

    @Override
    public void releaseCameraManager() {
        super.releaseCameraManager();
    }

    @Override
    public void initializeCameraManager(CameraConfigProvider cameraConfigProvider, Context context) {
        super.initializeCameraManager(cameraConfigProvider, context);

        numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; ++i) {
            final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                faceBackCameraId = i;
                faceBackCameraOrientation = cameraInfo.orientation;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                faceFrontCameraId = i;
                faceFrontCameraOrientation = cameraInfo.orientation;
            }
        }
    }

    @Override
    public Size getPictureSizeForQuality(@CameraConfig.MediaQuality int mediaQuality) {
        return CameraHelper.getPictureSize(Size.fromList(camera.getParameters().getSupportedPictureSizes()), mediaQuality);
    }

    @Override
    protected void prepareCameraOutputs() {
        try {
            if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_AUTO) {
                camcorderProfile = CameraHelper.getCamcorderProfile(currentCameraId, cameraConfigProvider.getVideoFileSize(), cameraConfigProvider.getMinimumVideoDuration());
            } else
                camcorderProfile = CameraHelper.getCamcorderProfile(cameraConfigProvider.getMediaQuality(), currentCameraId);

            final List<Size> previewSizes = Size.fromList(camera.getParameters().getSupportedPreviewSizes());
            final List<Size> pictureSizes = Size.fromList(camera.getParameters().getSupportedPictureSizes());
            List<Size> videoSizes;
            if (Build.VERSION.SDK_INT > 10)
                videoSizes = Size.fromList(camera.getParameters().getSupportedVideoSizes());
            else videoSizes = previewSizes;

            videoSize = CameraHelper.getSizeWithClosestRatio(
                    (videoSizes == null || videoSizes.isEmpty()) ? previewSizes : videoSizes,
                    camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);

            photoSize = CameraHelper.getPictureSize(
                    (pictureSizes == null || pictureSizes.isEmpty()) ? previewSizes : pictureSizes,
                    cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_AUTO
                            ? CameraConfig.MEDIA_QUALITY_HIGHEST : cameraConfigProvider.getMediaQuality());

            if (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED) {
                previewSize = CameraHelper.getSizeWithClosestRatio(previewSizes, photoSize.getWidth(), photoSize.getHeight());
            } else {
                previewSize = CameraHelper.getSizeWithClosestRatio(previewSizes, videoSize.getWidth(), videoSize.getHeight());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while setup camera sizes.");
        }
    }

    @Override
    protected boolean prepareVideoRecorder() {
        videoRecorder = new MediaRecorder();
        try {
            camera.lock();
            camera.unlock();
            videoRecorder.setCamera(camera);

            videoRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            videoRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

            videoRecorder.setOutputFormat(camcorderProfile.fileFormat);
            videoRecorder.setVideoFrameRate(camcorderProfile.videoFrameRate);
            videoRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
            videoRecorder.setVideoEncodingBitRate(camcorderProfile.videoBitRate);
            videoRecorder.setVideoEncoder(camcorderProfile.videoCodec);

            videoRecorder.setAudioEncodingBitRate(camcorderProfile.audioBitRate);
            videoRecorder.setAudioChannels(camcorderProfile.audioChannels);
            videoRecorder.setAudioSamplingRate(camcorderProfile.audioSampleRate);
            videoRecorder.setAudioEncoder(camcorderProfile.audioCodec);

            videoRecorder.setOutputFile(outputPath.toString());

            if (cameraConfigProvider.getVideoFileSize() > 0) {
                videoRecorder.setMaxFileSize(cameraConfigProvider.getVideoFileSize());

                videoRecorder.setOnInfoListener(this);
            }
            if (cameraConfigProvider.getVideoDuration() > 0) {
                videoRecorder.setMaxDuration(cameraConfigProvider.getVideoDuration());

                videoRecorder.setOnInfoListener(this);
            }

            videoRecorder.setOrientationHint(getVideoOrientation(cameraConfigProvider.getSensorPosition()));
            videoRecorder.setPreviewDisplay(surface);

            videoRecorder.prepare();

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

    @Override
    protected void onMaxDurationReached() {
        stopVideoRecord(null);
    }

    @Override
    protected void onMaxFileSizeReached() {
        stopVideoRecord(null);
    }

    @Override
    protected void releaseVideoRecorder() {
        super.releaseVideoRecorder();

        try {
            camera.lock(); // lock camera for later use
        } catch (Exception ignore) {
        }
    }

    //------------------------Implementation------------------

    private void startPreview(SurfaceHolder surfaceHolder) {
        try {
            final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(currentCameraId, cameraInfo);
            int cameraRotationOffset = cameraInfo.orientation;

            final Camera.Parameters parameters = camera.getParameters();
            setAutoFocus(camera, parameters);
            setFlashMode(cameraConfigProvider.getFlashMode());

            if (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED)
                turnPhotoCameraFeaturesOn(camera, parameters);
            else if (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO)
                turnVideoCameraFeaturesOn(camera, parameters);

            final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; // Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; // Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;// Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;// Landscape right
            }

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                displayRotation = (cameraRotationOffset + degrees) % 360;
                displayRotation = (360 - displayRotation) % 360; // compensate
            } else {
                displayRotation = (cameraRotationOffset - degrees + 360) % 360;
            }

            this.camera.setDisplayOrientation(displayRotation);

            if (Build.VERSION.SDK_INT > 13
                    && (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_VIDEO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED)) {
//                parameters.setRecordingHint(true);
            }

            if (Build.VERSION.SDK_INT > 14
                    && parameters.isVideoStabilizationSupported()
                    && (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_VIDEO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED)) {
                parameters.setVideoStabilization(true);
            }

            parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
            parameters.setPictureSize(photoSize.getWidth(), photoSize.getHeight());

            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

        } catch (IOException error) {
            Log.d(TAG, "Error setting camera preview: " + error.getMessage());
        } catch (Exception ignore) {
            Log.d(TAG, "Error starting camera preview: " + ignore.getMessage());
        }
    }

    private void turnPhotoCameraFeaturesOn(Camera camera, Camera.Parameters parameters) {
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(parameters);
    }

    private void turnVideoCameraFeaturesOn(Camera camera, Camera.Parameters parameters) {
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(parameters);
    }

    private void setAutoFocus(Camera camera, Camera.Parameters parameters) {
        try {
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.setParameters(parameters);
            }
        } catch (Exception ignore) {
        }
    }

    private void setFlashMode(Camera camera, Camera.Parameters parameters, @CameraConfig.FlashMode int flashMode) {
        try {
            switch (flashMode) {
                case CameraConfig.FLASH_MODE_AUTO:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    break;
                case CameraConfig.FLASH_MODE_ON:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    break;
                case CameraConfig.FLASH_MODE_OFF:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    break;
                default:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    break;
            }
            camera.setParameters(parameters);
        } catch (Exception ignore) {
        }
    }

    private void setCameraPhotoQuality(Camera camera) {
        final Camera.Parameters parameters = camera.getParameters();

        parameters.setPictureFormat(PixelFormat.JPEG);

        if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_LOW) {
            parameters.setJpegQuality(50);
        } else if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_MEDIUM) {
            parameters.setJpegQuality(75);
        } else if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_HIGH) {
            parameters.setJpegQuality(100);
        } else if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_HIGHEST) {
            parameters.setJpegQuality(100);
        }
        parameters.setPictureSize(photoSize.getWidth(), photoSize.getHeight());

        camera.setParameters(parameters);
    }

    @Override
    protected int getPhotoOrientation(@CameraConfig.SensorPosition int sensorPosition) {
        final int rotate;
        if (currentCameraId.equals(faceFrontCameraId)) {
            rotate = (360 + faceFrontCameraOrientation + cameraConfigProvider.getDegrees()) % 360;
        } else {
            rotate = (360 + faceBackCameraOrientation - cameraConfigProvider.getDegrees()) % 360;
        }

        if (rotate == 0) {
            orientation = ExifInterface.ORIENTATION_NORMAL;
        } else if (rotate == 90) {
            orientation = ExifInterface.ORIENTATION_ROTATE_90;
        } else if (rotate == 180) {
            orientation = ExifInterface.ORIENTATION_ROTATE_180;
        } else if (rotate == 270) {
            orientation = ExifInterface.ORIENTATION_ROTATE_270;
        }

        return orientation;

    }

    @Override
    protected int getVideoOrientation(@CameraConfig.SensorPosition int sensorPosition) {
        int degrees = 0;
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
        }

        final int rotate;
        if (currentCameraId.equals(faceFrontCameraId)) {
            rotate = (360 + faceFrontCameraOrientation + degrees) % 360;
        } else {
            rotate = (360 + faceBackCameraOrientation - degrees) % 360;
        }
        return rotate;
    }

    protected void onPictureTaken(final byte[] bytes, Camera camera, final CameraResultListener callback) {
        final File pictureFile = outputPath;
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions.");
            return;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (FileNotFoundException error) {
            Log.e(TAG, "File not found: " + error.getMessage());
        } catch (IOException error) {
            Log.e(TAG, "Error accessing file: " + error.getMessage());
        } catch (Throwable error) {
            Log.e(TAG, "Error saving file: " + error.getMessage());
        }

        try {
            final ExifInterface exif = new ExifInterface(pictureFile.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + getPhotoOrientation(cameraConfigProvider.getSensorPosition()));
            exif.saveAttributes();

            if (photoListener != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        photoListener.onPhotoTaken(bytes, outputPath, callback);
                    }
                });
            }
        } catch (Throwable error) {
            Log.e(TAG, "Can't save exif info: " + error.getMessage());
        }
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        final List<CharSequence> videoQualities = new ArrayList<>();

        if (cameraConfigProvider.getMinimumVideoDuration() > 0)
            videoQualities.add(new VideoQualityOption(CameraConfig.MEDIA_QUALITY_AUTO, CameraHelper.getCamcorderProfile(CameraConfig.MEDIA_QUALITY_AUTO, getCurrentCameraId()), cameraConfigProvider.getMinimumVideoDuration()));

        CamcorderProfile camcorderProfile = CameraHelper.getCamcorderProfile(CameraConfig.MEDIA_QUALITY_HIGH, getCurrentCameraId());
        double videoDuration = CameraHelper.calculateApproximateVideoDuration(camcorderProfile, cameraConfigProvider.getVideoFileSize());
        videoQualities.add(new VideoQualityOption(CameraConfig.MEDIA_QUALITY_HIGH, camcorderProfile, videoDuration));

        camcorderProfile = CameraHelper.getCamcorderProfile(CameraConfig.MEDIA_QUALITY_MEDIUM, getCurrentCameraId());
        videoDuration = CameraHelper.calculateApproximateVideoDuration(camcorderProfile, cameraConfigProvider.getVideoFileSize());
        videoQualities.add(new VideoQualityOption(CameraConfig.MEDIA_QUALITY_MEDIUM, camcorderProfile, videoDuration));

        camcorderProfile = CameraHelper.getCamcorderProfile(CameraConfig.MEDIA_QUALITY_LOW, getCurrentCameraId());
        videoDuration = CameraHelper.calculateApproximateVideoDuration(camcorderProfile, cameraConfigProvider.getVideoFileSize());
        videoQualities.add(new VideoQualityOption(CameraConfig.MEDIA_QUALITY_LOW, camcorderProfile, videoDuration));

        final CharSequence[] array = new CharSequence[videoQualities.size()];
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
}
