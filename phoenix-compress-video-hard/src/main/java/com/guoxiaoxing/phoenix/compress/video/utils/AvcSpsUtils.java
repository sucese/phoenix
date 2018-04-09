/*
 * Copyright (C) 2016 Yuya Tanaka
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
package com.guoxiaoxing.phoenix.compress.video.utils;

import java.nio.ByteBuffer;

public class AvcSpsUtils {
    public static byte getProfileIdc(ByteBuffer spsBuffer) {
        // Refer: http://www.cardinalpeak.com/blog/the-h-264-sequence-parameter-set/
        // First byte after NAL.
        return spsBuffer.get(0);
    }
}
