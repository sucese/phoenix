package com.guoxiaoxing.phoenix.upload.internal;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.guoxiaoxing.phoenix.core.listener.OnProcessorListener;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.upload.model.AliyunUpload;
import com.guoxiaoxing.phoenix.upload.model.ProgressRequestBody;
import com.guoxiaoxing.phoenix.upload.model.SoucheUpload;
import com.guoxiaoxing.phoenix.upload.util.PictureUtils;
import com.guoxiaoxing.phoenix.upload.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/10/10 下午5:14
 */
public final class MediaUploader {

    private static final String TAG = "MediaUploader";

    private final static String prePath = "files/default/";
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final MediaType MEDIA_TYPE_MP4 = MediaType.parse("video/mp4");

    private static volatile Gson gsonInstance;
    private static volatile OkHttpClient okHttpClientInstance;

    /**
     * 同步上传
     *
     * @param mediaEntity mediaEntity
     */
    public static MediaEntity syncUploadMedia(MediaEntity mediaEntity) {

        byte[] fileByte;
        String fileMd5;
        String thumbnailMD5;

        MediaType mediaType;
        String localPath = mediaEntity.getFinalPath();

        //上传视频
        if (mediaEntity.getFileType() == MimeType.ofVideo()) {
            mediaType = MEDIA_TYPE_MP4;
            fileByte = videoToByte(localPath);
            fileMd5 = StringUtils.md5sum(new File(localPath)).toLowerCase() + ".mp4";
            thumbnailMD5 = StringUtils.md5sum(new File(localPath)).toLowerCase() + ".jpg";

            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(localPath, MediaStore.Images.Thumbnails.MINI_KIND);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] thumbnailByte = baos.toByteArray();

            if (fileByte.length == 0) {
                Log.d(TAG, "The fileByte of image is null");
                return mediaEntity;
            }

            String onlineThumbnailPath = syncUploadAliyun(newOkhttpClientInstance(), thumbnailByte, thumbnailMD5, mediaType);
            String onlinePath = syncUploadAliyun(newOkhttpClientInstance(), fileByte, fileMd5, mediaType);
            if (!TextUtils.isEmpty(onlinePath) && !TextUtils.isEmpty(onlineThumbnailPath)) {
                mediaEntity.setUploaded(true);
                mediaEntity.setOnlinePath(onlinePath);
                mediaEntity.setOnlineThumbnailPath(onlineThumbnailPath);
            }
        }
        //上传图片
        else if (mediaEntity.getFileType() == MimeType.ofImage()) {
            mediaType = MEDIA_TYPE_JPEG;
            fileByte = PictureUtils.getCompressedBitmap(mediaEntity.getFinalPath());
            fileMd5 = StringUtils.md5sum(new File(localPath)).toLowerCase() + ".jpg";

            if (fileByte.length == 0) {
                Log.d(TAG, "The fileByte of image is null");
                return mediaEntity;
            }


            String onlinePath = syncUploadAliyun(newOkhttpClientInstance(), fileByte, fileMd5, mediaType);
            if (!TextUtils.isEmpty(onlinePath)) {
                mediaEntity.setUploaded(true);
                mediaEntity.setOnlinePath(onlinePath);
            }
        }
        return mediaEntity;
    }

    private static String syncUploadAliyun(OkHttpClient okHttpClient, byte[] fileByte, String fileMd5, MediaType mediaType) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + fileMd5 + "\""),
                        new ProgressRequestBody(RequestBody.create(mediaType, fileByte), new ProgressRequestBody.Listener() {
                            @Override
                            public void onProgress(int progress) {

                            }
                        }))
                .addFormDataPart("dir", prePath)
                .build();

        Request request = new Request.Builder()
                .url("http://niu.souche.com/upload/aliyun")
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            AliyunUpload aliyunUpload = newGsonInstance().fromJson(result, AliyunUpload.class);
            if (aliyunUpload.getSuccess() == 1) {
                return aliyunUpload.getPath();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步上传
     *
     * @param mediaEntity         mediaEntity
     * @param onProcessorListener onProcessorListener
     */
    public static void asyncUploadMedia(MediaEntity mediaEntity, OnProcessorListener onProcessorListener) {

        byte[] fileByte;
        String fileMd5;
        String thumbnailMD5;

        MediaType mediaType;
        String localPath = mediaEntity.getFinalPath();

        //上传视频
        if (mediaEntity.getFileType() == MimeType.ofVideo()) {
            mediaType = MEDIA_TYPE_MP4;
            fileByte = videoToByte(localPath);
            fileMd5 = StringUtils.md5sum(new File(localPath)).toLowerCase() + ".mp4";
            thumbnailMD5 = StringUtils.md5sum(new File(localPath)).toLowerCase() + ".jpg";

            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(localPath, MediaStore.Images.Thumbnails.MINI_KIND);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] thumbnailByte = baos.toByteArray();

            if (fileByte.length == 0) {
                onProcessorListener.onFailed("The fileByte of image is null");
                return;
            }

            String onlineThumbnailPath = syncUploadAliyun(newOkhttpClientInstance(), thumbnailByte, thumbnailMD5, mediaType);
            mediaEntity.setOnlineThumbnailPath(onlineThumbnailPath);
            asyncUploadAliyun(newOkhttpClientInstance(), mediaEntity, fileByte, fileMd5, mediaType, onProcessorListener);

        }
        //上传图片
        else if (mediaEntity.getFileType() == MimeType.ofImage()) {
            mediaType = MEDIA_TYPE_JPEG;
            fileByte = PictureUtils.getCompressedBitmap(mediaEntity.getFinalPath());
            fileMd5 = StringUtils.md5sum(new File(localPath)).toLowerCase() + ".jpg";

            if (fileByte.length == 0) {
                onProcessorListener.onFailed("The fileByte of image is null");
                return;
            }

            asyncUploadAliyun(newOkhttpClientInstance(), mediaEntity, fileByte, fileMd5, mediaType, onProcessorListener);
        }
    }

    private static void asyncUploadAliyun(OkHttpClient okHttpClient, final MediaEntity mediaEntity, byte[] fileByte, String fileMd5,
                                          MediaType mediaType, final OnProcessorListener onProcessorListener) {

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + fileMd5 + "\""),
                        new ProgressRequestBody(RequestBody.create(mediaType, fileByte), new ProgressRequestBody.Listener() {
                            @Override
                            public void onProgress(int progress) {
                                onProcessorListener.onProgress(progress);
                            }
                        }))
                .addFormDataPart("dir", prePath)
                .build();

        Request request = new Request.Builder()
                .url("http://niu.souche.com/upload/aliyun")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                onProcessorListener.onFailed("AliyunUpload faild: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                AliyunUpload aliyunUpload = newGsonInstance().fromJson(result, AliyunUpload.class);
                if (aliyunUpload.getSuccess() == 1 && !TextUtils.isEmpty(aliyunUpload.getPath())) {
                    mediaEntity.setOnlinePath(aliyunUpload.getPath());
                    mediaEntity.setUploaded(true);
                    onProcessorListener.onSuccess(mediaEntity);
                } else {
                    onProcessorListener.onFailed("AliyunUpload faild: " + aliyunUpload.getSuccess());
                }
            }
        });
    }

    private static byte[] videoToByte(String filePath) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] buf = new byte[1024];
        int n;
        try {
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * 搜车上传视频接口
     *
     * @param fileMd5             fileMd5
     * @param filePath            filePath
     * @param onProcessorListener onProcessorListener
     */
    private static void uploadVideo(String fileMd5, String filePath, final OnProcessorListener onProcessorListener) {

        byte[] fileByte = videoToByte(filePath);
        if (fileByte.length == 0) {
            onProcessorListener.onFailed("上传失败，文件异常");
            return;
        }

        RequestBody fileBody = RequestBody.create(MEDIA_TYPE_MP4, fileByte);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("video", fileMd5, fileBody)
                .build();
        Request request = new Request.Builder()
                .url("http://loan-platform-org-web.sqaproxy.souche-fin.com/api/utils/v1/fileutils/uploadVideo.json")
                .post(requestBody)
                .build();

        Call call = newOkhttpClientInstance().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onProcessorListener.onFailed("上传失败，网络异常，" + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();
                try {
                    Log.d(TAG, "SoucheUpload upload, onResponse: " + result);
                    SoucheUpload soucheUpload = newGsonInstance().fromJson(result, SoucheUpload.class);
                    if (soucheUpload == null) {
                        onProcessorListener.onFailed("上传失败，网络异常");
                    }
//                    onProcessorListener.onSuccess(soucheUpload);
                } catch (Exception e) {
                    onProcessorListener.onFailed("上传失败，网络异常");
                }
            }
        });
    }

    private static OkHttpClient newOkhttpClientInstance() {
        if (okHttpClientInstance == null) {
            synchronized (MediaUploader.class) {
                if (okHttpClientInstance == null) {
                    okHttpClientInstance = new OkHttpClient();
                }
            }
        }
        return okHttpClientInstance;
    }

    private static Gson newGsonInstance() {
        if (gsonInstance == null) {
            synchronized (MediaUploader.class) {
                if (gsonInstance == null) {
                    gsonInstance = new Gson();
                }
            }
        }
        return gsonInstance;
    }
}
