package com.guoxiaoxing.phoenix.picker.ui.camera.widget;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.guoxiaoxing.phoenix.R;

public class CameraSettingsView extends AppCompatImageButton {

    public CameraSettingsView(Context context) {
        this(context, null);
    }

    public CameraSettingsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
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

    private void init() {
//        setBackgroundResource(R.drawable.phoenix_circle_frame_background_dark);
        setImageResource(R.drawable.phoenix_settings_white);
        setScaleType(ScaleType.CENTER);
    }
}
