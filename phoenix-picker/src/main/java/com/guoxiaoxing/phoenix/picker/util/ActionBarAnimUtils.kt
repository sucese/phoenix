package com.guoxiaoxing.phoenix.picker.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import com.guoxiaoxing.phoenix.picture.edit.widget.hierarchy.ActionFrameLayout
import java.util.*

class ActionBarAnimUtils(layerActionView: ActionFrameLayout, val editorBar: View, val funcView: View, val activityContext: Context)
    : ActionFrameLayout.ActionListener {

    private var mDirtyAnimating = false
    private var mDirtyScreen = true
    private var mMoveActionHandled = false
    private val hideRunnable = HideOrShowRunnable(false)
    private val showRunnable = HideOrShowRunnable(true)
    var interceptDirtyAnimation = false
    private val mHandler = Handler(Looper.getMainLooper())
    private val mFunBarAnimateListeners = ArrayList<OnFunBarAnimationListener>()

    inner class HideOrShowRunnable(val show: Boolean) : Runnable {
        override fun run() {
            showOrHideFuncAndBarView(show)
        }

    }

    init {
        layerActionView.actionListener = this
    }

    fun showOrHideFuncAndBarView(show: Boolean, listener: AnimatorListenerAdapter? = null) {
        if (mDirtyScreen == show) return
        mDirtyScreen = show
        if (mDirtyScreen) {
            mHandler.removeCallbacks(hideRunnable)
        } else {
            mHandler.removeCallbacks(showRunnable)
        }
        mDirtyAnimating = true
        invokeAnimateListener(show)
        val barHeight = editorBar.height.toFloat()
        val barAnimator = ObjectAnimator.ofFloat(editorBar, "translationY", if (!show) 0f else -barHeight, if (!show) -barHeight else 0f)
        val funcHeight = funcView.height.toFloat()
        val funcAnimator = ObjectAnimator.ofFloat(funcView, "translationY", if (!show) 0f else funcHeight, if (!show) funcHeight else 0f)
        val set = AnimatorSet()
        set.playTogether(barAnimator, funcAnimator)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mDirtyAnimating = false
                listener?.onAnimationEnd(animation)
            }
        })
        set.duration = 300
        set.start()
        if (show) {
            MatrixUtils.showStatusBar(activityContext as Activity)
        } else {
            MatrixUtils.hideStatusBar(activityContext as Activity)
        }
    }

    override fun actionMove() {
        if (interceptDirtyAnimation) {
            return
        }
        if (mDirtyScreen && !mMoveActionHandled) {
            if (!mDirtyAnimating) {
                mMoveActionHandled = true
                mHandler.postDelayed(hideRunnable, 300)
            }
        }
    }

    override fun actionUp() {
        if (interceptDirtyAnimation) {
            return
        }
        mMoveActionHandled = false
        if (mDirtyScreen) {
            mHandler.removeCallbacks(hideRunnable)
            if (!mDirtyAnimating) {
                mHandler.postDelayed(hideRunnable, 300)
            }
        } else {
            mHandler.removeCallbacks(showRunnable)
            if (!mDirtyAnimating) {
                mHandler.postDelayed(showRunnable, 300)
            }
        }
    }

    private fun invokeAnimateListener(show: Boolean) {
        for (listener in mFunBarAnimateListeners) {
            listener.onFunBarAnimate(show)
        }
    }

    fun addFunBarAnimateListener(listener: OnFunBarAnimationListener) {
        mFunBarAnimateListeners.add(listener)
    }

    fun removeFunBarAnimateListener(listener: OnFunBarAnimationListener) {
        mFunBarAnimateListeners.remove(listener)
    }

    interface OnFunBarAnimationListener {
        fun onFunBarAnimate(show: Boolean)
    }

}