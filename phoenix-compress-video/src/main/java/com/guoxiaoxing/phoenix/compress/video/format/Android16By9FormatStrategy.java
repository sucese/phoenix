package com.guoxiaoxing.phoenix.compress.video.format;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

class Android16By9FormatStrategy implements MediaFormatStrategy {
    public static final int AUDIO_BITRATE_AS_IS = -1;
    public static final int AUDIO_CHANNELS_AS_IS = -1;
    public static final int SCALE_720P = 5;
    private static final String TAG = "Android16By9FormatStrategy";
    private final int mScale;
    private final int mVideoBitrate;
    private final int mAudioBitrate;
    private final int mAudioChannels;

    public Android16By9FormatStrategy(int scale, int videoBitrate) {
        this(scale, videoBitrate, AUDIO_BITRATE_AS_IS, AUDIO_CHANNELS_AS_IS);
    }

    public Android16By9FormatStrategy(int scale, int videoBitrate, int audioBitrate, int audioChannels) {
        mScale = scale;
        mVideoBitrate = videoBitrate;
        mAudioBitrate = audioBitrate;
        mAudioChannels = audioChannels;
    }

    @Override
    public MediaFormat createVideoOutputFormat(MediaFormat inputFormat) {
        int width = inputFormat.getInteger(MediaFormat.KEY_WIDTH);
        int height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT);
        int targetLonger = mScale * 16 * 16;
        int targetShorter = mScale * 16 * 9;
        int longer, shorter, outWidth, outHeight;
        if (width >= height) {
            longer = width;
            shorter = height;
            outWidth = targetLonger;
            outHeight = targetShorter;
        } else {
            shorter = width;
            longer = height;
            outWidth = targetShorter;
            outHeight = targetLonger;
        }
        if (longer * 9 != shorter * 16) {
            throw new OutputFormatUnavailableException("This video is not 16:9, and is not able to transcode. (" + width + "x" + height + ")");
        }
        if (shorter <= targetShorter) {
            Log.d(TAG, "This video's height is less or equal to " + targetShorter + ", pass-through. (" + width + "x" + height + ")");
            return null;
        }
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", outWidth, outHeight);
        // From Nexus 4 Camera in 720p
        format.setInteger(MediaFormat.KEY_BIT_RATE, mVideoBitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        return format;
    }

    @Override
    public MediaFormat createAudioOutputFormat(MediaFormat inputFormat) {
        if (mAudioBitrate == AUDIO_BITRATE_AS_IS || mAudioChannels == AUDIO_CHANNELS_AS_IS) return null;

        // Use original sample rate, as resampling is not supported yet.
        final MediaFormat format = MediaFormat.createAudioFormat(MediaFormatExtraConstants.MIMETYPE_AUDIO_AAC,
                inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), mAudioChannels);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mAudioBitrate);
        return format;
    }
}
