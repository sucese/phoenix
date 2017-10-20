package com.guoxiaoxing.phoenix.picker.ui.editor

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant

/**
 * The picture edit activity
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
class PictureEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_edit)

        val path = intent.getStringExtra(PhoenixConstant.KEY_FILE_PATH)
        if(TextUtils.isEmpty(path)){
            return
        }

        val pictureFragment = PictureEditFragment.newInstance()
        val bundle = Bundle()
        bundle.putString(PhoenixConstant.KEY_FILE_PATH, path)
        pictureFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, pictureFragment)
                .commitAllowingStateLoss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
