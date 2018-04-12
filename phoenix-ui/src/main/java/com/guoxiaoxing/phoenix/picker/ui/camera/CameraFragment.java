package com.guoxiaoxing.phoenix.picker.ui.camera;

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

import com.guoxiaoxing.phoenix.R;
import com.guoxiaoxing.phoenix.picker.ui.BaseFragment;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.model.Camera;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.model.Flash;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.model.MediaAction;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.model.Record;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.CameraConfigProvider;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.CameraConfigProviderImpl;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.PictureQualityOption;
import com.guoxiaoxing.phoenix.picker.ui.camera.config.VideoQualityOption;
import com.guoxiaoxing.phoenix.picker.ui.camera.lifecycle.CameraLifecycle;
import com.guoxiaoxing.phoenix.picker.ui.camera.lifecycle.impl.Camera1Lifecycle;
import com.guoxiaoxing.phoenix.picker.ui.camera.lifecycle.impl.Camera2Lifecycle;
import com.guoxiaoxing.phoenix.picker.ui.camera.lifecycle.listener.CameraView;
import com.guoxiaoxing.phoenix.picker.ui.camera.listener.CameraControlListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.listener.CameraStateListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.listener.CameraVideoRecordTextListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.listener.ICameraFragment;
import com.guoxiaoxing.phoenix.picker.ui.camera.listener.OnCameraResultListener;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.CameraUtils;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.Size;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.DeviceUtils;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.timer.CountdownTask;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.timer.TimerTask;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.timer.TimerTaskBase;
import com.guoxiaoxing.phoenix.picker.ui.camera.widget.AutoFitFrameLayout;

import java.io.File;

/**
 * The camera fragment
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
public class CameraFragment<CameraId> extends BaseFragment implements ICameraFragment {

    public static final String ARG_CONFIGURATION = "ARG_CONFIGURATION";
    public static final int MIN_VERSION_ICECREAM = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;

    private AutoFitFrameLayout mPreviewContainer;

    private SensorManager mSensorManager;
    private AlertDialog mSettingsDialog;
    private CameraLifecycle mCameraLifecycle;

    private CameraConfig mCameraConfig;
    @CameraConfig.MediaQuality
    private int mNewQuality = -1;
    private CharSequence[] mPhotoQualities;
    private CharSequence[] mVideoQualities;
    private CameraConfigProvider mCameraConfigProvider;
    private OnCameraResultListener mOnCameraResultListener;

    @Flash.FlashMode
    private int mFlashMode = Flash.FLASH_AUTO;
    @Camera.CameraType
    private int mCameraType = Camera.CAMERA_TYPE_REAR;
    @MediaAction.MediaActionState
    private int mMediaActionState = MediaAction.ACTION_PHOTO;
    @Record.RecordState
    private int mRecordState = Record.TAKE_PHOTO_STATE;

    private String mMediaFilePath;
    private FileObserver mFileObserver;
    private long mMaxVideoFileSize = 0;
    private TimerTaskBase mCountDownTimer;

    private CameraControlListener mCameraControlListener;
    private CameraStateListener mCameraStateListener;
    private CameraVideoRecordTextListener mCameraVideoRecordTextListener;

    private final TimerTaskBase.Callback mTimerCallBack = new TimerTaskBase.Callback() {
        @Override
        public void setText(String text) {
            if (mCameraVideoRecordTextListener != null) {
                mCameraVideoRecordTextListener.setRecordDurationText(text);
            }
        }

        @Override
        public void setTextVisible(boolean visible) {
            if (mCameraVideoRecordTextListener != null) {
                mCameraVideoRecordTextListener.setRecordDurationTextVisible(visible);
            }
        }
    };

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (this) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    if (sensorEvent.values[0] < 4 && sensorEvent.values[0] > -4) {
                        if (sensorEvent.values[1] > 0) {
                            // UP
                            mCameraConfigProvider.setSensorPosition(CameraConfig.SENSOR_POSITION_UP);
                            mCameraConfigProvider.setDegrees(mCameraConfigProvider.getDeviceDefaultOrientation() == CameraConfig.ORIENTATION_PORTRAIT ? 0 : 90);
                        } else if (sensorEvent.values[1] < 0) {
                            // UP SIDE DOWN
                            mCameraConfigProvider.setSensorPosition(CameraConfig.SENSOR_POSITION_UP_SIDE_DOWN);
                            mCameraConfigProvider.setDegrees(mCameraConfigProvider.getDeviceDefaultOrientation() == CameraConfig.ORIENTATION_PORTRAIT ? 180 : 270);
                        }
                    } else if (sensorEvent.values[1] < 4 && sensorEvent.values[1] > -4) {
                        if (sensorEvent.values[0] > 0) {
                            // LEFT
                            mCameraConfigProvider.setSensorPosition(CameraConfig.SENSOR_POSITION_LEFT);
                            mCameraConfigProvider.setDegrees(mCameraConfigProvider.getDeviceDefaultOrientation() == CameraConfig.ORIENTATION_PORTRAIT ? 90 : 180);
                        } else if (sensorEvent.values[0] < 0) {
                            // RIGHT
                            mCameraConfigProvider.setSensorPosition(CameraConfig.SENSOR_POSITION_RIGHT);
                            mCameraConfigProvider.setDegrees(mCameraConfigProvider.getDeviceDefaultOrientation() == CameraConfig.ORIENTATION_PORTRAIT ? 270 : 0);
                        }
                    }
                    onScreenRotation(mCameraConfigProvider.getDegrees());
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

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
            mCameraConfig = (CameraConfig) arguments.getSerializable(ARG_CONFIGURATION);
        }
        this.mCameraConfigProvider = new CameraConfigProviderImpl();
        this.mCameraConfigProvider.setCameraConfig(mCameraConfig);

        this.mSensorManager = (SensorManager) getContext().getSystemService(Activity.SENSOR_SERVICE);

        final CameraView cameraView = new CameraView() {

            @Override
            public void updateCameraPreview(Size size, View cameraPreview) {
                if (mCameraControlListener != null) {
                    mCameraControlListener.unLockControls();
                    mCameraControlListener.allowRecord(true);
                }
                setCameraPreview(cameraPreview, size);
            }

            @Override
            public void updateUiForMediaAction(@CameraConfig.MediaAction int mediaAction) {

            }

            @Override
            public void updateCameraSwitcher(int numberOfCameras) {
                if (mCameraControlListener != null) {
                    mCameraControlListener.allowCameraSwitching(numberOfCameras > 1);
                }
            }

            @Override
            public void onPictureTaken(byte[] bytes, OnCameraResultListener callback) {
                final String filePath = mCameraLifecycle.getOutputFile().toString();
                if (mOnCameraResultListener != null) {
                    mOnCameraResultListener.onPhotoTaken(bytes, filePath);
                }
                if (callback != null) {
                    callback.onPhotoTaken(bytes, filePath);
                }
            }

            @Override
            public void onVideoRecordStart(int width, int height) {
                final File outputFile = mCameraLifecycle.getOutputFile();
                onStartVideoRecord(outputFile);
            }

            @Override
            public void onVideoRecordStop(@Nullable OnCameraResultListener callback) {
                //CameraFragment.this.onStopVideoRecord(callback);
            }

            @Override
            public void releaseCameraPreview() {
                clearCameraPreview();
            }
        };

        if (CameraUtils.hasCamera2(getContext())) {
            mCameraLifecycle = new Camera2Lifecycle(getContext(), cameraView, mCameraConfigProvider);
        } else {
            mCameraLifecycle = new Camera1Lifecycle(getContext(), cameraView, mCameraConfigProvider);
        }
        mCameraLifecycle.onCreate(savedInstanceState);

        //onProcessBundle
        mMediaActionState = mCameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_VIDEO ?
                MediaAction.ACTION_VIDEO : MediaAction.ACTION_PHOTO;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreviewContainer = (AutoFitFrameLayout) view.findViewById(R.id.previewContainer);

        final int defaultOrientation = DeviceUtils.getDeviceDefaultOrientation(getContext());
        switch (defaultOrientation) {
            case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                mCameraConfigProvider.setDeviceDefaultOrientation(CameraConfig.ORIENTATION_LANDSCAPE);
                break;
            default:
                mCameraConfigProvider.setDeviceDefaultOrientation(CameraConfig.ORIENTATION_PORTRAIT);
                break;
        }

        switch (mCameraConfigProvider.getFlashMode()) {
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

        if (mCameraControlListener != null) {
            setMaxVideoDuration(mCameraConfigProvider.getVideoDuration());
            setMaxVideoFileSize(mCameraConfigProvider.getVideoFileSize());
        }

        setCameraTypeFrontBack(mCameraConfigProvider.getCameraFace());
        notifyListeners();
    }

    public void notifyListeners() {
        onFlashModeChanged();
        onActionPhotoVideoChanged();
        onCameraTypeFrontBackChanged();
    }



    @Override
    public void onResume() {
        super.onResume();

        mCameraLifecycle.onResume();
        mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        if (mCameraControlListener != null) {
            mCameraControlListener.lockControls();
            mCameraControlListener.allowRecord(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mCameraLifecycle.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);

        if (mCameraControlListener != null) {
            mCameraControlListener.lockControls();
            mCameraControlListener.allowRecord(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCameraLifecycle.onDestroy();
    }

    @Override
    public void takePicture(@Nullable String directoryPath, @Nullable String fileName, OnCameraResultListener callback) {
        if (Build.VERSION.SDK_INT > MIN_VERSION_ICECREAM) {
            new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
        }
        setRecordState(Record.TAKE_PHOTO_STATE);
        this.mCameraLifecycle.takePhoto(callback, directoryPath, fileName);
        if (mCameraStateListener != null) {
            mCameraStateListener.onRecordStatePhoto();
        }
    }

    @Override
    public void startRecordingVideo(@Nullable String directoryPath, @Nullable String fileName) {
        setRecordState(Record.RECORD_IN_PROGRESS_STATE);
        this.mCameraLifecycle.startVideoRecord(directoryPath, fileName);

        if (mCameraStateListener != null) {
            mCameraStateListener.onRecordStateVideoInProgress();
        }
    }

    @Override
    public void stopRecordingVideo(OnCameraResultListener callback) {
        setRecordState(Record.READY_FOR_RECORD_STATE);
        this.mCameraLifecycle.stopVideoRecord(callback);

        this.onStopVideoRecord(callback);

        if (mCameraStateListener != null) {
            mCameraStateListener.onRecordStateVideoReadyForRecord();
        }
    }


    protected void setMaxVideoFileSize(long maxVideoFileSize) {
        this.mMaxVideoFileSize = maxVideoFileSize;
    }

    protected void setMaxVideoDuration(int maxVideoDurationInMillis) {
        if (maxVideoDurationInMillis > 0) {
            this.mCountDownTimer = new CountdownTask(mTimerCallBack, maxVideoDurationInMillis);
        } else {
            this.mCountDownTimer = new TimerTask(mTimerCallBack);
        }
    }

    @Override
    public void openSettingDialog() {
        final Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (mMediaActionState == MediaAction.ACTION_VIDEO) {
            builder.setSingleChoiceItems(mVideoQualities, getVideoOptionCheckedIndex(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index) {
                    mNewQuality = ((VideoQualityOption) mVideoQualities[index]).getMediaQuality();
                }
            });
            if (mCameraConfigProvider.getVideoFileSize() > 0)
                builder.setTitle(String.format(getString(R.string.settings_video_quality_title),
                        "(Max " + String.valueOf(mCameraConfigProvider.getVideoFileSize() / (1024 * 1024) + " MB)")));
            else
                builder.setTitle(String.format(getString(R.string.settings_video_quality_title), ""));
        } else {
            builder.setSingleChoiceItems(mPhotoQualities, getPhotoOptionCheckedIndex(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index) {
                    mNewQuality = ((PictureQualityOption) mPhotoQualities[index]).getMediaQuality();
                }
            });
            builder.setTitle(R.string.settings_photo_quality_title);
        }

        builder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mNewQuality > 0 && mNewQuality != mCameraConfigProvider.getMediaQuality()) {
                    mCameraConfigProvider.setMediaQuality(mNewQuality);
                    dialogInterface.dismiss();
                    if (mCameraControlListener != null) {
                        mCameraControlListener.lockControls();
                    }
                    mCameraLifecycle.switchQuality();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        mSettingsDialog = builder.create();
        mSettingsDialog.show();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(mSettingsDialog.getWindow().getAttributes());
        layoutParams.width = DeviceUtils.convertDipToPixels(context, 350);
        layoutParams.height = DeviceUtils.convertDipToPixels(context, 350);
        mSettingsDialog.getWindow().setAttributes(layoutParams);
    }

    @Override
    public void switchCameraTypeFrontBack() {
        if (mCameraControlListener != null) {
            mCameraControlListener.lockControls();
            mCameraControlListener.allowRecord(false);
        }

        int cameraFace = CameraConfig.CAMERA_FACE_REAR;
        switch (mCameraType) {
            case Camera.CAMERA_TYPE_FRONT:
                mCameraType = Camera.CAMERA_TYPE_REAR;
                cameraFace = CameraConfig.CAMERA_FACE_REAR;
                break;
            case Camera.CAMERA_TYPE_REAR:
                mCameraType = Camera.CAMERA_TYPE_FRONT;
                cameraFace = CameraConfig.CAMERA_FACE_FRONT;
                break;
        }

        onCameraTypeFrontBackChanged();
        this.mCameraLifecycle.switchCamera(cameraFace);

        if (mCameraControlListener != null) {
            mCameraControlListener.unLockControls();
        }
    }

    protected void setCameraTypeFrontBack(@CameraConfig.CameraFace int cameraFace) {
        switch (cameraFace) {
            case CameraConfig.CAMERA_FACE_FRONT:
                mCameraType = Camera.CAMERA_TYPE_FRONT;
                cameraFace = CameraConfig.CAMERA_FACE_FRONT;
                break;
            case CameraConfig.CAMERA_FACE_REAR:
                mCameraType = Camera.CAMERA_TYPE_REAR;
                cameraFace = CameraConfig.CAMERA_FACE_REAR;
                break;
        }
        onCameraTypeFrontBackChanged();
        this.mCameraLifecycle.switchCamera(cameraFace);

    }

    protected void onCameraTypeFrontBackChanged() {
        if (mCameraStateListener != null) {
            switch (mCameraType) {
                case Camera.CAMERA_TYPE_REAR:
                    mCameraStateListener.onCurrentCameraBack();
                    break;
                case Camera.CAMERA_TYPE_FRONT:
                    mCameraStateListener.onCurrentCameraFront();
                    break;
            }
        }
    }

    @Override
    public void switchCaptureAction(int actionType) {
        mMediaActionState = actionType;
        onActionPhotoVideoChanged();
    }

    protected void onActionPhotoVideoChanged() {
        if (mCameraStateListener != null) {
            switch (mMediaActionState) {
                case MediaAction.ACTION_VIDEO:
                    mCameraStateListener.onCameraSetupForVideo();
                    break;
                case MediaAction.ACTION_PHOTO:
                    mCameraStateListener.onCameraSetupForPhoto();
                    break;
            }
        }
    }

    @Override
    public void toggleFlashMode() {
        switch (mFlashMode) {
            case Flash.FLASH_AUTO:
                mFlashMode = Flash.FLASH_OFF;
                break;
            case Flash.FLASH_OFF:
                mFlashMode = Flash.FLASH_ON;
                break;
            case Flash.FLASH_ON:
                mFlashMode = Flash.FLASH_AUTO;
                break;
        }
        onFlashModeChanged();
    }

    private void onFlashModeChanged() {
        switch (mFlashMode) {
            case Flash.FLASH_AUTO:
                if (mCameraStateListener != null) mCameraStateListener.onFlashAuto();
                mCameraConfigProvider.setFlashMode(CameraConfig.FLASH_MODE_AUTO);
                this.mCameraLifecycle.setFlashMode(CameraConfig.FLASH_MODE_AUTO);
                break;
            case Flash.FLASH_ON:
                if (mCameraStateListener != null) mCameraStateListener.onFlashOn();
                mCameraConfigProvider.setFlashMode(CameraConfig.FLASH_MODE_ON);
                this.mCameraLifecycle.setFlashMode(CameraConfig.FLASH_MODE_ON);
                break;
            case Flash.FLASH_OFF:
                if (mCameraStateListener != null) mCameraStateListener.onFlashOff();
                mCameraConfigProvider.setFlashMode(CameraConfig.FLASH_MODE_OFF);
                this.mCameraLifecycle.setFlashMode(CameraConfig.FLASH_MODE_OFF);
                break;
        }
    }

    protected void onScreenRotation(int degrees) {
        if (mCameraStateListener != null) {
            mCameraStateListener.shouldRotateControls(degrees);
        }
        rotateSettingsDialog(degrees);
    }

    protected void setRecordState(@Record.RecordState int recordState) {
        this.mRecordState = recordState;
    }


    //@Override
    //public void onActivityResult(int requestCode, int resultCode, Intent data) {
    //    if (resultCode == Activity.RESULT_OK) {
    //        if (requestCode == REQUEST_PREVIEW_CODE) {
    //            final FragmentActivity activity = getActivity();
    //            if (activity != null) {
    //                if (CameraPreviewActivity.isResultConfirm(data)) {
    //                    Intent resultIntent = new Intent();
    //                    resultIntent.putExtra(CameraConfig.Arguments.FILE_PATH,
    //                            CameraPreviewActivity.getMediaFilePatch(data));
    //                    activity.setResult(Activity.RESULT_OK, resultIntent);
    //                    activity.finish();
    //                } else if (CameraPreviewActivity.isResultCancel(data)) {
    //                    activity.setResult(Activity.RESULT_CANCELED);
    //                    activity.finish();
    //                } else if (CameraPreviewActivity.isResultRetake(data)) {
    //                    //ignore, just proceed the camera
    //                }
    //            }
    //        }
    //    }
    //}

    protected void setFlashMode(@Flash.FlashMode int mode) {
        this.mFlashMode = mode;
        onFlashModeChanged();
    }

    protected void rotateSettingsDialog(int degrees) {
        if (mSettingsDialog != null && mSettingsDialog.isShowing() && Build.VERSION.SDK_INT > 10) {
            ViewGroup dialogView = (ViewGroup) mSettingsDialog.getWindow().getDecorView();
            for (int i = 0; i < dialogView.getChildCount(); i++) {
                dialogView.getChildAt(i).setRotation(degrees);
            }
        }
    }

    protected int getVideoOptionCheckedIndex() {
        int checkedIndex = -1;

        final int mediaQuality = mCameraConfigProvider.getMediaQuality();
        final int passedMediaQuality = mCameraConfigProvider.getPassedMediaQuality();

        if (mediaQuality == CameraConfig.MEDIA_QUALITY_AUTO) checkedIndex = 0;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_HIGH) checkedIndex = 1;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_MEDIUM) checkedIndex = 2;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_LOW) checkedIndex = 3;

        if (passedMediaQuality != CameraConfig.MEDIA_QUALITY_AUTO) checkedIndex--;

        return checkedIndex;
    }

    protected int getPhotoOptionCheckedIndex() {
        int checkedIndex = -1;

        final int mediaQuality = mCameraConfigProvider.getMediaQuality();

        if (mediaQuality == CameraConfig.MEDIA_QUALITY_HIGHEST) checkedIndex = 0;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_HIGH) checkedIndex = 1;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_MEDIUM) checkedIndex = 2;
        else if (mediaQuality == CameraConfig.MEDIA_QUALITY_LOWEST) checkedIndex = 3;
        return checkedIndex;
    }

    protected void clearCameraPreview() {
        if (mPreviewContainer != null)
            mPreviewContainer.removeAllViews();
    }

    protected void setCameraPreview(View preview, Size previewSize) {
        //onCameraControllerReady()
        mVideoQualities = mCameraLifecycle.getVideoQualityOptions();
        mPhotoQualities = mCameraLifecycle.getPhotoQualityOptions();

        if (mPreviewContainer == null || preview == null) return;
        mPreviewContainer.removeAllViews();
        mPreviewContainer.addView(preview);

        mPreviewContainer.setAspectRatio(previewSize.getHeight() / (double) previewSize.getWidth());
    }

    protected void setMediaFilePath(final File mediaFile) {
        this.mMediaFilePath = mediaFile.toString();
    }

    protected void onStartVideoRecord(final File mediaFile) {
        setMediaFilePath(mediaFile);
        if (mMaxVideoFileSize > 0) {

            if (mCameraVideoRecordTextListener != null) {
                mCameraVideoRecordTextListener.setRecordSizeText(mMaxVideoFileSize, "1Mb" + " / " + mMaxVideoFileSize / (1024 * 1024) + "Mb");
                mCameraVideoRecordTextListener.setRecordSizeTextVisible(true);
            }
            try {
                mFileObserver = new FileObserver(this.mMediaFilePath) {
                    private long lastUpdateSize = 0;

                    @Override
                    public void onEvent(int event, String path) {
                        final long fileSize = mediaFile.length() / (1024 * 1024);
                        if ((fileSize - lastUpdateSize) >= 1) {
                            lastUpdateSize = fileSize;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mCameraVideoRecordTextListener != null) {
                                        mCameraVideoRecordTextListener.setRecordSizeText(mMaxVideoFileSize, fileSize + "Mb" + " / " + mMaxVideoFileSize / (1024 * 1024) + "Mb");
                                    }
                                }
                            });
                        }
                    }
                };
                mFileObserver.startWatching();
            } catch (Exception e) {
                Log.e("FileObserver", "setMediaFilePath: ", e);
            }
        }

        if (mCountDownTimer == null) {
            this.mCountDownTimer = new TimerTask(mTimerCallBack);
        }
        mCountDownTimer.start();

        if (mCameraStateListener != null) {
            mCameraStateListener.onStartVideoRecord(mediaFile);
        }
    }

    protected void onStopVideoRecord(@Nullable OnCameraResultListener callback) {
        if (mCameraControlListener != null) {
            mCameraControlListener.allowRecord(false);
        }
        if (mCameraStateListener != null) {
            mCameraStateListener.onStopVideoRecord();
        }
        setRecordState(Record.READY_FOR_RECORD_STATE);

        if (mFileObserver != null)
            mFileObserver.stopWatching();

        if (mCountDownTimer != null) {
            mCountDownTimer.stop();
        }

        final int mediaAction = mCameraConfigProvider.getMediaAction();
        if (mCameraControlListener != null) {
            if (mediaAction != CameraConfig.MEDIA_ACTION_UNSPECIFIED) {
                mCameraControlListener.setMediaActionSwitchVisible(false);
            } else {
                mCameraControlListener.setMediaActionSwitchVisible(true);
            }
        }

        final String filePath = this.mCameraLifecycle.getOutputFile().toString();
        if (mOnCameraResultListener != null) {
            mOnCameraResultListener.onVideoRecorded(filePath);
        }

        if (callback != null) {
            callback.onVideoRecorded(filePath);
        }
    }

    @Override
    public void setStateListener(CameraStateListener cameraStateListener) {
        this.mCameraStateListener = cameraStateListener;
    }

    @Override
    public void setTextListener(CameraVideoRecordTextListener cameraVideoRecordTextListener) {
        this.mCameraVideoRecordTextListener = cameraVideoRecordTextListener;
    }

    @Override
    public void setControlsListener(CameraControlListener cameraControlListener) {
        this.mCameraControlListener = cameraControlListener;
    }

    @Override
    public void setResultListener(OnCameraResultListener onCameraResultListener) {
        this.mOnCameraResultListener = onCameraResultListener;
    }
}
