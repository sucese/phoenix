# <img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/logo.png" alt="Phoenix" width="40" height="40" align="bottom" /> Phoenix

## Introduce

[![Jitpack version](https://jitpack.io/v/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 
[![License](https://img.shields.io/github/license/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 
[![Stars](https://img.shields.io/github/stars/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 
[![Forks](https://img.shields.io/github/forks/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 

>The one-stop solution for image/video selection, editing and compression on the Android platform.

Picture/video selection, editing and compression is a common requirement in the daily development, Phoenix fully implements these features and provides 
an elegant way of calling. The core function of Phoenix is implemented by Kotlin and the outer interface is implemented by Java, so it's easy to use
between Kotlin and Java.

**Feature**

- The implementation of each function depends on the agreed interface，the functions are independent of each other.
- Built-in four color schemes, developers can also customize their own UI through simple cameraConfig of simple style files.
- Use  the enableXXX(true) method to turn on a function and get the result from the MediaEntity.
- Support for RxJava, each feature provides synchronous and asynchronous implementations for developers to make use of RxJava for functional composition and nesting.
- Support for runtime permission.

<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/play_1.gif" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/play_2.gif" height="400"/>
</p>

**Function**

- Take pictures
- Picture select
- Picture preview
- Picture compression
- Picture marking, mapping, smearing and cutting
- Video select
- Video preview
- Video compression

<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/function_1.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/function_2.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/function_3.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/function_4.png" height="400"/>
</p>

**Theme**

- Default themeColor
- Orange themeColor
- Red themeColor
- Blue themeColor

<p align="center">
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/theme_default.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/theme_red.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/theme_orange.png" height="400"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/theme_blue.png" height="400"/>
</p>

## Getting started

### Dependency

Add it in your root build.gradle at the end of repositories

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency

```
//Picture/video selection, preview, edit and take photos
implementation 'com.github.guoxiaoxing.phoenix:phoenix-ui:0.0.13'

//Optional - image compression, open function: Phoenix,with().enableCompress(true)
//obtain results: MediaEntity.getCompressPath()
implementation 'com.github.guoxiaoxing.phoenix:phoenix-compress-picture:0.0.13'

//Optional - video compression, open function: Phoenix,with().enableCompress(true)
//obtain results: MediaEntity.getCompressPath()
implementation 'com.github.guoxiaoxing.phoenix:phoenix-compress-video:0.0.13'
```

### Start

```java
Phoenix.with()
        .themeColor(PhoenixOption.THEME_DEFAULT)// themeColor
        .fileType(MimeType.ofAll())//Display file type allList, video, image and video
        .maxPickNumber(10)// Maximum number of options
        .minPickNumber(0)// Minimum number of options
        .spanCount(4)// The number of displays per row
        .pickMode(PhoenixConstant.MULTIPLE)// Multiple choice/option
        .enablePreview(true)// Whether to open a preview
        .enableCamera(true)// Whether to open a photo or not
        .enableAnimation(true)// Select interface image to click effect
        .enableCompress(true)// Open compression
        .thumbnailHeight(160)// Select the image height of the interface
        .thumbnailWidth(160)// Select interface image width
        .enableClickSound(true)//ƒ Whether to turn on the click sound
        .pickedMediaList(pickList)// Selected image data
        .videoFilterTime(0)//Show video within seconds
        .onPickerListener(new OnPickerListener() {
            @Override
            public void onPickSuccess(List<MediaEntity> pickList) {
                mAdapter.setList(pickList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPickFailed(String errorMessage) {

            }
        })//Open function, TYPE_PICK_MEDIA - select the image/video/audio TYPE_TAKE_PICTURE - to take a picture
        .start(MainActivity.this, PhoenixOption.TYPE_PICK_MEDIA);
```

## Update

[Change log](https://github.com/guoxiaoxing/phoenix/wiki/Change-log)

Scan the qr code to download the Demo or enter the site with your mobile browser:  https://fir.im/phoenix

Note: you can also download the [demo](https://github.com/guoxiaoxing/phoenix/raw/master/art/Phoenix.apk) from github.

<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/apk.png"/>

## Contribute

Welcome to join in the improvement of this project.

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