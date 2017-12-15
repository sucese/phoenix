package com.guoxiaoxing.phoenix.picker.ui.camera.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.guoxiaoxing.phoenix.R;
import com.guoxiaoxing.phoenix.picker.ui.camera.util.DeviceUtils;

public class MediaActionSwitchView extends ImageButton {

    @Nullable
    private OnMediaActionStateChangeListener onMediaActionStateChangeListener;

    public interface OnMediaActionStateChangeListener {
        void switchAction();
    }

    private Drawable photoDrawable;
    private Drawable videoDrawable;
    private int padding = 5;

    public MediaActionSwitchView(Context context) {
        this(context, null);
    }

    public MediaActionSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView();
    }

    public MediaActionSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void initializeView() {
        Context context = getContext();

        photoDrawable = ContextCompat.getDrawable(context, R.drawable.phoenix_photo_camera_white);
        photoDrawable = DrawableCompat.wrap(photoDrawable);
        DrawableCompat.setTintList(photoDrawable.mutate(), ContextCompat.getColorStateList(context, R.drawable.phoenix_selector_switch_camera_mode));

        videoDrawable = ContextCompat.getDrawable(context, R.drawable.phoenix_videocam_white);
        videoDrawable = DrawableCompat.wrap(videoDrawable);
        DrawableCompat.setTintList(videoDrawable.mutate(), ContextCompat.getColorStateList(context, R.drawable.phoenix_selector_switch_camera_mode));

        setBackgroundResource(R.drawable.phoenix_circle_frame_background_dark);
//        setBackgroundResource(R.drawable.phoenix_circle_frame_background);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onMediaActionStateChangeListener != null) {
                    onMediaActionStateChangeListener.switchAction();
                }
            }
        });

        padding = DeviceUtils.convertDipToPixels(context, padding);
        setPadding(padding, padding, padding, padding);

        displayActionWillSwitchVideo();
    }

    public void displayActionWillSwitchPhoto(){
        setImageDrawable(photoDrawable);
    }

    public void displayActionWillSwitchVideo(){
        setImageDrawable(videoDrawable);
    }

    public void setOnMediaActionStateChangeListener(OnMediaActionStateChangeListener onMediaActionStateChangeListener) {
        this.onMediaActionStateChangeListener = onMediaActionStateChangeListener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (Build.VERSION.SDK_INT > 10) {
            if (enabled) {
                setAlpha(1f);
            } else {
                setAlpha(0.5f);
            }
        }
    }

}
