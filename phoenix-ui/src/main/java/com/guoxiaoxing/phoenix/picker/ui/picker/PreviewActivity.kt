package com.guoxiaoxing.phoenix.picker.ui.picker

import android.os.Bundle
import android.os.Parcelable
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picker.ui.BaseActivity
import java.util.*

class PreviewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        setupFragment()
    }

    private fun setupFragment() {
        val position = intent.getIntExtra(PhoenixConstant.KEY_POSITION, 0)
        val selectImages = intent.getSerializableExtra(PhoenixConstant.KEY_PICK_LIST) as List<MediaEntity>
        val images = intent.getSerializableExtra(PhoenixConstant.KEY_ALL_LIST) as List<MediaEntity>
        val previewType = intent.getIntExtra(PhoenixConstant.KEY_PREVIEW_TYPE, PhoenixConstant.TYPE_PREIVEW_FROM_PICK)
        val fragment = PreviewFragment.newInstance()
        val bundle = Bundle()
        bundle.putParcelable(PhoenixConstant.PHOENIX_OPTION, option)
        bundle.putInt(PhoenixConstant.KEY_POSITION, position)
        bundle.putInt(PhoenixConstant.KEY_PREVIEW_TYPE, previewType)
        bundle.putParcelableArrayList(PhoenixConstant.KEY_PICK_LIST, selectImages as ArrayList<out Parcelable>)
        bundle.putParcelableArrayList(PhoenixConstant.KEY_ALL_LIST, images as ArrayList<out Parcelable>)
        fragment.arguments = bundle

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.preview_fragment_container, fragment)
                .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(supportFragmentManager.fragments.size <= 1){
            finish()
            overridePendingTransition(0, R.anim.phoenix_activity_out)
        }
    }
}
