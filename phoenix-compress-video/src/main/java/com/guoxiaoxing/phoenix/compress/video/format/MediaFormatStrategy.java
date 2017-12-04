package com.guoxiaoxing.phoenix.compress.video.format;

import android.media.MediaFormat;

public interface MediaFormatStrategy {

    /**
     * Returns preferred video format for encoding.
     *
     * @param inputFormat MediaFormat from MediaExtractor, contains csd-0/csd-1.
     * @return null for passthrough.
     * @throws OutputFormatUnavailableException if input could not be transcoded because of restrictions.
     */
    public MediaFormat createVideoOutputFormat(MediaFormat inputFormat);

    /**
     * Caution: this method should return null currently.
     *
     * @return null for passthrough.
     * @throws OutputFormatUnavailableException if input could not be transcoded because of restrictions.
     */
    public MediaFormat createAudioOutputFormat(MediaFormat inputFormat);

}
