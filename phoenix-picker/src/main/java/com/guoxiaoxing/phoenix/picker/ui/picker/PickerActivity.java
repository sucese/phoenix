package com.guoxiaoxing.phoenix.picker.ui.picker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.guoxiaoxing.phoenix.R;
import com.guoxiaoxing.phoenix.picker.adapter.PickerAlbumAdapter;
import com.guoxiaoxing.phoenix.picker.adapter.PickerAdapter;
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.core.model.MimeType;
import com.guoxiaoxing.phoenix.picker.model.EventEntity;
import com.guoxiaoxing.phoenix.picker.model.MediaFolder;
import com.guoxiaoxing.phoenix.picker.model.MediaLoader;
import com.guoxiaoxing.phoenix.picker.rx.permission.RxPermissions;
import com.guoxiaoxing.phoenix.picker.rx.bus.ImagesObservable;
import com.guoxiaoxing.phoenix.picker.rx.bus.RxBus;
import com.guoxiaoxing.phoenix.picker.rx.bus.Subscribe;
import com.guoxiaoxing.phoenix.picker.rx.bus.ThreadMode;
import com.guoxiaoxing.phoenix.picker.ui.BaseActivity;
import com.guoxiaoxing.phoenix.picker.util.AttrsUtils;
import com.guoxiaoxing.phoenix.picker.util.DateUtils;
import com.guoxiaoxing.phoenix.picker.util.DebugUtil;
import com.guoxiaoxing.phoenix.picker.util.LightStatusBarUtils;
import com.guoxiaoxing.phoenix.picker.util.ScreenUtils;
import com.guoxiaoxing.phoenix.picker.util.StringUtils;
import com.guoxiaoxing.phoenix.picker.widget.FolderPopWindow;
import com.guoxiaoxing.phoenix.picker.widget.GridSpacingItemDecoration;
import com.guoxiaoxing.phoenix.picker.widget.dialog.CustomDialog;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * The media pick activity
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
public class PickerActivity extends BaseActivity implements View.OnClickListener, PickerAlbumAdapter.OnItemClickListener,
        PickerAdapter.OnPhotoSelectChangedListener {

    private final static String TAG = PickerActivity.class.getSimpleName();

    private ImageView picture_left_back;
    private TextView picture_title, picture_right, picture_tv_ok, tv_empty,
            picture_tv_img_num, picture_id_preview, tv_PlayPause, tv_Stop, tv_Quit,
            tv_musicStatus, tv_musicTotal, tv_musicTime;
    private RelativeLayout rl_picture_title, rl_bottom;
    private LinearLayout id_ll_ok;
    private RecyclerView picture_recycler;
    private PickerAdapter adapter;
    private List<MediaEntity> images = new ArrayList<>();
    private List<MediaFolder> foldersList = new ArrayList<>();
    private FolderPopWindow folderWindow;
    private Animation animation = null;
    private boolean anim = false;
    private int preview_textColor;
    private RxPermissions rxPermissions;
    private MediaLoader mediaLoader;
    private MediaPlayer mediaPlayer;
    private SeekBar musicSeekBar;
    private boolean isPlayAudio = false;
    private CustomDialog audioDialog;
    private int audioH;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBus(EventEntity obj) {
        switch (obj.what) {
            //receive the select result from PreviewActivity
            case PhoenixConstant.FLAG_PREVIEW_UPDATE_SELECT:
                List<MediaEntity> selectImages = obj.mediaEntities;
                anim = selectImages.size() > 0 ? true : false;
                int position = obj.position;
                DebugUtil.INSTANCE.i(TAG, "刷新下标::" + position);
                adapter.bindSelectImages(selectImages);
                //通知点击项发生了改变
                boolean isExceedMax = selectImages.size() >= getMaxSelectNum() && getMaxSelectNum() != 0;
                adapter.setExceedMax(isExceedMax);
                if (isExceedMax || selectImages.size() == (getMaxSelectNum() - 1)) {
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.notifyItemChanged(position);
                }
                break;
            case PhoenixConstant.FLAG_PREVIEW_COMPLETE:
                List<MediaEntity> mediaEntities = obj.mediaEntities;
                processMedia(mediaEntities);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!RxBus.Companion.getDefault().isRegistered(this)) {
            RxBus.Companion.getDefault().register(this);
        }
        rxPermissions = new RxPermissions(this);
        LightStatusBarUtils.INSTANCE.setLightStatusBar(this, getStatusFont());
        //TODO
        if (false) {
            if (savedInstanceState == null) {
                rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                if (aBoolean) {
                                    onTakePhoto();
                                } else {
                                    showToast(getString(R.string.picture_camera));
                                    closeActivity();
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
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    , WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_empty);
        } else {
            setContentView(R.layout.activity_picker);
            setupView();
        }
    }

    /**
     * init views
     */
    private void setupView() {

        rl_picture_title = (RelativeLayout) findViewById(R.id.rl_picture_title);
        picture_left_back = (ImageView) findViewById(R.id.picture_left_back);
        picture_title = (TextView) findViewById(R.id.picture_title);
        picture_right = (TextView) findViewById(R.id.picture_right);
        picture_tv_ok = (TextView) findViewById(R.id.pick_tv_ok);
        picture_id_preview = (TextView) findViewById(R.id.picture_id_preview);
        picture_tv_img_num = (TextView) findViewById(R.id.pick_tv_picture_number);
        picture_recycler = (RecyclerView) findViewById(R.id.picture_recycler);
        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        id_ll_ok = (LinearLayout) findViewById(R.id.pick_ll_ok);
        tv_empty = (TextView) findViewById(R.id.tv_empty);
        rl_bottom.setVisibility(getSelectionMode() == PhoenixConstant.SINGLE ? View.GONE : View.VISIBLE);
        isNumComplete(getNumComplete());
        picture_id_preview.setOnClickListener(this);
        if (getFileType() == MimeType.ofAudio()) {
            picture_id_preview.setVisibility(View.GONE);
            audioH = ScreenUtils.INSTANCE.getScreenHeight(getMContext())
                    + ScreenUtils.INSTANCE.getStatusBarHeight(getMContext());
        } else {
            picture_id_preview.setVisibility(getFileType() == PhoenixConstant.TYPE_VIDEO
                    ? View.GONE : View.VISIBLE);
        }
        picture_left_back.setOnClickListener(this);
        picture_right.setOnClickListener(this);
        id_ll_ok.setOnClickListener(this);
        picture_title.setOnClickListener(this);
        String title = getFileType() == MimeType.ofAudio() ? getString(R.string.picture_all_audio)
                : getString(R.string.picture_camera_roll);
        picture_title.setText(title);
        folderWindow = new FolderPopWindow(this, getFileType());
        folderWindow.setPictureTitleView(picture_title);
        folderWindow.setOnItemClickListener(this);
        picture_recycler.setHasFixedSize(true);
        picture_recycler.addItemDecoration(new GridSpacingItemDecoration(getSpanCount(),
                ScreenUtils.INSTANCE.dip2px(this, 2), false));
        picture_recycler.setLayoutManager(new GridLayoutManager(this, getSpanCount()));
        // 解决调用 notifyItemChanged 闪烁问题,取消默认动画
        ((SimpleItemAnimator) picture_recycler.getItemAnimator())
                .setSupportsChangeAnimations(false);
        mediaLoader = new MediaLoader(this, getFileType(), isGif(), getVideoSecond());
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        showLoadingDialog();
                        if (aBoolean) {
                            readLocalMedia();
                        } else {
                            showToast(getString(R.string.picture_jurisdiction));
                            dismissLoadingDialog();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        tv_empty.setText(getFileType() == MimeType.ofAudio() ?
                getString(R.string.picture_audio_empty)
                : getString(R.string.picture_empty));
        StringUtils.INSTANCE.tempTextFont(tv_empty, getFileType());
        preview_textColor = AttrsUtils.INSTANCE.getTypeValueColor(this, R.attr.phoenix_picker_preview_text_color);
        adapter = new PickerAdapter(getMContext(), getOption());
        adapter.bindSelectImages(getMediaList());
        changeImageNumber(getMediaList());
        picture_recycler.setAdapter(adapter);
        adapter.setOnPhotoSelectChangedListener(PickerActivity.this);
        String titleText = picture_title.getText().toString().trim();
        if (getEnableCamera()) {
            setEnableCamera(StringUtils.INSTANCE.isCamera(titleText));
        }
    }


    /**
     * none number style
     */
    @SuppressLint("StringFormatMatches")
    private void isNumComplete(boolean numComplete) {
        picture_tv_ok.setText(numComplete ? getString(R.string.picture_done_front_num, 0, getMaxSelectNum())
                : getString(R.string.picture_please_select));
        if (!numComplete) {
            animation = AnimationUtils.loadAnimation(this, R.anim.phoenix_window_in);
        }
        animation = numComplete ? null : AnimationUtils.loadAnimation(this, R.anim.phoenix_window_in);
    }

    /**
     * get MediaEntity s
     */
    protected void readLocalMedia() {
        mediaLoader.loadAllMedia(new MediaLoader.LocalMediaLoadListener() {
            @Override
            public void loadComplete(List<MediaFolder> folders) {
                DebugUtil.INSTANCE.i("loadComplete:" + folders.size());
                if (folders.size() > 0) {
                    foldersList = folders;
                    MediaFolder folder = folders.get(0);
                    folder.setChecked(true);
                    List<MediaEntity> localImg = folder.getImages();
                    // 这里解决有些机型会出现拍照完，相册列表不及时刷新问题
                    // 因为onActivityResult里手动添加拍照后的照片，
                    // 如果查询出来的图片大于或等于当前adapter集合的图片则取更新后的，否则就取本地的
                    if (localImg.size() >= images.size()) {
                        images = localImg;
                        folderWindow.bindFolder(folders);
                    }
                }
                if (adapter != null) {
                    if (images == null) {
                        images = new ArrayList<>();
                    }
                    adapter.bindImagesData(images);
                    tv_empty.setVisibility(images.size() > 0
                            ? View.INVISIBLE : View.VISIBLE);
                }
                dismissLoadingDialog();
            }
        });
    }

    /**
     * 生成uri
     *
     * @param cameraFile cameraFile
     * @return Uri
     */
    private Uri parUri(File cameraFile) {
        Uri imageUri;
        String authority = getPackageName() + ".provider";
        Log.d(TAG, "authority: " + authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            imageUri = FileProvider.getUriForFile(getMContext(), authority, cameraFile);
        } else {
            imageUri = Uri.fromFile(cameraFile);
        }
        return imageUri;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.picture_left_back || id == R.id.picture_right) {
            if (folderWindow.isShowing()) {
                folderWindow.dismiss();
            } else {
                closeActivity();
            }
        }
        if (id == R.id.picture_title) {
            if (folderWindow.isShowing()) {
                folderWindow.dismiss();
            } else {
                if (images != null && images.size() > 0) {
                    folderWindow.showAsDropDown(rl_picture_title);
                    List<MediaEntity> selectedImages = adapter.getSelectedImages();
                    folderWindow.notifyDataCheckedStatus(selectedImages);
                }
            }
        }

        if (id == R.id.picture_id_preview) {
            List<MediaEntity> selectedImages = adapter.getSelectedImages();

            List<MediaEntity> mediaEntities = new ArrayList<>();
            for (MediaEntity mediaEntity : selectedImages) {
                mediaEntities.add(mediaEntity);
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(PhoenixConstant.KEY_LIST, (Serializable) mediaEntities);
            bundle.putSerializable(PhoenixConstant.KEY_SELECT_LIST, (Serializable) selectedImages);
            bundle.putBoolean(PhoenixConstant.EXTRA_BOTTOM_PREVIEW, true);
            startActivity(PreviewActivity.class, bundle);
            overridePendingTransition(R.anim.phoenix_activity_in, 0);
        }

        if (id == R.id.pick_ll_ok) {
            List<MediaEntity> images = adapter.getSelectedImages();
            String pictureType = images.size() > 0 ? images.get(0).getMimeType() : "";
            int size = images.size();
            boolean eqImg = !TextUtils.isEmpty(pictureType) && pictureType.startsWith(PhoenixConstant.IMAGE);

            // 如果设置了图片最小选择数量，则判断是否满足条件
            if (getMinSelectNum() > 0 && getSelectionMode() == PhoenixConstant.MULTIPLE) {
                if (size < getMinSelectNum()) {
                    @SuppressLint("StringFormatMatches") String str = eqImg ? getString(R.string.picture_min_img_num, getMinSelectNum())
                            : getString(R.string.phoenix_message_min_number, getMinSelectNum());
                    showToast(str);
                    return;
                }
            }
            processMedia(images);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        processMedia(images);
    }

    //  通过 Handler 更新 UI 上的组件状态
    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mediaPlayer != null) {
                    tv_musicTime.setText(DateUtils.INSTANCE.timeParse(mediaPlayer.getCurrentPosition()));
                    musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                    musicSeekBar.setMax(mediaPlayer.getDuration());
                    tv_musicTotal.setText(DateUtils.INSTANCE.timeParse(mediaPlayer.getDuration()));
                    handler.postDelayed(runnable, 200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 初始化音频播放组件
     *
     * @param path path
     */
    private void initPlayer(String path) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            playAudio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放音频
     */
    private void playAudio() {
        if (mediaPlayer != null) {
            musicSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            musicSeekBar.setMax(mediaPlayer.getDuration());
        }
        String ppStr = tv_PlayPause.getText().toString();
        if (ppStr.equals(getString(R.string.picture_play_audio))) {
            tv_PlayPause.setText(getString(R.string.picture_pause_audio));
            tv_musicStatus.setText(getString(R.string.picture_play_audio));
            playOrPause();
        } else {
            tv_PlayPause.setText(getString(R.string.picture_play_audio));
            tv_musicStatus.setText(getString(R.string.picture_pause_audio));
            playOrPause();
        }
        if (isPlayAudio == false) {
            handler.post(runnable);
            isPlayAudio = true;
        }
    }

    /**
     * 停止播放
     *
     * @param path path
     */
    public void stop(String path) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 暂停播放
     */
    public void playOrPause() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(String folderName, List<MediaEntity> images) {
        picture_title.setText(folderName);
        adapter.bindImagesData(images);
        folderWindow.dismiss();
    }

    @Override
    public void onTakePhoto() {
        // 启动相机拍照,先判断手机是否有拍照权限
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    startCamera();
                } else {
                    showToast(getString(R.string.picture_camera));
                    if (getEnableCamera()) {
                        closeActivity();
                    }
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

    @Override
    public void onChange(List<MediaEntity> selectImages) {
        changeImageNumber(selectImages);
    }

    @Override
    public void onPictureClick(MediaEntity mediaEntity, int position) {
        List<MediaEntity> images = adapter.getImages();
        startPreview(images, position);
    }

    /**
     * preview image and video
     *
     * @param previewImages previewImages
     * @param position      position
     */
    public void startPreview(List<MediaEntity> previewImages, int position) {
        MediaEntity mediaEntity = previewImages.get(position);
        String pictureType = mediaEntity.getMimeType();
        Bundle bundle = new Bundle();
        List<MediaEntity> result = new ArrayList<>();
        int mediaType = MimeType.getFileType(pictureType);
        DebugUtil.INSTANCE.i(TAG, "mediaType:" + mediaType);
        if (getSelectionMode() == PhoenixConstant.SINGLE) {
            if (getEnableCrop()) {
                setOriginalPath(mediaEntity.getLocalPath());
                boolean gif = MimeType.isGif(pictureType);
                if (gif) {
                    result.add(mediaEntity);
                    handlerResult(result);
                } else {
//                    cropPicture(mediaEntity);
                }
            } else {
                result.add(mediaEntity);
                handlerResult(result);
            }
        } else {
            List<MediaEntity> selectedImages = adapter.getSelectedImages();
            ImagesObservable.Companion.getInstance().saveLocalMedia(previewImages);
            bundle.putSerializable(PhoenixConstant.KEY_SELECT_LIST, (Serializable) selectedImages);
            bundle.putInt(PhoenixConstant.KEY_POSITION, position);
            startActivity(PreviewActivity.class, bundle);
            overridePendingTransition(R.anim.phoenix_activity_in, 0);
        }
    }

    /**
     * change image selector state
     *
     * @param selectImages
     */
    @SuppressLint("StringFormatMatches")
    public void changeImageNumber(List<MediaEntity> selectImages) {
        // 如果选择的视频没有预览功能
        String pictureType = selectImages.size() > 0
                ? selectImages.get(0).getMimeType() : "";
        if (getFileType() == MimeType.ofAudio()) {
            picture_id_preview.setVisibility(View.GONE);
        } else {
            boolean isVideo = MimeType.isVideo(pictureType);
            picture_id_preview.setVisibility(isVideo ? View.GONE : View.VISIBLE);
        }
        boolean enable = selectImages.size() != 0;
        if (enable) {
            id_ll_ok.setEnabled(true);
            picture_id_preview.setEnabled(true);
            picture_id_preview.setTextColor(preview_textColor);
            if (getNumComplete()) {
                picture_tv_ok.setText(getString
                        (R.string.picture_done_front_num, selectImages.size(), getMaxSelectNum()));
            } else {
                if (!anim) {
                    picture_tv_img_num.startAnimation(animation);
                }
                picture_tv_img_num.setVisibility(View.VISIBLE);
                picture_tv_img_num.setText(selectImages.size() + "");
                picture_tv_ok.setText(getString(R.string.picture_completed));
                anim = false;
            }
        } else {
            id_ll_ok.setEnabled(false);
            picture_id_preview.setEnabled(false);
            picture_id_preview.setTextColor(ContextCompat.getColor(getMContext(), R.color.color_gray_1));
            if (getNumComplete()) {
                picture_tv_ok.setText(getString(R.string.picture_done_front_num, 0, getMaxSelectNum()));
            } else {
                picture_tv_img_num.setVisibility(View.GONE);
                picture_tv_ok.setText(getString(R.string.picture_please_select));
            }
        }
    }

    /**
     * 手动添加拍照后的相片到图片列表，并设为选中
     *
     * @param mediaEntity mediaEntity
     */
    private void manualSaveFolder(MediaEntity mediaEntity) {
        try {
            createNewFolder(foldersList);
            MediaFolder folder = getImageFolder(mediaEntity.getLocalPath(), foldersList);
            MediaFolder cameraFolder = foldersList.size() > 0 ? foldersList.get(0) : null;
            if (cameraFolder != null && folder != null) {
                // 相机胶卷
                cameraFolder.setFirstImagePath(mediaEntity.getLocalPath());
                cameraFolder.setImages(images);
                cameraFolder.setImageNumber(cameraFolder.getImageNumber() + 1);
                // 拍照相册
                int num = folder.getImageNumber() + 1;
                folder.setImageNumber(num);
                folder.getImages().add(0, mediaEntity);
                folder.setFirstImagePath(getCameraPath());
                folderWindow.bindFolder(foldersList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (RxBus.Companion.getDefault().isRegistered(this)) {
            RxBus.Companion.getDefault().unregister(this);
        }
        ImagesObservable.Companion.getInstance().clearLocalMedia();
        if (animation != null) {
            animation.cancel();
            animation = null;
        }
        if (mediaPlayer != null && handler != null) {
            handler.removeCallbacks(runnable);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void startCamera() {

    }
}
