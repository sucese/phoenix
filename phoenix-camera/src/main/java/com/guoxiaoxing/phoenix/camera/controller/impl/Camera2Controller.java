package com.guoxiaoxing.phoenix.camera.controller.impl;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;

import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.config.CameraConfigProvider;
import com.guoxiaoxing.phoenix.camera.controller.CameraController;
import com.guoxiaoxing.phoenix.camera.controller.view.CameraView;
import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.manager.CameraManager;
import com.guoxiaoxing.phoenix.camera.manager.impl.Camera2Manager;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraCloseListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraOpenListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraPictureListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraVideoListener;
import com.guoxiaoxing.phoenix.camera.util.CameraHelper;
import com.guoxiaoxing.phoenix.camera.util.Size;
import com.guoxiaoxing.phoenix.camera.widget.AutoFitTextureView;

import java.io.File;

/**
 * The camera controller for camera2
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Controller implements CameraController<String>, CameraOpenListener<String, TextureView.SurfaceTextureListener>
        , CameraPictureListener, CameraVideoListener, CameraCloseListener<String> {

    private final static String TAG = "Camera2Controller";

    private final Context mContext;
    private String mCameraId;
    private File mOutputFile;
    private CameraView mCameraView;
    private CameraConfigProvider mCameraConfigProvider;
    private CameraManager<String, TextureView.SurfaceTextureListener> mCamera2Manager;

    public Camera2Controller(Context context, CameraView cameraView, CameraConfigProvider cameraConfigProvider) {
        this.mContext = context;
        this.mCameraView = cameraView;
        this.mCameraConfigProvider = cameraConfigProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCamera2Manager = new Camera2Manager();
        mCamera2Manager.initializeCameraManager(mCameraConfigProvider, mContext);
        setCurrentCameraId(mCamera2Manager.getFaceBackCameraId());
    }

    @Override
    public void onResume() {
        mCamera2Manager.openCamera(mCameraId, this);
    }

    @Override
    public void onPause() {
        mCamera2Manager.closeCamera(null);
        mCameraView.releaseCameraPreview();
    }

    @Override
    public void onDestroy() {
        mCamera2Manager.releaseCameraManager();
    }

    @Override
    public void takePhoto(CameraResultListener callback) {
        takePhoto(callback, null, null);
    }

    @Override
    public void takePhoto(CameraResultListener callback, @Nullable String direcoryPath, @Nullable String fileName) {
        mOutputFile = CameraHelper.getOutputMediaFile(mContext, CameraConfig.MEDIA_ACTION_PHOTO, direcoryPath, fileName);
        mCamera2Manager.takePicture(mOutputFile, this, callback);
    }

    @Override
    public void startVideoRecord() {
        startVideoRecord(null, null);
    }

    @Override
    public void startVideoRecord(@Nullable String direcoryPath, @Nullable String fileName) {
        mOutputFile = CameraHelper.getOutputMediaFile(mContext, CameraConfig.MEDIA_ACTION_VIDEO, direcoryPath, fileName);
        mCamera2Manager.startVideoRecord(mOutputFile, this);
    }

    @Override
    public void stopVideoRecord(CameraResultListener callback) {
        mCamera2Manager.stopVideoRecord(callback);
    }

    @Override
    public boolean isVideoRecording() {
        return mCamera2Manager.isVideoRecording();
    }

    @Override
    public void switchCamera(final @CameraConfig.CameraFace int cameraFace) {
        final String currentCameraId = mCamera2Manager.getCurrentCameraId();
        final String faceFrontCameraId = mCamera2Manager.getFaceFrontCameraId();
        final String faceBackCameraId = mCamera2Manager.getFaceBackCameraId();

        if (cameraFace == CameraConfig.CAMERA_FACE_REAR && faceBackCameraId != null) {
            setCurrentCameraId(faceBackCameraId);
            mCamera2Manager.closeCamera(this);
        } else if (faceFrontCameraId != null) {
            setCurrentCameraId(faceFrontCameraId);
            mCamera2Manager.closeCamera(this);
        }

    }

    private void setCurrentCameraId(String currentCameraId) {
        this.mCameraId = currentCameraId;
        mCamera2Manager.setCameraId(currentCameraId);
    }

    @Override
    public void setFlashMode(@CameraConfig.FlashMode int flashMode) {
        mCamera2Manager.setFlashMode(flashMode);
    }

    @Override
    public void switchQuality() {
        mCamera2Manager.closeCamera(this);
    }

    @Override
    public int getNumberOfCameras() {
        return mCamera2Manager.getNumberOfCameras();
    }

    @Override
    public int getMediaAction() {
        return mCameraConfigProvider.getMediaAction();
    }

    @Override
    public File getOutputFile() {
        return mOutputFile;
    }

    @Override
    public String getCameraId() {
        return mCameraId;
    }

    @Override
    public void onCameraOpened(String openedCameraId, Size previewSize, TextureView.SurfaceTextureListener surfaceTextureListener) {
        mCameraView.updateUiForMediaAction(CameraConfig.MEDIA_ACTION_UNSPECIFIED);
        mCameraView.updateCameraPreview(previewSize, new AutoFitTextureView(mContext, surfaceTextureListener));
        mCameraView.updateCameraSwitcher(mCamera2Manager.getNumberOfCameras());
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(String closedCameraId) {
        mCameraView.releaseCameraPreview();

        mCamera2Manager.openCamera(mCameraId, this);
    }

    @Override
    public void onPictureTaken(byte[] bytes, File photoFile, CameraResultListener callback) {
        mCameraView.onPictureTaken(bytes, callback);
    }

    @Override
    public void onPictureTakeError() {
    }

    @Override
    public void onVideoRecordStarted(Size videoSize) {
        mCameraView.onVideoRecordStart(videoSize.getWidth(), videoSize.getHeight());
    }

    @Override
    public void onVideoRecordStopped(File videoFile, @Nullable CameraResultListener callback) {
        mCameraView.onVideoRecordStop(callback);
    }

    @Override
    public void onVideoRecordError() {

    }

    @Override
    public CameraManager getCameraManager() {
        return mCamera2Manager;
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        return mCamera2Manager.getVideoQualityOptions();
    }

    @Override
    public CharSequence[] getPhotoQualityOptions() {
        return mCamera2Manager.getPictureQualityOptions();
    }
}
