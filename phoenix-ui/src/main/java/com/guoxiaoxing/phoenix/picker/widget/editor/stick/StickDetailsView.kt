package com.guoxiaoxing.phoenix.picture.edit.widget.stick

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.model.InputStickModel

class StickDetailsView(ctx: Context) : FrameLayout(ctx) {
    private val stickerView: RecyclerView
    var onStickerClickListener: OnStickerClickResult? = null

    init {
        LayoutInflater.from(ctx).inflate(R.layout.item_edit_sticker_details, this, true)
        stickerView = findViewById(R.id.rvSticker) as RecyclerView
        val layoutManager = GridLayoutManager(ctx, 8)
        stickerView.adapter = SimpleStickerAdapter(Sticker.Emoji)
        stickerView.layoutManager = layoutManager
    }

    inner class SimpleStickerAdapter(val sticker: Sticker) : RecyclerView.Adapter<SimpleStickerAdapter.SimpleStickerHolder>() {
        val stickerResource: IntArray = StickerUtils.getStickers(sticker)!!

        override fun onBindViewHolder(holder: SimpleStickerHolder, position: Int) {
            holder.iv.setImageResource(stickerResource[position])
            holder.iv.setOnClickListener {
                onStickerClickListener?.onResult(InputStickModel(sticker, position))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleStickerHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_edit_sticker, parent, false)
            return SimpleStickerHolder(view)
        }

        override fun getItemCount() = stickerResource.size

        inner class SimpleStickerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val iv = itemView.findViewById(R.id.ivSimpleSticker) as ImageView
        }
    }

    interface OnStickerClickResult {
        fun onResult(stickModel: InputStickModel)
    }

}