package com.guoxiaoxing.phoenix.picker.ui.picker

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.PhoenixOption
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import com.guoxiaoxing.phoenix.picker.Phoenix
import com.guoxiaoxing.phoenix.picker.listener.OnPictureEditListener
import com.guoxiaoxing.phoenix.picker.model.EventEntity
import com.guoxiaoxing.phoenix.picker.rx.bus.RxBus
import com.guoxiaoxing.phoenix.picker.rx.bus.Subscribe
import com.guoxiaoxing.phoenix.picker.rx.bus.ThreadMode
import com.guoxiaoxing.phoenix.picker.ui.BaseFragment
import com.guoxiaoxing.phoenix.picker.ui.editor.PictureEditFragment
import com.guoxiaoxing.phoenix.picker.util.LightStatusBarUtils
import com.guoxiaoxing.phoenix.picker.util.ScreenUtil
import com.guoxiaoxing.phoenix.picker.util.ToolbarUtil
import com.guoxiaoxing.phoenix.picker.util.VoiceUtils
import com.guoxiaoxing.phoenix.picker.widget.photoview.PhotoView
import com.guoxiaoxing.phoenix.picker.widget.videoview.PhoenixVideoView
import kotlinx.android.synthetic.main.fragment_preview.*
import java.util.*

class PreviewFragment : BaseFragment(), View.OnClickListener, Animation.AnimationListener, OnPictureEditListener {

    private var position: Int = 0
    private var allMediaList: MutableList<MediaEntity> = ArrayList()
    private var pickMediaList: MutableList<MediaEntity> = ArrayList()
    private lateinit var adapter: SimpleFragmentAdapter
    private var refresh: Boolean = false
    private var index: Int = 0
    private var screenWidth: Int = 0
    private var previewType: Int = 0
    private var animation: Animation? = null

    //EventBus 3.0 回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBus(obj: EventEntity) {
        when (obj.what) {
            PhoenixConstant.CLOSE_PREVIEW_FLAG -> {
                // 压缩完后关闭预览界面
                activity.finish()
                activity.overridePendingTransition(0, R.anim.phoenix_activity_out)
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("StringFormatMatches")
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_preview, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupData()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (RxBus.default.isRegistered(this)) {
            RxBus.default.unregister(this)
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun setupView() {
        if (!RxBus.default.isRegistered(this)) {
            RxBus.default.register(this)
        }
        screenWidth = ScreenUtil.getScreenWidth(activity)

        ToolbarUtil.setColorNoTranslucent(activity, themeColor)
        LightStatusBarUtils.setLightStatusBar(activity, false)

        preview_rl_title.setBackgroundColor(themeColor)

        if (themeColor == PhoenixOption.THEME_DEFAULT) {
            preview_rl_bottom.setBackgroundColor(themeColor)

        } else {
            preview_tv_edit.setTextColor(themeColor)
            preview_rl_bottom.setBackgroundColor(Color.WHITE)
            preview_ll_ok.background = tintDrawable(R.drawable.phoenix_shape_complete_background, themeColor)
        }

        preview_tv_ok_text.text = getString(R.string.picture_please_select)
        animation = AnimationUtils.loadAnimation(mContext, R.anim.phoenix_window_in)

        ll_check.setOnClickListener(this)
        pickTvBack.setOnClickListener(this)
        preview_ll_ok.setOnClickListener(this)
        preview_ll_edit.setOnClickListener(this)
    }

    private fun setupData() {
        position = arguments.getInt(PhoenixConstant.KEY_POSITION, 0)
        pickMediaList = arguments.getParcelableArrayList<MediaEntity>(PhoenixConstant.KEY_PICK_LIST)
        allMediaList = arguments.getParcelableArrayList<MediaEntity>(PhoenixConstant.KEY_ALL_LIST)
        previewType = arguments.getInt(PhoenixConstant.KEY_PREVIEW_TYPE)

        pickTvTitle.text = (position + 1).toString() + "/" + allMediaList.size

        onPickNumberChange(false)
        onImageChecked(position)
        if (allMediaList.isNotEmpty()) {
            val mediaEntity = allMediaList[position]
            index = mediaEntity.getPosition()
        }

        adapter = SimpleFragmentAdapter()
        preview_pager.adapter = adapter
        preview_pager.currentItem = position

        val mediaEntity = allMediaList[preview_pager.currentItem]
        if (mediaEntity.fileType == MimeType.ofImage()) {
            ll_picture_edit.visibility = View.VISIBLE
        } else {
            ll_picture_edit.visibility = View.GONE
        }

        if(previewType == PhoenixConstant.TYPE_PREIVEW_FROM_PICK){
            ll_check.visibility = View.VISIBLE
            preview_ll_edit.visibility = View.VISIBLE
            preview_ll_ok.visibility = View.VISIBLE
        }else if(previewType == PhoenixConstant.TYPE_PREIVEW_FROM_PREVIEW){
            ll_check.visibility = View.GONE
            preview_ll_edit.visibility = View.GONE
            preview_ll_ok.visibility = View.GONE
        }else if(previewType == PhoenixConstant.TYPE_PREIVEW_FROM_CAMERA){
            ll_check.visibility = View.GONE
            preview_rl_bottom.visibility = View.GONE
            preview_ll_ok.visibility = View.GONE
        }

        preview_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                isPreviewEggs(previewEggs, position, positionOffsetPixels)
            }

            override fun onPageSelected(i: Int) {
                position = i
                pickTvTitle.text = (position + 1).toString() + "/" + allMediaList.size
                val mediaEntity = allMediaList[position]
                index = mediaEntity.getPosition()
                if (!previewEggs) {
                    onImageChecked(position)
                }
                if (mediaEntity.fileType == MimeType.ofImage()) {
                    ll_picture_edit.visibility = View.VISIBLE
                } else {
                    ll_picture_edit.visibility = View.GONE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    /**
     * 这里没实际意义，好处是预览图片时 滑动到屏幕一半以上可看到下一张图片是否选中了
     * @param previewEggs          是否显示预览友好体验
     * *
     * @param positionOffsetPixels 滑动偏移量
     */
    private fun isPreviewEggs(previewEggs: Boolean, position: Int, positionOffsetPixels: Int) {
        if (previewEggs) {
            if (allMediaList.size > 0) {
                val mediaEntity: MediaEntity
                val num: Int
                if (positionOffsetPixels < screenWidth / 2) {
                    mediaEntity = allMediaList[position]
                    tv_check.isSelected = isSelected(mediaEntity)
                } else {
                    mediaEntity = allMediaList[position + 1]
                    tv_check.isSelected = isSelected(mediaEntity)
                }
            }
        }
    }

    /**
     * 更新选择的顺序
     */
    private fun subSelectPosition() {
        run {
            var index = 0
            val len = pickMediaList.size
            while (index < len) {
                val mediaEntity = pickMediaList[index]
                mediaEntity.number = index + 1
                index++
            }
        }
    }

    /**
     * 判断当前图片是否选中
     * @param position
     */
    fun onImageChecked(position: Int) {
        if (allMediaList.isNotEmpty()) {
            val mediaEntity = allMediaList[position]
            tv_check.isSelected = isSelected(mediaEntity)
        } else {
            tv_check.isSelected = false
        }
    }

    /**
     * 当前图片是否选中
     * @param image
     * *
     * @return
     */
    fun isSelected(image: MediaEntity): Boolean {
        return pickMediaList.any { it.localPath == image.localPath }
    }

    /**
     * 更新图片选择数量
     */
    @SuppressLint("StringFormatMatches", "SetTextI18n")
    private fun onPickNumberChange(isRefresh: Boolean) {
        this.refresh = isRefresh
        val enable = pickMediaList.size > 0
        if (enable) {
            preview_ll_ok.isEnabled = true
            preview_ll_ok.alpha = 1F

            preview_tv_ok_number.startAnimation(animation)
            preview_tv_ok_number.visibility = View.VISIBLE
            preview_tv_ok_number.text = "(" + pickMediaList.size.toString() + ")"
            preview_tv_ok_text.text = getString(R.string.picture_completed)
        } else {
            preview_ll_ok.isEnabled = false
            preview_ll_ok.alpha = 0.7F
            preview_tv_ok_number.visibility = View.GONE
            preview_tv_ok_text.text = getString(R.string.picture_please_select)
        }
        updatePickerActivity(refresh)
    }

    /**
     * 更新图片列表选中效果
     * @param isRefresh isRefresh
     */
    private fun updatePickerActivity(isRefresh: Boolean) {
        if (isRefresh) {
            val obj = EventEntity(PhoenixConstant.FLAG_PREVIEW_UPDATE_SELECT, pickMediaList, index)
            RxBus.default.post(obj)
        }
    }

    override fun onAnimationStart(animation: Animation) {}

    override fun onAnimationEnd(animation: Animation) {
        updatePickerActivity(refresh)
    }

    override fun onAnimationRepeat(animation: Animation) {

    }

    override fun onEditSucess(editPath: String) {
        val mediaEntity = allMediaList[preview_pager.currentItem]
        mediaEntity.editPath = editPath
        for (pickMediaEntity in pickMediaList) {
            if (TextUtils.equals(mediaEntity.localPath, pickMediaEntity.localPath)) {
                pickMediaEntity.editPath = editPath
            }
        }
        adapter.notifyDataSetChanged()
        updatePickerActivity(true)
    }

    private inner class SimpleFragmentAdapter : PagerAdapter() {

        override fun getCount(): Int {
            return allMediaList.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val contentView = LayoutInflater.from(container.context).inflate(R.layout.adapter_preview, container, false)

            val preview_image = contentView.findViewById(R.id.preview_image) as PhotoView
            val preview_video = contentView.findViewById(R.id.preview_video) as PhoenixVideoView

            val mediaEntity = allMediaList[position]
            val mimeType = mediaEntity.mimeType
            val isVideo: Boolean
            if (TextUtils.isEmpty(mimeType)) {
                isVideo = mediaEntity.fileType == MimeType.ofVideo()
            } else {
                isVideo = mimeType.startsWith(PhoenixConstant.VIDEO)
            }

            val path = if (TextUtils.isEmpty(mediaEntity.finalPath))
                mediaEntity.localPath
            else
                mediaEntity.finalPath

            if (isVideo) {
                preview_video.visibility = View.VISIBLE
                preview_image.visibility = View.GONE

                preview_video.register(activity)
                preview_video.setVideoPath(path)
                preview_video.seekTo(100)

                preview_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {
                        if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                            preview_video.onPause()
                        }else{
                            preview_video.onResume()
                        }
                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    }

                    override fun onPageSelected(position: Int) {
                    }

                })
            } else {
                preview_video.visibility = View.GONE
                preview_image.visibility = View.VISIBLE
                Phoenix.config()
                        .imageLoader
                        .loadImage(context, preview_image, path, PhoenixConstant.IMAGE_PROCESS_TYPE_DEFAULT)
            }
            container.addView(contentView, 0)
            return contentView
        }
    }

    @SuppressLint("StringFormatMatches")
    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.pickTvBack) {
            activity.finish()
            activity.overridePendingTransition(0, R.anim.phoenix_activity_out)
        } else if (id == R.id.ll_check) {
            if (allMediaList.isNotEmpty()) {
                val image = allMediaList[preview_pager.currentItem]

                // 刷新图片列表中图片状态
                val isChecked = tv_check.isSelected
                if (pickMediaList.size >= maxSelectNum && !isChecked) {
                    showToast(getString(R.string.phoenix_message_max_number, maxSelectNum))
                    return
                }

                if (isChecked) {
                    tv_check.isSelected = false
                    for (mediaEntity in pickMediaList) {
                        if (mediaEntity.localPath == image.localPath) {
                            pickMediaList.remove(mediaEntity)
                            subSelectPosition()
                            break
                        }
                    }
                } else {
                    tv_check.isSelected = true
                    VoiceUtils.playVoice(mContext, openClickSound)
                    pickMediaList.add(image)
                    image.number = pickMediaList.size
                }
                onPickNumberChange(true)
            }
        } else if (id == R.id.preview_ll_ok) {
            val images = pickMediaList
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
            updatePickResult(images)
        } else if (id == R.id.preview_ll_edit) {
            val pictureEditFragment = PictureEditFragment.newInstance()
            val bundle = Bundle()
            bundle.putParcelable(PhoenixConstant.PHOENIX_OPTION, option)
            val path = allMediaList[preview_pager.currentItem].finalPath
            if (path != null) {
                bundle.putString(PhoenixConstant.KEY_FILE_PATH, path)
                pictureEditFragment.setArguments(bundle)
                pictureEditFragment.setTargetFragment(this, PhoenixConstant.REQUEST_CODE_PICTURE_EDIT)
                activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.preview_fragment_container, pictureEditFragment).addToBackStack(null).commitAllowingStateLoss();
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val editPath = data?.getStringExtra(PhoenixConstant.KEY_FILE_PATH)
        allMediaList.get(position).editPath = editPath

        pickMediaList
                .filter { TextUtils.equals(it.localPath, allMediaList.get(position).localPath) }
                .forEach { it.editPath = editPath }

        adapter.notifyDataSetChanged()
        updatePickerActivity(true)
    }

    fun updatePickResult(images: List<MediaEntity>) {
        RxBus.default.post(EventEntity(PhoenixConstant.FLAG_PREVIEW_COMPLETE, images))
        activity.finish()
        activity.overridePendingTransition(0, R.anim.phoenix_activity_out)
    }

    private val currentPath: String
        get() = allMediaList[preview_pager.currentItem].finalPath

    companion object {
        fun newInstance(): PreviewFragment {
            return PreviewFragment()
        }
    }
}
