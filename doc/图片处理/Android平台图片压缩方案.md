# Android平台图像压缩方案

**关于作者**

>郭孝星，程序员，吉他手，主要从事Android平台基础架构方面的工作，欢迎交流技术方面的问题，可以去我的[Github](https://github.com/guoxiaoxing)提issue或者发邮件至guoxiaoxingse@163.com与我交流。

**文章目录**

本篇文章用来介绍Android平台的图像压缩方案以及图像编解码的通识性理解，事实上Android平台对图像的处理最终都交由底层实现，篇幅有限，我们这里不会去分析底层的细节实现细节，但是
我们会提一下底层的实现方案概览，给向进一步扩展的同学提供一些思路。

在介绍图像压缩方案之前，我们先要了解一下和压缩相关的图像的基本知识，这也可以帮助我们理解Bitmap.java里定义的一些变量的含义。

像素密度

>像素密度指的是每英寸像素数目，在Bitmap里用mDensity/mTargetDensity，mDensity默认是设备屏幕的像素密度，mTargetDensity是图片的目标像素密度，在加载图片时就是 drawable 目录的像素密度。

色彩模式

>色彩模式是数字世界中表示颜色的一种算法，在Bitmap里用Config来表示。

- ARGB_8888：每个像素占四个字节，A、R、G、B 分量各占8位，是 Android 的默认设置；
- RGB_565：每个像素占两个字节，R分量占5位，G分量占6位，B分量占5位；
- ARGB_4444：每个像素占两个字节，A、R、G、B分量各占4位，成像效果比较差；
- Alpha_8: 只保存透明度，共8位，1字节；

另外提一点Bitmap计算大小的方法。

```
Bitamp 占用内存大小 = 宽度像素 x （inTargetDensity / inDensity） x 高度像素 x （inTargetDensity / inDensity）x 一个像素所占的内存
```
在Bitmap里有两个获取内存占用大小的方法。

- getByteCount()：API12 加入，代表存储 Bitmap 的像素需要的最少内存。
- getAllocationByteCount()：API19 加入，代表在内存中为 Bitmap 分配的内存大小，代替了 getByteCount() 方法。

在不复用 Bitmap 时，getByteCount() 和 getAllocationByteCount 返回的结果是一样的。在通过复用 Bitmap 来解码图片时，那么 getByteCount() 表示新解码图片占用内存的大
小，getAllocationByteCount() 表示被复用 Bitmap真实占用的内存大小（即 mBuffer 的长度）。

了解完基本的概念，我们来分析压缩图像的方法。

Android平台压缩图像的手段通常有两种：

- 质量压缩
- 尺寸压缩

## 一 质量压缩

>质量压缩的关键在于Bitmap.compress()函数，该函数不会改变图像的大小，但是可以降低图像的质量，从而降低存储大小，进而达到压缩的目的。

```java
compress(CompressFormat format, int quality, OutputStream stream)
```
它有三个参数

- CompressFormat format：压缩格式，它有JPEG、PNG、WEBP三种选择，JPEG是有损压缩，PNG是无损压缩，压缩后的图像大小不会变化（也就是没有压缩效果），WEBP是Google推出的
图像格式，它相比JPEG会节省30%左右的空间，处于兼容性和节省空间的综合考虑，我们一般会选择JPEG。
- int quality：0~100可选，数值越大，质量越高，图像越大。
- OutputStream stream：压缩后图像的输出流。                                            
                                       
我们来写个例子验证一下。
                                       
```
File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "timo_compress_quality_100.jpg");
if (!file.exists()) {
    try {
        file.createNewFile();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.timo);
BufferedOutputStream bos = null;
try {
    bos = new BufferedOutputStream(new FileOutputStream(file));
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
    bitmap.recycle();
} catch (FileNotFoundException e) {
    e.printStackTrace();
}finally {
    try {
        if(bos != null){
            bos.close();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```         
           
quality = 100

quality = 50

quality = 0

可以看到随着quality的降低，图像质量发生了明显的变化，但是图像的尺寸没有发生变化。这个方法是在Java层是很简单的，我们来探究它的底层实现原理。

```java
public boolean compress(CompressFormat format, int quality, OutputStream stream) {
    checkRecycled("Can't compress a recycled bitmap");
    // do explicit check before calling the native method
    if (stream == null) {
        throw new NullPointerException();
    }
    if (quality < 0 || quality > 100) {
        throw new IllegalArgumentException("quality must be 0..100");
    }
    Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, "Bitmap.compress");
    boolean result = nativeCompress(mNativePtr, format.nativeInt,
            quality, stream, new byte[WORKING_COMPRESS_STORAGE]);
    Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
    return result;
}
```

可以看到它在内部调用的是一个native方法nativeCompress()，这是定义在Bitmap.java里的一个函数，它的实现在[Bitmap.cpp](https://android.googlesource.com/platform/frameworks/base/+/35ef567/core/jni/android/graphics/Bitmap.cpp)里

它最终调用的是Bitmap.cpp里的Bitmap_compress()函数，我们来看看它的实现。

```c++
static bool Bitmap_compress(JNIEnv* env, jobject clazz, SkBitmap* bitmap,
                            int format, int quality,
                            jobject jstream, jbyteArray jstorage) {
    SkImageEncoder::Type fm;

    //根据编码类型选择SkImageEncoder
    switch (format) {
    case kJPEG_JavaEncodeFormat:
        fm = SkImageEncoder::kJPEG_Type;
        break;
    case kPNG_JavaEncodeFormat:
        fm = SkImageEncoder::kPNG_Type;
        break;
    case kWEBP_JavaEncodeFormat:
        fm = SkImageEncoder::kWEBP_Type;
        break;
    default:
        return false;
    }

    bool success = false;
    if (NULL != bitmap) {
        SkAutoLockPixels alp(*bitmap);

        if (NULL == bitmap->getPixels()) {
            return false;
        }

        SkWStream* strm = CreateJavaOutputStreamAdaptor(env, jstream, jstorage);
        if (NULL == strm) {
            return false;
        }

        //创建图像编码器
        SkImageEncoder* encoder = SkImageEncoder::Create(fm);
        if (NULL != encoder) {
            //调用encodeStream进行编码
            success = encoder->encodeStream(strm, *bitmap, quality);
            delete encoder;
        }
        delete strm;
    }
    return success;
}
```
可以看到该函数根据编码格式选择SkImageEncoder，从而创建对应的图像编码器，最后调用encodeStream(strm, *bitmap, quality)方法来完成编码。通过名字我们就可以
看出这是Google的Skia图形库。

>[Skia](https://skia.org/index_zh)是一个开源的二维图形库，提供各种常用的API，并可在多种软硬件平台上运行。谷歌Chrome浏览器、Chrome OS、安卓、火狐浏览器、火狐操作
系统以及其它许多产品都使用它作为图形引擎。

我们虽然在平时的开发中没有直接用到Skia，但它对我们太重要了，它是Android系统的重要组成部分，很多重要操作例如图像编解码，Canvas绘制在底层都是通过Skia来完成的。它同样被
广泛用于Google的其他产品中。

Skia本身提供了基本的画图和编解码功能，它同时还挂载了其他第三方编解码库，例如：libpng.so、libjpeg.so、libgif.so、所以我们上面想要编码成jpeg图像最终是由libjpeg来完成的。

>[libjpeg](http://libjpeg.sourceforge.net/)是一个完全用C语言编写的处理JPEG图像数据格式的自由库。它包含一个JPEG编解码器的算法实现，以及用于处理JPEG数据的多种实用程序。

这里提到了JPEG编解码器的算法实现，它实际上指的是Huffman 算法。

一般情况下，Android自带的libjpeg就可以满足日常的开发需求，如果业务对高质量和低存储的需求比较大，可以考虑一下以下两个库：

- [libjpeg-turbo](https://github.com/libjpeg-turbo/libjpeg-turbo)：增强版libjpeg，它是一种JPEG图像编解码器，它使用SIMD指令（MMX，SSE2，NEON，AltiVec）来加速x86，x86-64，ARM和
PowerPC系统上的基准JPEG压缩和解压缩。 在这样的系统上，libjpeg-turbo的速度通常是libjpeg的2-6倍，其他的都是相等的。 在其他类型的系统上，依靠其高度优化的Huffman编码例程，libjpeg-turbo仍然
可以胜过libjpeg。 在许多情况下，libjpeg-turbo的性能与专有的高速JPEG编解码器相媲美。
- [mozilla/mozjpeg](https://github.com/mozilla/mozjpeg)：基于libjpeg-turbo.实现，保证不降低图像质量且兼容主流编解码器的情况下进行jpeg压缩。

## 二 尺寸压缩

>尺寸压缩本质上就是一个重新采样的过程，放大图像称为上采样，缩小图像称为下采样，Android提供了两种图像采样方法，邻近采样和双线性采样。

### 2.1 邻近采样

>邻近采样采用邻近点插值算法，用一个像素点代替邻近的像素点，

它的实现代码大家也非常熟悉。

```java
BitmapFactory.Options options = new BitmapFactory.Options();
options.inSampleSize = 32;
Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.blue, options);
String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/timo_BitmapFactory_2.png";
ImageUtils.save(bitmap, savePath, Bitmap.CompressFormat.PNG);
```

inSampleSize = 1


inSampleSize = 32




可以看到这种方式的关键在于inSampleSize的选择，它决定了压缩后图像的大小。

>inSampleSize代表了压缩后的图像一个像素点代表了原来的几个像素点，例如inSampleSize为4，则压缩后的图像的宽高是原来的1/4，像素点数是原来的1/16，inSampleSize
一般会选择2的指数，如果不是2的指数，内部计算的时候也会像2的指数靠近。

关于inSampleSize的计算，[Luban](https://github.com/Curzibn/Luban)提供了很好的思路，作者也给出了算法思路。

```
1. 判断图像比例值，是否处于以下区间内；
  - [1, 0.5625)    即图像处于 [1:1 ~ 9:16) 比例范围内
  - [0.5625, 0.5)  即图像处于 [9:16 ~ 1:2) 比例范围内
  - [0.5, 0)       即图像处于 [1:2 ~ 1:∞) 比例范围内
2. 判断图像最长边是否过边界值；
  - [1, 0.5625)   边界值为：1664 * n（n=1）, 4990 * n（n=2）, 1280 * pow(2, n-1)（n≥3）
  - [0.5625, 0.5) 边界值为：1280 * pow(2, n-1)（n≥1）
  - [0.5, 0)      边界值为：1280 * pow(2, n-1)（n≥1）
3. 计算压缩图像实际边长值，以第2步计算结果为准，超过某个边界值则：width / pow(2, n-1)，height/pow(2, n-1)
4. 计算压缩图像的实际文件大小，以第2、3步结果为准，图像比例越大则文件越大。  
    size = (newW * newH) / (width * height) * m；
  - [1, 0.5625) 则 width & height 对应 1664，4990，1280 * n（n≥3），m 对应 150，300，300；
  - [0.5625, 0.5) 则 width = 1440，height = 2560, m = 200；
  - [0.5, 0) 则 width = 1280，height = 1280 / scale，m = 500；注：scale为比例值
5. 判断第4步的size是否过小
  - [1, 0.5625) 则最小 size 对应 60，60，100
  - [0.5625, 0.5) 则最小 size 都为 100
  - [0.5, 0) 则最小 size 都为 100
6. 将前面求到的值压缩图像 width, height, size 传入压缩流程，压缩图像直到满足以上数值
```

同样的我们也来看看这种方式的底层实现原理，BitmapFactory里有很多decode方法，它们最终调用的是native方法。

```java
private static native Bitmap nativeDecodeStream(InputStream is, byte[] storage,
        Rect padding, Options opts);
private static native Bitmap nativeDecodeFileDescriptor(FileDescriptor fd,
        Rect padding, Options opts);
private static native Bitmap nativeDecodeAsset(long nativeAsset, Rect padding, Options opts);
private static native Bitmap nativeDecodeByteArray(byte[] data, int offset,
        int length, Options opts);
```
这些native方法在BitmapFactory.cpp里实现，这些方法最终调用的是doDecode()方法

```c++

```

### 2.12 双线性采样

>双线性采样采用双线性插值算法，相比邻近采样简单粗暴的选择一个像素点代替其他像素点，双线性采样参考源像素相应位置周围2x2个点的值，根据相对位置取对应的权重，经过计算得到目标图像。

它的实现方式也很简单

```java
Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.blue_red);
Matrix matrix = new Matrix();
matrix.setScale(0.5f, 0.5f);
Bitmap sclaedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth()/2, bitmap.getHeight()/2, matrix, true);
String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/timo_BitmapFactory_1.png";
ImageUtils.save(bitmap, savePath, Bitmap.CompressFormat.PNG);
```

这种方式的关键在于Bitmap.createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter)方法。

这个方法有七个参数：

- Bitmap source：源图像
- int x：目标图像第一个像素的x坐标
- int y：目标图像第一个像素的y坐标
- int width：目标图像的宽度（像素点个数）
- int height：目标图像的高度（像素点个数）
- Matrix m：变换矩阵
- boolean filter：是否开启过滤

这个方法同样也调用了底层的native方法：

```java
private static native Bitmap nativeCreate(int[] colors, int offset,
                                              int stride, int width, int height,
                                              int nativeConfig, boolean mutable);
```

该方法在Bitmap.cpp里实现。