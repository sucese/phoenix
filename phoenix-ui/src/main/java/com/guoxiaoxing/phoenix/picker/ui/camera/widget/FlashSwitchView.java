package com.guoxiaoxing.phoenix.picker.ui.camera.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.guoxiaoxing.phoenix.R;

public class FlashSwitchView extends ImageButton {

    private Drawable flashOnDrawable;
    private Drawable flashOffDrawable;
    private Drawable flashAutoDrawable;

    public FlashSwitchView(@NonNull Context context) {
        this(context, null);
    }

    public FlashSwitchView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        flashOnDrawable = ContextCompat.getDrawable(context, R.drawable.phoenix_flash_on_white);
        flashOffDrawable = ContextCompat.getDrawable(context, R.drawable.phoenix_flash_off_white);
        flashAutoDrawable = ContextCompat.getDrawable(context, R.drawable.phoenix_flash_auto_white);
        setup();
    }

    private void setup() {
        setBackgroundColor(Color.TRANSPARENT);
        displayFlashAuto();
    }

    public void displayFlashOff() {
        setImageDrawable(flashOffDrawable);
    }

    public void displayFlashOn() {
        setImageDrawable(flashOnDrawable);
    }

    public void displayFlashAuto() {
        setImageDrawable(flashAutoDrawable);
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
