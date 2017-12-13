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

那我们在开发中应该使用哪一种呢？🤔事实上是两个都用的，Camera2是Android 5.0之后才推出的API，因此我们需要做向下兼容。Android 5.0以下使用Camera、Android 5.0以上使用Camera2。

相机开发的一般流程是什么样的？

1. 检测并访问相机资源 检查手机是否存在相机资源，如果存在则请求访问相机资源。
2. 创建预览界面，创建继承自SurfaceView并实现SurfaceHolder接口的拍摄预览类。有了拍摄预览类，即可创建一个布局文件，将预览画面与设计好的用户界面控件融合在一起，实时显示相机的预览图像。
3. 设置拍照监听器，给用户界面控件绑定监听器，使其能响应用户操作, 开始拍照过程。
4. 拍照并保存文件，将拍摄获得的图像转换成位图文件，最终输出保存成各种常用格式的图片。
5. 释放相机资源，相机是一个共享资源，当相机使用完毕后，必须正确地将其释放，以免其它程序访问使用时发生冲突。

相机开发一般需要注意哪些问题？

1. 版本兼容性问题，Android 5.0以上使用Camera2，Android 5.0要做Camera兼容。Android 6.0以上要做相机等运行时权限兼容。
2. 设备兼容性问题，Camera/Camera2里的各种特性在有些手机厂商的设备实现方式和支持程度是不一样的，这个需要做兼容性测试，一点点踩坑。

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




        /**
         * This is called immediately after the surface is first created.
         * Implementations of this should start up whatever rendering code
         * they desire.  Note that only one thread can ever draw into
         * a {@link Surface}, so you should not draw into the Surface here
         * if your normal rendering will be in another thread.
         * 
         * @param holder The SurfaceHolder whose surface is being created.
         */
        public void 

        /**
         * This is called immediately after any structural changes (format or
         * size) have been made to the surface.  You should at this point update
         * the imagery in the surface.  This method is always called at least
         * once, after {@link #surfaceCreated}.
         * 
         * @param holder The SurfaceHolder whose surface has changed.
         * @param format The new PixelFormat of the surface.
         * @param width The new width of the surface.
         * @param height The new height of the surface.
         */
        public void 

        /**
         * This is called immediately before a surface is being destroyed. After
         * returning from this call, you should no longer try to access this
         * surface.  If you have a rendering thread that directly accesses
         * the surface, you must ensure that thread is no longer touching the 
         * Surface before returning from this function.
         * 
         * @param holder The SurfaceHolder whose surface is being destroyed.
         */
        public void 

## 二 Camera2实践指南

Camera2 API中主要涉及以下几个关键类：

- CameraManager：摄像头管理器，用于打开和关闭系统摄像头
- CameraCharacteristics：描述摄像头的各种特性，我们可以通过CameraManager的getCameraCharacteristics(@NonNull String cameraId)方法来获取。
- CameraDevice：描述系统摄像头，类似于早期的Camera。
- CameraCaptureSession：Session类，当需要拍照、预览等功能时，需要先创建该类的实例，然后通过该实例里的方法进行控制（例如：拍照 capture()）。
- CameraRequest：描述了一次操作请求，拍照、预览等操作都需要先传入CameraRequest参数，具体的参数控制也是通过CameraRequest的成员变量来设置。

## 附录

### Camera常见问题

