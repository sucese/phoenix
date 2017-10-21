package com.guoxiaoxing.phoenix.demo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.guoxiaoxing.phoenix.picker.Phoenix;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant;
import com.guoxiaoxing.phoenix.core.listener.OnPickerListener;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.picker.rx.permission.RxPermissions;
import com.guoxiaoxing.phoenix.picker.util.PictureFileUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private List<MediaEntity> mPickList = new ArrayList<>();
    private MediaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoenix_demo);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(MainActivity.this, 4, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        adapter = new MediaAdapter(MainActivity.this, onAddPicClickListener);
        adapter.setList(mPickList);
        adapter.setSelectMax(10);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new MediaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (mPickList.size() > 0) {
                    //预览
                    Phoenix.with()
                            .currentIndex(position)
                            .mediaList(mPickList)
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

        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.INSTANCE.deleteCacheDirFile(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private MediaAdapter.onAddPicClickListener onAddPicClickListener = new MediaAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            List<MediaEntity> pickList = new ArrayList<>();

            Phoenix.with()
                    .theme(PhoenixOption.THEME_DEFAULT)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                    .fileType(MimeType.ofAll())
                    .maxSelectNum(10)// 最大图片选择数量
                    .minSelectNum(0)// 最小选择数量
                    .imageSpanCount(4)// 每行显示个数
                    .selectionMode(PhoenixConstant.MULTIPLE)// 多选 or 单选
                    .enablePreview(true)// 是否可预览图片
                    .enPreviewVideo(true)// 是否可预览视频
                    .enablePreviewAudio(true) // 是否可播放音频
                    .enableCamera(true)// 是否显示拍照按钮
                    .zoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .outputCameraPath("/CustomPath")// 自定义拍照保存路径
                    .enableCrop(true)// 是否裁剪
                    .enableCompress(true)// 是否压缩
                    .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                    .overrideHeight(160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                    .overrideWidth(160)
                    .aspect_ratio_x(3)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .aspect_ratio_y(3)
                    .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
                    .enableGif(true)// 是否显示gif图片
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                    .circleDimmedLayer(true)// 是否圆形裁剪
                    .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .openClickSound(true)//ƒ 是否开启点击声音
                    .mediaList(pickList)// 是否传入已选图片
                    .previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                    .cropCompressQuality(90)// 裁剪压缩质量 默认100
                    .compressMaxSize(10 * 1000)//压缩最大值kb compressGrade()为Luban.CUSTOM_GEAR有效
                    .compressMaxHeight(500)
                    .compressMaxWidth(300)
                    .rotateEnabled(true) // 裁剪是否可旋转图片
                    .scaleEnabled(true)// 裁剪是否可放大缩小图片
                    .videoQuality(1)// 视频录制质量 0 or 1
                    .videoSecond(0)//显示多少秒以内的视频or音频也可适用
                    .recordVideoSecond(2 * 60)//录制视频秒数 默认60s
                    .enableUpload(true)//是否开启上传
                    .enableCrop(true)
                    .enableCameraHint(true)
                    .enablePictureBlur(true)
                    .enablePictureMark(true)
                    .onPickerListener(new OnPickerListener() {
                        @Override
                        public void onPickSuccess(List<MediaEntity> pickList) {
                            adapter.setList(pickList);
                            adapter.notifyDataSetChanged();

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
                    }).start(MainActivity.this, PhoenixOption.TYPE_PICK_MEDIA);
        }
    };

    @Override
    public void onClick(View v) {

    }
}
