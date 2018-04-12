package com.guoxiaoxing.phoenix.picker.ui.picker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.ui.camera.CameraActivity
import com.guoxiaoxing.phoenix.core.PhoenixOption.THEME_DEFAULT
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
import kotlinx.android.synthetic.main.include_title_bar.*
import java.io.Serializable
import java.util.*

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

    private lateinit var pickAdapter: PickerAdapter
    private var allMediaList: MutableList<MediaEntity> = ArrayList()
    private var allFolderList: MutableList<MediaFolder> = ArrayList()

    private var isAnimation = false
    private lateinit var folderWindow: FolderPopWindow
    private var animation: Animation? = null
    private lateinit var rxPermissions: RxPermissions
    private lateinit var mediaLoader: MediaLoader

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBus(obj: EventEntity) {
        when (obj.what) {
        //receive the select result from CameraPreviewActivity
            PhoenixConstant.FLAG_PREVIEW_UPDATE_SELECT -> {
                val selectImages = obj.mediaEntities
                isAnimation = selectImages.size > 0
                val position = obj.position
                DebugUtil.i(TAG, "刷新下标::" + position)
                pickAdapter.setPickMediaList(selectImages)
                //通知点击项发生了改变
                val isExceedMax = selectImages.size >= maxSelectNum && maxSelectNum != 0
                pickAdapter.isExceedMax = isExceedMax
                if (isExceedMax || selectImages.size == maxSelectNum - 1) {
                    pickAdapter.notifyDataSetChanged()
                } else {
                    pickAdapter.notifyItemChanged(position)
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
        ToolbarUtil.setColorNoTranslucent(this, themeColor)
        LightStatusBarUtils.setLightStatusBar(this, false)
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
        pickRlTitle.setBackgroundColor(themeColor)

        if (themeColor == THEME_DEFAULT) {
            rl_bottom.setBackgroundColor(themeColor)

        } else {
            rl_bottom.setBackgroundColor(Color.WHITE)
            pickTvPreview.setTextColor(themeColor)
            pickLlOk.background = tintDrawable(R.drawable.phoenix_shape_complete_background, themeColor)
        }

        isNumberComplete()
        pickTvTitle.text = if (fileType == MimeType.ofAudio()) getString(R.string.picture_all_audio) else getString(R.string.picture_camera_roll)
        pick_tv_empty.text = if (fileType == MimeType.ofAudio()) getString(R.string.picture_audio_empty) else getString(R.string.picture_empty)
        StringUtils.tempTextFont(pick_tv_empty, fileType)

        val titleText = pickTvTitle.getText().toString().trim { it <= ' ' }
        if (enableCamera) {
            enableCamera = StringUtils.isCamera(titleText)
        }

        folderWindow = FolderPopWindow(this, fileType)
        folderWindow.setPictureTitleView(pickTvTitle)
        folderWindow.setOnItemClickListener(this)

        pickTvPreview.setOnClickListener(this)
        pickTvBack.setOnClickListener(this)
        pickTvCancel.setOnClickListener(this)
        pickLlOk.setOnClickListener(this)
        pickTvTitle.setOnClickListener(this)
    }

    private fun setupData() {
        pickRecyclerView.setHasFixedSize(true)
        pickRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount,
                ScreenUtil.dip2px(this, 2f), false))
        pickRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
        (pickRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        pickAdapter = PickerAdapter(mContext, option)
        pickRecyclerView.adapter = pickAdapter
        pickAdapter.setOnPickChangedListener(this)
        pickAdapter.setPickMediaList(mediaList)
        changeImageNumber(mediaList)

        mediaLoader = MediaLoader(this, fileType, isGif, videoFilterTime.toLong(), mediaFilterSize)
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
    private fun isNumberComplete() {
        pickTvOk.text = getString(R.string.picture_please_select)
        animation = AnimationUtils.loadAnimation(this, R.anim.phoenix_window_in)
    }

    /**
     * get MediaEntity s
     */
    private fun readLocalMedia() {
        mediaLoader.loadAllMedia(object : MediaLoader.LocalMediaLoadListener {
            override fun loadComplete(folders: MutableList<MediaFolder>) {
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
                if (pickAdapter != null) {
                    if (allMediaList == null) {
                        allMediaList = ArrayList()
                    }
                    pickAdapter.setAllMediaList(allMediaList)
                    pick_tv_empty.visibility = if (allMediaList.size > 0) View.INVISIBLE else View.VISIBLE
                }
                dismissLoadingDialog()
            }
        })
    }

    @SuppressLint("StringFormatMatches")
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.pickTvBack || id == R.id.pickTvCancel) {
            if (folderWindow.isShowing) {
                folderWindow.dismiss()
            } else {
                closeActivity()
            }
        }
        if (id == R.id.pickTvTitle) {
            if (folderWindow.isShowing()) {
                folderWindow.dismiss()
            } else {
                if (allMediaList.size > 0) {
                    folderWindow.showAsDropDown(pickRlTitle)
                    val selectedImages = pickAdapter.getPickMediaList()
                    folderWindow.notifyDataCheckedStatus(selectedImages)
                }
            }
        }

        if (id == R.id.pickTvPreview) {
            val selectedImages = pickAdapter.getPickMediaList()

            val bundle = Bundle()
            bundle.putParcelable(PhoenixConstant.PHOENIX_OPTION, option)
            bundle.putSerializable(PhoenixConstant.KEY_ALL_LIST, selectedImages as Serializable)
            bundle.putSerializable(PhoenixConstant.KEY_PICK_LIST, selectedImages as Serializable)
            bundle.putBoolean(PhoenixConstant.EXTRA_BOTTOM_PREVIEW, true)
            bundle.putInt(PhoenixConstant.KEY_PREVIEW_TYPE, PhoenixConstant.TYPE_PREIVEW_FROM_PICK)
            startActivity(PreviewActivity::class.java, bundle)
            overridePendingTransition(R.anim.phoenix_activity_in, 0)
        }

        if (id == R.id.pickLlOk) {
            val images = pickAdapter.getPickMediaList()
            val pictureType = if (images.size > 0) images[0].mimeType else ""
            val size = images.size
            val eqImg = !TextUtils.isEmpty(pictureType) && pictureType.startsWith(PhoenixConstant.IMAGE)

            // 如果设置了图片最小选择数量，则判断是否满足条件
            if (minSelectNum > 0) {
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
        //processMedia(allMediaList)   //我觉得这行代码没啥用

        if (Activity.RESULT_OK != resultCode) return

        when (requestCode) {
            PhoenixConstant.REQUEST_CODE_CAPTURE -> {
                onResult(data!!.getSerializableExtra(PhoenixConstant.PHOENIX_RESULT) as MutableList<MediaEntity>)
            }
            else -> {
            }
        }
    }

    override fun onItemClick(folderName: String, images: MutableList<MediaEntity>) {
        pickTvTitle.text = folderName
        pickAdapter.setAllMediaList(images)
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
        val images = pickAdapter.getAllMediaList()
        startPreview(images, position)
    }

    /**
     * preview image and video

     * @param previewImages previewImages
     * *
     * @param position      position
     */
    private fun startPreview(previewImages: MutableList<MediaEntity>, position: Int) {
        val mediaEntity = previewImages[position]
        val pictureType = mediaEntity.mimeType
        val bundle = Bundle()
        val mediaType = MimeType.getFileType(pictureType)
        DebugUtil.i(TAG, "mediaType:" + mediaType)
        val selectedImages = pickAdapter.getPickMediaList()
        ImagesObservable.instance.saveLocalMedia(previewImages)
        bundle.putParcelable(PhoenixConstant.PHOENIX_OPTION, option)
        bundle.putSerializable(PhoenixConstant.KEY_ALL_LIST, previewImages as Serializable)
        bundle.putSerializable(PhoenixConstant.KEY_PICK_LIST, selectedImages as Serializable)
        bundle.putInt(PhoenixConstant.KEY_POSITION, position)
        bundle.putInt(PhoenixConstant.KEY_PREVIEW_TYPE, PhoenixConstant.TYPE_PREIVEW_FROM_PICK)
        startActivity(PreviewActivity::class.java, bundle)
        overridePendingTransition(R.anim.phoenix_activity_in, 0)
    }

    /**
     * change image selector state

     * @param selectImages
     */
    @SuppressLint("StringFormatMatches")
    private fun changeImageNumber(selectImages: List<MediaEntity>) {
        val enable = selectImages.isNotEmpty()
        if (enable) {
            pickLlOk.isEnabled = true
            pickLlOk.alpha = 1F
            pickTvPreview.isEnabled = true
            pickTvPreview.setTextColor(if (themeColor == THEME_DEFAULT) ContextCompat.getColor(mContext, R.color.green) else themeColor)
            if (!isAnimation) {
                pickTvNumber.startAnimation(animation)
            }
            pickTvNumber.visibility = View.VISIBLE
            pickTvNumber.text = String.format("(%d)", selectImages.size)
            pickTvOk.text = getString(R.string.picture_completed)
            isAnimation = false
        } else {
            pickLlOk.isEnabled = false
            pickLlOk.alpha = 0.7F
            pickTvPreview.isEnabled = false
            pickTvPreview.setTextColor(ContextCompat.getColor(mContext, R.color.color_gray_1))
            pickTvNumber.visibility = View.GONE
            pickTvOk.text = getString(R.string.picture_please_select)
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
        animation?.cancel()
    }

    private fun startCamera() {
        val bundle = Bundle()
        bundle.putParcelable(PhoenixConstant.PHOENIX_OPTION, option)
        startActivity(CameraActivity::class.java, bundle, PhoenixConstant.REQUEST_CODE_CAPTURE)
        overridePendingTransition(R.anim.phoenix_activity_in, 0)
    }
}