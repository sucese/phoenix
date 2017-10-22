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
                            .pickedMediaList(mPickList)
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
                    .theme(PhoenixOption.THEME_DEFAULT)// 主题
                    .fileType(MimeType.ofAll())//显示的文件类型图片、视频、图片和视频
                    .maxPickNumber(10)// 最大选择数量
                    .minPickNumber(0)// 最小选择数量
                    .spanCount(4)// 每行显示个数
                    .pickMode(PhoenixConstant.MULTIPLE)// 多选/单选
                    .enablePreview(true)// 是否开启预览
                    .enableCamera(true)// 是否开启拍照
                    .enableAnimation(true)// 选择界面图片点击效果
                    .enableCompress(true)// 是否开启压缩
                    .thumbnailHeight(160)// 选择界面图片高度
                    .thumbnailWidth(160)// 选择界面图片宽度
                    .enableClickSound(true)//ƒ 是否开启点击声音
                    .pickedMediaList(pickList)// 已选图片数据
                    .videoSecond(0)//显示多少秒以内的视频
                    .onPickerListener(new OnPickerListener() {
                        @Override
                        public void onPickSuccess(List<MediaEntity> pickList) {
                            adapter.setList(pickList);
                            adapter.notifyDataSetChanged();
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
