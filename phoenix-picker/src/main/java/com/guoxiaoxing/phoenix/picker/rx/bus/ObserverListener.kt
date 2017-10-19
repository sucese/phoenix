package com.guoxiaoxing.phoenix.picker.rx.bus

import com.guoxiaoxing.phoenix.core.model.MediaEntity
import com.guoxiaoxing.phoenix.picker.model.MediaFolder

interface ObserverListener {
    fun observerUpFoldersData(folders: List<MediaFolder>)

    fun observerUpSelectsData(selectMediaEntities: List<MediaEntity>)
}
