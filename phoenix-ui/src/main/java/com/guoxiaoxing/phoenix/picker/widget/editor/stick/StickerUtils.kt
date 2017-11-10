package com.guoxiaoxing.phoenix.picture.edit.widget.stick

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.guoxiaoxing.phoenix.R
import java.util.*

object StickerUtils {

    private val mEmojiResource = intArrayOf(R.drawable.f14, R.drawable.phoenix_emoji_f1, R.drawable.f2,
            R.drawable.f3, R.drawable.f4, R.drawable.f5, R.drawable.phoenix_emoji_f6, R.drawable.phoenix_emoji_f7,
            R.drawable.phoenix_emoji_f8, R.drawable.phoenix_emoji_f9, R.drawable.phoenix_emoji_f10, R.drawable.f11, R.drawable.f12,
            R.drawable.f13, R.drawable.phoenix_emoji_f0, R.drawable.f15, R.drawable.f16, R.drawable.f96,
            R.drawable.f18, R.drawable.f19, R.drawable.f20, R.drawable.f21, R.drawable.f22,
            R.drawable.f23, R.drawable.f24, R.drawable.f25, R.drawable.f26, R.drawable.f27,
            R.drawable.f28, R.drawable.f29, R.drawable.f30, R.drawable.f31, R.drawable.f32,
            R.drawable.f33, R.drawable.f34, R.drawable.f35, R.drawable.f36, R.drawable.f37,
            R.drawable.f38, R.drawable.f39, R.drawable.phoenix_emoji_f97, R.drawable.phoenix_emoji_f98, R.drawable.phoenix_emoji_f99,
            R.drawable.phoenix_emoji_f100, R.drawable.f101, R.drawable.phoenix_emoji_f102, R.drawable.phoenix_emoji_f103, R.drawable.f104,
            R.drawable.f105, R.drawable.f106, R.drawable.phoenix_emoji_f107, R.drawable.f108, R.drawable.f109,
            R.drawable.f110, R.drawable.f111, R.drawable.f112, R.drawable.phoenix_emoji_f89, R.drawable.f113,
            R.drawable.f114, R.drawable.f115, R.drawable.phoenix_emoji_f60, R.drawable.f61, R.drawable.f46,
            R.drawable.phoenix_emoji_f63, R.drawable.phoenix_emoji_f64, R.drawable.f116, R.drawable.f66, R.drawable.phoenix_emoji_f67,
            R.drawable.f53, R.drawable.phoenix_emoji_f54, R.drawable.phoenix_emoji_f55, R.drawable.phoenix_emoji_f56, R.drawable.phoenix_emoji_f57,
            R.drawable.f117, R.drawable.phoenix_emoji_f59, R.drawable.phoenix_emoji_f75, R.drawable.phoenix_emoji_f74, R.drawable.phoenix_emoji_f69,
            R.drawable.phoenix_emoji_f49, R.drawable.phoenix_emoji_f76, R.drawable.phoenix_emoji_f77, R.drawable.phoenix_emoji_f78, R.drawable.phoenix_emoji_f79,
            R.drawable.f118, R.drawable.f119, R.drawable.f120, R.drawable.f121, R.drawable.f122,
            R.drawable.f123, R.drawable.f124)


    fun getByIndex(sticker: Sticker, index: Int): Int? {
        if (isIndexValidate(sticker, index)) {
            return mEmojiResource[index]
        }
        return null
    }

    private fun isIndexValidate(sticker: Sticker, index: Int): Boolean {
        if (sticker == Sticker.Emoji) {
            return index >= 0 && index < mEmojiResource.size
        }
        return false
    }

    fun getStickers(sticker: Sticker): IntArray? {
        if (sticker == Sticker.Emoji) {
            return Arrays.copyOf(mEmojiResource, mEmojiResource.size)
        }
        return null
    }

    fun getStickerBitmap(context: Context, sticker: Sticker, index: Int): Bitmap? {
        val resId = getByIndex(sticker, index)
        resId ?: return null
        if (sticker == Sticker.Emoji) {
            val drawable = getLocalDrawable(context, resId)
            drawable ?: return null
            return (drawable as BitmapDrawable).bitmap
        }
        return null
    }

    private fun getLocalDrawable(context: Context, id: Int): Drawable? {
        return context.resources.getDrawable(id)
    }
}