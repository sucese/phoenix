package com.guoxiaoxing.phoenix.picker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.PhoenixOption
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picker.rx.bus.ImagesObservable
import com.guoxiaoxing.phoenix.picker.ui.picker.PreviewActivity
import com.guoxiaoxing.phoenix.picker.util.DoubleUtils
import java.io.Serializable

/**
 * centralized view navigator.
 *
 * @author RobinVangYang
 * @since 2018-04-12 22:19.
 */
class Navigator {
    companion object {

        /**
         * preview media files, for now, only support images or videos.
         */
        fun showPreviewView(activity: Activity, option: PhoenixOption,
                            previewMediaList: MutableList<MediaEntity>, pickedMediaList: MutableList<MediaEntity>,
                            currentPosition: Int) {
            if (DoubleUtils.isFastDoubleClick) return

            ImagesObservable.instance.savePreviewMediaList(previewMediaList)
            val bundle = Bundle()
            bundle.putParcelable(PhoenixConstant.PHOENIX_OPTION, option)
            bundle.putSerializable(PhoenixConstant.KEY_PICK_LIST, pickedMediaList as Serializable)
            bundle.putInt(PhoenixConstant.KEY_POSITION, currentPosition)
            bundle.putInt(PhoenixConstant.KEY_PREVIEW_TYPE, PhoenixConstant.TYPE_PREIVEW_FROM_PICK)

            activity.startActivity(Intent(activity, PreviewActivity::class.java).putExtras(bundle))
            activity.overridePendingTransition(R.anim.phoenix_activity_in, 0)
        }
    }
}