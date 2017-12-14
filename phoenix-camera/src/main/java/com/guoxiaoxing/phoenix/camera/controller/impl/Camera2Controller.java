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
public class Camera2Controller implements CameraController<String>,
        CameraOpenListener<String, TextureView.SurfaceTextureListener>,
        CameraPictureListener, CameraVideoListener, CameraCloseListener<String> {

    private final static String TAG = "Camera2Controller";

    private final Context context;
    private String currentCameraId;
    private CameraConfigProvider cameraConfigProvider;
    private CameraManager<String, TextureView.SurfaceTextureListener> camera2Manager;
    private CameraView cameraView;

    private File outputFile;

    public Camera2Controller(Context context, CameraView cameraView, CameraConfigProvider cameraConfigProvider) {
        this.context = context;
        this.cameraView = cameraView;
        this.cameraConfigProvider = cameraConfigProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        camera2Manager = new Camera2Manager();
        camera2Manager.initializeCameraManager(cameraConfigProvider, context);
        setCurrentCameraId(camera2Manager.getFaceBackCameraId());
    }

    @Override
    public void onResume() {
        camera2Manager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPause() {
        camera2Manager.closeCamera(null);
        cameraView.releaseCameraPreview();
    }

    @Override
    public void onDestroy() {
        camera2Manager.releaseCameraManager();
    }

    @Override
    public void takePhoto(CameraResultListener callback) {
        takePhoto(callback, null, null);
    }

    @Override
    public void takePhoto(CameraResultListener callback, @Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, CameraConfig.MEDIA_ACTION_PHOTO, direcoryPath, fileName);
        camera2Manager.takePicture(outputFile, this, callback);
    }

    @Override
    public void startVideoRecord() {
        startVideoRecord(null, null);
    }

    @Override
    public void startVideoRecord(@Nullable String direcoryPath, @Nullable String fileName) {
        outputFile = CameraHelper.getOutputMediaFile(context, CameraConfig.MEDIA_ACTION_VIDEO, direcoryPath, fileName);
        camera2Manager.startVideoRecord(outputFile, this);
    }

    @Override
    public void stopVideoRecord(CameraResultListener callback) {
        camera2Manager.stopVideoRecord(callback);
    }

    @Override
    public boolean isVideoRecording() {
        return camera2Manager.isVideoRecording();
    }

    @Override
    public void switchCamera(final @CameraConfig.CameraFace int cameraFace) {
        final String currentCameraId = camera2Manager.getCurrentCameraId();
        final String faceFrontCameraId = camera2Manager.getFaceFrontCameraId();
        final String faceBackCameraId = camera2Manager.getFaceBackCameraId();

        if (cameraFace == CameraConfig.CAMERA_FACE_REAR && faceBackCameraId != null) {
            setCurrentCameraId(faceBackCameraId);
            camera2Manager.closeCamera(this);
        } else if (faceFrontCameraId != null) {
            setCurrentCameraId(faceFrontCameraId);
            camera2Manager.closeCamera(this);
        }

    }

    private void setCurrentCameraId(String currentCameraId) {
        this.currentCameraId = currentCameraId;
        camera2Manager.setCameraId(currentCameraId);
    }

    @Override
    public void setFlashMode(@CameraConfig.FlashMode int flashMode) {
        camera2Manager.setFlashMode(flashMode);
    }

    @Override
    public void switchQuality() {
        camera2Manager.closeCamera(this);
    }

    @Override
    public int getNumberOfCameras() {
        return camera2Manager.getNumberOfCameras();
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
    public String getCameraId() {
        return currentCameraId;
    }

    @Override
    public void onCameraOpened(String openedCameraId, Size previewSize, TextureView.SurfaceTextureListener surfaceTextureListener) {
        cameraView.updateUiForMediaAction(CameraConfig.MEDIA_ACTION_UNSPECIFIED);
        cameraView.updateCameraPreview(previewSize, new AutoFitTextureView(context, surfaceTextureListener));
        cameraView.updateCameraSwitcher(camera2Manager.getNumberOfCameras());
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(String closedCameraId) {
        cameraView.releaseCameraPreview();

        camera2Manager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPictureTaken(byte[] bytes, File photoFile, CameraResultListener callback) {
        cameraView.onPictureTaken(bytes, callback);
    }

    @Override
    public void onPictureTakeError() {
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
        return camera2Manager;
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        return camera2Manager.getVideoQualityOptions();
    }

    @Override
    public CharSequence[] getPhotoQualityOptions() {
        return camera2Manager.getPictureQualityOptions();
    }
}
