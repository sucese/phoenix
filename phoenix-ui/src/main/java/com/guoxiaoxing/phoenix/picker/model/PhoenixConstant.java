package com.guoxiaoxing.phoenix.picker.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class PhoenixConstant {

    public static final String PHOENIX_RESULT = "PHOENIX_RESULT";
    public static final String PHOENIX_OPTION = "PHOENIX_OPTION";

    public static final int IMAGE_PROCESS_TYPE_DEFAULT = 0x000000;


    public static final int REQUEST_CODE_PICTURE_EDIT = 0x000011;
    public static final int RESULT_CODE_PICTURE_EDIT = 0x000022;


    /**
     * 拍摄照片的最大数量
     */
    public static final String KEY_TAKE_PICTURE_MAX_NUM = "KEY_TAKE_PICTURE_MAX_NUM";

    /**
     * 文件路径
     */
    public static final String KEY_FILE_PATH = "KEY_FILE_PATH";

    /**
     * 文件byte数组
     */
    public static final String KEY_FILE_BYTE = "KEY_FILE_BYTE";

    /**
     * 当前索引/位置
     */
    public static final String KEY_POSITION = "KEY_POSITION";

    /**
     * 图片方向
     */
    public static final String KEY_ORIENTATION = "KEY_ORIENTATION";

    /**
     * 图片/视频列表
     */
    public final static String KEY_LIST = "KEY_LIST";

    /**
     * 已选择的图片/视频列表
     */
    public final static String KEY_SELECT_LIST = "KEY_SELECT_LIST";

    public static final String BITMPA_KEY = "bitmap_key";
    public static final String ROTATION_KEY = "rotation_key";
    public static final String IMAGE_INFO = "image_info";

    public final static String FC_TAG = "picture";
    public final static String EXTRA_RESULT_SELECTION = "extra_result_media";
    public final static String EXTRA_LOCAL_MEDIAS = "localMedias";

    public final static String EXTRA_ON_PICTURE_DELETE_LISTNER = "onPictureDeleteListener";
    public final static String EXTRA_MEDIA = "media";
    public final static String DIRECTORY_PATH = "directory_path";
    public final static String BUNDLE_CAMERA_PATH = "CameraPath";
    public final static String BUNDLE_ORIGINAL_PATH = "OriginalPath";
    public final static String EXTRA_BOTTOM_PREVIEW = "bottom_preview";
    public final static String EXTRA_PICKER_OPTION = "PictureSelectorConfig";
    public final static String AUDIO = "audio";
    public final static String IMAGE = "image";
    public final static String VIDEO = "video";

    public final static int FLAG_PREVIEW_UPDATE_SELECT = 2774;// 预览界面更新选中数据 标识
    public final static int CLOSE_PREVIEW_FLAG = 2770;// 关闭预览界面 标识
    public final static int FLAG_PREVIEW_COMPLETE = 2771;// 预览界面图片 标识
    public final static int TYPE_ALL = 0;
    public final static int TYPE_IMAGE = 1;
    public final static int TYPE_VIDEO = 2;
    public final static int TYPE_AUDIO = 3;

    public static final int MAX_COMPRESS_SIZE = 102400;
    public static final int TYPE_CAMERA = 1;
    public static final int TYPE_PICTURE = 2;

    public final static int SINGLE = 1;
    public final static int MULTIPLE = 2;

    public final static int LUBAN_COMPRESS_MODE = 1;
    public final static int SYSTEM_COMPRESS_MODE = 2;

    public final static int CHOOSE_REQUEST = 188;
    public final static int REQUEST_CAMERA = 909;
    public final static int READ_EXTERNAL_STORAGE = 0x01;
    public final static int CAMERA = 0x02;

    //车辆图片裁剪
    public static final String ALIYUN_RESOLUTION = "@225w_170h_1e_1c_2o";
    public static final String ALIYUN_PREVIEW = "@1000h_1e_1c_2o";
    public static final String DOMAIN = "http://souche.oss-cn-hangzhou.aliyuncs.com/";
    public static final String IMAGE_REPLY_URL = "http://img.souche.com/";

    //compress
    public static final int FIRST_GEAR = 1; // 一档
    public static final int THIRD_GEAR = 3; // 三档
    public static final int CUSTOM_GEAR = 4;// 四档

    @IntDef({FIRST_GEAR, THIRD_GEAR, CUSTOM_GEAR})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @Documented
    @Inherited
    @interface GEAR {

    }
}
