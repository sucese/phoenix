# Phoenix

## 功能介绍

>Android平台上图片/视频选择、压缩、编辑的一站式解决方案。

**特点**

- 功能相互独立，各个功能的实现依赖于约定的接口，彼此互不依赖，开发者不必为了引入某一个功能而带入一堆依赖。
- 高度的UI定制性，内置四种配色方案，开发者也可以通过简单的style文件的简单配置来定制自己的UI。
- 调用的便利性，开启某个功能只需要调用enableXXX(true)方法，结果统一在MediaEntity里获取。
- RxJava良好的支持性，每个功能都提供了同步与异步两种实现，便于开发者利用RxJava进行功能的组合与嵌套。
- 良好的版本兼容性，运行时权限等内容都做了兼容性处理。

**功能**

- 图片选择
- 图片预览
- 图片裁剪
- 图片上传
- 图片批量压缩
- 拍照
- 图片涂抹/标记
- 视频选择
- 视频预览
- 视频上传
- 视频批量压缩
- 支持gif
- 支持webp
- 点击音效


内置四种主题

- 大风车主题
- 车牛主题
- 弹个车主题
- 搜车贷主题

## 快递开始

### 添加依赖

```
//选填 - 图片/视频选择、拍照、图片/视频预览

//选填 - 图片/视频上传，开启上传功能会默认开启压缩功能，因此添加上传依赖库时要同时添加压缩依赖库，开启功能：SCPicker.with().enableUpload(true)，获取结果：MediaEntity.getOnlinePath()

//选填 - 图片压缩，开启功能：SCPicker.with().enableCompress(true)，获取结果：MediaEntity.getCompressPath()

//选填 - 视频压缩，开启功能：SCPicker.with().enableCompress(true)，获取结果：MediaEntity.getCompressPath()
```

### 调用功能

```java
SCPicker.with()
        .theme(PhoenixOption.THEME_DEFAULT)
        .enableCompress(true)
        .enableUpload(true)
        .currentIndex(1)
        .mediaList(model)
        .nnPickerListener(new OnPickerListener() {
            @Override
            public void onPickSuccess(List<MediaEntity> pickList) {

            }

            @Override
            public void onPickFailed(String errorMessage) {

            }
        })
        .start(MainActivity.this, PhoenixOption.TYPE_TAKE_PICTURE);
```

最后的start()方法用来完成启动某项功能，根据type不同启动不同的功能，具体含义如下：

```
//功能 - 选择图片/视频/音频
public static final int TYPE_PICK_MEDIA = 0x000001;
//功能 - 拍照
public static final int TYPE_TAKE_PICTURE = 0x000002;
//功能 - 预览
public static final int TYPE_BROWSER_PICTURE = 0x000003;
```

定制界面与配置功能也十分简单。

#### 定制界面

theme()方法可以用来配置主题，定制界面。

内置四种主题：

```
//主题 - 大风车主题
public static final String THEME_DEFAULT = "THEME_DEFAULT";
//主题 - 车牛主题
public static final String THEME_RED = "THEME_RED";
//主题 - 弹个车主题
public static final String THEME_ORANGE = "THEME_ORANGE";
//主题 - 搜车贷主题
public static final String THEME_BLUE = "THEME_BLUE";
```

如果内置主题不满足需求，可以添加以下配置到你的style.xml文件。

```xml
  <!--默认样式-->
    <style name="picker.style.default" parent="Theme.AppCompat.Light.DarkActionBar">
        <!-- Customize your theme here. -->
        <!--标题栏背景色-->
        <item name="colorPrimary">@color/bar_grey</item>
        <!--状态栏背景色-->
        <item name="colorPrimaryDark">@color/bar_grey</item>
        <!--是否改变图片列表界面状态栏字体颜色为黑色-->
        <item name="picture.statusFontColor">false</item>
        <!--返回键图标-->
        <item name="picture.leftBack.icon">@drawable/picture_back</item>
        <!--标题下拉箭头-->
        <item name="picture.arrow_down.icon">@drawable/arrow_down</item>
        <!--标题上拉箭头-->
        <item name="picture.arrow_up.icon">@drawable/arrow_up</item>
        <!--标题文字颜色-->
        <item name="picture.title.textColor">@color/white</item>
        <!--标题栏右边文字-->
        <item name="picture.right.textColor">@color/white</item>
        <!--图片列表勾选样式-->
        <item name="picture.checked.style">@drawable/checkbox_selector</item>
        <!--开启图片列表勾选数字模式-->
        <item name="picture.style.checkNumberMode">false</item>
        <!--选择图片样式0/9-->
        <item name="picture.style.numComplete">false</item>
        <!--图片列表底部背景色-->
        <item name="picture.bottom.bg">@color/color_fa</item>
        <!--图片列表预览文字颜色-->
        <item name="picture.preview.textColor">@color/tab_color_true</item>
        <!--图片列表已完成文字颜色-->
        <item name="picture.complete.textColor">@color/tab_color_true</item>
        <!--图片已选数量圆点背景色-->
        <item name="picture.number.style">@drawable/num_oval</item>
        <!--预览界面标题文字颜色-->
        <item name="picture.ac_preview.title.textColor">@color/white</item>
        <!--预览界面已完成文字颜色-->
        <item name="picture.ac_preview.complete.textColor">@color/tab_color_true</item>
        <!--预览界面标题栏背景色-->
        <item name="picture.ac_preview.title.bg">@color/bar_grey</item>
        <!--预览界面底部背景色-->
        <item name="picture.ac_preview.bottom.bg">@color/bar_grey_90</item>
        <!--预览界面状态栏颜色-->
        <item name="picture.status.color">@color/bar_grey_90</item>
        <!--预览界面返回箭头-->
        <item name="picture.preview.leftBack.icon">@drawable/picture_back</item>
        <!--是否改变预览界面状态栏字体颜色为黑色-->
        <item name="picture.preview.statusFontColor">false</item>
        <!--裁剪页面标题背景色-->
        <item name="picture.crop.toolbar.bg">@color/bar_grey</item>
        <!--裁剪页面状态栏颜色-->
        <item name="picture.crop.status.color">@color/bar_grey</item>
        <!--裁剪页面标题文字颜色-->
        <item name="picture.crop.title.color">@color/white</item>
        <!--相册文件夹列表选中图标-->
        <item name="picture.folder_checked_dot">@drawable/orange_oval</item>
    </style>
```

#### 配置功能

我们可以调用与变量同名的方法来配置选项。

选项含义：

```java

private int fileType;//选择列表显示的文件类型，全部：MimeType.ofAll()、图片：MimeType.ofImage()、视频：MimeType.ofVideo()，音频：MimeType.ofAudio()
private boolean enableCamera = false;//是否显示拍照按钮
private String outputCameraPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();//拍照保存路径
private int theme = THEME_DEFAULT;//主题样式，有默认样式、大风车样式、车牛样式、弹个车样式，可定制
private int selectionMode = PickerConstant.MULTIPLE;//选择类型，单选、多选
private int maxSelectNum = 0;//最大选择张数，默认为0，表示不限制
private int minSelectNum = 0;//最小选择张数，默认为0，表示不限制
private int videoQuality;//视频录制质量 0/1
private int videoSecond;//显示多少秒以内的视频or音频也可适用
private int recordVideoSecond;//视频秒数录制 默认60s
private int imageSpanCount = 4;//图片选择界面每行图片个数
private int overrideWidth = 160; //选择列表图片宽度
private int overrideHeight = 160; //选择列表图片高度
private float sizeMultiplier;//glide加载图片大小，0-1之间，如果设置，则overrideWidth与overrideHeight无效
private boolean zoomAnim = true;//选择列表点击动画效果
private boolean enableGif;//是否显示gif图片
private boolean enablePreview = true;//是否开启点击预览
private boolean enPreviewVideo = true;//是否开启视频点击预览
private boolean enablePreviewAudio = true;//是否开启音频预览
private boolean checkNumberMode;//是否开启数字显示模式
private boolean openClickSound = true;//是否开启点击声音
private boolean previewEggs = true;//预览图片时，是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)

private int currentIndex;//当前索引
private List<MediaEntity> mediaList = new ArrayList<>();//已选择的数据、图片/视频/音频预览的数据

private boolean enableCompress;//是否开启压缩
private int compressMaxPixel;
private int compressMaxSize;//压缩最大值kb
private int compressMaxHeight;//压缩最大高度
private int compressMaxWidth;//压缩最大宽度
private boolean compreEnablePixel;
private boolean compressEnableQuality;
private boolean compressEnableReserveRaw;

private boolean enableCrop;//是否开启裁剪
private int cropWidth;//裁剪宽度
private int cropHeight;//裁剪高度
private int cropCompressQuality;//裁剪压缩质量 默认90
private boolean showCropFrame = false;//是否显示裁剪矩形边框
private boolean showCropGrid;//是否显示裁剪矩形网格
private boolean rotateEnabled = true;//裁剪是否可旋转图片
private boolean scaleEnabled = true;//裁剪是否可放大缩小图片
private int aspect_ratio_x;//裁剪比例 如16:9 3:2 3:4 1:1 可自定义 x/y
private int aspect_ratio_y;//裁剪比例 如16:9 3:2 3:4 1:1 可自定义 x/y
private boolean freeStyleCropEnabled;//裁剪框是否可拖拽
private boolean circleDimmedLayer;//是否圆形裁剪
private boolean hideBottomControls = false;//是否显示uCrop工具栏

private boolean enableUpload;//是否开启上传，开启上传会默认开启压缩
private boolean showCarGuide;//给车辆拍照时是否显示拍照提示（车牛）
private boolean showMarkButton;//是否显示标记按钮
private boolean showBlurButton;//是否显示涂抹按钮
private String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();//拍照、压缩、裁剪后的保存路径
private OnPickerListener onPickerListener;//选择监听
```

注：在SCPicker里开启功能，在OnPickerListener的回调方法onPickSuccess(List<MediaEntity> pickList) 中获取结果。

例如

- 开启压缩功能：SCPicker.with().enableCompress(true) -> 获取压缩结果：mediaEntity.getCompressPath()
- 开启上传功能：SCPicker.with().enableUpload(true)   -> 获取上传结果：mediaEntity.getOnlinePath()

如果对内部实现感兴趣，请移步[图片/视频库实现原理](http://git.souche.com/souche-wireless-component/souche-support-component-library/tree/master/doc/图片库实现原理.md)。

另外，图片压缩、视频压缩、图片/视频上传等功能都基于相同的Processor接口实现。

```java
public interface OnProcessorListener {

    /**
     * Call when operation is on start
     *
     * @param mediaEntity mediaEntity
     */
    void onStart(MediaEntity mediaEntity);

    /**
     * Call when operation is on progress
     *
     * @param progress progress
     */
    void onProgress(int progress);

    /**
     * Call when operation is on success
     *
     * @param mediaEntity mediaEntity
     */
    void onSuccess(MediaEntity mediaEntity);

    /**
     * Call when operation is on failed
     *
     * @param errorMessage errorMessage
     */
    void onFailed(String errorMessage);
}

```

所有它们都用相同的调用方式。

```
//压缩图片
final Processor compressPictureProcessor = ReflectUtils.loadProcessor(ReflectUtils.PictureCompressProcessor);
//压缩视频
final Processor compressVideoProcessor = ReflectUtils.loadProcessor(ReflectUtils.VideoCompressProcessor);
```

## 更新日志

## 贡献代码

## Thanks

- [PhotoView](https://github.com/chrisbanes/PhotoView)
- [uCrop](https://github.com/Yalantis/uCrop)

## License
