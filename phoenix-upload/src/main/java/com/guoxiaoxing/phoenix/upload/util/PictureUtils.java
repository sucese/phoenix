package com.guoxiaoxing.phoenix.upload.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

public class PictureUtils {

    private static final String PICTURE_SUFFIX = ".jpg";
    private Context mContext;

    public PictureUtils(Context mContext) {
        this.mContext = mContext;
    }

    private static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, 500, 500);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }


    public static byte[] getCompressedBitmap(String imagePath) {
        float maxHeight = 1920.0f;
        float maxWidth = 1080.0f;
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        if (actualHeight <= 0 || actualWidth <= 0) {
            return new byte[]{};
        }

        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);

        return out.toByteArray();
    }

    public static Bitmap createScaledBitmap(Bitmap src, int newWidth, int newHeight) {
        int sourceWidth = src.getWidth();
        int sourceHeight = src.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, src.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(src, null, targetRect, null);

        return dest;
    }

    public static byte[] bmpToByteArray(Bitmap bmp, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);  // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > size) { // 循环判断如果压缩后图片是否大于size kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            if (options <= 0) {
                break;
            }
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10; // 每次都减少10
        }
        return baos.toByteArray();
    }

    public static Bitmap decodeScaledBitmapFromSdCard(String filePath,
                                                      int reqWidth, int reqHeight) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError err) {
            return null;
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromUrl(URL url, int reqWidth, int reqHeight) {

        try {
            byte[] imageData = downloadUrl(url);
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
        } catch (OutOfMemoryError error) {
            return null;
        }
    }

    private static byte[] downloadUrl(URL toDownload) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            byte[] chunk = new byte[4096];
            int bytesRead;
            InputStream stream = toDownload.openStream();

            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outputStream.toByteArray();
    }

    public static Bitmap decodeSampledBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {

        try {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(is, null, options);
        } catch (OutOfMemoryError error) {
            return null;
        }
    }

    public static void galleryAddPic(Context context, String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * 保存图片
     *
     * @param bitName bitName
     * @param mBitmap mBitmap
     * @return 图片保存位置
     */
    public static String saveMyBitmap(String bitName, Bitmap mBitmap) {

        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera";
        File file = new File(dirPath);
        String path = dirPath + File.separator + bitName;

        File f = new File(path);
        try {

            if (!file.exists()) {
                file.mkdirs();
            }

            if (!f.exists()) {
                f.createNewFile();
            }

            FileOutputStream fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            fOut.flush();
            fOut.close();
            mBitmap.recycle();

            return path;

        } catch (IOException e) {
            return null;
        }

    }

    /**
     * 把View绘制到Bitmap上
     *
     * @param width  该View的宽度
     * @param height 该View的高度
     * @return 返回Bitmap对象
     * add by WelliJohn
     */
    public static Bitmap getViewBitmap(View view, int width, int height) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 把WebView绘制到Bitmap上
     */
    public static Bitmap getWebViewShotBitmap(WebView webView) {
        int width = webView.getWidth();
        int height = webView.getHeight();
        if (width > 0 && height > 0) {
            try {
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                webView.draw(canvas);
                return bitmap;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 截屏的bitmap是否被压缩保存
     */
    public static boolean isShotBitmapCompressed(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    /**
     * 保存照片到存储卡中并返回图片的路径
     *
     * @param data
     * @return
     */
    public String savePicture(byte[] data) {
        try {
            return new SaveImageTask().execute(data).get();
        } catch (Exception e) {
        }
        return null;
    }

    public String savePicture(String fileName, byte[] data) {
        try {
            return new SaveImageTask(fileName).execute(data).get();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 保存编辑后的图片
     *
     * @param bitmap
     * @return
     */
    public String savePicture(Bitmap bitmap) {
        return savePicture(null, bitmap);
    }

    public String savePicture(String fileName, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        String path = savePicture(fileName, baos.toByteArray());
        return path;
    }

    private File getOutputMediaFile(String fileName) {
        if (isExternalStorageWritable()) {
            //如果是生成缩略图，就放到.thumbnail目录下，避免缩略图也在相册中显示
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "carPictures");
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            if (TextUtils.isEmpty(fileName)) {
                fileName = UUID.randomUUID().toString();
            }

            return new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + fileName + PICTURE_SUFFIX);
        } else {
            return null;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void scanFileAsync(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(path)));
        mContext.sendBroadcast(scanIntent);
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, String> {

        private String fileName;

        public SaveImageTask() {
        }

        public SaveImageTask(String fileName) {
            this.fileName = fileName;
        }

        @Override
        protected String doInBackground(byte[]... data) {
            String path = null;
            File pictureFile = getOutputMediaFile(fileName);
            if (pictureFile == null) {
                return path;
            } else {
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data[0]);
                    fos.flush();
                    fos.close();
                    path = pictureFile.getAbsolutePath();
                    scanFileAsync(path);
                    return path;
                } catch (IOException e) {
                    return path;
                }
            }
        }
    }
}
