package com.guoxiaoxing.phoenix.core;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;

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

    //主题颜色 - 默认
    public static final int THEME_DEFAULT = Color.parseColor("#333333");
    //主题 - 中国红主题
    public static final int THEME_RED = Color.parseColor("#FF4040");
    //主题 - 青春橙主题
    public static final int THEME_ORANGE = Color.parseColor("#FF571A");
    //主题 - 天空蓝主题
    public static final int THEME_BLUE = Color.parseColor("#538EEB");

    //选择列表显示的文件类型，全部：MimeType.ofAll()、图片：MimeType.ofImage()、视频：MimeType.ofVideo()，音频：MimeType.ofAudio()
    private int fileType = MimeType.ofImage();
    //是否显示拍照按钮
    private boolean enableCamera = false;
    //主题样式，有默认样式、大风车样式、车牛样式、弹个车样式，可定制
    private int theme = THEME_DEFAULT;
    //最大选择张数，默认为0，表示不限制
    private int maxPickNumber = 0;
    //最小选择张数，默认为0，表示不限制
    private int minPickNumber = 0;
    //显示多少秒以内的视频
    private int videoFilterTime;
    //视频秒数录制 默认60s
    private int recordVideoTime = 60;
    //图片选择界面每行图片个数
    private int spanCount = 4;
    //选择列表图片宽度
    private int thumbnailWidth = 160;
    //选择列表图片高度
    private int thumbnailHeight = 160;
    //选择列表点击动画效果
    private boolean enableAnimation = true;
    //是否显示gif图片
    private boolean enableGif;
    //是否开启点击预览
    private boolean enablePreview = true;
    //是否开启数字显示模式
    private boolean pickNumberMode;
    //是否开启点击声音
    private boolean enableClickSound = true;
    //预览图片时，是否增强左右滑动图片体验
    private boolean previewEggs = true;

    //是否开启压缩
    private boolean enableCompress;
    //视频压缩阈值（kb以下的视频不进行压缩）
    private int compressVideoFilterSize;
    //图片压缩阈值（kb以下的图片不进行压缩）
    private int compressPictureFilterSize;

    //已选择的数据、图片/视频/音频预览的数据
    private List<MediaEntity> pickedMediaList = new ArrayList<>();

    //拍照、视频的保存地址
    private String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    //选择监听
    private OnPickerListener onPickerListener;

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

    public boolean isPickNumberMode() {
        return pickNumberMode;
    }

    public boolean isEnableClickSound() {
        return enableClickSound;
    }

    public boolean isPreviewEggs() {
        return previewEggs;
    }

    public List<MediaEntity> getPickedMediaList() {
        return pickedMediaList;
    }

    public boolean isEnableCompress() {
        return enableCompress;
    }

    public int getCompressVideoFilterSize() {
        return compressVideoFilterSize;
    }

    public int getCompressPictureFilterSize() {
        return compressPictureFilterSize;
    }

    public String getSavePath() {
        return savePath;
    }

    public OnPickerListener getOnPickerListener() {
        return onPickerListener;
    }

    public int getTheme() {
        return theme;
    }

    public int getMaxPickNumber() {
        return maxPickNumber;
    }

    public int getMinPickNumber() {
        return minPickNumber;
    }

    public int getVideoFilterTime() {
        return videoFilterTime;
    }

    public int getRecordVideoTime() {
        return recordVideoTime;
    }

    public int getSpanCount() {
        return spanCount;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public boolean isEnableAnimation() {
        return enableAnimation;
    }

    public PhoenixOption fileType(int val) {
        fileType = val;
        return this;
    }

    public PhoenixOption enableCamera(boolean val) {
        enableCamera = val;
        return this;
    }

    public PhoenixOption theme(int val) {
        theme = val;
        return this;
    }

    public PhoenixOption maxPickNumber(int val) {
        maxPickNumber = val;
        return this;
    }

    public PhoenixOption minPickNumber(int val) {
        minPickNumber = val;
        return this;
    }

    public PhoenixOption videoFilterTime(int val) {
        videoFilterTime = val;
        return this;
    }

    public PhoenixOption recordVideoTime(int val) {
        recordVideoTime = val;
        return this;
    }

    public PhoenixOption spanCount(int val) {
        spanCount = val;
        return this;
    }

    public PhoenixOption thumbnailWidth(int val) {
        thumbnailWidth = val;
        return this;
    }

    public PhoenixOption thumbnailHeight(int val) {
        thumbnailHeight = val;
        return this;
    }

    public PhoenixOption enableAnimation(boolean val) {
        enableAnimation = val;
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

    public PhoenixOption pickNumberMode(boolean val) {
        pickNumberMode = val;
        return this;
    }

    public PhoenixOption enableClickSound(boolean val) {
        enableClickSound = val;
        return this;
    }

    public PhoenixOption previewEggs(boolean val) {
        previewEggs = val;
        return this;
    }

    public PhoenixOption pickedMediaList(List<MediaEntity> val) {
        pickedMediaList = val;
        return this;
    }

    public PhoenixOption enableCompress(boolean val) {
        enableCompress = val;
        return this;
    }

    public PhoenixOption compressVideoFilterSize(int val) {
        compressVideoFilterSize = val;
        return this;
    }

    public PhoenixOption compressPictureFilterSize(int val) {
        compressPictureFilterSize = val;
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
        Starter starter = ReflectUtils.loadStarter(ReflectUtils.Phoenix);
        if (starter != null) {
            starter.start(context, type);
        }
    }
}
