/**
 * Created by jianxi on 2017/6/4.
 * https://github.com/mabeijianxi
 * mabeijianxi@gmail.com
 */
#ifndef JIANXIFFMPEG_FFMPEG_RUN_H
#define JIANXIFFMPEG_FFMPEG_RUN_H

#include <jni.h>

JNIEXPORT jint JNICALL
Java_com_guoxiaoxing_phoenix_compress_video_soft_interfaces_FFmpegBridge_jxCMDRun(JNIEnv *env, jclass type,
                                                                       jobjectArray commands);

void log_callback(void* ptr, int level, const char* fmt,
                            va_list vl);

JNIEXPORT void JNICALL
Java_com_guoxiaoxing_phoenix_compress_video_soft_interfaces_FFmpegBridge_initJXFFmpeg(JNIEnv *env, jclass type,
        jboolean debug,
jstring logUrl_);

int ffmpeg_cmd_run(int argc, char **argv);
#endif //JIANXIFFMPEG_FFMPEG_RUN_H
