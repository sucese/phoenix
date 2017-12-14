# Android平台FFmpeg实践：基本原理与源码编译

**关于作者**

>郭孝星，程序员，吉他手，主要从事Android平台基础架构方面的工作，欢迎交流技术方面的问题，可以去我的[Github](https://github.com/guoxiaoxing)提issue或者发邮件至guoxiaoxingse@163.com与我交流。

**文章目录**

- 一 音视频编解码基本原理
- 二 FFmpeg源码编译

从这篇文章开始，我们开始来了解FFmpeg在Android平台上的应用。本篇文章介绍音视频编解码的基本原理，文章内并不会牵扯过于复杂的概念，旨在给大家带来音视频编解码
通识上的了解，为后续的FFmpeg使用做铺垫。

## 一 音视频编解码基本原理

首先第一个问题，我们看视频一般都是通过视频软件，那么一个视频是如何从视频服务商那里开始传递最终被我们看到的？🤔

一般说来，视频的传递及解码有以下四个流程：

- 解协议：将流媒体协议的数据，解析成标准的封装格式。音视频在网络上传播的时候会封装成各种流媒体协议，例如HTTP、RTMP或者MMS等。这些协议在传递音视频的同时还会携带信令数据（播放控制、网络状态等信息），解
协议的过程就是去掉这些数据，保留音视频数据，例如RTMP流媒体经过解协议，输出FLV格式的数据。
- 解封装：将封装好的音视频数据分离成音频流和视频流，封装格式也是我们常见的，例如MP4、MKV、FLV等，它们的作用是将音视频按照一定的格式放在一起，例如FLV格式的数据，解封装之后输出H264视频码流和AAC
音频码流。
- 解码：将压缩的音频流和视频流转换为非压缩的音频流和视频流。音频的压缩标准包括AAC，MP3，AC-3等，视频的压缩标准包括H.264，MPEG2，VC-1等。经过解码压缩的视频输出非压缩的颜色数据，例如YUV420P，RGB。
压缩的音频输出非压缩的音频抽样数据，例如PCM。
- 音视频同步：根据解封装得到的参数信息，同步解码出来的音频视频数据，并输出到系统的显卡和声卡进行播放。

上面各个流程中都有许许多多的标准，就目前的技术趋势而言，一般会有以下配置：

- 直播平台：RTMP流媒体协议 + FLV封装格式 + H.264视频编码格式 + AAC音频编码格式
- 点播平台：HTTP流媒体协议 + MP4/FLV封装格式 + H.264视频编码格式 + AAC音频编码格式

这里说一下RTMP作为点播平台的流媒体协议，在于它对Flash播放去的支持，因为Flash播放器被绝大多数浏览器支持，因为简化了直播平台客户端的操作。HTTP作为点播平台的流媒体协议，在于HTTP是基于TCP
协议的应用层协议，保证了视频传输的质量，另外，HTTP也被大多数Web服务器支持，简化的客户端的播放操作。

## 二 FFmpeg源码编译

而我们今天要学习的FFmpeg就是在音视频编解码方向有着统治地位的一套框架。

>A complete, cross-platform solution to record, convert and stream audio and video.

官方网站：http://ffmpeg.org/

在编译FFmpeg之前，你要掌握在Android Studio上的JNI开发，Android Studio对C++的支持已经非常好了，我们简单的说一说，注意我们这里说的是通过
Android Studio的CMake插件来进行JNI开发的方式，这是Android Studio 2.2以后推出来的功能，这种方式只需要简单的配置即可进行底层开发，无需再
编写Makefile。

1 首先在Android Studio创建项目时，选择添加C++支持，并选择C++ 11.

<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/ffmpeg/ffmpeg_build_01.png" width="500"/>
<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/ffmpeg/ffmpeg_build_02.png" width="500"/>

2 然后创建出来的项目是这个样子的，比常规的项目多了cpp的源码目录，以及存放编译出来的so库的目录。

<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/ffmpeg/ffmpeg_build_03.png" width="500"/>

build.gradle也有些变化，cppFlags指定了我们使用的C++版本，CMakeLists.txt用来定制原生代码的。

<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/ffmpeg/ffmpeg_build_04.png" width="500"/>

我们可以来看下CMakeLists.txt里面的信息。

```
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


add_library( # 设置底层库的名字，Java层loadLibrary()时使用
             native-lib

             # 设置这个库是动态库还是静态库
             SHARED

             # 设置编译这个库所需的源码目录
             src/main/cpp/native-lib.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # find_library主要用来引用其他底层库，这里引用的是底层log库
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # 指定要关联到原生库的库
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
```

更多关于CMake的信息可以参考：https://developer.android.google.cn/ndk/guides/cmake.html

3 等你做完以上的事情，剩下的事情就一目了然了，总体来说CMake插件的方式极大的简化了底层的开发，推荐大家使用这种方式。

<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/ffmpeg/ffmpeg_build_05.png"/>

