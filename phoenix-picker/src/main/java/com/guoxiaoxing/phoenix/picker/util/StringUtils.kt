package com.guoxiaoxing.phoenix.picker.util

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.widget.TextView

import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.model.MimeType

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import kotlin.experimental.and

object StringUtils {

    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    fun isCamera(title: String): Boolean {
        if (!TextUtils.isEmpty(title) && title.startsWith("相机胶卷")
                || title.startsWith("CameraRoll")
                || title.startsWith("所有音频")
                || title.startsWith("All audio")) {
            return true
        }

        return false
    }

    fun tempTextFont(tv: TextView, mimeType: Int) {
        val text = tv.text.toString().trim { it <= ' ' }
        val str = if (mimeType == MimeType.ofAudio())
            tv.context.getString(R.string.picture_empty_audio_title)
        else
            tv.context.getString(R.string.picture_empty_title)
        val sumText = str + text
        val placeSpan = SpannableString(sumText)
        placeSpan.setSpan(RelativeSizeSpan(0.8f), str.length, sumText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv.text = placeSpan
    }

    fun modifyTextViewDrawable(v: TextView, drawable: Drawable, index: Int) {
        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        //index 0:左 1：上 2：右 3：下
        if (index == 0) {
            v.setCompoundDrawables(drawable, null, null, null)
        } else if (index == 1) {
            v.setCompoundDrawables(null, drawable, null, null)
        } else if (index == 2) {
            v.setCompoundDrawables(null, null, drawable, null)
        } else {
            v.setCompoundDrawables(null, null, null, drawable)
        }
    }

    fun md5sum(file: File): String {
        val fis: InputStream
        val buffer = ByteArray(1024)
        var numRead: Int
        val md5: MessageDigest
        try {
            fis = FileInputStream(file)
            md5 = MessageDigest.getInstance("MD5")
            numRead = fis.read(buffer)
            while (numRead > 0) {
                md5.update(buffer, 0, numRead)
            }
            fis.close()
            return toHexString(md5.digest())
        } catch (e: Exception) {
            return System.currentTimeMillis().toString() + ""
        }

    }

    fun toHexString(b: ByteArray): String {
        val sb = StringBuilder(b.size * 2)
        for (c in b) {
            sb.append(HEX_DIGITS[(c.toInt() and 0xf0) ushr 4])
            sb.append(HEX_DIGITS[c.toInt() and 0x0f])
        }
        return sb.toString()
    }
}
