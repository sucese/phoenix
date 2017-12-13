package com.guoxiaoxing.phoenix.camera.manager;

import android.content.Context;

import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.config.CameraConfigProvider;
import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraCloseListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraOpenListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraPhotoListener;
import com.guoxiaoxing.phoenix.camera.manager.listener.CameraVideoListener;
import com.guoxiaoxing.phoenix.camera.util.Size;

import java.io.File;

/**
 * The camera manager for manage camera resource
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
public interface CameraManager<CameraId, SurfaceListener> {

    /**
     * 初始化相机
     *
     * @param cameraConfigProvider cameraConfigProvider
     * @param context              context
     */
    void initializeCameraManager(CameraConfigProvider cameraConfigProvider, Context context);

    /**
     * 释放相机
     */
    void releaseCameraManager();

    /**
     * 打开相机
     *
     * @param cameraId           cameraId
     * @param cameraOpenListener cameraOpenListener
     */
    void openCamera(CameraId cameraId, CameraOpenListener<CameraId, SurfaceListener> cameraOpenListener);

    /**
     * 关闭相机
     *
     * @param cameraCloseListener cameraCloseListener
     */
    void closeCamera(CameraCloseListener<CameraId> cameraCloseListener);

    /**
     * 设置相机ID
     *
     * @param cameraId cameraId
     */
    void setCameraId(CameraId cameraId);

    /**
     * 设置闪光模式
     *
     * @param flashMode flashMode
     */
    void setFlashMode(@CameraConfig.FlashMode int flashMode);

    /**
     * 获取当前摄像头ID
     */
    CameraId getCurrentCameraId();

    /**
     * 获取前置摄像头ID
     */
    CameraId getFaceFrontCameraId();

    /**
     * 获取后置摄像头ID
     */
    CameraId getFaceBackCameraId();

    /**
     * 获取摄像头个数
     */
    int getNumberOfCameras();

    /**
     * 获取前置摄像头拍摄方向
     */
    int getFaceFrontCameraOrientation();

    /**
     * 获取后置摄像头拍摄方向
     */
    int getFaceBackCameraOrientation();

    /**
     * 从图像质量参数中获取图像大小
     *
     * @param mediaQuality mediaQuality
     */
    Size getPictureSizeForQuality(@CameraConfig.MediaQuality int mediaQuality);

    /**
     * 获取图像质量参数
     */
    CharSequence[] getPictureQualityOptions();


    /**
     * 获取图像质量参数
     */
    CharSequence[] getVideoQualityOptions();

    /**
     * 拍照
     *
     * @param photoFile           photoFile
     * @param cameraPhotoListener cameraPhotoListener
     * @param callback            callback
     */
    void takePicture(File photoFile, CameraPhotoListener cameraPhotoListener, CameraResultListener callback);

    /**
     * 开始视频录制
     *
     * @param videoFile           videoFile
     * @param cameraVideoListener cameraVideoListener
     */
    void startVideoRecord(File videoFile, CameraVideoListener cameraVideoListener);

    /**
     * 结束视频录制
     *
     * @param callback callback
     */
    void stopVideoRecord(CameraResultListener callback);

    /**
     * 视频是否处于录制状态
     */
    boolean isVideoRecording();
}
