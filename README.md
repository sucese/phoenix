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
- RxJava良好的支持性，每个功能都提供了同步与异步两种实现，便于开发者利用RxJava进行功能的组合与嵌套。
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
compile 'com.github.guoxiaoxing:phoenix:1.0.1'

//选填 - 图片压缩，开启功能：Phoenix.with().enableCompress(true)，获取结果：MediaEntity.getCompressPath()
compile 'com.github.guoxiaoxing:phoenix-compress-picture:1.0.1'

//选填 - 视频压缩，开启功能：Phoenix.with().enableCompress(true)，获取结果：MediaEntity.getCompressPath()
compile 'com.github.guoxiaoxing:phoenix-compress-video-hard:1.0.1'
```

### 调用功能

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