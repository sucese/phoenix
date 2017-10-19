package com.guoxiaoxing.phoenix.picture.edit.operation

import com.guoxiaoxing.phoenix.picture.edit.R

/**
 * The picture edit operation
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
enum class Operation {

    //    ZoomOperation {
//        override fun getIcon() = -1
//    },

    /**
     * 绘画标记
     */
    PaintOperation {
        override fun canPersistMode() = true

        override fun onOperation(selected: Boolean, operationProcessor: OperationProcessor) = operationProcessor.operatePaint(selected)

        override fun getIcon() = R.drawable.phoenix_selector_edit_image_pen_tool
    },

    /**
     * 添加表情
     */
    StickOperation {
        override fun canPersistMode() = false

        override fun onOperation(selected: Boolean, operationProcessor: OperationProcessor) = operationProcessor.operateStick(selected)

        override fun getIcon() = R.drawable.phoenix_selector_edit_image_emotion_tool
    },

    /**
     * 输入文字
     */
    TextOperation {
        override fun canPersistMode() = false

        override fun onOperation(selected: Boolean, operationProcessor: OperationProcessor) = operationProcessor.operateText(selected)

        override fun getIcon() = R.drawable.phoenix_selector_edit_image_text_tool
    },

    /**
     * 图片模糊
     */
    BlurOperation {
        override fun canPersistMode() = true

        override fun onOperation(selected: Boolean, operationProcessor: OperationProcessor) = operationProcessor.operateBlur(selected)

        override fun getIcon() = R.drawable.phoenix_selector_edit_image_mosaic_tool
    },

    /**
     * 图片裁剪
     */
    CropOperation {
        override fun canPersistMode() = false

        override fun onOperation(selected: Boolean, operationProcessor: OperationProcessor) = operationProcessor.operateCrop(selected)

        override fun getIcon() = R.drawable.phoenix_selector_edit_image_crop_tool
    };

//    AllOperation {
//        override fun getIcon() = -1
//    };

    abstract fun getIcon(): Int

    abstract fun onOperation(selected: Boolean, operationProcessor: OperationProcessor)

    abstract fun canPersistMode(): Boolean
}