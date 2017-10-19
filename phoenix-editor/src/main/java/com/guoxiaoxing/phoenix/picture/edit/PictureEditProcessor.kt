package com.guoxiaoxing.phoenix.picture.edit

import android.content.Context
import android.content.Intent

import com.guoxiaoxing.phoenix.core.PhoenixOption
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.listener.OnProcessorListener
import com.guoxiaoxing.phoenix.core.listener.Processor
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picture.edit.ui.PictureEditActivity

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.

 * @author guoxiaoxing
 * *
 * @since 2017/10/12 下午2:12
 */
class PictureEditProcessor : Processor {

    override fun syncProcess(context: Context, mediaEntity: MediaEntity, phoenixOption: PhoenixOption): MediaEntity? {
        val intent = Intent(context, PictureEditActivity::class.java)
        intent.putExtra(PhoenixConstant.KEY_FILE_PATH, mediaEntity.localPath)
        context.startActivity(intent)
        return null
    }

    override fun asyncProcess(context: Context, mediaEntity: MediaEntity, phoenixOption: PhoenixOption, onProcessorListener: OnProcessorListener) {
        val intent = Intent(context, PictureEditActivity::class.java)
        context.startActivity(intent)
    }
}
