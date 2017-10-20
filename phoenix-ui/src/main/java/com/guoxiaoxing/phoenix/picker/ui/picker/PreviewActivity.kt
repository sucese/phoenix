package com.guoxiaoxing.phoenix.picker.ui.picker

import android.os.Bundle
import android.os.Parcelable

import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picker.rx.bus.ImagesObservable
import com.guoxiaoxing.phoenix.picker.ui.BaseActivity

import java.util.ArrayList

class PreviewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        setupFragment()
    }

    fun setupFragment() {
        val position = intent.getIntExtra(PhoenixConstant.KEY_POSITION, 0)

        val selectImages = if (intent.getSerializableExtra(PhoenixConstant.KEY_SELECT_LIST) != null) {
            intent.getSerializableExtra(PhoenixConstant.KEY_SELECT_LIST) as List<MediaEntity>
        }else{
            ArrayList()
        }

        val is_bottom_preview = intent.getBooleanExtra(PhoenixConstant.EXTRA_BOTTOM_PREVIEW, false)
        val images: List<MediaEntity>
        if (is_bottom_preview) {
            // 底部预览按钮过来
            images = intent.getSerializableExtra(PhoenixConstant.KEY_LIST) as List<MediaEntity>
        } else {
            images = ImagesObservable.instance.readLocalMedias()
        }

        val fragment = PreviewFragment.newInstance()
        val bundle = Bundle()
        bundle.putInt(PhoenixConstant.KEY_POSITION, position)
        bundle.putParcelableArrayList(PhoenixConstant.KEY_SELECT_LIST, selectImages as ArrayList<out Parcelable>)
        bundle.putParcelableArrayList(PhoenixConstant.KEY_LIST, images as ArrayList<out Parcelable>)
        fragment.arguments = bundle
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.preview_fragment_container, fragment)
                .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(0, R.anim.phoenix_activity_out)
    }
}
