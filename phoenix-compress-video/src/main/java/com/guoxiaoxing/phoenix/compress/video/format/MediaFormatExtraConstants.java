/*
 * Copyright (C) 2014 Yuya Tanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guoxiaoxing.phoenix.compress.video.format;

public class MediaFormatExtraConstants {
    // from MediaFormat of API level >= 21, but might be usable in older APIs as native code implementation exists.
    // https://android.googlesource.com/platform/frameworks/av/+/lollipop-release/media/libstagefright/ACodec.cpp#2621
    // NOTE: native code enforces baseline profile.
    // https://android.googlesource.com/platform/frameworks/av/+/lollipop-release/media/libstagefright/ACodec.cpp#2638
    /** For encoder parameter. Use value of MediaCodecInfo.CodecProfileLevel.AVCProfile* . */
    public static final String KEY_PROFILE = "profile";

    // from https://android.googlesource.com/platform/frameworks/av/+/lollipop-release/media/libstagefright/ACodec.cpp#2623
    /** For encoder parameter. Use value of MediaCodecInfo.CodecProfileLevel.AVCLevel* . */
    public static final String KEY_LEVEL = "level";

    // from https://android.googlesource.com/platform/frameworks/av/+/lollipop-release/media/libstagefright/MediaCodec.cpp#2197
    /** Included in MediaFormat from {@link android.media.MediaExtractor#getTrackFormat(int)}. Value is {@link java.nio.ByteBuffer}. */
    public static final String KEY_AVC_SPS = "csd-0";
    /** Included in MediaFormat from {@link android.media.MediaExtractor#getTrackFormat(int)}. Value is {@link java.nio.ByteBuffer}. */
    public static final String KEY_AVC_PPS = "csd-1";

    /**
     * For decoder parameter and included in MediaFormat from {@link android.media.MediaExtractor#getTrackFormat(int)}.
     * Decoder rotates specified degrees before rendering video to surface.
     * NOTE: Only included in track format of API &gt;= 21.
     */
    public static final String KEY_ROTATION_DEGREES = "rotation-degrees";

    // Video formats
    // from MediaFormat of API level >= 21
    public static final String MIMETYPE_VIDEO_AVC = "video/avc";
    public static final String MIMETYPE_VIDEO_H263 = "video/3gpp";
    public static final String MIMETYPE_VIDEO_VP8 = "video/x-vnd.on2.vp8";

    // Audio formats
    // from MediaFormat of API level >= 21
    public static final String MIMETYPE_AUDIO_AAC = "audio/mp4a-latm";

    private MediaFormatExtraConstants() {
        throw new RuntimeException();
    }
}
