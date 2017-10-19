package com.guoxiaoxing.phoenix.demo;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.guoxiaoxing.phoenix.picker.SCPicker;
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

public class PhoenixDemoActivity extends AppCompatActivity implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

    private final static String TAG = PhoenixDemoActivity.class.getSimpleName();
    private List<MediaEntity> selectList = new ArrayList<>();
    private RecyclerView recyclerView;
    private GridImageAdapter adapter;
    private int maxSelectNum = 3;
    private TextView tv_select_num;
    private ImageView  minus, plus;
    private RadioGroup rgb_crop, rgb_compress, rgb_style, rgb_photo_mode;
    private int aspect_ratio_x, aspect_ratio_y;
    private CheckBox cb_voice, cb_choose_mode, cb_isCamera, cb_isGif,
            cb_preview_img, cb_preview_video, cb_crop, cb_compress,
            cb_mode, cb_hide, cb_crop_circular, cb_styleCrop, cb_showCropGrid,
            cb_showCropFrame, cb_preview_audio;

    private int compressMode = PhoenixConstant.SYSTEM_COMPRESS_MODE;
    private int chooseMode = MimeType.ofAll();
    private String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoenix_demo);
        minus = (ImageView) findViewById(R.id.minus);
        plus = (ImageView) findViewById(R.id.plus);
        tv_select_num = (TextView) findViewById(R.id.tv_select_num);
        rgb_crop = (RadioGroup) findViewById(R.id.rgb_crop);
        rgb_style = (RadioGroup) findViewById(R.id.rgb_style);
        rgb_photo_mode = (RadioGroup) findViewById(R.id.rgb_photo_mode);
        rgb_compress = (RadioGroup) findViewById(R.id.rgb_compress);
        cb_voice = (CheckBox) findViewById(R.id.cb_voice);
        cb_choose_mode = (CheckBox) findViewById(R.id.cb_choose_mode);
        cb_isCamera = (CheckBox) findViewById(R.id.cb_isCamera);
        cb_isGif = (CheckBox) findViewById(R.id.cb_isGif);
        cb_preview_img = (CheckBox) findViewById(R.id.cb_preview_img);
        cb_preview_video = (CheckBox) findViewById(R.id.cb_preview_video);
        cb_crop = (CheckBox) findViewById(R.id.cb_crop);
        cb_styleCrop = (CheckBox) findViewById(R.id.cb_styleCrop);
        cb_compress = (CheckBox) findViewById(R.id.cb_compress);
        cb_mode = (CheckBox) findViewById(R.id.cb_mode);
        cb_showCropGrid = (CheckBox) findViewById(R.id.cb_showCropGrid);
        cb_showCropFrame = (CheckBox) findViewById(R.id.cb_showCropFrame);
        cb_preview_audio = (CheckBox) findViewById(R.id.cb_preview_audio);
        cb_hide = (CheckBox) findViewById(R.id.cb_hide);
        cb_crop_circular = (CheckBox) findViewById(R.id.cb_crop_circular);
        rgb_crop.setOnCheckedChangeListener(this);
        rgb_compress.setOnCheckedChangeListener(this);
        rgb_style.setOnCheckedChangeListener(this);
        rgb_photo_mode.setOnCheckedChangeListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        minus.setOnClickListener(this);
        plus.setOnClickListener(this);
        cb_crop.setOnCheckedChangeListener(this);
        cb_crop_circular.setOnCheckedChangeListener(this);
        cb_compress.setOnCheckedChangeListener(this);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(PhoenixDemoActivity.this, 4, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        adapter = new GridImageAdapter(PhoenixDemoActivity.this, onAddPicClickListener);
        adapter.setList(selectList);
        adapter.setSelectMax(maxSelectNum);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (selectList.size() > 0) {
                    MediaEntity mediaEntity = selectList.get(position);
                    String pictureType = mediaEntity.getMimeType();
                    int mediaType = MimeType.pictureToVideo(pictureType);
                    switch (mediaType) {
                        case 1:
                            // 预览图片 可自定长按保存路径
                            SCPicker.with()
                                    .currentIndex(position)
                                    .mediaList(selectList)
                                    .onPickerListener(new OnPickerListener() {
                                        @Override
                                        public void onPickSuccess(List<MediaEntity> pickList) {

                                        }

                                        @Override
                                        public void onPickFailed(String errorMessage) {

                                        }
                                    })
                                    .start(PhoenixDemoActivity.this, PhoenixOption.TYPE_BROWSER_PICTURE);
                            break;
                        case 2:
                            // 预览视频
                            break;
                        case 3:
                            // 预览音频
                            break;
                    }
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
                    PictureFileUtils.INSTANCE.deleteCacheDirFile(PhoenixDemoActivity.this);
                } else {
                    Toast.makeText(PhoenixDemoActivity.this,
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

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            SCPicker.with()
                    .theme(TextUtils.isEmpty(theme) ? PhoenixOption.THEME_DEFAULT : theme)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                    .fileType(chooseMode)
                    .maxSelectNum(maxSelectNum)// 最大图片选择数量
                    .minSelectNum(1)// 最小选择数量
                    .imageSpanCount(4)// 每行显示个数
                    .selectionMode(cb_choose_mode.isChecked() ? PhoenixConstant.MULTIPLE : PhoenixConstant.SINGLE)// 多选 or 单选
                    .enablePreview(cb_preview_img.isChecked())// 是否可预览图片
                    .enPreviewVideo(cb_preview_video.isChecked())// 是否可预览视频
                    .enablePreviewAudio(cb_preview_audio.isChecked()) // 是否可播放音频
                    .enableCamera(cb_isCamera.isChecked())// 是否显示拍照按钮
                    .zoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .outputCameraPath("/CustomPath")// 自定义拍照保存路径
                    .enableCrop(cb_crop.isChecked())// 是否裁剪
                    .enableCompress(cb_compress.isChecked())// 是否压缩
                    .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                    .overrideHeight(160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                    .overrideWidth(160)
                    .aspect_ratio_x(aspect_ratio_x)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .aspect_ratio_y(aspect_ratio_y)
                    .hideBottomControls(!cb_hide.isChecked())// 是否显示uCrop工具栏，默认不显示
                    .enableGif(cb_isGif.isChecked())// 是否显示gif图片
                    .freeStyleCropEnabled(cb_styleCrop.isChecked())// 裁剪框是否可拖拽
                    .circleDimmedLayer(cb_crop_circular.isChecked())// 是否圆形裁剪
                    .showCropFrame(cb_showCropFrame.isChecked())// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropGrid(cb_showCropGrid.isChecked())// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .openClickSound(cb_voice.isChecked())//ƒ 是否开启点击声音
                    .mediaList(selectList)// 是否传入已选图片
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
                            selectList.addAll(pickList);
                            adapter.setList(selectList);
                            adapter.notifyDataSetChanged();

                            StringBuilder stringBuilder = new StringBuilder();
                            for (MediaEntity mediaEntity : selectList) {
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
                    }).start(PhoenixDemoActivity.this, PhoenixOption.TYPE_PICK_MEDIA);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.minus:
                if (maxSelectNum > 1) {
                    maxSelectNum--;
                }
                tv_select_num.setText(maxSelectNum + "");
                adapter.setSelectMax(maxSelectNum);
                break;
            case R.id.plus:
                maxSelectNum++;
                tv_select_num.setText(maxSelectNum + "");
                adapter.setSelectMax(maxSelectNum);
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.rb_all:
                chooseMode = MimeType.ofAll();
                cb_preview_img.setChecked(true);
                cb_preview_video.setChecked(true);
                cb_isGif.setChecked(false);
                cb_preview_video.setChecked(true);
                cb_preview_img.setChecked(true);
                cb_preview_video.setVisibility(View.VISIBLE);
                cb_preview_img.setVisibility(View.VISIBLE);
                cb_compress.setVisibility(View.VISIBLE);
                cb_crop.setVisibility(View.VISIBLE);
                cb_isGif.setVisibility(View.VISIBLE);
                cb_preview_audio.setVisibility(View.GONE);
                break;
            case R.id.rb_image:
                chooseMode = MimeType.ofImage();
                cb_preview_img.setChecked(true);
                cb_preview_video.setChecked(false);
                cb_isGif.setChecked(false);
                cb_preview_video.setChecked(false);
                cb_preview_video.setVisibility(View.GONE);
                cb_preview_img.setChecked(true);
                cb_preview_audio.setVisibility(View.GONE);
                cb_preview_img.setVisibility(View.VISIBLE);
                cb_compress.setVisibility(View.VISIBLE);
                cb_crop.setVisibility(View.VISIBLE);
                cb_isGif.setVisibility(View.VISIBLE);
                break;
            case R.id.rb_video:
                chooseMode = MimeType.ofVideo();
                cb_preview_img.setChecked(false);
                cb_preview_video.setChecked(true);
                cb_isGif.setChecked(false);
                cb_isGif.setVisibility(View.GONE);
                cb_preview_video.setChecked(true);
                cb_preview_video.setVisibility(View.VISIBLE);
                cb_preview_img.setVisibility(View.GONE);
                cb_preview_img.setChecked(false);
                cb_compress.setVisibility(View.GONE);
                cb_preview_audio.setVisibility(View.GONE);
                cb_crop.setVisibility(View.GONE);
                break;
            case R.id.rb_audio:
                chooseMode = MimeType.ofAudio();
                cb_preview_audio.setVisibility(View.VISIBLE);
                break;
            case R.id.rb_crop_default:
                aspect_ratio_x = 0;
                aspect_ratio_y = 0;
                break;
            case R.id.rb_crop_1to1:
                aspect_ratio_x = 1;
                aspect_ratio_y = 1;
                break;
            case R.id.rb_crop_3to4:
                aspect_ratio_x = 3;
                aspect_ratio_y = 4;
                break;
            case R.id.rb_crop_3to2:
                aspect_ratio_x = 3;
                aspect_ratio_y = 2;
                break;
            case R.id.rb_crop_16to9:
                aspect_ratio_x = 16;
                aspect_ratio_y = 9;
                break;
            case R.id.rb_compress_system:
                compressMode = PhoenixConstant.SYSTEM_COMPRESS_MODE;
                break;
            case R.id.rb_compress_luban:
                compressMode = PhoenixConstant.LUBAN_COMPRESS_MODE;
                break;
            case R.id.rb_default_style:
                theme = PhoenixOption.THEME_DEFAULT;
                break;
            case R.id.rb_cn_style:
                theme = PhoenixOption.THEME_RED;
                break;
            case R.id.rb_tgc_style:
                theme = PhoenixOption.THEME_ORANGE;
                break;
            case R.id.rb_scd_style:
                theme = PhoenixOption.THEME_BLUE;
                break;
        }
    }

    private int x = 0, y = 0;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_crop:
                rgb_crop.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                cb_hide.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                cb_crop_circular.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                cb_styleCrop.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                cb_showCropFrame.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                cb_showCropGrid.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
            case R.id.cb_crop_circular:
                if (isChecked) {
                    x = aspect_ratio_x;
                    y = aspect_ratio_y;
                    aspect_ratio_x = 1;
                    aspect_ratio_y = 1;
                } else {
                    aspect_ratio_x = x;
                    aspect_ratio_y = y;
                }
                rgb_crop.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                if (isChecked) {
                    cb_showCropFrame.setChecked(false);
                    cb_showCropGrid.setChecked(false);
                } else {
                    cb_showCropFrame.setChecked(true);
                    cb_showCropGrid.setChecked(true);
                }
                break;
            case R.id.cb_compress:
                rgb_compress.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
        }
    }
}
