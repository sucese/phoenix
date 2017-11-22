package com.guoxiaoxing.phoenix.compress.video.engine;

import java.nio.ShortBuffer;

public interface AudioRemixer {
    void remix(final ShortBuffer inSBuff, final ShortBuffer outSBuff);

    AudioRemixer DOWNMIX = new AudioRemixer() {
        private static final int SIGNED_SHORT_LIMIT = 32768;
        private static final int UNSIGNED_SHORT_MAX = 65535;

        @Override
        public void remix(final ShortBuffer inSBuff, final ShortBuffer outSBuff) {
            // Down-mix stereo to mono
            // Viktor Toth's algorithm -
            // See: http://www.vttoth.com/CMS/index.php/technical-notes/68
            //      http://stackoverflow.com/a/25102339
            final int inRemaining = inSBuff.remaining() / 2;
            final int outSpace = outSBuff.remaining();

            final int samplesToBeProcessed = Math.min(inRemaining, outSpace);
            for (int i = 0; i < samplesToBeProcessed; ++i) {
                // Convert to unsigned
                final int a = inSBuff.get() + SIGNED_SHORT_LIMIT;
                final int b = inSBuff.get() + SIGNED_SHORT_LIMIT;
                int m;
                // Pick the equation
                if ((a < SIGNED_SHORT_LIMIT) || (b < SIGNED_SHORT_LIMIT)) {
                    // Viktor's first equation when both sources are "quiet"
                    // (i.e. less than middle of the dynamic range)
                    m = a * b / SIGNED_SHORT_LIMIT;
                } else {
                    // Viktor's second equation when one or both sources are loud
                    m = 2 * (a + b) - (a * b) / SIGNED_SHORT_LIMIT - UNSIGNED_SHORT_MAX;
                }
                // Convert output back to signed short
                if (m == UNSIGNED_SHORT_MAX + 1) m = UNSIGNED_SHORT_MAX;
                outSBuff.put((short) (m - SIGNED_SHORT_LIMIT));
            }
        }
    };

    AudioRemixer UPMIX = new AudioRemixer() {
        @Override
        public void remix(final ShortBuffer inSBuff, final ShortBuffer outSBuff) {
            // Up-mix mono to stereo
            final int inRemaining = inSBuff.remaining();
            final int outSpace = outSBuff.remaining() / 2;

            final int samplesToBeProcessed = Math.min(inRemaining, outSpace);
            for (int i = 0; i < samplesToBeProcessed; ++i) {
                final short inSample = inSBuff.get();
                outSBuff.put(inSample);
                outSBuff.put(inSample);
            }
        }
    };

    AudioRemixer PASSTHROUGH = new AudioRemixer() {
        @Override
        public void remix(final ShortBuffer inSBuff, final ShortBuffer outSBuff) {
            // Passthrough
            outSBuff.put(inSBuff);
        }
    };
}
