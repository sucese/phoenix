package com.guoxiaoxing.phoenix.picker.widget.dialog

/**
 * If a view implements this interface passed to the HUD as a custom view, its animation
 * speed can be change by calling setAnimationSpeed() on the HUD.
 * This interface only provides convenience, how animation speed work depends on the view implementation.
 */
interface PictureIndeterminate {
    fun setAnimationSpeed(scale: Float)
}
