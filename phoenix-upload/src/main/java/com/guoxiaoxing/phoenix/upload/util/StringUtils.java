package com.guoxiaoxing.phoenix.upload.util;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class StringUtils {

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static boolean isCamera(String title) {
        if (!TextUtils.isEmpty(title) && title.startsWith("相机胶卷")
                || title.startsWith("CameraRoll")
                || title.startsWith("所有音频")
                || title.startsWith("All audio")) {
            return true;
        }

        return false;
    }

    public static String md5sum(File file) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead;
        MessageDigest md5;
        try {
            fis = new FileInputStream(file);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            return System.currentTimeMillis() + "";
        }
    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte c : b) {
            sb.append(HEX_DIGITS[(c & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[c & 0x0f]);
        }
        return sb.toString();
    }
}
