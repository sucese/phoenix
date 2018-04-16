package com.guoxiaoxing.phoenix.picker.rx.bus

import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picker.model.MediaFolder
import java.util.*

class ImagesObservable private constructor() : SubjectListener {

    //观察者接口集合
    private val observers = ArrayList<ObserverListener>()

    private var folders: MutableList<MediaFolder>
    private var previewMediaEntities: MutableList<MediaEntity>
    private var selectedImages: MutableList<MediaEntity>

    init {
        folders = ArrayList<MediaFolder>()
        previewMediaEntities = ArrayList<MediaEntity>()
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
    fun savePreviewMediaList(list: MutableList<MediaEntity>) {
        previewMediaEntities = list
    }


    /**
     * 读取图片
     */
    fun readPreviewMediaEntities(): List<MediaEntity> {
        if (previewMediaEntities == null) {
            previewMediaEntities = ArrayList<MediaEntity>()
        }
        return previewMediaEntities
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

    fun clearCachedData() {
        previewMediaEntities.clear()
    }

    fun clearSelectedLocalMedia() {
        selectedImages.clear()
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