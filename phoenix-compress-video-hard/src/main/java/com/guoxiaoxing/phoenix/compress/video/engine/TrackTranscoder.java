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
package com.guoxiaoxing.phoenix.compress.video.engine;

import android.media.MediaFormat;

public interface TrackTranscoder {

    void setup();

    /**
     * Get actual MediaFormat which is used to write to muxer.
     * To determine you should call {@link #stepPipeline()} several times.
     *
     * @return Actual output format determined by coder, or {@code null} if not yet determined.
     */
    MediaFormat getDeterminedFormat();

    /**
     * Step pipeline if output is available in any step of it.
     * It assumes muxer has been started, so you should call muxer.start() first.
     *
     * @return true if data moved in pipeline.
     */
    boolean stepPipeline();

    /**
     * Get presentation time of last sample written to muxer.
     *
     * @return Presentation time in micro-second. Return value is undefined if finished writing.
     */
    long getWrittenPresentationTimeUs();

    boolean isFinished();

    void release();
}
