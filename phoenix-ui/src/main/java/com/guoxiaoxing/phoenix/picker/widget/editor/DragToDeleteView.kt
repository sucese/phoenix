package com.guoxiaoxing.phoenix.picker.widget.editor

import android.graphics.RectF
import android.view.View
import android.widget.TextView
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils
import com.guoxiaoxing.phoenix.picker.util.OnLayoutRectChange
import com.guoxiaoxing.phoenix.picker.util.setInt

/**
 * ## Ui element of pasting View to drag and delete .
 *
 * Created by lxw
 */
class DragToDeleteView(private val view: View) {

    var onLayoutRectChange: OnLayoutRectChange? = null
    private val mTextView: TextView

    init {
        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val rect = RectF()
            rect.setInt(left, top, right, bottom)
            onLayoutRectChange?.invoke(view, rect)
        }
        mTextView = view.findViewById(R.id.tvDragDelete) as TextView
    }

    fun showOrHide(show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setDrag2DeleteText(focus: Boolean) {
        val text = if (focus) MatrixUtils.getResourceString(view.context, R.string.editor_drag_to_delete)
        else MatrixUtils.getResourceString(view.context, R.string.editor_release_to_delete)
        mTextView.text = text
    }
}