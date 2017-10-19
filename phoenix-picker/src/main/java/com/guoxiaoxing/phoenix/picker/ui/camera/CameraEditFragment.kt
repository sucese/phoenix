package com.guoxiaoxing.phoenix.picker.ui.camera

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.guoxiaoxing.phoenix.R
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant
import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.core.model.MimeType
import com.guoxiaoxing.phoenix.picker.ui.BaseFragment
import com.guoxiaoxing.phoenix.picker.util.BitmapUtils
import com.guoxiaoxing.phoenix.picker.util.ImageUtils

import java.lang.ref.WeakReference

class CameraEditFragment : BaseFragment(), View.OnClickListener, OnPictureEditListener {

    private var ivPreview: ImageView? = null
    private var tvNext: TextView? = null
    private var tvRephotograph: TextView? = null
    private var tvFinish: TextView? = null

    private lateinit var sourceBitmap: Bitmap
    private var path: Uri? = null
    private val superHandler = SuperHandler(this)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_camera_edit, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivPreview = view!!.findViewById(R.id.camera_iv_preview) as ImageView
        tvNext = view.findViewById(R.id.camera_tv_next_picture) as TextView
        tvRephotograph = view.findViewById(R.id.camera_tv_rephotograph) as TextView
        tvFinish = view.findViewById(R.id.camera_tv_finish) as TextView

        tvFinish!!.setOnClickListener(this)
        tvRephotograph!!.setOnClickListener(this)
        tvNext!!.setOnClickListener(this)

        val rotation = arguments.getInt(PhoenixConstant.ROTATION_KEY)
        val date = arguments.getByteArray(PhoenixConstant.BITMPA_KEY)
        val parameters = arguments.getParcelable<CameraParameter>(PhoenixConstant.IMAGE_INFO) ?: return

        sourceBitmap = ImageUtils.roatePicture(rotation, date, activity)
        ivPreview!!.setImageBitmap(sourceBitmap)
    }

    // 消除可能存在内存泄漏的警告
    private class SuperHandler<T>(params: T) : Handler() {
        private val outer: WeakReference<T>

        init {
            outer = WeakReference(params)
        }

        override fun handleMessage(msg: Message) {

            super.handleMessage(msg)
            val imageEditFragment = outer.get() as CameraEditFragment
            if (imageEditFragment != null) {
                imageEditFragment.ivPreview!!.setImageDrawable(Drawable.createFromPath(msg.obj as String))
                imageEditFragment.ivPreview!!.postInvalidate()
            }
        }
    }

    override fun onClick(v: View) {
        val resId = v.id

        if (resId == R.id.camera_ll_edit) {
            //            RotateFragment rotateFragment = RotateFragment.newInstance();
            //            rotateFragment.setOnPictureEditListener(this);
            //            Bundle bundle = new Bundle();
            //            String path = getPath(false);
            //            if (path != null) {
            //                bundle.putString(PhoenixConstant.KEY_FILE_PATH, path);
            //                rotateFragment.setArguments(bundle);
            //                getActivity().getSupportFragmentManager().beginTransaction()
            //                        .replace(R.id.fragment_root, rotateFragment).addToBackStack(null).commitAllowingStateLoss();
            //            }
        } else if (resId == R.id.camera_tv_rephotograph) {
            activity.supportFragmentManager.popBackStack()

        } else if (resId == R.id.camera_tv_finish) {
            tvFinish!!.isClickable = false
            if (sourceBitmap == null) {
                return
            }
            if (path == null) {
                path = ImageUtils.savePicture(context, sourceBitmap, false)
                if (path != null && path!!.path != null) {
                    val mediaEntity = MediaEntity.newBuilder()
                            .fileType(MimeType.ofImage())
                            .localPath(path!!.path)
                            .build()
                    CameraActivity.cameraList.add(mediaEntity)
                }
            } else {
                if (path!!.path != null) {
                    val mediaEntity = MediaEntity.newBuilder()
                            .fileType(MimeType.ofImage())
                            .localPath(path!!.path)
                            .build()
                    CameraActivity.cameraList.add(mediaEntity)
                }
            }
            processMedia(CameraActivity.cameraList)
        } else if (resId == R.id.camera_tv_next_picture) {
            tvNext!!.isClickable = false
            activity.supportFragmentManager.popBackStack()
            //保存照片
            //            int index = option.getCurrentIndex();
            //            option.setIndex(index + 1);
            val path = getPath(true)
            if (!TextUtils.isEmpty(path)) {
                val mediaEntity = MediaEntity.newBuilder()
                        .fileType(MimeType.ofImage())
                        .localPath(path)
                        .build()
                CameraActivity.cameraList.add(mediaEntity)
            }
        }
    }

    fun getPath(isKnowMedia: Boolean): String? {
        if (sourceBitmap == null) {
            return null
        }
        if (path == null) {
            path = ImageUtils.savePicture(context, sourceBitmap, isKnowMedia)
        }
        return if (path == null) "" else path!!.path
    }

    override fun onEditSucess(editPath: String) {
        sourceBitmap = BitmapUtils.getSmallBitmap(editPath, context)
        val message = superHandler.obtainMessage()
        message.obj = editPath
        message.what = 0
        path = Uri.parse(editPath)
        superHandler.sendMessage(message)
    }

    companion object {

        private val TAG = "CameraEditFragment"

        fun newInstance(bytes: ByteArray, rotation: Int, parameters: CameraParameter): CameraEditFragment {
            val fragment = CameraEditFragment()
            val bundle = Bundle()
            bundle.putByteArray(PhoenixConstant.BITMPA_KEY, bytes)
            bundle.putInt(PhoenixConstant.ROTATION_KEY, rotation)
            bundle.putParcelable(PhoenixConstant.IMAGE_INFO, parameters)
            fragment.arguments = bundle
            return fragment
        }
    }
}
