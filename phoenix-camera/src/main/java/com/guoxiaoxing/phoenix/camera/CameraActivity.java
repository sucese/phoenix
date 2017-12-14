package com.guoxiaoxing.phoenix.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.guoxiaoxing.phoenix.camera.common.MediaAction;
import com.guoxiaoxing.phoenix.camera.config.CameraConfig;
import com.guoxiaoxing.phoenix.camera.listener.CameraControlAdapter;
import com.guoxiaoxing.phoenix.camera.listener.CameraStateAdapter;
import com.guoxiaoxing.phoenix.camera.listener.CameraVideoRecordTextAdapter;
import com.guoxiaoxing.phoenix.camera.listener.OnCameraResultAdapter;
import com.guoxiaoxing.phoenix.camera.widget.CameraSettingsView;
import com.guoxiaoxing.phoenix.camera.widget.CameraSwitchView;
import com.guoxiaoxing.phoenix.camera.widget.FlashSwitchView;
import com.guoxiaoxing.phoenix.camera.widget.RecordButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG_CAMERA_FRAGMENT = "CameraFragment";
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 0x000100;
    private static final int REQUEST_CODE_PREVIEW = 0x000101;

    private static final String DIRECTORY_NAME = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
            + "/Camera";

    View mCameraLayout;
    RecordButton mRecordButton;
    TextView mRecordDurationText;
    TextView mRecordSizeText;

    CameraSettingsView mSettingsView;
    FlashSwitchView mFlashSwitchView;
    CameraSwitchView mCameraSwitchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_camera);
        setupView();
        setupCameraFragment();
    }

    @Override
    public void onClick(View v) {
        final ICameraFragment cameraFragment = getCameraFragment();
        if (cameraFragment == null) {
            return;
        }
        if (v.getId() == R.id.flash_switch_view) {
            cameraFragment.toggleFlashMode();
        } else if (v.getId() == R.id.front_back_camera_switcher) {
            cameraFragment.switchCameraTypeFrontBack();
        } else if (v.getId() == R.id.settings_view) {
            cameraFragment.openSettingDialog();
        }
    }

    private void setupView() {
        mSettingsView = (CameraSettingsView) findViewById(R.id.settings_view);
        mFlashSwitchView = (FlashSwitchView) findViewById(R.id.flash_switch_view);
        mCameraSwitchView = (CameraSwitchView) findViewById(R.id.front_back_camera_switcher);
        mRecordButton = (RecordButton) findViewById(R.id.record_button);
        mRecordDurationText = (TextView) findViewById(R.id.record_duration_text);
        mRecordSizeText = (TextView) findViewById(R.id.record_size_mb_text);
        mCameraLayout = findViewById(R.id.rl_camera_control);

        mSettingsView.setOnClickListener(this);
        mFlashSwitchView.setOnClickListener(this);
        mCameraSwitchView.setOnClickListener(this);
        mCameraLayout.setOnClickListener(this);

        mRecordButton.setOnRecordButtonListener(new RecordButton.OnRecordButtonListener() {
            @Override
            public void onClick() {
                final ICameraFragment cameraFragment = getCameraFragment();
                cameraFragment.switchCaptureAction(MediaAction.ACTION_PHOTO);
                cameraFragment.takePicture(DIRECTORY_NAME, "IMG_" + System.currentTimeMillis(), new OnCameraResultAdapter() {
                            @Override
                            public void onPhotoTaken(byte[] bytes, String filePath) {
                                Toast.makeText(getBaseContext(), "onPictureTaken "
                                        + filePath, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }

            @Override
            public void onLongClickStart() {
                final ICameraFragment cameraFragment = getCameraFragment();
                cameraFragment.switchCaptureAction(MediaAction.ACTION_VIDEO);
                cameraFragment.startRecordingVideo(DIRECTORY_NAME, "VID_" + System.currentTimeMillis());
            }

            @Override
            public void onLongClickEnd() {
                final ICameraFragment cameraFragment = getCameraFragment();
                cameraFragment.stopRecordingVideo(new OnCameraResultAdapter() {
                    @Override
                    public void onVideoRecorded(String filePath) {
                        Toast.makeText(getBaseContext(), "onVideoRecorded "
                                + filePath, Toast.LENGTH_SHORT).show();
                    }
                });
                cameraFragment.switchCaptureAction(MediaAction.ACTION_PHOTO);
            }
        });
    }

    private void setupCameraFragment() {
        if (Build.VERSION.SDK_INT > 15) {
            final String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};

            final List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), REQUEST_CODE_CAMERA_PERMISSIONS);
            } else addCameraFragment();
        } else {
            addCameraFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length != 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            addCameraFragment();
        }
    }

    public void addCameraFragment() {
        mCameraLayout.setVisibility(View.VISIBLE);
        final CameraFragment cameraFragment = CameraFragment.newInstance(new CameraConfig.Builder()
                .setCamera(CameraConfig.CAMERA_FACE_REAR).build());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, cameraFragment, TAG_CAMERA_FRAGMENT)
                .commitAllowingStateLoss();

        if (cameraFragment != null) {
            //cameraFragmentApi.setResultListener(new OnCameraResultListener() {
            //    @Override
            //    public void onVideoRecorded(String filePath) {
            //        Intent intent = PreviewActivity.newIntentVideo(CameraFragmentMainActivity.this, filePath);
            //        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
            //    }
//
            //    @Override
            //    public void onPictureTaken(byte[] bytes, String filePath) {
            //        Intent intent = PreviewActivity.newIntentPhoto(CameraFragmentMainActivity.this, filePath);
            //        startActivityForResult(intent, REQUEST_CODE_PREVIEW);
            //    }
            //});

            cameraFragment.setStateListener(new CameraStateAdapter() {

                @Override
                public void onCurrentCameraBack() {
                    mCameraSwitchView.displayBackCamera();
                }

                @Override
                public void onCurrentCameraFront() {
                    mCameraSwitchView.displayFrontCamera();
                }

                @Override
                public void onFlashAuto() {
                    mFlashSwitchView.displayFlashAuto();
                }

                @Override
                public void onFlashOn() {
                    mFlashSwitchView.displayFlashOn();
                }

                @Override
                public void onFlashOff() {
                    mFlashSwitchView.displayFlashOff();
                }

                @Override
                public void onCameraSetupForPhoto() {
//                    mFlashSwitchView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCameraSetupForVideo() {
//                    mFlashSwitchView.setVisibility(View.GONE);
                }

                @Override
                public void shouldRotateControls(int degrees) {
                    ViewCompat.setRotation(mCameraSwitchView, degrees);
                    ViewCompat.setRotation(mFlashSwitchView, degrees);
                    ViewCompat.setRotation(mRecordDurationText, degrees);
                    ViewCompat.setRotation(mRecordSizeText, degrees);
                }

                @Override
                public void onRecordStateVideoReadyForRecord() {
                }

                @Override
                public void onRecordStateVideoInProgress() {
                }

                @Override
                public void onRecordStatePhoto() {
                }

                @Override
                public void onStopVideoRecord() {
                    mRecordSizeText.setVisibility(View.GONE);
//                    mSettingsView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStartVideoRecord(File outputFile) {
                }
            });

            cameraFragment.setControlsListener(new CameraControlAdapter() {
                @Override
                public void lockControls() {
                    mCameraSwitchView.setEnabled(false);
                    mRecordButton.setEnabled(false);
                    mSettingsView.setEnabled(false);
                    mFlashSwitchView.setEnabled(false);
                }

                @Override
                public void unLockControls() {
                    mCameraSwitchView.setEnabled(true);
                    mRecordButton.setEnabled(true);
                    mSettingsView.setEnabled(true);
                    mFlashSwitchView.setEnabled(true);
                }

                @Override
                public void allowCameraSwitching(boolean allow) {
                    mCameraSwitchView.setVisibility(allow ? View.VISIBLE : View.GONE);
                }

                @Override
                public void allowRecord(boolean allow) {
                    mRecordButton.setEnabled(allow);
                }

                @Override
                public void setMediaActionSwitchVisible(boolean visible) {
                }
            });

            cameraFragment.setTextListener(new CameraVideoRecordTextAdapter() {
                @Override
                public void setRecordSizeText(long size, String text) {
                    mRecordSizeText.setText(text);
                }

                @Override
                public void setRecordSizeTextVisible(boolean visible) {
                    mRecordSizeText.setVisibility(visible ? View.VISIBLE : View.GONE);
                }

                @Override
                public void setRecordDurationText(String text) {
                    mRecordDurationText.setText(text);
                }

                @Override
                public void setRecordDurationTextVisible(boolean visible) {
                    mRecordDurationText.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    private ICameraFragment getCameraFragment() {
        return (ICameraFragment) getSupportFragmentManager().findFragmentByTag(TAG_CAMERA_FRAGMENT);
    }
}
