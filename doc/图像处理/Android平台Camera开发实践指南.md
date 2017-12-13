# Android平台Camera开发实践指南

**关于作者**

>郭孝星，程序员，吉他手，主要从事Android平台基础架构方面的工作，欢迎交流技术方面的问题，可以去我的[Github](https://github.com/guoxiaoxing)提issue或者发邮件至guoxiaoxingse@163.com与我交流。

**文章目录**


Android系统提供了两种方式实现拍照/视频：

- 通过Intent调用系统组件，优点是快速方便，适合于直接获取图片的场景。
- 通过相机API自定义相机，优点是定制性强，适合于需要定制相机界面或者特殊相机功能的场景（例如：滤镜、贴纸）。

另外在Android系统中存在着两套相机API：

- Camera
- Camera2

那我们在开发中应该使用哪一种呢？🤔事

实上是两个都用的，Camera2是Android 5.0之后才推出的API，因此我们需要做向下兼容。Android 5.0以下使用Camera、Android 5.0以上使用Camera2。

相机开发的一般流程是什么样的？

1. 检测并访问相机资源 检查手机是否存在相机资源，如果存在则请求访问相机资源。
2. 创建预览界面，创建继承自SurfaceView并实现SurfaceHolder接口的拍摄预览类。有了拍摄预览类，即可创建一个布局文件，将预览画面与设计好的用户界面控件融合在一起，实时显示相机的预览图像。
3. 设置拍照监听器，给用户界面控件绑定监听器，使其能响应用户操作, 开始拍照过程。
4. 拍照并保存文件，将拍摄获得的图像转换成位图文件，最终输出保存成各种常用格式的图片。
5. 释放相机资源，相机是一个共享资源，当相机使用完毕后，必须正确地将其释放，以免其它程序访问使用时发生冲突。

相机开发一般需要注意哪些问题？

1. 版本兼容性问题，Android 5.0以上使用Camera2，Android 5.0要做Camera兼容。Android 6.0以上要做相机等运行时权限兼容。
2. 设备兼容性问题，Camera/Camera2里的各种特性在有些手机厂商的设备实现方式和支持程度是不一样的，这个需要做兼容性测试，一点点踩坑。

那么如何处理兼容性问题呢，一个比较好的思路就是利用多态的思想，利用接口定义统一的功能，针对不同版本提供不同的实现，使用的时候也是根据不同的版本
来创建不同的实例。

我们不难发现，这个接口一般要定义以下功能：

- 打开相机
- 关闭相机
- 开启预览
- 关闭预览
- 拍照
- 开始视频录制
- 结束视频录制

当然了，实际的应用应该还有一些参数设置的功能，我们先挑主要的看。接下来，我们就Camera/Camera2如何实现这个接口进行展开分析。

## 一 Camera实践指南

Camera API中主要涉及以下几个关键类：

- Camera：操作和管理相机资源，支持相机资源切换，设置预览和拍摄尺寸，设置光圈、曝光等相关参数。
- SurfaceView：用于绘制相机预览图像，提供实时预览的图像。
- SurfaceHolder：用于控制Surface的一个抽象接口，它可以控制Surface的尺寸、格式与像素等，并可以监视Surface的变化。
- SurfaceHolder.Callback：用于监听Surface状态变化的接口。

SurfaceView和普通的View相比有什么区别呢？🤔

>普通View都是共享一个Surface的，所有的绘制也都在UI线程中进行，因为UI线程还要处理其他逻辑，因此对View的更新速度和绘制帧率无法保证。这显然不适合相机实时
预览这种情况，因而SurfaceView持有一个单独的Surface，它负责管理这个Surface的格式、尺寸以及显示位置，它的Surface绘制也在单独的线程中进行，因而拥有更高
的绘制效率和帧率。

SurfaceHolder.Callback接口里定义了三个函数：

- surfaceCreated(SurfaceHolder holder); 当Surface第一次创建的时候调用，可以在这个方法里调用camera.open()、camera.setPreviewDisplay()来实现打开相机以及连接Camera与Surface
等操作。
- surfaceChanged(SurfaceHolder holder, int format, int width, int height); 当Surface的size、format等发生变化的时候调用，可以在这个方法里调用camera.startPreview()开启预览。
- surfaceDestroyed(SurfaceHolder holder); 当Surface被销毁的时候调用，可以在这个方法里调用camera.stopPreview()，camera.release()等方法来实现结束预览以及释放

### 1.1 打开相机
### 1.2 关闭相机
### 1.3 开启预览
### 1.4 关闭预览
### 1.5 拍照
### 1.6 开始视频录制
### 1.7 结束视频录制

## 二 Camera2实践指南

- [Android Camera2 官方视频](https://www.youtube.com/watch?v=Xtp3tH27OFs)
- [Android Camera2 官方文档](https://developer.android.com/reference/android/hardware/camera2/package-summary.html)
- [Android Camera2 官方用例](https://github.com/googlesamples/android-Camera2Basic)

Camera2 API中主要涉及以下几个关键类：

- CameraManager：摄像头管理器，用于打开和关闭系统摄像头
- CameraCharacteristics：描述摄像头的各种特性，我们可以通过CameraManager的getCameraCharacteristics(@NonNull String cameraId)方法来获取。
- CameraDevice：描述系统摄像头，类似于早期的Camera。
- CameraCaptureSession：Session类，当需要拍照、预览等功能时，需要先创建该类的实例，然后通过该实例里的方法进行控制（例如：拍照 capture()）。
- CaptureRequest：描述了一次操作请求，拍照、预览等操作都需要先传入CaptureRequest参数，具体的参数控制也是通过CameraRequest的成员变量来设置。
- CaptureResult：描述拍照完成后的结果。

Camera2拍照流程如下所示：

<img src="https://github.com/guoxiaoxing/phoenix/raw/master/art/camera/camera2_structure.png" width="500"/>

开发者通过创建CaptureRequest向摄像头发起Capture请求，这些请求会排成一个队列供摄像头处理，摄像头将结果包装在CaptureMetadata中返回给开发者。整个流程建立在一个CameraCaptureSession的会话中。

### 2.1 打开相机

打开相机之前，我们首先要获取CameraManager，然后获取相机列表，进而获取各个摄像头（主要是前置摄像头和后置摄像头）的参数。

```java
mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
try {
    final String[] ids = mCameraManager.getCameraIdList();
    numberOfCameras = ids.length;
    for (String id : ids) {
        final CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);

        final int orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (orientation == CameraCharacteristics.LENS_FACING_FRONT) {
            faceFrontCameraId = id;
            faceFrontCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            frontCameraCharacteristics = characteristics;
        } else {
            faceBackCameraId = id;
            faceBackCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            backCameraCharacteristics = characteristics;
        }
    }
} catch (Exception e) {
    Log.e(TAG, "Error during camera initialize");
}
```

Camera2与Camera一样也有cameraId的概念，我们通过mCameraManager.getCameraIdList()来获取cameraId列表，然后通过mCameraManager.getCameraCharacteristics(id)
获取每个id对应摄像头的参数。

关于CameraCharacteristics里面的参数，主要用到的有以下几个：

- LENS_FACING：前置摄像头（LENS_FACING_FRONT）还是后置摄像头（LENS_FACING_BACK）。
- SENSOR_ORIENTATION：摄像头拍照方向。
- FLASH_INFO_AVAILABLE：是否支持闪光灯。
- CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL：获取当前设备支持的相机特性。

注：事实上，在各个厂商的的Android设备上，Camera2的各种特性并不都是可用的，需要通过characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)方法
来根据返回值来获取支持的级别，具体说来：

- INFO_SUPPORTED_HARDWARE_LEVEL_FULL：全方位的硬件支持，允许手动控制全高清的摄像、支持连拍模式以及其他新特性。              
- INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED：有限支持，这个需要单独查询。
- INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY：所有设备都会支持，也就是和过时的Camera API支持的特性是一致的。

更多ameraCharacteristics参数，可以参见[CameraCharacteristics官方文档](https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html)。

打开相机主要调用的是mCameraManager.openCamera(currentCameraId, stateCallback, backgroundHandler)方法，如你所见，它有三个参数：

- String cameraId：摄像头的唯一ID。
- CameraDevice.StateCallback callback：摄像头打开的相关回调。
- Handler handler：StateCallback需要调用的Handler，我们一般可以用当前线程的Handler。

```java
 mCameraManager.openCamera(currentCameraId, stateCallback, backgroundHandler);
```

上面我们提到了CameraDevice.StateCallback，它是摄像头打开的一个回调，定义了打开，关闭以及出错等各种回调方法，我们可以在
这些回调方法里做对应的操作。

```java
private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
        //获取CameraDevice
        mcameraDevice = cameraDevice;
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
        //关闭CameraDevice
        cameraDevice.close();

    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int error) {
        //关闭CameraDevice
        cameraDevice.close();
    }
};
```

### 2.2 关闭相机

通过上面的描述，关闭就很简单了。

```java
//关闭CameraDevice
cameraDevice.close();
```

### 2.3 开启预览

Camera2都是通过创建请求会话的方式进行调用的，具体说来：

1. 调用mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)方法创建CaptureRequest，调用
2. mCameraDevice.createCaptureSession()方法创建CaptureSession。

```java
CaptureRequest.Builder createCaptureRequest(@RequestTemplate int templateType)
```

createCaptureRequest()方法里参数templateType代表了请求类型，请求类型一共分为六种，分别为：

- TEMPLATE_PREVIEW：创建预览的请求
- TEMPLATE_STILL_CAPTURE：创建一个适合于静态图像捕获的请求，图像质量优先于帧速率。
- TEMPLATE_RECORD：创建视频录制的请求
- TEMPLATE_VIDEO_SNAPSHOT：创建视视频录制时截屏的请求
- TEMPLATE_ZERO_SHUTTER_LAG：创建一个适用于零快门延迟的请求。在不影响预览帧率的情况下最大化图像质量。
- TEMPLATE_MANUAL：创建一个基本捕获请求，这种请求中所有的自动控制都是禁用的(自动曝光，自动白平衡、自动焦点)。

```java
createCaptureSession(@NonNull List<Surface> outputs, @NonNull CameraCaptureSession.StateCallback callback, @Nullable Handler handler)
```
createCaptureSession()方法一共包含三个参数：

- List<Surface> outputs：我们需要输出到的Surface列表。
- CameraCaptureSession.StateCallback callback：会话状态相关回调。
- Handler handler：callback可以有多个（来自不同线程），这个handler用来区别那个callback应该被回调，一般写当前线程的Handler即可。

关于CameraCaptureSession.StateCallback里的回调方法：

- onConfigured(@NonNull CameraCaptureSession session); 摄像头完成配置，可以处理Capture请求了。
- onConfigureFailed(@NonNull CameraCaptureSession session); 摄像头配置失败
- onReady(@NonNull CameraCaptureSession session); 摄像头处于就绪状态，当前没有请求需要处理。
- onActive(@NonNull CameraCaptureSession session); 摄像头正在处理请求。
- onClosed(@NonNull CameraCaptureSession session); 会话被关闭
- onSurfacePrepared(@NonNull CameraCaptureSession session, @NonNull Surface surface); Surface准备就绪

理解了这些东西，创建预览请求就十分简单了。

```java
previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
previewRequestBuilder.addTarget(workingSurface);

//注意这里除了预览的Surface，我们还添加了imageReader.getSurface()它就是负责拍照完成后用来获取数据的
mCameraDevice.createCaptureSession(Arrays.asList(workingSurface, imageReader.getSurface()),
        new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                cameraCaptureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                Log.d(TAG, "Fail while starting preview: ");
            }
        }, null);
```

可以发现，在onConfigured()里调用了cameraCaptureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)，这样我们就可以
持续的进行预览了。

注：上面我们说了添加了imageReader.getSurface()它就是负责拍照完成后用来获取数据，具体操作就是为ImageReader设置一个OnImageAvailableListener，然后在它的onImageAvailable()
方法里获取。

```java
mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            //当图片可得到的时候获取图片并保存
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

 };
```

### 2.4 关闭预览

关闭预览就是关闭当前预览的会话，结合上面开启预览的内容，具体实现如下：

```java
if (captureSession != null) {
    captureSession.close();
    try {
        captureSession.abortCaptures();
    } catch (Exception ignore) {
    } finally {
        captureSession = null;
    }
}
```

### 2.5 拍照

拍照具体来说分为三步：

1. 对焦

```java
try {
    //相机对焦
    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
    //修改状态
    previewState = STATE_WAITING_LOCK;
    //发送对焦请求
    captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
} catch (Exception ignore) {
}
```

我们定义了一个CameraCaptureSession.CaptureCallback来处理对焦请求返回的结果。

```java
private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {
            //等待对焦
            final Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (afState == null) {
                //对焦失败，直接拍照
                captureStillPicture();
            } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                    || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                    || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                    || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    previewState = STATE_PICTURE_TAKEN;
                    //对焦完成，进行拍照
                    captureStillPicture();
                } else {
                    runPreCaptureSequence();
                }
            }
    }
};
```

2. 拍照

我们定义了一个captureStillPicture()来进行拍照。


```java
private void captureStillPicture() {
    try {
        if (null == mCameraDevice) {
            return;
        }
        
        //构建用来拍照的CaptureRequest
        final CaptureRequest.Builder captureBuilder =
                mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(imageReader.getSurface());

        //使用相同的AR和AF模式作为预览
        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        //设置方向
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getPhotoOrientation(cameraConfigProvider.getSensorPosition()));

        //创建会话
        CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                           @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                Log.d(TAG, "onCaptureCompleted: ");
            }
        };
        //停止连续取景
        captureSession.stopRepeating();
        //捕获照片
        captureSession.capture(captureBuilder.build(), CaptureCallback, null);

    } catch (CameraAccessException e) {
        Log.e(TAG, "Error during capturing picture");
    }
}
```
3. 取消对焦

拍完照片后，我们还要解锁相机焦点，让相机恢复到预览状态。

```java
try {
    //重置自动对焦
    previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
    captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
    //相机恢复正常的预览状态
    previewState = STATE_PREVIEW;
    //打开连续取景模式
    captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
} catch (Exception e) {
    Log.e(TAG, "Error during focus unlocking");
}
```

### 2.6 开始视频录制

```java
//构建视频录制aptureRequest
previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
final List<Surface> surfaces = new ArrayList<>();

final Surface previewSurface = workingSurface;
surfaces.add(previewSurface);
//设置视频录制预览Surface
previewRequestBuilder.addTarget(previewSurface);

//这里跟上面的图像一样创建了一个MediaRecorder来读取录制额数据
workingSurface = videoRecorder.getSurface();
surfaces.add(workingSurface);
//设置视频录制预览Surface
previewRequestBuilder.addTarget(workingSurface);

//构建视频录制CaptureSession
mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
    @Override
    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
        captureSession = cameraCaptureSession;

        previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            //持续进行视频录制
            captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, backgroundHandler);
        } catch (Exception e) {
        }

        try {
            //开启videoRecorder，准备接收录制数据
            videoRecorder.start();
        } catch (Exception ignore) {
            Log.e(TAG, "videoRecorder.start(): ", ignore);
        }
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
        Log.d(TAG, "onConfigureFailed");
    }
}, backgroundHandler);
```

这里面有个VideoRecorder，它实际上是个MediaRecorder，MediaRecorder和上面的ImageReader一样都是用来接收摄像头传来的数据的。MediaRecorder在录制之前
进行初始化，具体说来：

```java
videoRecorder = new MediaRecorder();
videoRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
videoRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

//输出格式
videoRecorder.setOutputFormat(camcorderProfile.fileFormat);
//视频帧率
videoRecorder.setVideoFrameRate(camcorderProfile.videoFrameRate);
//视频大小
videoRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
//视频比特率
videoRecorder.setVideoEncodingBitRate(camcorderProfile.videoBitRate);
//视频编码器
videoRecorder.setVideoEncoder(camcorderProfile.videoCodec);

//音频编码率
videoRecorder.setAudioEncodingBitRate(camcorderProfile.audioBitRate);
//音频声道
videoRecorder.setAudioChannels(camcorderProfile.audioChannels);
//音频采样率
videoRecorder.setAudioSamplingRate(camcorderProfile.audioSampleRate);
//音频编码器
videoRecorder.setAudioEncoder(camcorderProfile.audioCodec);

File outputFile = outputPath;
String outputFilePath = outputFile.toString();
//输出路径
videoRecorder.setOutputFile(outputFilePath);

//设置视频输出的最大尺寸
if (cameraConfigProvider.getVideoFileSize() > 0) {
    videoRecorder.setMaxFileSize(cameraConfigProvider.getVideoFileSize());
    videoRecorder.setOnInfoListener(this);
}

//设置视频输出的最大时长
if (cameraConfigProvider.getVideoDuration() > 0) {
    videoRecorder.setMaxDuration(cameraConfigProvider.getVideoDuration());
    videoRecorder.setOnInfoListener(this);
}
videoRecorder.setOrientationHint(getVideoOrientation(cameraConfigProvider.getSensorPosition()));

//准备
videoRecorder.prepare();
```

值得一提的是，日常的业务中经常对拍摄视频的时长或者大小有要求，这个可以通过videoRecorder.setOnInfoListener()来处理，OnInfoListener会监听正在录制的视频，然后我们
可以在它的回调方法里处理。


```java
   @Override
public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
    if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
        //到达最大时长
    } else if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == what) {
        //到达最大尺寸
    }
}
```
以上便是视频录制的全部内容，就是简单的API使用，还是比较简单的。

### 2.7 结束视频录制

结束视频录制主要也是关闭会话以及释放一些资源，具体说来：

1. 关闭预览会话
2. 停止VideoRecorder
3. 释放VideoRecorder

```java
//关闭预览会话
if (captureSession != null) {
    captureSession.close();
    try {
        captureSession.abortCaptures();
    } catch (Exception ignore) {
    } finally {
        captureSession = null;
    }
}

//停止VideoRecorder
if (videoRecorder != null) {
    try {
        videoRecorder.stop();
    } catch (Exception ignore) {
    }
}

//释放VideoRecorder
try {
    if (videoRecorder != null) {
        videoRecorder.reset();
        videoRecorder.release();
    }
} catch (Exception ignore) {

} finally {
    videoRecorder = null;
}
```

## 附录


### 关于一些常见的坑

### 关于权限问题的处理

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```