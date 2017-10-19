package com.guoxiaoxing.phoenix.picker.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import com.guoxiaoxing.phoenix.picker.model.MediaFolder

import java.util.ArrayList

class PickerAlbumAdapter(private val mContext: Context) : RecyclerView.Adapter<PickerAlbumAdapter.ViewHolder>() {

    private var folders: List<MediaFolder> = ArrayList()
    private var mimeType: Int = 0

    fun bindFolderData(folders: List<MediaFolder>) {
        this.folders = folders
        notifyDataSetChanged()
    }

    fun setMimeType(mimeType: Int) {
        this.mimeType = mimeType
    }

    val folderData: List<MediaFolder>
        get() {
            if (folders == null) {
                folders = ArrayList<MediaFolder>()
            }
            return folders
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(mContext).inflate(R.layout.picture_album_folder_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = folders[position]
        val name = folder.name
        val imageNum = folder.imageNumber
        val imagePath = folder.firstImagePath
        val isChecked = folder.isChecked
        val checkedNum = folder.checkedNumber
        holder.tv_sign.visibility = if (checkedNum > 0) View.VISIBLE else View.INVISIBLE
        holder.itemView.isSelected = isChecked
        if (mimeType == MimeType.ofAudio()) {
            holder.first_image.setImageResource(R.drawable.phoenix_audio_placeholder)
        } else {
            val options = RequestOptions()
                    .placeholder(R.drawable.phoenix_placeholder)
                    .centerCrop()
                    .sizeMultiplier(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(160, 160)
            Glide.with(holder.itemView.context)
                    .asBitmap()
                    .load(imagePath)
                    .apply(options)
                    .into(object : BitmapImageViewTarget(holder.first_image) {
                        override fun setResource(resource: Bitmap?) {
                            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.resources, resource)
                            circularBitmapDrawable.cornerRadius = 8f
                            holder.first_image.setImageDrawable(circularBitmapDrawable)
                        }
                    })
        }
        holder.image_num.text = "($imageNum)"
        holder.tv_folder_name.text = name
        holder.itemView.setOnClickListener {
            if (onItemClickListener != null) {
                for (mediaFolder in folders) {
                    mediaFolder.isChecked = false
                }
                folder.isChecked = true
                notifyDataSetChanged()
                onItemClickListener!!.onItemClick(folder.name, folder.images)
            }
        }
    }

    override fun getItemCount(): Int {
        return folders!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var first_image: ImageView
        var tv_folder_name: TextView
        var image_num: TextView
        var tv_sign: TextView

        init {
            first_image = itemView.findViewById(R.id.first_image) as ImageView
            tv_folder_name = itemView.findViewById(R.id.tv_folder_name) as TextView
            image_num = itemView.findViewById(R.id.image_num) as TextView
            tv_sign = itemView.findViewById(R.id.tv_sign) as TextView
        }
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(folderName: String, images: List<MediaEntity>)
    }
}
