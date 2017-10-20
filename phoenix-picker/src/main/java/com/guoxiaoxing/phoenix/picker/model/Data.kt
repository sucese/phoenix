package com.guoxiaoxing.phoenix.picker.model

import android.graphics.*
import com.guoxiaoxing.phoenix.picker.util.MatrixUtils
import com.guoxiaoxing.phoenix.picker.util.recycleBitmap
import com.guoxiaoxing.phoenix.picture.edit.widget.blur.BlurMode
import com.guoxiaoxing.phoenix.picture.edit.widget.stick.Sticker
import java.io.Serializable

/**
 * The data model class
 * <p>
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 */
interface SharableData : Serializable

data class InputTextModel(val id: String?, val text: String?, val color: Int?) : SharableData

data class InputStickModel(val sticker: Sticker, val stickerIndex: Int) : SharableData

data class HierarchyEditResult(val supportMatrix: Matrix, val bitmap: Bitmap?) : SharableData

/**
 * inner editor cache for reEdit and undo or just say recover
 */
data class HierarchyCache(val hierarchyCache: Map<String, SaveStateMarker>) : SharableData

/**
 * mark of image editor's detail function panel's share data structure
 */
interface FuncDetailsMarker

data class PaintDetail(val color: Int) : FuncDetailsMarker

data class BlurDetal(val blurMode: BlurMode) : FuncDetailsMarker

/**
 * it's important for each painting layer,each layer holds it's special dataStructure
 * 1.redraw all view's cache
 * 2.save view's painting data for restore simply called restore info
 */
abstract class SaveStateMarker {
    var id = MatrixUtils.randomId()
    override fun equals(other: Any?): Boolean {
        if (other is SaveStateMarker) {
            return id == (other.id)
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    open fun reset() {

    }

    open fun deepCopy(): SaveStateMarker {
        return this
    }
}

/**
 * Crop func's holding data structure
 * @property originalBitmap bitmap to edit
 * @property lastDisplayRectF last rectF set by editor
 * @property originalMatrix    bitmap to fit imageView generated this matrix
 * @property supportMatrix rootView's matrix operation
 * @property cropRect display window's crop rect
 * @property originalCropRation  original view->crop display view ration
 */
data class CropSaveState(var originalBitmap: Bitmap, var lastDisplayRectF: RectF, val originalMatrix: Matrix,
                         var supportMatrix: Matrix,
                         var cropRect: RectF, val originalCropRation: Float)
    : SaveStateMarker() {
    var cropBitmap: Bitmap? = null
    var cropFitCenterMatrix: Matrix = Matrix()
    override fun reset() {
        recycleBitmap(originalBitmap)
        recycleBitmap(cropBitmap)
        cropFitCenterMatrix.reset()
    }

    override fun deepCopy(): SaveStateMarker {
        val state = CropSaveState(originalBitmap, RectF(lastDisplayRectF), Matrix(originalMatrix), Matrix(supportMatrix), RectF(cropRect), originalCropRation)
        state.id = this.id
        return state
    }
}

/**
 * Scrawl func's holding data structure
 */
data class PaintSaveState(var paint: Paint, var path: Path) : SaveStateMarker() {
    override fun deepCopy(): SaveStateMarker {
        val state = PaintSaveState(MatrixUtils.copyPaint(paint), path)
        state.id = this.id
        return state
    }
}

abstract class PastingSaveStateMarker(val initDisplayRect: RectF, val displayMatrix: Matrix) : SaveStateMarker() {
    //for rebound.
    var initEventDisplayMatrix = Matrix()

}

/**
 * TextPasting func's holding data structure
 */
data class TextPastingSaveState(val text: String, val textColor: Int, val initTextRect: RectF, private val initDisplay: RectF, private val display: Matrix) : PastingSaveStateMarker(initDisplay, display) {
    override fun deepCopy(): SaveStateMarker {
        val state = TextPastingSaveState(text, textColor, RectF(initTextRect), RectF(initDisplay), Matrix(display))
        state.id = this.id
        return state
    }

}

/**
 *  Sticker func's holding data structure
 */
data class StickSaveState(val sticker: Sticker, val stickerIndex: Int, private val initDisplay: RectF, private val display: Matrix) : PastingSaveStateMarker(initDisplay, display) {
    override fun deepCopy(): SaveStateMarker {
        val state = StickSaveState(sticker, stickerIndex, RectF(initDisplay), Matrix(display))
        state.id = this.id
        return state
    }
}

/**
 * Mosaic func's holding data structure
 */
data class BlurSaveState(var mode: BlurMode, var path: Path) : SaveStateMarker() {
    override fun deepCopy(): SaveStateMarker {
        return super.deepCopy()
    }
}