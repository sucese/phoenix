# Phoenix

## 功能介绍

[![](https://jitpack.io/v/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix)

>Android平台上图片/视频选择、压缩、编辑的一站式解决方案。


**特点**

- 功能相互独立，各个功能的实现依赖于约定的接口，彼此互不依赖，开发者不必为了引入某一个功能而带入一堆依赖。
- 高度的UI定制性，内置四种配色方案，开发者也可以通过简单的style文件的简单配置来定制自己的UI。
- 调用的便利性，开启某个功能只需要调用enableXXX(true)方法，结果统一在MediaEntity里获取。
- RxJava良好的支持性，每个功能都提供了同步与异步两种实现，便于开发者利用RxJava进行功能的组合与嵌套。
- 良好的版本兼容性，运行时权限等内容都做了兼容性处理。


<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/play_1.gif" height="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/play_2.gif" height="500"/>
</p>


**功能**

- 拍照
- 图片选择
- 图片预览
- 图片压缩
- 图片标记、贴图、涂抹与裁剪
- 视频选择
- 视频预览
- 视频压缩

<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/function_1.png" height="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/function_2.png" height="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/function_3.png" height="500"/>
</p>

<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/function_4.png" height="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/function_5.png" height="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/function_6.png" height="500"/>
</p>

多种主题

<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/theme_default.png" height="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/theme_red.png" height="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/theme_orange.png" height="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/blob/master/art/theme_blue.png" height="500"/>
</p>

## 快递开始

### 添加依赖

```
//图片/视频选择、拍照、图片/视频预览
compile 'com.github.guoxiaoxing.phoenix:phoenix-ui:0.0.5'

//选填 - 图片压缩，开启功能：Phoenix.with().enableCompress(true)，获取结果：MediaEntity.getCompressPath()
compile 'com.github.guoxiaoxing.phoenix:phoenix-compress-picture:0.0.5'

//选填 - 视频压缩，开启功能：Phoenix.with().enableCompress(true)，获取结果：MediaEntity.getCompressPath()
compile 'com.github.guoxiaoxing.phoenix:phoenix-compress-video:0.0.5'
```

### 调用功能

```java
Phoenix.with()
        .theme(PhoenixOption.THEME_DEFAULT )// 主题样式
        .fileType(MimeType.ofAll())
        .maxSelectNum(10)// 最大图片选择数量
        .minSelectNum(0)// 最小选择数量
        .spanCount(4)// 每行显示个数
        .pickMode(PhoenixConstant.MULTIPLE)// 多选 or 单选
        .enablePreview(true)// 是否可预览图片
        .enableCamera(true)// 是否显示拍照按钮
        .zoomAnim(true)// 图片列表点击 缩放效果 默认true
        .enableCompress(true)// 是否压缩
        .overrideHeight(160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
        .overrideWidth(160)
        .enableGif(true)// 是否显示gif图片
        .enableClickSound(true)//ƒ 是否开启点击声音
        .pickedMediaList(pickList)// 是否传入已选图片
        .previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
        .compressMaxSize(10 * 1000)//压缩最大值kb compressGrade()为Luban.CUSTOM_GEAR有效
        .compressMaxHeight(500)
        .compressMaxWidth(300)
        .videoSecond(0)//显示多少秒以内的视频or音频也可适用
        .recordVideoSecond(2 * 60)//录制视频秒数 默认60s
        .onPickerListener(new OnPickerListener() {
            @Override
            public void onPickSuccess(List<MediaEntity> pickList) {
                adapter.setList(pickList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onPickFailed(String errorMessage) {

            }
        }).start(MainActivity.this, PhoenixOption.TYPE_PICK_MEDIA);
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

## 更新日志

## 贡献代码

## Thanks

## License
