package com.guoxiaoxing.phoenix.demo.video;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.guoxiaoxing.phoenix.picker.ui.camera.CameraActivity;
import com.guoxiaoxing.phoenix.demo.R;

public class VideoDemoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_TAKE_PICTURE = 0x000001;
    private static final int REQUEST_CODE_TAKE_VIDEO = 0x000002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_camera_activity:
                startActivity(new Intent(this, CameraActivity.class));;
                break;
            case R.id.btn_take_picture:
                takePicture();
                break;
            case R.id.btn_take_video:
                takeVideo();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
    }

    private void takeVideo(){

    }
}
