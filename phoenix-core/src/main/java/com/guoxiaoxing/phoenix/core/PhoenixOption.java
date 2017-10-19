package com.guoxiaoxing.phoenix.core;

import android.content.Context;
import android.os.Environment;

import com.guoxiaoxing.phoenix.core.common.PhoenixConstant;
import com.guoxiaoxing.phoenix.core.listener.OnPickerListener;
import com.guoxiaoxing.phoenix.core.listener.Starter;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.core.util.ReflectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/8/14 上午9:47
 */
public class PhoenixOption {

    //功能 - 选择图片/视频/音频
    public static final int TYPE_PICK_MEDIA = 0x000001;
    //功能 - 拍照
    public static final int TYPE_TAKE_PICTURE = 0x000002;
    //功能 - 预览
    public static final int TYPE_BROWSER_PICTURE = 0x000003;

    //主题 - 默认
    public static final String THEME_DEFAULT = "THEME_DEFAULT";
    //主题 - 中国红主题
    public static final String THEME_RED = "THEME_RED";
    //主题 - 青春橙主题
    public static final String THEME_ORANGE = "THEME_ORANGE";
    //主题 - 天空蓝主题
    public static final String THEME_BLUE = "THEME_BLUE";

    //选择列表显示的文件类型，全部：MimeType.ofAll()、图片：MimeType.ofImage()、视频：MimeType.ofVideo()，音频：MimeType.ofAudio()
    private int fileType = MimeType.ofImage();
    //是否显示拍照按钮
    private boolean enableCamera = false;
    //拍照保存路径
    private String outputCameraPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
    //主题样式，有默认样式、大风车样式、车牛样式、弹个车样式，可定制
    private String theme = THEME_DEFAULT;
    //选择类型，单选、多选
    private int selectionMode = PhoenixConstant.MULTIPLE;
    //最大选择张数，默认为0，表示不限制
    private int maxSelectNum = 0;
    //最小选择张数，默认为0，表示不限制
    private int minSelectNum = 0;
    //视频录制质量 0/1
    private int videoQuality;
    //显示多少秒以内的视频or音频也可适用
    private int videoSecond;
    //视频秒数录制 默认60s
    private int recordVideoSecond;
    //图片选择界面每行图片个数
    private int imageSpanCount = 4;
    //选择列表图片宽度
    private int overrideWidth = 160;
    //选择列表图片高度
    private int overrideHeight = 160;
    //glide加载图片大小，0-1之间，如果设置，则overrideWidth与overrideHeight无效
    private float sizeMultiplier;
    //选择列表点击动画效果
    private boolean zoomAnim = true;
    //是否显示gif图片
    private boolean enableGif;
    //是否开启点击预览
    private boolean enablePreview = true;
    //是否开启视频点击预览
    private boolean enPreviewVideo = true;
    //是否开启音频预览
    private boolean enablePreviewAudio = true;
    //是否开启数字显示模式
    private boolean checkNumMode;
    //是否开启点击声音
    private boolean openClickSound = true;
    //预览图片时，是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
    private boolean previewEggs = true;

    //浏览图片时是否可以删除图片
    private boolean enableDelete = false;
    //当前索引
    private int currentIndex;
    //已选择的数据、图片/视频/音频预览的数据
    private List<MediaEntity> mediaList = new ArrayList<>();

    //是否开启压缩
    private boolean enableCompress;
    private int compressMaxPixel;
    //压缩最大值kb
    private int compressMaxSize;
    //压缩最大高度
    private int compressMaxHeight;
    //压缩最大宽度
    private int compressMaxWidth;
    private boolean compreEnablePixel;
    private boolean compressEnableQuality;
    private boolean compressEnableReserveRaw;

    //是否开启裁剪
    private boolean enableCrop;
    //裁剪宽度
    private int cropWidth;
    //裁剪高度
    private int cropHeight;
    //裁剪压缩质量 默认90
    private int cropCompressQuality;
    //是否显示裁剪矩形边框
    private boolean showCropFrame = false;
    //是否显示裁剪矩形网格
    private boolean showCropGrid;
    //裁剪是否可旋转图片
    private boolean rotateEnabled = true;
    //裁剪是否可放大缩小图片
    private boolean scaleEnabled = true;
    //裁剪比例 如16:9 3:2 3:4 1:1 可自定义 x/y
    private int aspect_ratio_x;
    //裁剪比例 如16:9 3:2 3:4 1:1 可自定义 x/y
    private int aspect_ratio_y;
    //裁剪框是否可拖拽
    private boolean freeStyleCropEnabled;
    //是否圆形裁剪
    private boolean circleDimmedLayer;
    //是否显示uCrop工具栏
    private boolean hideBottomControls = false;

    //是否开启上传，开启上传会默认开启压缩
    private boolean enableUpload;

    //给车辆拍照时是否显示拍照提示（车牛）
    private boolean enableCameraHint;
    //是否开启拍照模板
    private boolean enableCameraModel;
    //是否开启图片旋转
    private boolean enablePictureRotate;
    //是否开启图片标记
    private boolean enbalePictureMark;
    //是否开启图片涂抹
    private boolean enablePictureBlur;


    private String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();//拍照、压缩、裁剪后的保存路径
    private OnPickerListener onPickerListener;//选择监听

    public PhoenixOption() {

    }

    public int getFileType() {
        return fileType;
    }

    public boolean isEnableCamera() {
        return enableCamera;
    }

    public boolean isEnableGif() {
        return enableGif;
    }

    public boolean isEnablePreview() {
        return enablePreview;
    }

    public boolean isEnPreviewVideo() {
        return enPreviewVideo;
    }

    public boolean isEnablePreviewAudio() {
        return enablePreviewAudio;
    }

    public boolean isCheckNumMode() {
        return checkNumMode;
    }

    public boolean isOpenClickSound() {
        return openClickSound;
    }

    public boolean isFreeStyleCropEnabled() {
        return freeStyleCropEnabled;
    }

    public boolean isCircleDimmedLayer() {
        return circleDimmedLayer;
    }

    public boolean isHideBottomControls() {
        return hideBottomControls;
    }

    public boolean isRotateEnabled() {
        return rotateEnabled;
    }

    public boolean isScaleEnabled() {
        return scaleEnabled;
    }

    public boolean isPreviewEggs() {
        return previewEggs;
    }

    public boolean isEnableDelete() {
        return enableDelete;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<MediaEntity> getMediaList() {
        return mediaList;
    }

    public boolean isEnableCompress() {
        return enableCompress;
    }

    public int getCompressMaxPixel() {
        return compressMaxPixel;
    }

    public int getCompressMaxSize() {
        return compressMaxSize;
    }

    public int getCompressMaxHeight() {
        return compressMaxHeight;
    }

    public int getCompressMaxWidth() {
        return compressMaxWidth;
    }

    public boolean isCompreEnablePixel() {
        return compreEnablePixel;
    }

    public boolean isCompressEnableQuality() {
        return compressEnableQuality;
    }

    public boolean isCompressEnableReserveRaw() {
        return compressEnableReserveRaw;
    }

    public boolean isEnableCrop() {
        return enableCrop;
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public int getCropHeight() {
        return cropHeight;
    }

    public int getCropCompressQuality() {
        return cropCompressQuality;
    }

    public boolean isShowCropFrame() {
        return showCropFrame;
    }

    public boolean isShowCropGrid() {
        return showCropGrid;
    }

    public boolean isEnableUpload() {
        return enableUpload;
    }

    public boolean isEnableCameraHint() {
        return enableCameraHint;
    }

    public boolean isEnableCameraModel() {
        return enableCameraModel;
    }

    public boolean isEnablePictureRotate() {
        return enablePictureRotate;
    }

    public boolean isEnbalePictureMark() {
        return enbalePictureMark;
    }

    public boolean isEnablePictureBlur() {
        return enablePictureBlur;
    }

    public String getSavePath() {
        return savePath;
    }

    public OnPickerListener getOnPickerListener() {
        return onPickerListener;
    }

    public String getOutputCameraPath() {
        return outputCameraPath;
    }

    public String getTheme() {
        return theme;
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public int getMaxSelectNum() {
        return maxSelectNum;
    }

    public int getMinSelectNum() {
        return minSelectNum;
    }

    public int getVideoQuality() {
        return videoQuality;
    }

    public int getVideoSecond() {
        return videoSecond;
    }

    public int getRecordVideoSecond() {
        return recordVideoSecond;
    }

    public int getImageSpanCount() {
        return imageSpanCount;
    }

    public int getOverrideWidth() {
        return overrideWidth;
    }

    public int getOverrideHeight() {
        return overrideHeight;
    }

    public int getAspect_ratio_x() {
        return aspect_ratio_x;
    }

    public int getAspect_ratio_y() {
        return aspect_ratio_y;
    }

    public float getSizeMultiplier() {
        return sizeMultiplier;
    }

    public boolean isZoomAnim() {
        return zoomAnim;
    }

    public PhoenixOption fileType(int val) {
        fileType = val;
        return this;
    }

    public PhoenixOption enableCamera(boolean val) {
        enableCamera = val;
        return this;
    }

    public PhoenixOption outputCameraPath(String val) {
        outputCameraPath = val;
        return this;
    }

    public PhoenixOption theme(String val) {
        theme = val;
        return this;
    }

    public PhoenixOption selectionMode(int val) {
        selectionMode = val;
        return this;
    }

    public PhoenixOption maxSelectNum(int val) {
        maxSelectNum = val;
        return this;
    }

    public PhoenixOption minSelectNum(int val) {
        minSelectNum = val;
        return this;
    }

    public PhoenixOption videoQuality(int val) {
        videoQuality = val;
        return this;
    }

    public PhoenixOption videoSecond(int val) {
        videoSecond = val;
        return this;
    }

    public PhoenixOption recordVideoSecond(int val) {
        recordVideoSecond = val;
        return this;
    }

    public PhoenixOption imageSpanCount(int val) {
        imageSpanCount = val;
        return this;
    }

    public PhoenixOption overrideWidth(int val) {
        overrideWidth = val;
        return this;
    }

    public PhoenixOption overrideHeight(int val) {
        overrideHeight = val;
        return this;
    }

    public PhoenixOption aspect_ratio_x(int val) {
        aspect_ratio_x = val;
        return this;
    }

    public PhoenixOption aspect_ratio_y(int val) {
        aspect_ratio_y = val;
        return this;
    }

    public PhoenixOption sizeMultiplier(float val) {
        sizeMultiplier = val;
        return this;
    }

    public PhoenixOption zoomAnim(boolean val) {
        zoomAnim = val;
        return this;
    }

    public PhoenixOption enableGif(boolean val) {
        enableGif = val;
        return this;
    }

    public PhoenixOption enablePreview(boolean val) {
        enablePreview = val;
        return this;
    }

    public PhoenixOption enPreviewVideo(boolean val) {
        enPreviewVideo = val;
        return this;
    }

    public PhoenixOption enablePreviewAudio(boolean val) {
        enablePreviewAudio = val;
        return this;
    }

    public PhoenixOption checkNumMode(boolean val) {
        checkNumMode = val;
        return this;
    }

    public PhoenixOption openClickSound(boolean val) {
        openClickSound = val;
        return this;
    }

    public PhoenixOption freeStyleCropEnabled(boolean val) {
        freeStyleCropEnabled = val;
        return this;
    }

    public PhoenixOption circleDimmedLayer(boolean val) {
        circleDimmedLayer = val;
        return this;
    }

    public PhoenixOption hideBottomControls(boolean val) {
        hideBottomControls = val;
        return this;
    }

    public PhoenixOption rotateEnabled(boolean val) {
        rotateEnabled = val;
        return this;
    }

    public PhoenixOption scaleEnabled(boolean val) {
        scaleEnabled = val;
        return this;
    }

    public PhoenixOption previewEggs(boolean val) {
        previewEggs = val;
        return this;
    }

    public PhoenixOption enableDelete(boolean val) {
        enableDelete = val;
        return this;
    }

    public PhoenixOption currentIndex(int val) {
        currentIndex = val;
        return this;
    }

    public PhoenixOption mediaList(List<MediaEntity> val) {
        mediaList = val;
        return this;
    }

    public PhoenixOption enableCompress(boolean val) {
        enableCompress = val;
        return this;
    }

    public PhoenixOption compressMaxPixel(int val) {
        compressMaxPixel = val;
        return this;
    }

    public PhoenixOption compressMaxSize(int val) {
        compressMaxSize = val;
        return this;
    }

    public PhoenixOption compressMaxHeight(int val) {
        compressMaxHeight = val;
        return this;
    }

    public PhoenixOption compressMaxWidth(int val) {
        compressMaxWidth = val;
        return this;
    }

    public PhoenixOption compreEnablePixel(boolean val) {
        compreEnablePixel = val;
        return this;
    }

    public PhoenixOption compressEnableQuality(boolean val) {
        compressEnableQuality = val;
        return this;
    }

    public PhoenixOption compressEnableReserveRaw(boolean val) {
        compressEnableReserveRaw = val;
        return this;
    }

    public PhoenixOption enableCrop(boolean val) {
        enableCrop = val;
        return this;
    }

    public PhoenixOption cropWidth(int val) {
        cropWidth = val;
        return this;
    }

    public PhoenixOption cropHeight(int val) {
        cropHeight = val;
        return this;
    }

    public PhoenixOption cropCompressQuality(int val) {
        cropCompressQuality = val;
        return this;
    }

    public PhoenixOption showCropFrame(boolean val) {
        showCropFrame = val;
        return this;
    }

    public PhoenixOption showCropGrid(boolean val) {
        showCropGrid = val;
        return this;
    }

    public PhoenixOption enableUpload(boolean val) {
        enableUpload = val;
        if (val) {
            enableCompress = true;
        }
        return this;
    }

    public PhoenixOption enableCameraHint(boolean val) {
        enableCameraHint = val;
        return this;
    }

    public PhoenixOption enableCameraModel(boolean val) {
        enableCameraModel = val;
        return this;
    }

    public PhoenixOption enablePictureRotate(boolean val) {
        enablePictureRotate = val;
        return this;
    }

    public PhoenixOption enablePictureMark(boolean val) {
        enbalePictureMark = val;
        return this;
    }

    public PhoenixOption enablePictureBlur(boolean val) {
        enablePictureBlur = val;
        return this;
    }

    public PhoenixOption savePath(String val) {
        savePath = val;
        return this;
    }

    public PhoenixOption onPickerListener(OnPickerListener val) {
        onPickerListener = val;
        return this;
    }

    public void start(Context context, int type) {
        Starter starter = ReflectUtils.loadStarter(ReflectUtils.SCPicker);
        if (starter != null) {
            starter.start(context, type);
        }
    }
}
