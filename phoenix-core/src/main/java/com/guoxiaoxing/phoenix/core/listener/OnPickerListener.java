package com.guoxiaoxing.phoenix.core.listener;

import com.guoxiaoxing.phoenix.core.model.MediaEntity;

import java.util.List;

/**
 * For more information, you can visit https://github.com/guoxiaoxing or contact me by
 * guoxiaoxingse@163.com.
 *
 * @author guoxiaoxing
 * @since 2017/7/26 下午5:52
 */
public interface OnPickerListener {

    void onPickSuccess(List<MediaEntity> pickList);

    void onPickFailed(String errorMessage);
}