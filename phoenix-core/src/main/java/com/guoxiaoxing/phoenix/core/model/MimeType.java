package com.guoxiaoxing.phoenix.core.model;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import com.guoxiaoxing.phoenix.core.common.PhoenixConstant;

import java.io.File;

public final class MimeType {

    public static int ofAll() {
        return PhoenixConstant.TYPE_ALL;
    }

    public static int ofImage() {
        return PhoenixConstant.TYPE_IMAGE;
    }

    public static int ofVideo() {
        return PhoenixConstant.TYPE_VIDEO;
    }

    public static int ofAudio() {;
        return PhoenixConstant.TYPE_AUDIO;
    }

    public static int getFileType(String mimeType) {

        switch (mimeType) {
            case "image/png":
            case "image/PNG":
            case "image/jpeg":
            case "image/JPEG":
            case "image/webp":
            case "image/WEBP":
            case "image/gif":
            case "image/GIF":
                return PhoenixConstant.TYPE_IMAGE;
            case "video/3gp":
            case "video/3gpp":
            case "video/avi":
            case "video/mp4":
            case "video/x-msvideo":
                return PhoenixConstant.TYPE_VIDEO;
            case "audio/mpeg":
            case "audio/x-ms-wma":
            case "audio/x-wav":
            case "audio/amr":
            case "audio/wav":
            case "audio/aac":
            case "audio/mp4":
            case "audio/quicktime":
                return PhoenixConstant.TYPE_AUDIO;
        }
        return PhoenixConstant.TYPE_IMAGE;
    }

    public static boolean isGif(String mimeType) {

        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }

        switch (mimeType) {
            case "image/gif":
            case "image/GIF":
                return true;
        }
        return false;
    }

    public static boolean isVideo(String mimeType) {

        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }

        switch (mimeType) {
            case "video/3gp":
            case "video/3gpp":
            case "video/avi":
            case "video/mp4":
            case "video/x-msvideo":
                return true;
        }
        return false;
    }

    public static boolean isHttp(String path) {
        if (!TextUtils.isEmpty(path)) {
            if (path.startsWith("http")
                    || path.startsWith("https")) {
                return true;
            }
        }
        return false;
    }

    public static String fileToType(File file) {
        if (file != null) {
            String name = file.getName();
            if (name.endsWith(".mp4") || name.endsWith(".avi")
                    || name.endsWith(".3gpp") || name.endsWith(".3gp")) {
                return "video/mp4";
            } else if (name.endsWith(".PNG") || name.endsWith(".png") || name.endsWith(".jpeg")
                    || name.endsWith(".gif") || name.endsWith(".GIF") || name.endsWith(".jpg")
                    || name.endsWith(".webp") || name.endsWith(".WEBP") || name.endsWith(".JPEG")) {
                return "image/jpeg";
            } else if (name.endsWith(".mp3") || name.endsWith(".amr")
                    || name.endsWith(".aac") || name.endsWith(".war")
                    || name.endsWith(".flac")) {
                return "audio/mpeg";
            }
        }
        return "image/jpeg";
    }

    public static boolean mimeToEqual(String p1, String p2) {
        return getFileType(p1) == getFileType(p2);
    }

    public static String createImageType(String path) {
        try {
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                String fileName = file.getName();
                int last = fileName.lastIndexOf(".") + 1;
                String temp = fileName.substring(last, fileName.length());
                return "image/" + temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "image/jpeg";
        }
        return "image/jpeg";
    }

    public static String createVideoType(String path) {
        try {
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                String fileName = file.getName();
                int last = fileName.lastIndexOf(".") + 1;
                String temp = fileName.substring(last, fileName.length());
                return "video/" + temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "video/mp4";
        }
        return "video/mp4";
    }

    public static int pictureToVideo(String mimeType) {
        if (!TextUtils.isEmpty(mimeType)) {
            if (mimeType.startsWith("video")) {
                return PhoenixConstant.TYPE_VIDEO;
            } else if (mimeType.startsWith("audio")) {
                return PhoenixConstant.TYPE_AUDIO;
            }
        }
        return PhoenixConstant.TYPE_IMAGE;
    }

    public static int getLocalVideoDuration(String videoPath) {
        int duration;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            duration = Integer.parseInt(mmr.extractMetadata
                    (MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return duration;
    }
}
