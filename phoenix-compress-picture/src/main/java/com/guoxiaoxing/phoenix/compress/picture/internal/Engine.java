package com.guoxiaoxing.phoenix.compress.picture.internal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class Engine {

  private int mFilterSize;
  private ExifInterface mSourceExif;
  private File mSourcePicture;
  private File mTargetPicture;
  private int mSourceWidth;
  private int mSourceHeight;

  Engine(File sourcePicture, File targetPicture, int filterSize) throws IOException {
    if (isJpeg(sourcePicture)) {
      this.mSourceExif = new ExifInterface(sourcePicture.getAbsolutePath());
    }

    this.mSourcePicture = sourcePicture;
    this.mTargetPicture = targetPicture;
    this.mFilterSize = filterSize;

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    options.inSampleSize = 1;

    BitmapFactory.decodeFile(mSourcePicture.getAbsolutePath(), options);
    this.mSourceWidth = options.outWidth;
    this.mSourceHeight = options.outHeight;
  }

  private boolean isJpeg(File photo) {
    return photo.getAbsolutePath().contains("jpeg") || photo.getAbsolutePath().contains("jpg");
  }

  private int computeSize() {
    int mSampleSize;

    mSourceWidth = mSourceWidth % 2 == 1 ? mSourceWidth + 1 : mSourceWidth;
    mSourceHeight = mSourceHeight % 2 == 1 ? mSourceHeight + 1 : mSourceHeight;

    mSourceWidth = mSourceWidth > mSourceHeight ? mSourceHeight : mSourceWidth;
    mSourceHeight = mSourceWidth > mSourceHeight ? mSourceWidth : mSourceHeight;

    double scale = ((double) mSourceWidth / mSourceHeight);

    if (scale <= 1 && scale > 0.5625) {
      if (mSourceHeight < 1664) {
        mSampleSize = 1;
      } else if (mSourceHeight >= 1664 && mSourceHeight < 4990) {
        mSampleSize = 2;
      } else if (mSourceHeight >= 4990 && mSourceHeight < 10240) {
        mSampleSize = 4;
      } else {
        mSampleSize = mSourceHeight / 1280 == 0 ? 1 : mSourceHeight / 1280;
      }
    } else if (scale <= 0.5625 && scale > 0.5) {
      mSampleSize = mSourceHeight / 1280 == 0 ? 1 : mSourceHeight / 1280;
    } else {
      mSampleSize = (int) Math.ceil(mSourceHeight / (1280.0 / scale));
    }

    return mSampleSize;
  }

  private Bitmap rotatingImage(Bitmap bitmap) {
    if (mSourceExif == null) return bitmap;

    Matrix matrix = new Matrix();
    int angle = 0;
    int orientation = mSourceExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        angle = 90;
        break;
      case ExifInterface.ORIENTATION_ROTATE_180:
        angle = 180;
        break;
      case ExifInterface.ORIENTATION_ROTATE_270:
        angle = 270;
        break;
    }

    matrix.postRotate(angle);

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }

  File compress() throws IOException {

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = computeSize();

    Bitmap tagBitmap = BitmapFactory.decodeFile(mSourcePicture.getAbsolutePath(), options);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    tagBitmap = rotatingImage(tagBitmap);
    tagBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
    tagBitmap.recycle();

    FileOutputStream fos = new FileOutputStream(mTargetPicture);
    fos.write(stream.toByteArray());
    fos.flush();
    fos.close();
    stream.close();
    return mTargetPicture;
  }
}