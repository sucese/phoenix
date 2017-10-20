package com.guoxiaoxing.phoenix.picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.listener.Starter;
import com.guoxiaoxing.phoenix.R;
import com.guoxiaoxing.phoenix.picker.ui.camera.CameraActivity;
import com.guoxiaoxing.phoenix.picker.ui.picker.PickerActivity;
import com.guoxiaoxing.phoenix.picker.ui.picker.PreviewActivity;
import com.guoxiaoxing.phoenix.picker.util.DoubleUtils;

public final class Phoenix implements Starter {

    private static volatile PhoenixOption option;

    public Phoenix() {

    }

    public static PhoenixOption with() {

        if (option == null) {
            synchronized (Phoenix.class) {
                if (option == null) {
                    option = new PhoenixOption();
                }
            }
        }
        return option;
    }

    @Override
    public void start(Context context, int type) {
        if (!DoubleUtils.INSTANCE.isFastDoubleClick()) {
            switch (type) {
                case PhoenixOption.TYPE_PICK_MEDIA: {
                    Intent intent = new Intent(context, PickerActivity.class);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) (context)).overridePendingTransition(R.anim.phoenix_activity_in, 0);
                    }
                    break;
                }
                case PhoenixOption.TYPE_TAKE_PICTURE: {
                    Intent intent = new Intent(context, CameraActivity.class);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) (context)).overridePendingTransition(R.anim.phoenix_activity_in, 0);
                    }
                }
                break;
                case PhoenixOption.TYPE_BROWSER_PICTURE: {
                    Intent intent = new Intent(context, PreviewActivity.class);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) (context)).overridePendingTransition(R.anim.phoenix_activity_in, 0);
                    }
                }
                break;
            }
        }
    }
}
