package com.guoxiaoxing.phoenix.compress.video.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.guoxiaoxing.phoenix.compress.video.util.Util;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class VideoCompressor {

    private static final String TAG = "VideoCompressor";

    public static String videoCompressionPath;
    private boolean isconverted;
    private Context mContext;
    private String mDestinationUri;

    private VideoCompressor(Builder builder) {
        isconverted = builder.isconverted;
        mContext = builder.context;
        mDestinationUri = builder.destinationUri;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Observable<MediaEntity> singleAction(final MediaEntity mediaEntity) {
        return Observable.fromCallable(new Callable<MediaEntity>() {
            @Override
            public MediaEntity call() throws Exception {
                return compressVideo(mediaEntity);
            }
        }).subscribeOn(Schedulers.computation());
    }

    public Observable<List<MediaEntity>> multiAction(List<MediaEntity> mediaEntitys) {
        List<Observable<MediaEntity>> observables = new ArrayList<>(mediaEntitys.size());
        for (final MediaEntity mediaEntity : mediaEntitys) {
            observables.add(Observable.fromCallable(new Callable<MediaEntity>() {
                @Override
                public MediaEntity call() throws Exception {
                    return compressVideo(mediaEntity);
                }
            }));
        }

        return Observable.zip(observables, new Function<Object[], List<MediaEntity>>() {
            @Override
            public List<MediaEntity> apply(Object[] objects) throws Exception {
                List<MediaEntity> paths = new ArrayList<>(objects.length);
                for (Object object : objects) {
                    paths.add((MediaEntity) object);
                }
                return paths;
            }
        });
    }

    /**
     * Compresses the image at the specified Uri String and and return the filepath of the compressed image.
     *
     * @param imageUri imageUri Uri (String) of the source image you wish to compress
     * @return filepath
     */
    public String compress(String imageUri, File destination) {
        return compressImage(imageUri, destination);
    }

    /**
     * Compresses the image at the specified Uri String and and return the filepath of the compressed image.
     *
     * @param imageUri imageUri Uri (String) of the source image you wish to compress
     * @return filepath
     */
    public String compress(String imageUri, File destination, boolean deleteSourceImage) {

        String compressUri = compressImage(imageUri, destination);

        if (deleteSourceImage) {
            File source = new File(getRealPathFromURI(imageUri));
            if (source.exists()) {
                boolean isdeleted = source.delete();
                Log.d(TAG, (isdeleted) ? "SourceImage File deleted" : "SourceImage File not deleted");
            }
        }

        return compressUri;
    }


    public String compress(int drawableID) throws IOException {

        // Create a bitmap from this drawable
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getApplicationContext().getResources(), drawableID);
        if (null != bitmap) {
            // Create a file from the bitmap

            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            FileOutputStream out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            // Compress the new file
            Uri copyImageUri = Uri.fromFile(image);

            String compressImagePath = compressImage(copyImageUri.toString(), new File(Environment.getExternalStorageDirectory(), "Silicompressor/images"));

            // Delete the file create from the drawable Id
            if (image.exists()) {
                boolean isdeleted = image.delete();
                Log.d(TAG, (isdeleted) ? "SourceImage File deleted" : "SourceImage File not deleted");
            }

            // return the path to the compress image
            return compressImagePath;
        }

        return null;
    }


    /**
     * Compresses the image at the specified Uri String and and return the bitmap data of the compressed image.
     *
     * @param imageUri imageUri Uri (String) of the source image you wish to compress
     * @return Bitmap format of the new image file (compressed)
     * @throws IOException
     */
    public Bitmap getCompressBitmap(String imageUri) throws IOException {
        File imageFile = new File(compressImage(imageUri, new File(Environment.getExternalStorageDirectory(), "Silicompressor/images")));
        Uri newImageUri = Uri.fromFile(imageFile);
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), newImageUri);
        return bitmap;
    }

    /**
     * Compresses the image at the specified Uri String and and return the bitmap data of the compressed image.
     *
     * @param imageUri          Uri (String) of the source image you wish to compress
     * @param deleteSourceImage If True will delete the source file
     * @return Compress image bitmap
     * @throws IOException
     */
    public Bitmap getCompressBitmap(String imageUri, boolean deleteSourceImage) throws IOException {
        File imageFile = new File(compressImage(imageUri, new File(Environment.getExternalStorageDirectory(), "Silicompressor/images")));
        Uri newImageUri = Uri.fromFile(imageFile);
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), newImageUri);

        if (deleteSourceImage) {
            File source = new File(getRealPathFromURI(imageUri));
            if (source.exists()) {
                boolean isdeleted = source.delete();
                Log.d(TAG, (isdeleted) ? "SourceImage File deleted" : "SourceImage File not deleted");
            }
        }
        return bitmap;
    }

    /**
     * Do the actual compression of this image
     *
     * @param imageUri      source image file to compress
     * @param destDirectory destination directory to place image in
     * @return uri string of the compressed image
     */
    private String compressImage(String imageUri, File destDirectory) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

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

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
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

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename(imageUri, destDirectory);
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    /**
     * Perform background video compression. Make sure the videofileUri and mDestinationUri are valid
     * resources because this method does not account for missing directories hence your converted file
     * could be in an unknown location
     *
     * @param mediaEntity source uri for the video file
     * @return The Path of the compressed video file
     */
    public MediaEntity compressVideo(MediaEntity mediaEntity) {
        String originPath = "file:///" + mediaEntity.getLocalPath();
        String compressPath = null;
        try {
            compressPath = MediaController.getInstance().convertVideo(Util.getFilePath(mContext,
                    Uri.parse(originPath)), new File(mDestinationUri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mediaEntity.setCompressed(true);
        mediaEntity.setCompressPath(compressPath);
        return mediaEntity;
    }

    public boolean isconverted() {
        return isconverted;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private String getFilename(String filename, File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        String ext = ".jpg";
        //get extension
        /*if (Pattern.matches("^[.][p][n][g]", filename)){
            ext = ".png";
        }*/

        return (file.getAbsolutePath() + "/IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ext);

    }

    /**
     * Gets a valid path from the supply contentURI
     *
     * @param contentURI
     * @return A validPath of the image
     */
    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = mContext.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String str = cursor.getString(index);
            cursor.close();
            return str;
        }
        // return  FileUtils.getPath(mContext, contentUri);

        // return  getRealPathFromURI_API19(mContext, contentUri);
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    public static final class Builder {
        private boolean isconverted;
        private Context context;
        private String destinationUri;

        private Builder() {
        }

        public Builder isconverted(boolean val) {
            isconverted = val;
            return this;
        }

        public Builder context(Context val) {
            context = val;
            return this;
        }

        public Builder destinationUri(String val) {
            destinationUri = val;
            return this;
        }

        public VideoCompressor build() {
            return new VideoCompressor(this);
        }
    }
}
