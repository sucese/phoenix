package com.guoxiaoxing.phoenix.picker.model

import com.guoxiaoxing.phoenix.core.model.MediaEntity

import java.io.Serializable

data class MediaFolder(var name: String,
                       var path: String,
                       var firstImagePath: String,
                       var imageNumber: Int,
                       var checkedNumber: Int,
                       var isChecked: Boolean,
                       var images: MutableList<MediaEntity>) : Serializable