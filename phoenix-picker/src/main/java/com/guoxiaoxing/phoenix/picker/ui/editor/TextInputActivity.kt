package com.guoxiaoxing.phoenix.picker.ui.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.model.InputTextModel
import com.guoxiaoxing.phoenix.picker.util.ScreenUtils
import com.guoxiaoxing.phoenix.picker.widget.editor.ColorSeekBar
import kotlinx.android.synthetic.main.activity_editor_text_input.*

/**
 * The text input activity
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
class TextInputActivity : AppCompatActivity() {
    private val mResultCode = 301
    private var mTextColor = 0
    private var mTextInputId: String? = null

    companion object {
        private val EXTRA_CODE = "extraInput"
        fun intent(context: Context, model: InputTextModel?): Intent {
            val intent = Intent(context, TextInputActivity::class.java)
            intent.putExtra(EXTRA_CODE, model)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_text_input)
        ScreenUtils.assistActivity(this)
        setData()
        setupListener()
    }

    private fun setData() {
        val readyData = intent.getSerializableExtra(EXTRA_CODE) as? InputTextModel
        readyData?.let {
            mTextInputId = readyData.id
            etInput.setText(readyData.text ?: "")
            colorBarInput.setOnInitDoneListener(object : ColorSeekBar.OnInitDoneListener {
                override fun done() {
                    var position = 8
                    readyData.color?.let {
                        position = colorBarInput.getColorIndexPosition(it)
                    }
                    colorBarInput.setColorBarPosition(position)
                }
            })
        }
    }

    private fun setupListener() {
        tvCancelInput.setOnClickListener {
            finish()
        }
        tvConfirmInput.setOnClickListener {
            val text = etInput.text.trim()
            if (text.isBlank()) {
                finish()
            } else {
                val intent = Intent()
                intent.putExtra(mResultCode.toString(), InputTextModel(mTextInputId, text.toString(), mTextColor))
                setResult(mResultCode, intent)
                finish()
            }

        }
        colorBarInput.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
            override fun onColorChangeListener(colorBarPosition: Int, alphaBarPosition: Int, color: Int) {
                etInput.setTextColor(color)
                mTextColor = color
            }
        })
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.animation_top_to_bottom)
    }
}
