package com.guoxiaoxing.phoenix.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.guoxiaoxing.phoenix.picker.SCPicker;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant;
import com.guoxiaoxing.phoenix.core.listener.OnPickerListener;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.picture.edit.ui.PictureEditActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout mLlRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLlRoot = (LinearLayout) findViewById(R.id.activity_main);

        findViewById(R.id.btn_picture_preview).setOnClickListener(this);
        findViewById(R.id.btn_picture_pick).setOnClickListener(this);
        findViewById(R.id.btn_take_picture).setOnClickListener(this);
        findViewById(R.id.btn_picture_edit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_picture_preview:
                List<MediaEntity> mediaEntityList = new ArrayList<>();
                MediaEntity mediaEntity1 = MediaEntity.newBuilder()
                        .fileType(MimeType.ofImage())
                        .onlinePath("http://img.souche.com/files/default/a55f77f36716ac51254e45d2590cc8e6.jpg")
                        .build();

                MediaEntity mediaEntity2 = MediaEntity.newBuilder()
                        .fileType(MimeType.ofImage())
                        .onlinePath("http://img.souche.com/files/default/88fa2b8b2c907115dc9cfb0f634b359d.jpg")
                        .build();

                MediaEntity mediaEntity3 = MediaEntity.newBuilder()
                        .fileType(MimeType.ofImage())
                        .onlinePath("http://img.souche.com/files/default/4ed29e4fc4ac18cf1c2daacc50d462e3.jpg")
                        .build();

                MediaEntity mediaEntity4 = MediaEntity.newBuilder()
                        .fileType(MimeType.ofVideo())
                        .onlineThumbnailPath("http://img.souche.com/files/default/0a396309749ed24c8ca54c998e14c67b.jpg")
                        .onlinePath("http://img.souche.com/files/default/8139857ae9fa5034138bce4d56ed2f48.mp4")
                        .build();

                mediaEntityList.add(mediaEntity1);
                mediaEntityList.add(mediaEntity2);
                mediaEntityList.add(mediaEntity3);
                mediaEntityList.add(mediaEntity4);
                SCPicker.with()
                        .mediaList(mediaEntityList)
                        .currentIndex(3)
                        .onPickerListener(new OnPickerListener() {
                            @Override
                            public void onPickSuccess(List<MediaEntity> pickList) {

                            }

                            @Override
                            public void onPickFailed(String errorMessage) {

                            }
                        })
                        .start(MainActivity.this, PhoenixOption.TYPE_BROWSER_PICTURE);
                break;
            case R.id.btn_picture_pick:
                startActivity(new Intent(MainActivity.this, PhoenixDemoActivity.class));
                break;
            case R.id.btn_take_picture:
                SCPicker.with()
                        .enableCompress(true)
                        .enableUpload(true)
                        .enableCameraHint(true)
                        .enableCameraModel(true)
                        .enablePictureBlur(true)
                        .enablePictureMark(true)
                        .onPickerListener(new OnPickerListener() {
                            @Override
                            public void onPickSuccess(List<MediaEntity> pickList) {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (MediaEntity mediaEntity : pickList) {
                                    stringBuilder.append("localPath: ")
                                            .append("\n")
                                            .append(mediaEntity.getLocalPath())
                                            .append("\n")
                                            .append("onlinePath: ")
                                            .append("\n")
                                            .append(mediaEntity.getOnlinePath())
                                            .append("\n")
                                            .append("compressPath: ")
                                            .append("\n")
                                            .append(mediaEntity.getCompressPath())
                                            .append("\n")
                                            .append("longitude:")
                                            .append("\n")
                                            .append(mediaEntity.getLongitude())
                                            .append("\n")
                                            .append("latitude:")
                                            .append("\n")
                                            .append(mediaEntity.getLatitude())
                                            .append("\n")
                                            .append("============================")
                                            .append("\n");
                                }


                            }

                            @Override
                            public void onPickFailed(String errorMessage) {

                            }
                        })
                        .start(MainActivity.this, PhoenixOption.TYPE_TAKE_PICTURE);
                break;
            case R.id.btn_picture_edit:
                String localPath = "/storage/emulated/0/DCIM/cache/1507891420935338.jpg";
                Intent intent = new Intent(MainActivity.this, PictureEditActivity.class);
                intent.putExtra(PhoenixConstant.KEY_FILE_PATH, localPath);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}