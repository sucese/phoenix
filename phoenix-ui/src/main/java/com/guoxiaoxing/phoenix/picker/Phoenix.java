package com.guoxiaoxing.phoenix.picker;

import android.app.Activity;
import android.content.Intent;

import com.guoxiaoxing.phoenix.R;
import com.guoxiaoxing.phoenix.camera.CameraActivity;
import com.guoxiaoxing.phoenix.core.PhoenixConfig;
import com.guoxiaoxing.phoenix.core.PhoenixOption;
import com.guoxiaoxing.phoenix.core.common.PhoenixConstant;
import com.guoxiaoxing.phoenix.core.listener.Starter;
import com.guoxiaoxing.phoenix.core.model.MediaEntity;
import com.guoxiaoxing.phoenix.picker.ui.picker.PickerActivity;
import com.guoxiaoxing.phoenix.picker.ui.picker.PreviewActivity;
import com.guoxiaoxing.phoenix.picker.util.DoubleUtils;

import java.util.List;

public final class Phoenix implements Starter {

    private static volatile PhoenixConfig config;

    public Phoenix() {

    }

    public static PhoenixConfig config() {
        if (config == null) {
            synchronized (Phoenix.class) {
                if (config == null) {
                    config = new PhoenixConfig();
                }
            }
        }
        return config;
    }

    public static PhoenixOption with() {
        return new PhoenixOption();
    }

    public static List<MediaEntity> result(Intent intent){
        if(intent == null){
            return null;
        }
        return intent.getParcelableArrayListExtra(PhoenixConstant.PHOENIX_RESULT);
    }

    @Override
    public void start(Activity activity, PhoenixOption option, int type, int requestCode) {
        if (!DoubleUtils.INSTANCE.isFastDoubleClick()) {
            switch (type) {
                case PhoenixOption.TYPE_PICK_MEDIA: {
                    Intent intent = new Intent(activity, PickerActivity.class);
                    intent.putExtra(PhoenixConstant.PHOENIX_OPTION, option);
                    activity.startActivityForResult(intent, requestCode);
                    activity.overridePendingTransition(R.anim.phoenix_activity_in, 0);
                    break;
                }
                case PhoenixOption.TYPE_TAKE_PICTURE: {
                    Intent intent = new Intent(activity, CameraActivity.class);
                    intent.putExtra(PhoenixConstant.PHOENIX_OPTION, option);
                    activity.startActivityForResult(intent, requestCode);
                    activity.overridePendingTransition(R.anim.phoenix_activity_in, 0);
                }
                break;
                case PhoenixOption.TYPE_BROWSER_PICTURE: {
                    Intent intent = new Intent(activity, PreviewActivity.class);
                    intent.putExtra(PhoenixConstant.PHOENIX_OPTION, option);
                    activity.startActivityForResult(intent, requestCode);
                    activity.overridePendingTransition(R.anim.phoenix_activity_in, 0);
                }
                break;
            }
        }
    }
}
