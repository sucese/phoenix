package com.guoxiaoxing.phoenix.camera.controller.impl;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;

import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.config.CameraConfigProvider;
import com.guoxiaoxing.phoenix.camera.controller.CameraController;
import com.guoxiaoxing.phoenix.camera.controller.view.CameraView;
import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.manager.CameraManager;
import com.guoxiaoxing.phoenix.camera.manager.impl.Camera1Manager;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraCloseListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraOpenListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraPhotoListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraVideoListener;
import com.guoxiaoxing.phoenix.camera.util.CameraHelper;
import com.guoxiaoxing.phoenix.camera.util.Size;
import com.guoxiaoxing.phoenix.camera.widget.AutoFitSurfaceView;

import java.io.File;

/**
 * The camera controller for camera1
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
@SuppressWarnings("deprecation")
public class Camera1Controller implements CameraController<Integer>, CameraOpenListener<Integer, SurfaceHolder.Callback>
        , CameraPhotoListener, CameraCloseListener<Integer>, CameraVideoListener {

    private final static String TAG = "Camera1Controller";

    private final Context context;

    private Integer currentCameraId;
    private CameraConfigProvider cameraConfigProvider;
    private CameraManager<Integer, SurfaceHolder.Callback> cameraManager;
    private CameraView cameraView;

    private File outputFile;

    public Camera1Controller(Context context, CameraView cameraView, CameraConfigProvider cameraConfigProvider) {
        this.context = context;
        this.cameraView = cameraView;
        this.cameraConfigProvider = cameraConfigProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        cameraManager = new Camera1Manager();
        cameraManager.initializeCameraManager(cameraConfigProvider, context);
        setCurrentCameraId(cameraManager.getFaceBackCameraId());
    }

    private void setCurrentCameraId(Integer cameraId) {
        this.currentCameraId = cameraId;
        cameraManager.setCameraId(cameraId);
    }

    @Override
    public void onResume() {
        cameraManager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPause() {
        cameraManager.closeCamera(null);
    }

    @Override
    public void onDestroy() {
        cameraManager.releaseCameraManager();
    }

    @Override
    public void takePhoto(CameraResultListener callback) {
        takePhoto(callback, null, null);
    }

    @Override
    public void takePhoto(CameraResultListener callback, @Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, CameraConfig.MEDIA_ACTION_PHOTO, direcoryPath, fileName);
        cameraManager.takePicture(outputFile, this, callback);
    }

    @Override
    public void startVideoRecord() {
        startVideoRecord(null, null);
    }

    @Override
    public void startVideoRecord(@Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, CameraConfig.MEDIA_ACTION_VIDEO, direcoryPath, fileName);
        cameraManager.startVideoRecord(outputFile, this);
    }

    @Override
    public void stopVideoRecord(CameraResultListener callback) {
        cameraManager.stopVideoRecord(callback);
    }

    @Override
    public boolean isVideoRecording() {
        return cameraManager.isVideoRecording();
    }

    @Override
    public void switchCamera(@CameraConfig.CameraFace final int cameraFace) {
        final Integer backCameraId = cameraManager.getFaceBackCameraId();
        final Integer frontCameraId = cameraManager.getFaceFrontCameraId();
        final Integer currentCameraId = cameraManager.getCurrentCameraId();

        if (cameraFace == CameraConfig.CAMERA_FACE_REAR && backCameraId != null) {
            setCurrentCameraId(backCameraId);
            cameraManager.closeCamera(this);
        } else if (frontCameraId != null && !frontCameraId.equals(currentCameraId)) {
            setCurrentCameraId(frontCameraId);
            cameraManager.closeCamera(this);
        }
    }

    @Override
    public void setFlashMode(@CameraConfig.FlashMode int flashMode) {
        cameraManager.setFlashMode(flashMode);
    }

    @Override
    public void switchQuality() {
        cameraManager.closeCamera(this);
    }

    @Override
    public int getNumberOfCameras() {
        return cameraManager.getNumberOfCameras();
    }

    @Override
    public int getMediaAction() {
        return cameraConfigProvider.getMediaAction();
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public Integer getCurrentCameraId() {
        return currentCameraId;
    }

    @Override
    public void onCameraOpened(Integer cameraId, Size previewSize, SurfaceHolder.Callback surfaceCallback) {
        cameraView.updateUiForMediaAction(cameraConfigProvider.getMediaAction());
        cameraView.updateCameraPreview(previewSize, new AutoFitSurfaceView(context, surfaceCallback));
        cameraView.updateCameraSwitcher(getNumberOfCameras());
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(Integer closedCameraId) {
        cameraView.releaseCameraPreview();

        cameraManager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPhotoTaken(byte[] bytes, File photoFile, CameraResultListener callback) {
        cameraView.onPhotoTaken(bytes, callback);
    }

    @Override
    public void onPhotoTakeError() {
    }

    @Override
    public void onVideoRecordStarted(Size videoSize) {
        cameraView.onVideoRecordStart(videoSize.getWidth(), videoSize.getHeight());
    }

    @Override
    public void onVideoRecordStopped(File videoFile, @Nullable CameraResultListener callback) {
        cameraView.onVideoRecordStop(callback);
    }

    @Override
    public void onVideoRecordError() {

    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        return cameraManager.getVideoQualityOptions();
    }

    @Override
    public CharSequence[] getPhotoQualityOptions() {
        return cameraManager.getPictureQualityOptions();
    }
}
