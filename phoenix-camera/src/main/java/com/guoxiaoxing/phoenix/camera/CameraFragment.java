package com.guoxiaoxing.phoenix.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.guoxiaoxing.phoenix.camera.common.Camera;
import com.guoxiaoxing.phoenix.camera.common.Flash;
import com.guoxiaoxing.phoenix.camera.common.MediaAction;
import com.guoxiaoxing.phoenix.camera.common.Record;
import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.config.CameraConfigProvider;
import com.guoxiaoxing.phoenix.camera.config.CameraConfigProviderImpl;
import com.guoxiaoxing.phoenix.camera.config.PictureQualityOption;
import com.guoxiaoxing.phoenix.camera.config.VideoQualityOption;
import com.guoxiaoxing.phoenix.camera.controller.CameraController;
import com.guoxiaoxing.phoenix.camera.controller.impl.Camera1Controller;
import com.guoxiaoxing.phoenix.camera.controller.impl.Camera2Controller;
import com.guoxiaoxing.phoenix.camera.controller.view.CameraView;
import com.guoxiaoxing.phoenix.camera.listener.CameraControlListener;
import com.guoxiaoxing.phoenix.camera.listener.CameraResultListener;
import com.guoxiaoxing.phoenix.camera.listener.CameraStateListener;
import com.guoxiaoxing.phoenix.camera.listener.CameraVideoRecordTextListener;
import com.guoxiaoxing.phoenix.camera.util.CameraHelper;
import com.guoxiaoxing.phoenix.camera.util.Size;
import com.guoxiaoxing.phoenix.camera.util.Utils;
import com.guoxiaoxing.phoenix.camera.util.timer.CountdownTask;
import com.guoxiaoxing.phoenix.camera.util.timer.TimerTask;
import com.guoxiaoxing.phoenix.camera.util.timer.TimerTaskBase;
import com.guoxiaoxing.phoenix.camera.widget.AutoFitFrameLayout;

import java.io.File;

/**
 * The camera fragment
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
public class CameraFragment<CameraId> extends Fragment implements CameraFragmentApi {

    public static final String ARG_CONFIGURATION = "cameraConfig";
    public static final int MIN_VERSION_ICECREAM = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;

    private AutoFitFrameLayout previewContainer;

    private SensorManager sensorManager;
    private AlertDialog settingsDialog;
    private CameraController cameraController;
    private CameraConfigProvider cameraConfigProvider;

    @CameraConfig.MediaQuality
    private int newQuality = -1;
    private CameraConfig cameraConfig;

    private CharSequence[] videoQualities;
    private CharSequence[] photoQualities;

    @Flash.FlashMode
    private int currentFlashMode = Flash.FLASH_AUTO;
    @Camera.CameraType
    private int currentCameraType = Camera.CAMERA_TYPE_REAR;
    @MediaAction.MediaActionState
    private int currentMediaActionState = MediaAction.ACTION_PHOTO;
    @Record.RecordState
    private int currentRecordState = Record.TAKE_PHOTO_STATE;

    private String mediaFilePath;
    private FileObserver fileObserver;
    private long maxVideoFileSize = 0;
    private TimerTaskBase countDownTimer;

    private CameraControlListener cameraControlListener;
    private CameraVideoRecordTextListener cameraVideoRecordTextListener;
    private CameraStateListener cameraStateListener;

    private final TimerTaskBase.Callback timerCallBack = new TimerTaskBase.Callback() {
        @Override
        public void setText(String text) {
            if (cameraVideoRecordTextListener != null) {
                cameraVideoRecordTextListener.setRecordDurationText(text);
            }
        }

        @Override
        public void setTextVisible(boolean visible) {
            if (cameraVideoRecordTextListener != null) {
                cameraVideoRecordTextListener.setRecordDurationTextVisible(visible);
            }
        }
    };

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    if (sensorEvent.values[0] < 4 && sensorEvent.values[0] > -4) {
                        if (sensorEvent.values[1] > 0) {
                            // UP
                            cameraConfigProvider.setSensorPosition(CameraConfig.SENSOR_POSITION_UP);
                            cameraConfigProvider.setDegrees(cameraConfigProvider.getDeviceDefaultOrientation() == CameraConfig.ORIENTATION_PORTRAIT ? 0 : 90);
                        } else if (sensorEvent.values[1] < 0) {
                            // UP SIDE DOWN
                            cameraConfigProvider.setSensorPosition(CameraConfig.SENSOR_POSITION_UP_SIDE_DOWN);
                            cameraConfigProvider.setDegrees(cameraConfigProvider.getDeviceDefaultOrientation() == CameraConfig.ORIENTATION_PORTRAIT ? 180 : 270);
                        }
                    } else if (sensorEvent.values[1] < 4 && sensorEvent.values[1] > -4) {
                        if (sensorEvent.values[0] > 0) {
                            // LEFT
                            cameraConfigProvider.setSensorPosition(CameraConfig.SENSOR_POSITION_LEFT);
                            cameraConfigProvider.setDegrees(cameraConfigProvider.getDeviceDefaultOrientation() == CameraConfig.ORIENTATION_PORTRAIT ? 90 : 180);
                        } else if (sensorEvent.values[0] < 0) {
                            // RIGHT
                            cameraConfigProvider.setSensorPosition(CameraConfig.SENSOR_POSITION_RIGHT);
                            cameraConfigProvider.setDegrees(cameraConfigProvider.getDeviceDefaultOrientation() == CameraConfig.ORIENTATION_PORTRAIT ? 270 : 0);
                        }
                    }
                    onScreenRotation(cameraConfigProvider.getDegrees());
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    private CameraResultListener cameraResultListener;

    protected static CameraFragment newInstance(CameraConfig cameraConfig) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONFIGURATION, cameraConfig);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View decorView = ((Activity) container.getContext()).getWindow().getDecorView();
        if (Build.VERSION.SDK_INT > MIN_VERSION_ICECREAM) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        return inflater.inflate(R.layout.phoenix_fragment_camera, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            cameraConfig = (CameraConfig) arguments.getSerializable(ARG_CONFIGURATION);
        }
        this.cameraConfigProvider = new CameraConfigProviderImpl();
        this.cameraConfigProvider.setupWithAnnaConfiguration(cameraConfig);

        this.sensorManager = (SensorManager) getContext().getSystemService(Activity.SENSOR_SERVICE);

        final CameraView cameraView = new CameraView() {

            @Override
            public void updateCameraPreview(Size size, View cameraPreview) {
                if (cameraControlListener != null) {
                    cameraControlListener.unLockControls();
                    cameraControlListener.allowRecord(true);
                }

                setCameraPreview(cameraPreview, size);
            }

            @Override
            public void updateUiForMediaAction(@CameraConfig.MediaAction int mediaAction) {

            }

            @Override
            public void updateCameraSwitcher(int numberOfCameras) {
                if (cameraControlListener != null) {
                    cameraControlListener.allowCameraSwitching(numberOfCameras > 1);
                }
            }

            @Override
            public void onPhotoTaken(byte[] bytes, CameraResultListener callback) {
                final String filePath = cameraController.getOutputFile().toString();
                if (cameraResultListener != null) {
                    cameraResultListener.onPhotoTaken(bytes, filePath);
                }
                if (callback != null) {
                    callback.onPhotoTaken(bytes, filePath);
                }
            }

            @Override
            public void onVideoRecordStart(int width, int height) {
                final File outputFile = cameraController.getOutputFile();
                onStartVideoRecord(outputFile);
            }

            @Override
            public void onVideoRecordStop(@Nullable CameraResultListener callback) {
                //CameraFragment.this.onStopVideoRecord(callback);
            }

            @Override
            public void releaseCameraPreview() {
                clearCameraPreview();
            }
        };

        if (CameraHelper.hasCamera2(getContext())) {
            cameraController = new Camera2Controller(getContext(), cameraView, cameraConfigProvider);
        } else {
            cameraController = new Camera1Controller(getContext(), cameraView, cameraConfigProvider);
        }
        cameraController.onCreate(savedInstanceState);

        //onProcessBundle
        currentMediaActionState = cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_VIDEO ?
                MediaAction.ACTION_VIDEO : MediaAction.ACTION_PHOTO;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previewContainer = (AutoFitFrameLayout) view.findViewById(R.id.previewContainer);

        final int defaultOrientation = Utils.getDeviceDefaultOrientation(getContext());
        switch (defaultOrientation) {
            case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                cameraConfigProvider.setDeviceDefaultOrientation(CameraConfig.ORIENTATION_LANDSCAPE);
                break;
            default:
                cameraConfigProvider.setDeviceDefaultOrientation(CameraConfig.ORIENTATION_PORTRAIT);
                break;
        }

        switch (cameraConfigProvider.getFlashMode()) {
            case CameraConfig.FLASH_MODE_AUTO:
                setFlashMode(Flash.FLASH_AUTO);
                break;
            case CameraConfig.FLASH_MODE_ON:
                setFlashMode(Flash.FLASH_ON);
                break;
            case CameraConfig.FLASH_MODE_OFF:
                setFlashMode(Flash.FLASH_OFF);
                break;
        }

        if (cameraControlListener != null) {
            setMaxVideoDuration(cameraConfigProvider.getVideoDuration());
            setMaxVideoFileSize(cameraConfigProvider.getVideoFileSize());
        }

        setCameraTypeFrontBack(cameraConfigProvider.getCameraFace());

        notifyListeners();

    }

    public void notifyListeners() {
        onFlashModeChanged();
        onActionPhotoVideoChanged();
        onCameraTypeFrontBackChanged();
    }

    @Override
    public void takePhotoOrCaptureVideo(final CameraResultListener resultListener, @Nullable String directoryPath, @Nullable String fileName) {
        switch (currentMediaActionState) {
            case MediaAction.ACTION_PHOTO:
                takePhoto(resultListener, directoryPath, fileName);
                break;
            case MediaAction.ACTION_VIDEO:
                switch (currentRecordState) {
                    case Record.RECORD_IN_PROGRESS_STATE:
                        stopRecording(resultListener);
                        break;
                    default:
                        startRecording(directoryPath, fileName);
                        break;
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        cameraController.onResume();
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        if (cameraControlListener != null) {
            cameraControlListener.lockControls();
            cameraControlListener.allowRecord(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        cameraController.onPause();
        sensorManager.unregisterListener(sensorEventListener);

        if (cameraControlListener != null) {
            cameraControlListener.lockControls();
            cameraControlListener.allowRecord(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cameraController.onDestroy();
    }

    protected void setMaxVideoFileSize(long maxVideoFileSize) {
        this.maxVideoFileSize = maxVideoFileSize;
    }

    protected void setMaxVideoDuration(int maxVideoDurationInMillis) {
        if (maxVideoDurationInMillis > 0) {
            this.countDownTimer = new CountdownTask(timerCallBack, maxVideoDurationInMillis);
        } else {
            this.countDownTimer = new TimerTask(timerCallBack);
        }
    }

    @Override
    public void openSettingDialog() {
        final Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (currentMediaActionState == MediaAction.ACTION_VIDEO) {
            builder.setSingleChoiceItems(videoQualities, getVideoOptionCheckedIndex(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index) {
                    newQuality = ((VideoQualityOption) videoQualities[index]).getMediaQuality();
                }
            });
            if (cameraConfigProvider.getVideoFileSize() > 0)
                builder.setTitle(String.format(getString(R.string.settings_video_quality_title),
                        "(Max " + String.valueOf(cameraConfigProvider.getVideoFileSize() / (1024 * 1024) + " MB)")));
            else
                builder.setTitle(String.format(getString(R.string.settings_video_quality_title), ""));
        } else {
            builder.setSingleChoiceItems(photoQualities, getPhotoOptionCheckedIndex(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index) {
                    newQuality = ((PictureQualityOption) photoQualities[index]).getMediaQuality();
                }
            });
            builder.setTitle(R.string.settings_photo_quality_title);
        }

        builder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (newQuality > 0 && newQuality != cameraConfigProvider.getMediaQuality()) {
                    cameraConfigProvider.setMediaQuality(newQuality);
                    dialogInterface.dismiss();
                    if (cameraControlListener != null) {
                        cameraControlListener.lockControls();
                    }
                    cameraController.switchQuality();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        settingsDialog = builder.create();
        settingsDialog.show();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(settingsDialog.getWindow().getAttributes());
        layoutParams.width = Utils.convertDipToPixels(context, 350);
        layoutParams.height = Utils.convertDipToPixels(context, 350);
        settingsDialog.getWindow().setAttributes(layoutParams);
    }

    @Override
    public void switchCameraTypeFrontBack() {
        if (cameraControlListener != null) {
            cameraControlListener.lockControls();
            cameraControlListener.allowRecord(false);
        }

        int cameraFace = CameraConfig.CAMERA_FACE_REAR;
        switch (currentCameraType) {
            case Camera.CAMERA_TYPE_FRONT:
                currentCameraType = Camera.CAMERA_TYPE_REAR;
                cameraFace = CameraConfig.CAMERA_FACE_REAR;
                break;
            case Camera.CAMERA_TYPE_REAR:
                currentCameraType = Camera.CAMERA_TYPE_FRONT;
                cameraFace = CameraConfig.CAMERA_FACE_FRONT;
                break;
        }

        onCameraTypeFrontBackChanged();
        this.cameraController.switchCamera(cameraFace);

        if (cameraControlListener != null) {
            cameraControlListener.unLockControls();
        }
    }

    protected void setCameraTypeFrontBack(@CameraConfig.CameraFace int cameraFace) {
        switch (cameraFace) {
            case CameraConfig.CAMERA_FACE_FRONT:
                currentCameraType = Camera.CAMERA_TYPE_FRONT;
                cameraFace = CameraConfig.CAMERA_FACE_FRONT;
                break;
            case CameraConfig.CAMERA_FACE_REAR:
                currentCameraType = Camera.CAMERA_TYPE_REAR;
                cameraFace = CameraConfig.CAMERA_FACE_REAR;
                break;
        }
        onCameraTypeFrontBackChanged();
        this.cameraController.switchCamera(cameraFace);

    }

    protected void onCameraTypeFrontBackChanged() {
        if (cameraStateListener != null) {
            switch (currentCameraType) {
                case Camera.CAMERA_TYPE_REAR:
                    cameraStateListener.onCurrentCameraBack();
                    break;
                case Camera.CAMERA_TYPE_FRONT:
                    cameraStateListener.onCurrentCameraFront();
                    break;
            }
        }
    }

    @Override
    public void switchActionPhotoVideo() {
        switch (currentMediaActionState) {
            case MediaAction.ACTION_PHOTO:
                currentMediaActionState = MediaAction.ACTION_VIDEO;
                break;
            case MediaAction.ACTION_VIDEO:
                currentMediaActionState = MediaAction.ACTION_PHOTO;
                break;
        }
        onActionPhotoVideoChanged();
    }

    protected void onActionPhotoVideoChanged() {
        if (cameraStateListener != null) {
            switch (currentMediaActionState) {
                case MediaAction.ACTION_VIDEO:
                    cameraStateListener.onCameraSetupForVideo();
                    break;
                case MediaAction.ACTION_PHOTO:
                    cameraStateListener.onCameraSetupForPhoto();
                    break;
            }
        }
    }

    @Override
    public void toggleFlashMode() {
        switch (currentFlashMode) {
            case Flash.FLASH_AUTO:
                currentFlashMode = Flash.FLASH_OFF;
                break;
            case Flash.FLASH_OFF:
                currentFlashMode = Flash.FLASH_ON;
                break;
            case Flash.FLASH_ON:
                currentFlashMode = Flash.FLASH_AUTO;
                break;
        }
        onFlashModeChanged();
    }

    private void onFlashModeChanged() {
        switch (currentFlashMode) {
            case Flash.FLASH_AUTO:
                if (cameraStateListener != null) cameraStateListener.onFlashAuto();
                cameraConfigProvider.setFlashMode(CameraConfig.FLASH_MODE_AUTO);
                this.cameraController.setFlashMode(CameraConfig.FLASH_MODE_AUTO);
                break;
            case Flash.FLASH_ON:
                if (cameraStateListener != null) cameraStateListener.onFlashOn();
                cameraConfigProvider.setFlashMode(CameraConfig.FLASH_MODE_ON);
                this.cameraController.setFlashMode(CameraConfig.FLASH_MODE_ON);
                break;
            case Flash.FLASH_OFF:
                if (cameraStateListener != null) cameraStateListener.onFlashOff();
                cameraConfigProvider.setFlashMode(CameraConfig.FLASH_MODE_OFF);
                this.cameraController.setFlashMode(CameraConfig.FLASH_MODE_OFF);
                break;
        }
    }

    protected void onScreenRotation(int degrees) {
        if (cameraStateListener != null) {
            cameraStateListener.shouldRotateControls(degrees);
        }
        rotateSettingsDialog(degrees);
    }

    protected void setRecordState(@Record.RecordState int recordState) {
        this.currentRecordState = recordState;
    }


    //@Override
    //public void onActivityResult(int requestCode, int resultCode, Intent data) {
    //    if (resultCode == Activity.RESULT_OK) {
    //        if (requestCode == REQUEST_PREVIEW_CODE) {
    //            final FragmentActivity activity = getActivity();
    //            if (activity != null) {
    //                if (PreviewActivity.isResultConfirm(data)) {
    //                    Intent resultIntent = new Intent();
    //                    resultIntent.putExtra(CameraConfig.Arguments.FILE_PATH,
    //                            PreviewActivity.getMediaFilePatch(data));
    //                    activity.setResult(Activity.RESULT_OK, resultIntent);
    //                    activity.finish();
    //                } else if (PreviewActivity.isResultCancel(data)) {
    //                    activity.setResult(Activity.RESULT_CANCELED);
    //                    activity.finish();
    //                } else if (PreviewActivity.isResultRetake(data)) {
    //                    //ignore, just proceed the camera
    //                }
    //            }
    //        }
    //    }
    //}

    protected void setFlashMode(@Flash.FlashMode int mode) {
        this.currentFlashMode = mode;
        onFlashModeChanged();
    }

    protected void rotateSettingsDialog(int degrees) {
        if (settingsDialog != null && settingsDialog.isShowing() && Build.VERSION.SDK_INT > 10) {
            ViewGroup dialogView = (ViewGroup) settingsDialog.getWindow().getDecorView();
            for (int i = 0; i < dialogView.getChildCount(); i++) {
                dialogView.getChildAt(i).setRotation(degrees);
            }
        }
    }

    protected int getVideoOptionCheckedIndex() {
        int checkedIndex = -1;

        final int mediaQuality = cameraConfigProvider.getMediaQuality();
        final int passedMediaQuality = cameraConfigProvider.getPassedMediaQuality();

        if (mediaQuality == CameraConfig.MEDIA_QUALITY_AUTO) checkedIndex = 0;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_HIGH) checkedIndex = 1;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_MEDIUM) checkedIndex = 2;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_LOW) checkedIndex = 3;

        if (passedMediaQuality != CameraConfig.MEDIA_QUALITY_AUTO) checkedIndex--;

        return checkedIndex;
    }

    protected int getPhotoOptionCheckedIndex() {
        int checkedIndex = -1;

        final int mediaQuality = cameraConfigProvider.getMediaQuality();

        if (mediaQuality == CameraConfig.MEDIA_QUALITY_HIGHEST) checkedIndex = 0;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_HIGH) checkedIndex = 1;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_MEDIUM) checkedIndex = 2;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_LOWEST) checkedIndex = 3;
        return checkedIndex;
    }

    protected void takePhoto(CameraResultListener callback, @Nullable String directoryPath, @Nullable String fileName) {
        if (Build.VERSION.SDK_INT > MIN_VERSION_ICECREAM) {
            new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
        }
        setRecordState(Record.TAKE_PHOTO_STATE);
        this.cameraController.takePhoto(callback, directoryPath, fileName);
        if (cameraStateListener != null) {
            cameraStateListener.onRecordStatePhoto();
        }
    }

    protected void startRecording(@Nullable String directoryPath, @Nullable String fileName) {
        if (Build.VERSION.SDK_INT > MIN_VERSION_ICECREAM) {
            new MediaActionSound().play(MediaActionSound.START_VIDEO_RECORDING);
        }

        setRecordState(Record.RECORD_IN_PROGRESS_STATE);
        this.cameraController.startVideoRecord(directoryPath, fileName);

        if (cameraStateListener != null) {
            cameraStateListener.onRecordStateVideoInProgress();
        }
    }

    protected void stopRecording(CameraResultListener callback) {
        if (Build.VERSION.SDK_INT > MIN_VERSION_ICECREAM) {
            new MediaActionSound().play(MediaActionSound.STOP_VIDEO_RECORDING);
        }

        setRecordState(Record.READY_FOR_RECORD_STATE);
        this.cameraController.stopVideoRecord(callback);

        this.onStopVideoRecord(callback);

        if (cameraStateListener != null) {
            cameraStateListener.onRecordStateVideoReadyForRecord();
        }
    }

    protected void clearCameraPreview() {
        if (previewContainer != null)
            previewContainer.removeAllViews();
    }

    protected void setCameraPreview(View preview, Size previewSize) {
        //onCameraControllerReady()
        videoQualities = cameraController.getVideoQualityOptions();
        photoQualities = cameraController.getPhotoQualityOptions();

        if (previewContainer == null || preview == null) return;
        previewContainer.removeAllViews();
        previewContainer.addView(preview);

        previewContainer.setAspectRatio(previewSize.getHeight() / (double) previewSize.getWidth());
    }

    protected void setMediaFilePath(final File mediaFile) {
        this.mediaFilePath = mediaFile.toString();
    }

    protected void onStartVideoRecord(final File mediaFile) {
        setMediaFilePath(mediaFile);
        if (maxVideoFileSize > 0) {

            if (cameraVideoRecordTextListener != null) {
                cameraVideoRecordTextListener.setRecordSizeText(maxVideoFileSize, "1Mb" + " / " + maxVideoFileSize / (1024 * 1024) + "Mb");
                cameraVideoRecordTextListener.setRecordSizeTextVisible(true);
            }
            try {
                fileObserver = new FileObserver(this.mediaFilePath) {
                    private long lastUpdateSize = 0;

                    @Override
                    public void onEvent(int event, String path) {
                        final long fileSize = mediaFile.length() / (1024 * 1024);
                        if ((fileSize - lastUpdateSize) >= 1) {
                            lastUpdateSize = fileSize;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (cameraVideoRecordTextListener != null) {
                                        cameraVideoRecordTextListener.setRecordSizeText(maxVideoFileSize, fileSize + "Mb" + " / " + maxVideoFileSize / (1024 * 1024) + "Mb");
                                    }
                                }
                            });
                        }
                    }
                };
                fileObserver.startWatching();
            } catch (Exception e) {
                Log.e("FileObserver", "setMediaFilePath: ", e);
            }
        }

        if (countDownTimer == null) {
            this.countDownTimer = new TimerTask(timerCallBack);
        }
        countDownTimer.start();

        if (cameraStateListener != null) {
            cameraStateListener.onStartVideoRecord(mediaFile);
        }
    }

    protected void onStopVideoRecord(@Nullable CameraResultListener callback) {
        if (cameraControlListener != null) {
            cameraControlListener.allowRecord(false);
        }
        if (cameraStateListener != null) {
            cameraStateListener.onStopVideoRecord();
        }
        setRecordState(Record.READY_FOR_RECORD_STATE);

        if (fileObserver != null)
            fileObserver.stopWatching();

        if (countDownTimer != null) {
            countDownTimer.stop();
        }

        final int mediaAction = cameraConfigProvider.getMediaAction();
        if (cameraControlListener != null) {
            if (mediaAction != CameraConfig.MEDIA_ACTION_UNSPECIFIED) {
                cameraControlListener.setMediaActionSwitchVisible(false);
            } else {
                cameraControlListener.setMediaActionSwitchVisible(true);
            }
        }

        final String filePath = this.cameraController.getOutputFile().toString();
        if (cameraResultListener != null) {
            cameraResultListener.onVideoRecorded(filePath);
        }

        if (callback != null) {
            callback.onVideoRecorded(filePath);
        }
    }

    @Override
    public void setStateListener(CameraStateListener cameraStateListener) {
        this.cameraStateListener = cameraStateListener;
    }

    @Override
    public void setTextListener(CameraVideoRecordTextListener cameraVideoRecordTextListener) {
        this.cameraVideoRecordTextListener = cameraVideoRecordTextListener;
    }

    @Override
    public void setControlsListener(CameraControlListener cameraControlListener) {
        this.cameraControlListener = cameraControlListener;
    }

    @Override
    public void setResultListener(CameraResultListener cameraResultListener) {
        this.cameraResultListener = cameraResultListener;
    }
}
