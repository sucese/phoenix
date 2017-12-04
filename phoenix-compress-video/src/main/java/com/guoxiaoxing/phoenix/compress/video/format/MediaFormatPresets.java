package com.guoxiaoxing.phoenix.compress.video.format;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

// Refer for example: https://gist.github.com/wobbals/3990442
// Refer for preferred parameters: https://developer.apple.com/library/ios/documentation/networkinginternet/conceptual/streamingmediaguide/UsingHTTPLiveStreaming/UsingHTTPLiveStreaming.html#//apple_ref/doc/uid/TP40008332-CH102-SW8
// Refer for available keys: (ANDROID ROOT)/media/libstagefright/ACodec.cpp
public class MediaFormatPresets {
    private static final int LONGER_LENGTH_960x540 = 960;

    private MediaFormatPresets() {
    }

    // preset similar to iOS SDK's AVAssetExportPreset960x540
    @Deprecated
    public static MediaFormat getExportPreset960x540() {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", 960, 540);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 5500 * 1000);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        return format;
    }

    /**
     * Preset similar to iOS SDK's AVAssetExportPreset960x540.
     * Note that encoding resolutions of this preset are not supported in all devices e.g. Nexus 4.
     * On unsupported device encoded video stream will be broken without any exception.
     * @param originalWidth Input video width.
     * @param originalHeight Input video height.
     * @return MediaFormat instance, or null if pass through.
     */
    public static MediaFormat getExportPreset960x540(int originalWidth, int originalHeight) {
        int longerLength = Math.max(originalWidth, originalHeight);
        int shorterLength = Math.min(originalWidth, originalHeight);

        if (longerLength <= LONGER_LENGTH_960x540) return null; // don't upscale

        int residue = LONGER_LENGTH_960x540 * shorterLength % longerLength;
        if (residue != 0) {
            double ambiguousShorter = (double) LONGER_LENGTH_960x540 * shorterLength / longerLength;
            throw new OutputFormatUnavailableException(String.format(
                    "Could not fit to integer, original: (%d, %d), scaled: (%d, %f)",
                    longerLength, shorterLength, LONGER_LENGTH_960x540, ambiguousShorter));
        }

        int scaledShorter = LONGER_LENGTH_960x540 * shorterLength / longerLength;
        int width, height;
        if (originalWidth >= originalHeight) {
            width = LONGER_LENGTH_960x540;
            height = scaledShorter;
        } else {
            width = scaledShorter;
            height = LONGER_LENGTH_960x540;
        }

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 5500 * 1000);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        return format;
    }
}
