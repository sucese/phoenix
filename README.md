# <img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/logo.png" alt="Phoenix" width="40" height="40" align="bottom" /> Phoenix

## Introduce

[![Jitpack version](https://jitpack.io/v/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 
[![License](https://img.shields.io/github/license/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 
[![Stars](https://img.shields.io/github/stars/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 
[![Forks](https://img.shields.io/github/forks/guoxiaoxing/phoenix.svg)](https://jitpack.io/#guoxiaoxing/phoenix) 

>The one-stop solution for image/video selection, editing and compression on the Android platform.

Image/video selection, editing and compression is a common requirement in the business, and Phoenix fully implements these features and provides elegant invocation 
styles. The core function of Phoenix is based on the Kotlin implementation, which is based on the Java implementation and facilitates the calls between Kotlin and 
Java.

**Feature**

- Functions are independent of each other, and the implementation of each function depends on the agreed interface.
- High level of UI customization, built-in four color schemes, developers can customize their own UI through simple configuration of simple style files.
- The convenience of invocation, the ability to turn on a function requires only the invocation of the enableXXX(true) method, which is obtained in the MediaEntity.
- Support for RxJava, each feature provides synchronous and asynchronous implementations for developers to make use of RxJava for functional composition and nesting.
- Support for runtime permission

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

- Default theme
- Orange theme
- Red theme
- Blue theme

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
compile 'com.github.guoxiaoxing.phoenix:phoenix-ui:0.0.12'

//Optional - image compression, open function: Phoenix, with (). EnableCompress (true), obtain results: MediaEntity. GetCompressPath ()
compile 'com.github.guoxiaoxing.phoenix:phoenix-compress-picture:0.0.12'

//Optional - video compression, open function: Phoenix, with (). EnableCompress (true), obtain results: MediaEntity. GetCompressPath ()
compile 'com.github.guoxiaoxing.phoenix:phoenix-compress-video:0.0.12'
```

### Start

```java
Phoenix.with()
        .theme(PhoenixOption.THEME_DEFAULT)// theme
        .fileType(MimeType.ofAll())//Display file type images, video, image and video
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
        .videoSecond(0)//Show video within seconds
        .onPickerListener(new OnPickerListener() {
            @Override
            public void onPickSuccess(List<MediaEntity> pickList) {
                adapter.setList(pickList);
                adapter.notifyDataSetChanged();
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