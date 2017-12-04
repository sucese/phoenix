package com.guoxiaoxing.phoenix.compress.video.format;

public class MediaFormatStrategyPresets {

    public static final int AUDIO_BITRATE_AS_IS = -1;
    public static final int AUDIO_CHANNELS_AS_IS = -1;

    /**
     * @deprecated Use {@link #createExportPreset960x540Strategy()}.
     */
    @Deprecated
    public static final MediaFormatStrategy EXPORT_PRESET_960x540 = new ExportPreset960x540Strategy();


    public static MediaFormatStrategy createAndroid16By9FormatStrategy(int scale, int videoBitrate){
        return new Android16By9FormatStrategy(scale, videoBitrate);
    }

    public static MediaFormatStrategy createAndroid480pFormatStrategy(){
        return new Android480pFormatStrategy();
    }

    public static MediaFormatStrategy createAndroid480pFormatStrategy(int bitrate){
        return new Android480pFormatStrategy(bitrate);
    }

    public static MediaFormatStrategy createAndroid480pFormatStrategy(int bitrate, int audioBitrate, int audioChannels){
        return new Android480pFormatStrategy(bitrate, audioBitrate, audioChannels);
    }

    /**
     * Preset based on Nexus 4 camera recording with 720p quality.
     * This preset is ensured to work on any Android &gt;=4.3 devices by Android CTS (if codec is available).
     * Default bitrate is 8Mbps. {@link #createAndroid720pStrategy(int)} to specify bitrate.
     */
    public static MediaFormatStrategy createAndroid720pStrategy() {
        return new Android720pFormatStrategy();
    }

    /**
     * Preset based on Nexus 4 camera recording with 720p quality.
     * This preset is ensured to work on any Android &gt;=4.3 devices by Android CTS (if codec is available).
     * Audio track will be copied as-is.
     *
     * @param bitrate Preferred bitrate for video encoding.
     */
    public static MediaFormatStrategy createAndroid720pStrategy(int bitrate) {
        return new Android720pFormatStrategy(bitrate);
    }

    /**
     * Preset based on Nexus 4 camera recording with 720p quality.
     * This preset is ensured to work on any Android &gt;=4.3 devices by Android CTS (if codec is available).
     * <br>
     * Note: audio transcoding is experimental feature.
     *
     * @param bitrate       Preferred bitrate for video encoding.
     * @param audioBitrate  Preferred bitrate for audio encoding.
     * @param audioChannels Output audio channels.
     */
    public static MediaFormatStrategy createAndroid720pStrategy(int bitrate, int audioBitrate, int audioChannels) {
        return new Android720pFormatStrategy(bitrate, audioBitrate, audioChannels);
    }

    /**
     * Preset similar to iOS SDK's AVAssetExportPreset960x540.
     * Note that encoding resolutions of this preset are not supported in all devices e.g. Nexus 4.
     * On unsupported device encoded video stream will be broken without any exception.
     */
    public static MediaFormatStrategy createExportPreset960x540Strategy() {
        return new ExportPreset960x540Strategy();
    }

    private MediaFormatStrategyPresets() {
    }
}
