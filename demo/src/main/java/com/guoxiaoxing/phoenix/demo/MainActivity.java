package com.guoxiaoxing.phoenix.demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.guoxiaoxing.phoenix.compress.video.soft.MediaRecorderActivity;
import com.guoxiaoxing.phoenix.compress.video.soft.SCCamera;
import com.guoxiaoxing.phoenix.compress.video.soft.model.MediaRecorderConfig;
import com.guoxiaoxing.phoenix.compress.video.soft.util.DeviceUtils;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant;
import com.guoxiaoxing.phoenix.core.listener.OnPickerListener;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.picker.Phoenix;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaAdapter.OnAddMediaListener {

    private MediaAdapter mMediaAdapter;

    static {
        // 设置拍摄视频缓存路径
        File dcim = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                SCCamera.setVideoCachePath(dcim + "/souche/");
            } else {
                SCCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/",
                        "/sdcard-ext/")
                        + "/souche/");
            }
        } else {
            SCCamera.setVideoCachePath(dcim + "/souche/");
        }
        // 初始化拍摄
        SCCamera.initialize(false, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoenix_demo);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4, GridLayoutManager.VERTICAL, false));
        mMediaAdapter = new MediaAdapter(this);
        recyclerView.setAdapter(mMediaAdapter);
        mMediaAdapter.setOnItemClickListener(new MediaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (mMediaAdapter.getData().size() > 0) {
                    //预览
                    Phoenix.with()
                            .pickedMediaList(mMediaAdapter.getData())
                            .onPickerListener(new OnPickerListener() {
                                @Override
                                public void onPickSuccess(List<MediaEntity> pickList) {

                                }

                                @Override
                                public void onPickFailed(String errorMessage) {

                                }
                            })
                            .start(MainActivity.this, PhoenixOption.TYPE_BROWSER_PICTURE);
                }
            }
        });

        findViewById(R.id.tv_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
                        .fullScreen(true)
//                        .smallVideoWidth(needFull?0:Integer.valueOf(width))
//                        .smallVideoHeight(Integer.valueOf(height))
//                        .recordTimeMax(Integer.valueOf(maxTime))
//                        .recordTimeMin(Integer.valueOf(minTime))
//                        .maxFrameRate(Integer.valueOf(maxFramerate))
//                        .videoBitrate(Integer.valueOf(bitrate))
                        .captureThumbnailsTime(1)
                        .build();
                MediaRecorderActivity.goSmallVideoRecorder(MainActivity.this, MainActivity.class.getName(), config);
            }
        });
    }

    @Override
    public void onaddMedia() {
        Phoenix.with()
                .theme(PhoenixOption.THEME_DEFAULT)// 主题
                .fileType(MimeType.ofAll())//显示的文件类型图片、视频、图片和视频
                .maxPickNumber(10)// 最大选择数量
                .minPickNumber(0)// 最小选择数量
                .spanCount(4)// 每行显示个数
                .enablePreview(true)// 是否开启预览
                .enableCamera(true)// 是否开启拍照
                .enableAnimation(true)// 选择界面图片点击效果
                .enableCompress(true)// 是否开启压缩
                .thumbnailHeight(160)// 选择界面图片高度
                .thumbnailWidth(160)// 选择界面图片宽度
                .enableClickSound(false)// 是否开启点击声音
                .pickedMediaList(mMediaAdapter.getData())// 已选图片数据
                .videoFilterTime(0)//显示多少秒以内的视频
                .onPickerListener(new OnPickerListener() {
                    @Override
                    public void onPickSuccess(List<MediaEntity> pickList) {
                        mMediaAdapter.setData(pickList);
                    }

                    @Override
                    public void onPickFailed(String errorMessage) {

                    }
                }).start(MainActivity.this, PhoenixOption.TYPE_PICK_MEDIA);
    }
}
