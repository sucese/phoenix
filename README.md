# <img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/logo.png" alt="Phoenix" width="40" height="40" align="bottom" /> Phoenix

## 功能介绍

[![Download](https://api.bintray.com/packages/guoxiaoxing/maven/phoenix/images/download.svg)](https://bintray.com/guoxiaoxing/maven/phoenix/_latestVersion)
[![License](https://img.shields.io/github/license/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 
[![Stars](https://img.shields.io/github/stars/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 
[![Forks](https://img.shields.io/github/forks/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 

>Android平台上图片/视频选择，编辑和压缩的一站式解决方案。

- [English Document](https://github.com/guoxiaoxing/phoenix/blob/master/README_EN.md)

图片/视频的选择，编辑和压缩是日常开发中的常见需求，Phoenix完整的实现了这些功能，并提供了优雅的调用方式。Phoenix的核心功能基于Kotlin实现，外层接口基于Java实现，方便Kotlin与Java双方的调用。

**特点**

- 功能相互独立，各个功能的实现依赖于约定的接口，彼此互不依赖，开发者不必为了引入某一个功能而带入一堆依赖。
- 高度的UI定制性，内置四种配色方案，开发者也可以通过简单的style文件的简单配置来定制自己的UI。
- 调用的便利性，开启某个功能只需要调用enableXXX(true)方法，结果统一在MediaEntity里获取。
- 库体积轻便小巧，视频压缩基于系统的MediaCodec实现，压缩速度快，无额外的依赖。
- RxJava良好的支持性，每个功能都提供了同步与异步两种实现，便于开发者利用RxJava进行功能的组合与嵌套。
- ImageLoader可配置，定制项目需要的图片加载方案。
- 良好的版本兼容性，运行时权限等内容都做了兼容性处理。

<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/play_1.gif" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/play_2.gif" height="400"/>
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
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/function_1.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/function_2.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/function_3.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/function_4.png" height="400"/>
</p>

**主题**

- 默认主题
- 橙色主图
- 红色主题
- 蓝色主题

<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/theme_default.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/theme_red.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/theme_orange.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/theme_blue.png" height="400"/>
</p>

## 快递开始

### 添加依赖

```
//图片/视频选择、预览、编辑与拍照
compile 'com.github.guoxiaoxing:phoenix:1.0.2'

//选填 - 图片压缩，开启功能：Phoenix.with().enableCompress(true)，获取结果：MediaEntity.getCompressPath()
compile 'com.github.guoxiaoxing:phoenix-compress-picture:1.0.2'

//选填 - 视频压缩，开启功能：Phoenix.with().enableCompress(true)，获取结果：MediaEntity.getCompressPath()
compile 'com.github.guoxiaoxing:phoenix-compress-video:1.0.2'
```

### 调用功能

初始化

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Phoenix.config()
                .imageLoader(new ImageLoader() {
                    @Override
                    public void loadImage(Context context, ImageView imageView
                                                , String imagePath, int type) {
                        Glide.with(context)
                                .load(imagePath)
                                .into(imageView);
                    }
                });
    }
}
```

开启功能

```java
Phoenix.with()
        .theme(PhoenixOption.THEME_DEFAULT)// 主题
        .fileType(MimeType.ofAll())//显示的文件类型图片、视频、图片和视频
        .maxPickNumber(10)// 最大选择数量
        .minPickNumber(0)// 最小选择数量
        .spanCount(4)// 每行显示个数
        .enablePreview(true)// 是否开启预览
        .enableCamera(true)// 是否开启拍照
        .enableAnimation(true)// 选择界面图片点击效果
        .enableCompress(true)// 是否开启压缩
        .compressPictureFilterSize(1024)//多少kb以下的图片不压缩
        .compressVideoFilterSize(2018)//多少kb以下的视频不压缩
        .thumbnailHeight(160)// 选择界面图片高度
        .thumbnailWidth(160)// 选择界面图片宽度
        .enableClickSound(false)// 是否开启点击声音
        .pickedMediaList(mMediaAdapter.getData())// 已选图片数据
        .videoFilterTime(0)//显示多少秒以内的视频
        .start(MainActivity.this, PhoenixOption.TYPE_PICK_MEDIA, REQUEST_CODE);
```

获取结果

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
        //返回的数据
        List<MediaEntity> result = Phoenix.result(data);
        mMediaAdapter.setData(result);
    }
}
```

另外，Phoenix内置了图片压缩库与视频压缩库，这两个功能都可以单独调用。

图片压缩

```java
String path;
if(!TextUtils.isEmpty(mediaEntity.getEditPath())){
    path = mediaEntity.getEditPath();
}else {
    path = mediaEntity.getLocalPath();
}

File file = new File(path);
PictureCompresser.with(context)
        .load(file)
        .setCompressListener(new OnCompressListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "Picture compress onStart");
            }

            @Override
            public void onSuccess(File file) {
                Log.d(TAG, "Picture compress onSuccess");
                mediaEntity.setCompressed(true);
                mediaEntity.setCompressPath(file.getAbsolutePath());
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "Picture compress onError : " + e.getMessage());
            }
        }).launch();
```

视频压缩

```java
final MediaEntity result = mediaEntity;

final File compressFile;
try {
    File compressCachePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "phoenix");
    compressCachePath.mkdir();
    compressFile = File.createTempFile("compress", ".mp4", compressCachePath);
} catch (IOException e) {
    Toast.makeText(context, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
    return;
}
MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
    @Override
    public void onTranscodeProgress(double progress) {
        
    }

    @Override
    public void onTranscodeCompleted() {
        result.setCompressed(true);
        result.setCompressPath(compressFile.getAbsolutePath());
    }

    @Override
    public void onTranscodeCanceled() {

    }

    @Override
    public void onTranscodeFailed(Exception exception) {
    }
};
try {
    MediaTranscoder.getInstance().asyncTranscodeVideo(mediaEntity.getLocalPath(), compressFile.getAbsolutePath(),
            MediaFormatStrategyPresets.createAndroid480pFormatStrategy(), listener);
} catch (IOException e) {
    e.printStackTrace();
}
```

我们来重点看看视频压缩的测试数据

|手机型号|源视频时长|源视频大小|压缩视频大小|压缩耗时|
|:------|:--------|:--------|:---------|:------|
|小米4   |60s      |55.67MB  |5.56MB    |20.17s|
|荣耀7   |60s      |68.37MB  |6.22MB    |18.83s|
|oppo r9 |60s      |55.57MB  |5.35MB    |38.27s|

可以看到对于60s的视频，压缩时间控制在了18s~22s之间，性能好的手机用时短一些，另外，压缩提供了480p、720p等多种规格的压缩参数。


## 更新日志

扫描二维码下载Demo，或用手机浏览器输入这个网址:  https://fir.im/phoenix

注：fir.im下载次数如果满了可以去[仓库](https://github.com/guoxiaoxing/phoenix/raw/master/art/Phoenix.apk)下载

<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/apk.png"/>

## 贡献代码

欢迎加入改进本项目。

## License

```
Copyright 2017 Guoxiaoxing

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```