package com.guoxiaoxing.phoenix.picker.ui.camera.config;

public class CameraConfigProviderImpl implements CameraConfigProvider {

    protected long videoFileSize = -1;
    protected int videoDuration = -1;
    protected int minimumVideoDuration = -1;

    @CameraConfig.MediaAction
    protected int mediaAction = CameraConfig.MEDIA_ACTION_UNSPECIFIED;

    @CameraConfig.MediaQuality
    protected int mediaQuality = CameraConfig.MEDIA_QUALITY_HIGH;

    @CameraConfig.MediaQuality
    protected int passedMediaQuality = CameraConfig.MEDIA_QUALITY_MEDIUM;

    @CameraConfig.FlashMode
    protected int flashMode = CameraConfig.FLASH_MODE_AUTO;

    @CameraConfig.CameraFace
    protected int cameraFace = CameraConfig.CAMERA_FACE_REAR;

    @CameraConfig.SensorPosition
    protected int sensorPosition = CameraConfig.SENSOR_POSITION_UNSPECIFIED;

    @CameraConfig.DeviceDefaultOrientation
    protected int deviceDefaultOrientation;

    private int degrees = -1;

    @Override
    public int getMediaAction() {
        return mediaAction;
    }

    @Override
    public int getMediaQuality() {
        return mediaQuality;
    }

    @Override
    public int getVideoDuration() {
        return videoDuration;
    }

    @Override
    public long getVideoFileSize() {
        return videoFileSize;
    }

    @Override
    public int getMinimumVideoDuration() {
        return minimumVideoDuration / 1000;
    }

    @Override
    public final int getSensorPosition() {
        return sensorPosition;
    }

    @Override
    public final int getDegrees() {
        return degrees;
    }

    @Override
    public int getFlashMode() {
        return flashMode;
    }

    @Override
    public void setMediaQuality(int mediaQuality) {
        this.mediaQuality = mediaQuality;
    }

    @Override
    public void setPassedMediaQuality(int mediaQuality) {
        this.passedMediaQuality = mediaQuality;
    }

    @Override
    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
    }

    @Override
    public void setVideoFileSize(long videoFileSize) {
        this.videoFileSize = videoFileSize;
    }

    @Override
    public void setMinimumVideoDuration(int minimumVideoDuration) {
        this.minimumVideoDuration = minimumVideoDuration;
    }

    @Override
    public void setFlashMode(int flashMode) {
        this.flashMode = flashMode;
    }

    public void setMediaAction(@CameraConfig.MediaAction int mediaAction) {
        this.mediaAction = mediaAction;
    }

    public int getPassedMediaQuality() {
        return passedMediaQuality;
    }

    @CameraConfig.CameraFace
    public int getCameraFace() {
        return cameraFace;
    }

    public void setCameraFace(@CameraConfig.CameraFace int cameraFace) {
        this.cameraFace = cameraFace;
    }

    @Override
    public void setCameraConfig(CameraConfig cameraConfig) {
        if (cameraConfig != null) {

            final int mediaAction = cameraConfig.getMediaAction();
            if (mediaAction != -1) {
                switch (mediaAction) {
                    case CameraConfig.MEDIA_ACTION_PHOTO:
                        setMediaAction(CameraConfig.MEDIA_ACTION_PHOTO);
                        break;
                    case CameraConfig.MEDIA_ACTION_VIDEO:
                        setMediaAction(CameraConfig.MEDIA_ACTION_VIDEO);
                        break;
                    default:
                        setMediaAction(CameraConfig.MEDIA_ACTION_UNSPECIFIED);
                        break;
                }
            }

            final int mediaQuality = cameraConfig.getMediaQuality();
            if (mediaQuality != -1) {
                switch (mediaQuality) {
                    case CameraConfig.MEDIA_QUALITY_AUTO:
                        setMediaQuality(CameraConfig.MEDIA_QUALITY_AUTO);
                        break;
                    case CameraConfig.MEDIA_QUALITY_HIGHEST:
                        setMediaQuality(CameraConfig.MEDIA_QUALITY_HIGHEST);
                        break;
                    case CameraConfig.MEDIA_QUALITY_HIGH:
                        setMediaQuality(CameraConfig.MEDIA_QUALITY_HIGH);
                        break;
                    case CameraConfig.MEDIA_QUALITY_MEDIUM:
                        setMediaQuality(CameraConfig.MEDIA_QUALITY_MEDIUM);
                        break;
                    case CameraConfig.MEDIA_QUALITY_LOW:
                        setMediaQuality(CameraConfig.MEDIA_QUALITY_LOW);
                        break;
                    case CameraConfig.MEDIA_QUALITY_LOWEST:
                        setMediaQuality(CameraConfig.MEDIA_QUALITY_LOWEST);
                        break;
                    default:
                        setMediaQuality(CameraConfig.MEDIA_QUALITY_MEDIUM);
                        break;
                }
                setPassedMediaQuality(getMediaQuality());
            }

            final int videoDuration = cameraConfig.getVideoDuration();
            if (videoDuration != -1) {
                setVideoDuration(videoDuration);
            }

            final int cameraFace = cameraConfig.getCameraFace();
            if (cameraFace != -1) {
                setCameraFace(cameraFace);
            }

            final long videoFileSize = cameraConfig.getVideoFileSize();
            if (videoFileSize != -1) {
                setVideoFileSize(videoFileSize);
            }

            final int minimumVideoDuration = cameraConfig.getMinimumVideoDuration();
            if (minimumVideoDuration != -1) {
                setMinimumVideoDuration(minimumVideoDuration);
            }

            final int flashMode = cameraConfig.getFlashMode();
            if (flashMode != -1)
                switch (flashMode) {
                    case CameraConfig.FLASH_MODE_AUTO:
                        setFlashMode(CameraConfig.FLASH_MODE_AUTO);
                        break;
                    case CameraConfig.FLASH_MODE_ON:
                        setFlashMode(CameraConfig.FLASH_MODE_ON);
                        break;
                    case CameraConfig.FLASH_MODE_OFF:
                        setFlashMode(CameraConfig.FLASH_MODE_OFF);
                        break;
                    default:
                        setFlashMode(CameraConfig.FLASH_MODE_AUTO);
                        break;
                }
        }
    }

    public void setSensorPosition(int sensorPosition) {
        this.sensorPosition = sensorPosition;
    }

    public int getDeviceDefaultOrientation() {
        return deviceDefaultOrientation;
    }

    public void setDeviceDefaultOrientation(int deviceDefaultOrientation) {
        this.deviceDefaultOrientation = deviceDefaultOrientation;
    }

    public void setDegrees(int degrees) {
        this.degrees = degrees;
    }
}
