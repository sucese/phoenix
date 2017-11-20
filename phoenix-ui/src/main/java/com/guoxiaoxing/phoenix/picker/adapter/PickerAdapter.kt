package com.guoxiaoxing.phoenix.picker.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.Toast
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.PhoenixOption
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import com.guoxiaoxing.phoenix.picker.Phoenix
import com.guoxiaoxing.phoenix.picker.util.*
import kotlinx.android.synthetic.main.item_camera.view.*
import kotlinx.android.synthetic.main.item_grid_media.view.*
import java.util.*

class PickerAdapter(private val context: Context, private val config: PhoenixOption) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var enableCamera = false
    private var onPicktChangedListener: OnPickChangedListener? = null
    private val maxSelectNum: Int
    private val allMediaList: MutableList<MediaEntity> = ArrayList()
    private val pickMediaList: MutableList<MediaEntity> = ArrayList()
    private val enablePreview: Boolean
    private val is_checked_num: Boolean
    private val enableVoice: Boolean
    private val overrideWidth: Int
    private val overrideHeight: Int
    private val animation: Animation by lazy { AnimationLoader.loadAnimation(context, R.anim.phoenix_window_in) }
    private val mimeType: Int
    private val zoomAnim: Boolean
    var isExceedMax: Boolean = false

    init {
        this.enableCamera = config.isEnableCamera
        this.maxSelectNum = config.maxPickNumber
        this.enablePreview = config.isEnablePreview
        this.is_checked_num = config.isPickNumberMode
        this.overrideWidth = config.thumbnailWidth
        this.overrideHeight = config.thumbnailHeight
        this.enableVoice = config.isEnableClickSound
        this.mimeType = config.fileType
        this.zoomAnim = config.isEnableAnimation
    }

    fun setAllMediaList(medias: MutableList<MediaEntity>) {
        allMediaList.clear()
        allMediaList.addAll(medias)
        notifyDataSetChanged()
    }

    fun getAllMediaList(): MutableList<MediaEntity> {
        return allMediaList
    }

    fun setPickMediaList(medias: MutableList<MediaEntity>) {
        pickMediaList.clear()
        pickMediaList.addAll(medias)
        subSelectPosition()
        if (onPicktChangedListener != null) {
            onPicktChangedListener!!.onChange(pickMediaList)
        }
    }

    fun getPickMediaList(): MutableList<MediaEntity> {
        return pickMediaList
    }

    override fun getItemViewType(position: Int): Int {
        if (enableCamera && position == 0) {
            return PhoenixConstant.TYPE_CAMERA
        } else {
            return PhoenixConstant.TYPE_PICTURE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == PhoenixConstant.TYPE_CAMERA) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_camera, parent, false)
            return HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grid_media, parent, false)
            return ContentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == PhoenixConstant.TYPE_CAMERA) {
            val headerHolder = holder as HeaderViewHolder
            headerHolder.itemView.camera.setOnClickListener {
                if (onPicktChangedListener != null) {
                    onPicktChangedListener!!.onTakePhoto()
                }
            }
        } else {
            val contentHolder = holder as ContentViewHolder
            val image = allMediaList!![if (enableCamera) position - 1 else position]
            image.position = contentHolder.adapterPosition
            val path = image.finalPath
            val pictureType = image.mimeType
            if (is_checked_num) {
                notifyCheckChanged(contentHolder, image)
            }
            selectImage(contentHolder, isSelected(image), false)

            val picture = MimeType.getFileType(pictureType)
            val gif = MimeType.isGif(pictureType)
            contentHolder.itemView.tv_isGif.visibility = if (gif) View.VISIBLE else View.GONE
            if (mimeType == MimeType.ofAudio()) {
                contentHolder.itemView.tvDuration.visibility = View.VISIBLE
                val drawable = ContextCompat.getDrawable(context, R.drawable.phoenix_audio)
                StringUtils.modifyTextViewDrawable(contentHolder.itemView.tvDuration, drawable, 0)
            } else {
                val drawable = ContextCompat.getDrawable(context, R.drawable.phoenix_video_icon)
                StringUtils.modifyTextViewDrawable(contentHolder.itemView.tvDuration, drawable, 0)
                contentHolder.itemView.tvDuration.visibility = if (picture == PhoenixConstant.TYPE_VIDEO)
                    View.VISIBLE
                else
                    View.GONE
            }
            val width = image.width
            val height = image.height
            val h = width * 5
            contentHolder.itemView.tv_long_chart.visibility = if (height > h) View.VISIBLE else View.GONE
            val duration = image.duration
            contentHolder.itemView.tvDuration.text = DateUtils.timeParse(duration)
            if (mimeType == MimeType.ofAudio()) {
                contentHolder.itemView.iv_picture.setImageResource(R.drawable.phoenix_audio_placeholder)
            } else {
                Phoenix.config()
                        .imageLoader
                        .loadImage(context, contentHolder.itemView.iv_picture, path, PhoenixConstant.IMAGE_PROCESS_TYPE_DEFAULT)
            }
            if (enablePreview) {
                contentHolder.itemView.ll_check.setOnClickListener { changeCheckboxState(contentHolder, image) }

            }
            contentHolder.itemView.setOnClickListener {
                if (enablePreview) {
                    val index = if (enableCamera) position - 1 else position
                    onPicktChangedListener!!.onPictureClick(image, index)
                } else {
                    changeCheckboxState(contentHolder, image)
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return if (enableCamera) allMediaList!!.size + 1 else allMediaList!!.size
    }

    inner class HeaderViewHolder(headerView: View) : RecyclerView.ViewHolder(headerView)

    inner class ContentViewHolder(contentView: View) : RecyclerView.ViewHolder(contentView)

    fun isSelected(image: MediaEntity): Boolean {
        for (mediaEntity in pickMediaList!!) {
            if (TextUtils.isEmpty(mediaEntity.localPath) || TextUtils.isEmpty(image.localPath)) {
                return false
            }
            if (mediaEntity.localPath == image.localPath) {
                return true
            }
        }
        return false
    }

    /**
     * 选择按钮更新
     */
    private fun notifyCheckChanged(contentViewHolder: ContentViewHolder, imageBean: MediaEntity) {
        contentViewHolder.itemView.tv_check.text = ""
        for (mediaEntity in pickMediaList!!) {
            if (mediaEntity.localPath == imageBean.localPath) {
                imageBean.number = mediaEntity.number
                mediaEntity.setPosition(imageBean.getPosition())
                contentViewHolder.itemView.tv_check.text = imageBean.number.toString()
            }
        }
    }

    /**
     * 改变图片选中状态
     * @param contentHolderContent contentHolderContent
     * *
     * @param image         image
     */
    @SuppressLint("StringFormatMatches")
    private fun changeCheckboxState(contentHolderContent: ContentViewHolder, image: MediaEntity) {
        val isChecked = contentHolderContent.itemView.tv_check.isSelected
        if (isChecked) {
            for (mediaEntity in pickMediaList) {
                if (mediaEntity.localPath == image.localPath) {
                    pickMediaList.remove(mediaEntity)
                    DebugUtil.i("pickMediaList remove::", config.pickedMediaList.size.toString() + "")
                    subSelectPosition()
                    disZoom(contentHolderContent.itemView.iv_picture)
                    break
                }
            }
        } else {
            if (isExceedMax) {
                notifyDataSetChanged()
                Toast.makeText(context, context.getString(R.string.phoenix_message_max_number, maxSelectNum),
                        Toast.LENGTH_LONG).show()
                return
            }

            pickMediaList.add(image)
            DebugUtil.i("pickMediaList add::", config.pickedMediaList.size.toString() + "")
            image.number = pickMediaList!!.size
            VoiceUtils.playVoice(context, enableVoice)
            zoom(contentHolderContent.itemView.iv_picture)
        }

        //通知点击项发生了改变
        isExceedMax = pickMediaList.size >= maxSelectNum && maxSelectNum != 0
        if (isExceedMax || pickMediaList.size == maxSelectNum - 1) {
            notifyDataSetChanged()
        } else {
            notifyItemChanged(contentHolderContent.adapterPosition)
            selectImage(contentHolderContent, !isChecked, false)
        }
        if (onPicktChangedListener != null) {
            onPicktChangedListener!!.onChange(pickMediaList)
        }
    }

    /**
     * 更新选择的顺序
     */
    private fun subSelectPosition() {
        if (is_checked_num) {
            val size = pickMediaList.size
            var index = 0
            val length = size
            while (index < length) {
                val mediaEntity = pickMediaList[index]
                mediaEntity.number = index + 1
                notifyItemChanged(mediaEntity.position)
                index++
            }
        }
    }

    fun selectImage(contentViewHolder: ContentViewHolder, isChecked: Boolean, isAnim: Boolean) {
        contentViewHolder.itemView.tv_check.isSelected = isChecked
        if (isChecked) {
            if (isAnim) {
                contentViewHolder.itemView.tv_check.startAnimation(animation)
            }
            contentViewHolder.itemView.iv_picture.setColorFilter(ContextCompat.getColor(context, R.color.color_black_4), PorterDuff.Mode.SRC_ATOP)
        } else {
            if (isExceedMax) {
                contentViewHolder.itemView.iv_picture.setColorFilter(ContextCompat.getColor(context, R.color.phoenix_transparent_white), PorterDuff.Mode.SRC_ATOP)
            } else {
                contentViewHolder.itemView.iv_picture.setColorFilter(ContextCompat.getColor(context, R.color.color_black_5), PorterDuff.Mode.SRC_ATOP)
            }
        }
    }

    interface OnPickChangedListener {
        fun onTakePhoto()

        fun onChange(selectImages: List<MediaEntity>)

        fun onPictureClick(mediaEntity: MediaEntity, position: Int)
    }

    fun setOnPickChangedListener(onPickChangedListener: OnPickChangedListener) {
        this.onPicktChangedListener = onPickChangedListener
    }

    private fun zoom(iv_img: ImageView) {
        if (zoomAnim) {
            val set = AnimatorSet()
            set.playTogether(
                    ObjectAnimator.ofFloat(iv_img, "scaleX", 1f, 1.12f),
                    ObjectAnimator.ofFloat(iv_img, "scaleY", 1f, 1.12f)
            )
            set.duration = DURATION.toLong()
            set.start()
        }
    }

    private fun disZoom(iv_img: ImageView) {
        if (zoomAnim) {
            val set = AnimatorSet()
            set.playTogether(
                    ObjectAnimator.ofFloat(iv_img, "scaleX", 1.12f, 1f),
                    ObjectAnimator.ofFloat(iv_img, "scaleY", 1.12f, 1f)
            )
            set.duration = DURATION.toLong()
            set.start()
        }
    }

    companion object {
        private val DURATION = 450
    }
}
