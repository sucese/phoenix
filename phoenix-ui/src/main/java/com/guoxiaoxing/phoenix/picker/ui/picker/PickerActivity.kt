package com.guoxiaoxing.phoenix.picker.ui.picker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import com.guoxiaoxing.phoenix.picker.adapter.PickerAdapter
import com.guoxiaoxing.phoenix.picker.adapter.PickerAlbumAdapter
import com.guoxiaoxing.phoenix.picker.model.EventEntity
import com.guoxiaoxing.phoenix.picker.model.MediaFolder
import com.guoxiaoxing.phoenix.picker.model.MediaLoader
import com.guoxiaoxing.phoenix.picker.rx.bus.ImagesObservable
import com.guoxiaoxing.phoenix.picker.rx.bus.RxBus
import com.guoxiaoxing.phoenix.picker.rx.bus.Subscribe
import com.guoxiaoxing.phoenix.picker.rx.bus.ThreadMode
import com.guoxiaoxing.phoenix.picker.rx.permission.RxPermissions
import com.guoxiaoxing.phoenix.picker.ui.BaseActivity
import com.guoxiaoxing.phoenix.picker.util.*
import com.guoxiaoxing.phoenix.picker.widget.FolderPopWindow
import com.guoxiaoxing.phoenix.picker.widget.GridSpacingItemDecoration
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_picker.*
import kotlinx.android.synthetic.main.picture_title_bar.*
import java.io.Serializable
import java.util.ArrayList

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/10/19 下午6:30
 */
class PickerActivity : BaseActivity(), View.OnClickListener, PickerAlbumAdapter.OnItemClickListener,
        PickerAdapter.OnPickChangedListener {

    private val TAG = PickerActivity::class.java.simpleName

    private lateinit var adapter: PickerAdapter
    private var allMediaList: MutableList<MediaEntity> = ArrayList()
    private var allFolderList: MutableList<MediaFolder> = ArrayList()

    private var isAnimation = false
    private var previewTextColor: Int = 0
    private lateinit var folderWindow: FolderPopWindow
    private lateinit var animation: Animation

    private lateinit var rxPermissions: RxPermissions
    private lateinit var mediaLoader: MediaLoader

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBus(obj: EventEntity) {
        when (obj.what) {
        //receive the select result from PreviewActivity
            PhoenixConstant.FLAG_PREVIEW_UPDATE_SELECT -> {
                val selectImages = obj.mediaEntities
                isAnimation = selectImages.size > 0
                val position = obj.position
                DebugUtil.i(TAG, "刷新下标::" + position)
                adapter.setPickMediaList(selectImages)
                //通知点击项发生了改变
                val isExceedMax = selectImages.size >= maxSelectNum && maxSelectNum != 0
                adapter.isExceedMax = isExceedMax
                if (isExceedMax || selectImages.size == maxSelectNum - 1) {
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.notifyItemChanged(position)
                }
            }
            PhoenixConstant.FLAG_PREVIEW_COMPLETE -> {
                val mediaEntities = obj.mediaEntities
                processMedia(mediaEntities)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LightStatusBarUtils.setLightStatusBar(this, statusFont)
        if (!RxBus.default.isRegistered(this)) {
            RxBus.default.register(this)
        }
        rxPermissions = RxPermissions(this)

        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(aBoolean: Boolean?) {
                        if (aBoolean!!) {
                            setContentView(R.layout.activity_picker)
                            setupView()
                            setupData()
                        } else {
                            showToast(getString(R.string.picture_jurisdiction))
                            closeActivity()
                        }
                    }

                    override fun onError(e: Throwable) {
                        showToast(getString(R.string.picture_jurisdiction))
                        closeActivity()
                    }

                    override fun onComplete() {}
                })
    }

    /**
     * init views
     */
    private fun setupView() {
        previewTextColor = AttrsUtils.getTypeValueColor(this, R.attr.phoenix_picker_preview_text_color)

        isNumberComplete(numComplete)

        rl_bottom.visibility = if (selectionMode == PhoenixConstant.SINGLE) View.GONE else View.VISIBLE
        if (fileType == MimeType.ofAudio()) {
            picture_id_preview.visibility = View.GONE
        } else {
            picture_id_preview.visibility = if (fileType == PhoenixConstant.TYPE_VIDEO) View.GONE else View.VISIBLE
        }

        picture_title.text = if (fileType == MimeType.ofAudio()) getString(R.string.picture_all_audio) else getString(R.string.picture_camera_roll)
        tv_empty.text = if (fileType == MimeType.ofAudio()) getString(R.string.picture_audio_empty) else getString(R.string.picture_empty)
        StringUtils.tempTextFont(tv_empty, fileType)

        val titleText = picture_title.getText().toString().trim { it <= ' ' }
        if (enableCamera) {
            enableCamera = StringUtils.isCamera(titleText)
        }

        folderWindow = FolderPopWindow(this, fileType)
        folderWindow.setPictureTitleView(picture_title)
        folderWindow.setOnItemClickListener(this)

        picture_id_preview.setOnClickListener(this)
        picture_left_back.setOnClickListener(this)
        picture_right.setOnClickListener(this)
        pick_ll_ok.setOnClickListener(this)
        picture_title.setOnClickListener(this)
    }

    private fun setupData(){
        picture_recycler.setHasFixedSize(true)
        picture_recycler.addItemDecoration(GridSpacingItemDecoration(spanCount,
                ScreenUtil.dip2px(this, 2f), false))
        picture_recycler.layoutManager = GridLayoutManager(this, spanCount)
        (picture_recycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        adapter = PickerAdapter(mContext, option)
        picture_recycler.adapter = adapter
        adapter.setOnPickChangedListener(this)
        adapter.setPickMediaList(mediaList)
        changeImageNumber(mediaList)

        mediaLoader = MediaLoader(this, fileType, isGif, videoSecond.toLong())
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(aBoolean: Boolean?) {
                        showLoadingDialog()
                        if (aBoolean!!) {
                            readLocalMedia()
                        } else {
                            showToast(getString(R.string.picture_jurisdiction))
                            dismissLoadingDialog()
                        }
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {}
                })
    }

    /**
     * none number style
     */
    @SuppressLint("StringFormatMatches")
    private fun isNumberComplete(numComplete: Boolean) {
        picture_tv_ok.text = if (numComplete)
            getString(R.string.picture_done_front_num, 0, maxSelectNum)
        else
            getString(R.string.picture_please_select)

        if (!numComplete) {
            animation = AnimationUtils.loadAnimation(this, R.anim.phoenix_window_in)
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.phoenix_window_in)
    }

    /**
     * get MediaEntity s
     */
    private fun readLocalMedia() {
        mediaLoader.loadAllMedia(object : MediaLoader.LocalMediaLoadListener {
            override fun loadComplete(folders: MutableList<MediaFolder>) {
                DebugUtil.i("loadComplete:" + folders.size)
                if (folders.size > 0) {
                    allFolderList = folders
                    val folder = folders[0]
                    folder.isChecked = true
                    val localImg = folder.images
                    // 这里解决有些机型会出现拍照完，相册列表不及时刷新问题
                    // 因为onActivityResult里手动添加拍照后的照片，
                    // 如果查询出来的图片大于或等于当前adapter集合的图片则取更新后的，否则就取本地的
                    if (localImg.size >= allMediaList.size) {
                        allMediaList = localImg
                        folderWindow.bindFolder(folders)
                    }
                }
                if (adapter != null) {
                    if (allMediaList == null) {
                        allMediaList = ArrayList<MediaEntity>()
                    }
                    adapter.setAllMediaList(allMediaList)
                    tv_empty.visibility = if (allMediaList.size > 0) View.INVISIBLE else View.VISIBLE
                }
                dismissLoadingDialog()
            }
        })
    }

    @SuppressLint("StringFormatMatches")
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.picture_left_back || id == R.id.picture_right) {
            if (folderWindow.isShowing()) {
                folderWindow.dismiss()
            } else {
                closeActivity()
            }
        }
        if (id == R.id.picture_title) {
            if (folderWindow.isShowing()) {
                folderWindow.dismiss()
            } else {
                if (allMediaList.size > 0) {
                    folderWindow.showAsDropDown(rl_picture_title)
                    val selectedImages = adapter.getPickMediaList()
                    folderWindow.notifyDataCheckedStatus(selectedImages)
                }
            }
        }

        if (id == R.id.picture_id_preview) {
            val selectedImages = adapter.getPickMediaList()

            val mediaEntities = ArrayList<MediaEntity>()
            for (mediaEntity in selectedImages) {
                mediaEntities.add(mediaEntity)
            }
            val bundle = Bundle()
            bundle.putSerializable(PhoenixConstant.KEY_LIST, mediaEntities as Serializable)
            bundle.putSerializable(PhoenixConstant.KEY_SELECT_LIST, selectedImages as Serializable)
            bundle.putBoolean(PhoenixConstant.EXTRA_BOTTOM_PREVIEW, true)
            startActivity(PreviewActivity::class.java, bundle)
            overridePendingTransition(R.anim.phoenix_activity_in, 0)
        }

        if (id == R.id.pick_ll_ok) {
            val images = adapter.getPickMediaList()
            val pictureType = if (images.size > 0) images[0].mimeType else ""
            val size = images.size
            val eqImg = !TextUtils.isEmpty(pictureType) && pictureType.startsWith(PhoenixConstant.IMAGE)

            // 如果设置了图片最小选择数量，则判断是否满足条件
            if (minSelectNum > 0 && selectionMode == PhoenixConstant.MULTIPLE) {
                if (size < minSelectNum) {
                    @SuppressLint("StringFormatMatches") val str = if (eqImg)
                        getString(R.string.picture_min_img_num, minSelectNum)
                    else
                        getString(R.string.phoenix_message_min_number, minSelectNum)
                    showToast(str)
                    return
                }
            }
            processMedia(images)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        processMedia(allMediaList)

    }

    override fun onItemClick(folderName: String, images: MutableList<MediaEntity>) {
        picture_title.text = folderName
        adapter.setAllMediaList(images)
        folderWindow.dismiss()
    }

    override fun onTakePhoto() {
        // 启动相机拍照,先判断手机是否有拍照权限
        rxPermissions.request(Manifest.permission.CAMERA).subscribe(object : Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(aBoolean: Boolean?) {
                if (aBoolean!!) {
                    startCamera()
                } else {
                    showToast(getString(R.string.picture_camera))
                    if (enableCamera) {
                        closeActivity()
                    }
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })
    }

    override fun onChange(selectImages: List<MediaEntity>) {
        changeImageNumber(selectImages)
    }

    override fun onPictureClick(mediaEntity: MediaEntity, position: Int) {
        val images = adapter.getAllMediaList()
        startPreview(images, position)
    }

    /**
     * preview image and video

     * @param previewImages previewImages
     * *
     * @param position      position
     */
    fun startPreview(previewImages: MutableList<MediaEntity>, position: Int) {
        val mediaEntity = previewImages[position]
        val pictureType = mediaEntity.mimeType
        val bundle = Bundle()
        val result = ArrayList<MediaEntity>()
        val mediaType = MimeType.getFileType(pictureType)
        DebugUtil.i(TAG, "mediaType:" + mediaType)
        if (selectionMode == PhoenixConstant.SINGLE) {
            if (enableCrop) {
                originalPath = mediaEntity.localPath
                val gif = MimeType.isGif(pictureType)
                if (gif) {
                    result.add(mediaEntity)
                    handlerResult(result)
                } else {
                    //                    cropPicture(mediaEntity);
                }
            } else {
                result.add(mediaEntity)
                handlerResult(result)
            }
        } else {
            val selectedImages = adapter.getPickMediaList()
            ImagesObservable.instance.saveLocalMedia(previewImages)
            bundle.putSerializable(PhoenixConstant.KEY_SELECT_LIST, selectedImages as Serializable)
            bundle.putInt(PhoenixConstant.KEY_POSITION, position)
            startActivity(PreviewActivity::class.java, bundle)
            overridePendingTransition(R.anim.phoenix_activity_in, 0)
        }
    }

    /**
     * change image selector state

     * @param selectImages
     */
    @SuppressLint("StringFormatMatches")
    fun changeImageNumber(selectImages: List<MediaEntity>) {
        // 如果选择的视频没有预览功能
        val pictureType = if (selectImages.isNotEmpty())
            selectImages[0].mimeType
        else
            ""
        if (fileType == MimeType.ofAudio()) {
            picture_id_preview.setVisibility(View.GONE)
        } else {
            val isVideo = MimeType.isVideo(pictureType)
            picture_id_preview.setVisibility(if (isVideo) View.GONE else View.VISIBLE)
        }
        val enable = selectImages.isNotEmpty()
        if (enable) {
            pick_ll_ok.setEnabled(true)
            picture_id_preview.setEnabled(true)
            picture_id_preview.setTextColor(previewTextColor)
            if (numComplete) {
                picture_tv_ok.setText(getString(R.string.picture_done_front_num, selectImages.size, maxSelectNum))
            } else {
                if (!isAnimation) {
                    pick_tv_picture_number.startAnimation(animation)
                }
                pick_tv_picture_number.visibility = View.VISIBLE
                pick_tv_picture_number.text = selectImages.size.toString() + ""
                picture_tv_ok.text = getString(R.string.picture_completed)
                isAnimation = false
            }
        } else {
            pick_ll_ok.setEnabled(false)
            picture_id_preview.setEnabled(false)
            picture_id_preview.setTextColor(ContextCompat.getColor(mContext, R.color.color_gray_1))
            if (numComplete) {
                picture_tv_ok.setText(getString(R.string.picture_done_front_num, 0, maxSelectNum))
            } else {
                pick_tv_picture_number.setVisibility(View.GONE)
                picture_tv_ok.setText(getString(R.string.picture_please_select))
            }
        }
    }

    /**
     * 手动添加拍照后的相片到图片列表，并设为选中

     * @param mediaEntity mediaEntity
     */
    private fun manualSaveFolder(mediaEntity: MediaEntity) {
        try {
            createNewFolder(allFolderList)
            val folder = getImageFolder(mediaEntity.localPath, allFolderList)
            val cameraFolder = if (allFolderList.size > 0) allFolderList[0] else null
            if (cameraFolder != null && folder != null) {
                // 相机胶卷
                cameraFolder.firstImagePath = mediaEntity.localPath
                cameraFolder.images = allMediaList
                cameraFolder.imageNumber = cameraFolder.imageNumber + 1
                // 拍照相册
                val num = folder.imageNumber + 1
                folder.imageNumber = num
                folder.images.add(0, mediaEntity)
                folder.firstImagePath = cameraPath
                folderWindow.bindFolder(allFolderList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        closeActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (RxBus.default.isRegistered(this)) {
            RxBus.default.unregister(this)
        }
        ImagesObservable.instance.clearLocalMedia()
        animation.cancel()
    }

    private fun startCamera() {

    }
}