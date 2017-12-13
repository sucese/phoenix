package com.guoxiaoxing.phoenix.demo.picture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.guoxiaoxing.phoenix.demo.R;
import com.guoxiaoxing.toolkit.ImageUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PictureDemoActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        findViewById(R.id.btn_bitmap_compress).setOnClickListener(this);
        findViewById(R.id.btn_bitmap_factory).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bitmap_compress:
                qualityCompress();
                break;
            case R.id.btn_bitmap_factory:
                nearestNeighbourResampling();
                break;
        }
    }

    private void qualityCompress() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "timo_compress_quality_100.jpg");
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
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void nearestNeighbourResampling() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.blue_red, options);
        String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
                + "/timo_BitmapFactory_1.png";
        ImageUtils.save(bitmap, savePath, Bitmap.CompressFormat.PNG);
    }

    private void bilinearResampling() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.blue_red);
        Matrix matrix = new Matrix();
        matrix.setScale(0.5f, 0.5f);
        Bitmap sclaedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth()/2, bitmap.getHeight()/2, matrix, true);
        String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/timo_BitmapFactory_1.png";
        ImageUtils.save(bitmap, savePath, Bitmap.CompressFormat.PNG);
    }
}
