package com.guoxiaoxing.phoenix.picker.util

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

import com.guoxiaoxing.phoenix.R

object VoiceUtils {
    private var soundPool: SoundPool? = null
    private var soundID: Int = 0//创建某个声音对应的音频ID

    /**
     * start SoundPool
     */
    fun playVoice(mContext: Context, enableVoice: Boolean) {
        if (soundPool == null) {
            soundPool = SoundPool(1, AudioManager.STREAM_ALARM, 0)
            soundID = soundPool!!.load(mContext, R.raw.music, 1)
        }
        if (enableVoice) {
            soundPool!!.play(
                    soundID,
                    0.1f,
                    0.5f,
                    0,
                    1,
                    1f
            )
        }
    }

    /**
     * release SoundPool
     */
    fun release() {
        if (soundPool != null) {
            soundPool!!.stop(soundID)
        }
        soundPool = null
    }
}
