package com.guoxiaoxing.phoenix.picker.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picker.adapter.PickerAlbumAdapter
import com.guoxiaoxing.phoenix.picker.model.MediaFolder
import com.guoxiaoxing.phoenix.picker.util.AttrsUtils
import com.guoxiaoxing.phoenix.picker.util.DebugUtil
import com.guoxiaoxing.phoenix.picker.util.ScreenUtil
import com.guoxiaoxing.phoenix.picker.util.StringUtils

class FolderPopWindow(private val context: Context, private val mimeType: Int) : PopupWindow(), View.OnClickListener {
    private val window: View = LayoutInflater.from(context).inflate(R.layout.window_folder, null)
    private var recyclerView: RecyclerView? = null
    private var adapter: PickerAlbumAdapter? = null
    private val animationIn: Animation
    private val animationOut: Animation
    private var isDismiss = false
    private var id_ll_root: LinearLayout? = null
    private var picture_title: TextView? = null
    private val drawableUp: Drawable
    private val drawableDown: Drawable

    init {
        this.contentView = window
        this.width = ScreenUtil.getScreenWidth(context)
        this.height = ScreenUtil.getScreenHeight(context)
        this.animationStyle = R.style.style_window
        this.isFocusable = true
        this.isOutsideTouchable = true
        this.update()
        this.setBackgroundDrawable(ColorDrawable(Color.argb(123, 0, 0, 0)))
        drawableUp = ContextCompat.getDrawable(context, R.drawable.phoenix_arrow_up)!!
        drawableDown = ContextCompat.getDrawable(context, R.drawable.phoenix_arrow_down)!!
        animationIn = AnimationUtils.loadAnimation(context, R.anim.phoenix_album_show)
        animationOut = AnimationUtils.loadAnimation(context, R.anim.phoenix_album_dismiss)
        initView()
    }

    fun initView() {
        id_ll_root = window.findViewById(R.id.id_ll_root) as LinearLayout
        adapter = PickerAlbumAdapter(context)
        recyclerView = window.findViewById(R.id.folder_list) as RecyclerView
        recyclerView!!.layoutParams.height = (ScreenUtil.getScreenHeight(context) * 0.6).toInt()
        recyclerView!!.addItemDecoration(RecycleViewDivider(
                context, LinearLayoutManager.HORIZONTAL, ScreenUtil.dip2px(context, 0f), ContextCompat.getColor(context, R.color.transparent)))
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
        id_ll_root!!.setOnClickListener(this)
    }

    fun bindFolder(folders: List<MediaFolder>) {
        adapter!!.setMimeType(mimeType)
        adapter!!.bindFolderData(folders)
    }

    fun setPictureTitleView(picture_title: TextView) {
        this.picture_title = picture_title
    }

    override fun showAsDropDown(anchor: View) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                val location = IntArray(2)
                anchor.getLocationOnScreen(location)
                val height = anchor.height
                val x = location[0]
                val y = location[1]
                super.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y + height)
            } else {
                super.showAsDropDown(anchor)
            }

            isDismiss = false
            recyclerView!!.startAnimation(animationIn)
            StringUtils.modifyTextViewDrawable(picture_title!!, drawableUp, 2)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setOnItemClickListener(onItemClickListener: PickerAlbumAdapter.OnItemClickListener) {
        adapter!!.setOnItemClickListener(onItemClickListener)
    }

    override fun dismiss() {
        DebugUtil.i("PopWindow:", "dismiss")
        if (isDismiss) {
            return
        }
        StringUtils.modifyTextViewDrawable(picture_title!!, drawableDown, 2)
        isDismiss = true
        recyclerView!!.startAnimation(animationOut)
        dismiss()
        animationOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                isDismiss = false
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    dismiss4Pop()
                } else {
                    super@FolderPopWindow.dismiss()
                }
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
    }

    /**
     * 在android4.1.1和4.1.2版本关闭PopWindow
     */
    private fun dismiss4Pop() {
        Handler().post { super@FolderPopWindow.dismiss() }
    }


    /**
     * 设置选中状态
     */
    fun notifyDataCheckedStatus(mediaEntities: List<MediaEntity>) {
        try {
            // 获取选中图片
            val folders = adapter!!.folderData
            for (folder in folders) {
                folder.checkedNumber = 0
            }
            if (mediaEntities.size > 0) {
                for (folder in folders) {
                    var num = 0// 记录当前相册下有多少张是选中的
                    val images = folder.images
                    for (mediaEntity in images!!) {
                        val path = mediaEntity.localPath
                        for (m in mediaEntities) {
                            if (path == m.localPath) {
                                num++
                                folder.checkedNumber = num
                            }
                        }
                    }
                }
            }
            adapter!!.bindFolderData(folders)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.id_ll_root) {
            dismiss()
        }
    }

}
