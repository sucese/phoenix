package com.guoxiaoxing.phoenix.picker.rx.bus

import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picker.model.MediaFolder
import com.guoxiaoxing.phoenix.picker.util.DebugUtil

import java.util.ArrayList

class ImagesObservable private constructor() : SubjectListener {

    //观察者接口集合
    private val observers = ArrayList<ObserverListener>()

    private var folders: MutableList<MediaFolder>
    private var mediaEntities: MutableList<MediaEntity>
    private var selectedImages: MutableList<MediaEntity>

    init {
        folders = ArrayList<MediaFolder>()
        mediaEntities = ArrayList<MediaEntity>()
        selectedImages = ArrayList<MediaEntity>()
    }

    /**
     * 存储文件夹图片

     * @param list
     */

    fun saveLocalFolders(list: MutableList<MediaFolder>?) {
        if (list != null) {
            folders = list
        }
    }


    /**
     * 存储图片

     * @param list
     */
    fun saveLocalMedia(list: MutableList<MediaEntity>) {
        mediaEntities = list
    }


    /**
     * 读取图片
     */
    fun readLocalMedias(): List<MediaEntity> {
        if (mediaEntities == null) {
            mediaEntities = ArrayList<MediaEntity>()
        }
        return mediaEntities
    }

    /**
     * 读取所有文件夹图片
     */
    fun readLocalFolders(): List<MediaFolder> {
        if (folders == null) {
            folders = ArrayList<MediaFolder>()
        }
        return folders
    }


    /**
     * 读取选中的图片
     */
    fun readSelectLocalMedias(): List<MediaEntity> {
        return selectedImages
    }


    fun clearLocalFolders() {
        if (folders != null)
            folders!!.clear()
    }

    fun clearLocalMedia() {
        if (mediaEntities != null)
            mediaEntities!!.clear()
        DebugUtil.i("ImagesObservable:", "clearLocalMedia success!")
    }

    fun clearSelectedLocalMedia() {
        selectedImages?.clear()
    }

    override fun add(observerListener: ObserverListener) {
        observers.add(observerListener)
    }

    override fun remove(observerListener: ObserverListener) {
        if (observers.contains(observerListener)) {
            observers.remove(observerListener)
        }
    }

    companion object {
        val instance: ImagesObservable by lazy { ImagesObservable() }
    }
}