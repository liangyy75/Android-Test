#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "REMOTE_JNI"
#define LOG_V(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)  // 定义LOG_V类型
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)  // 定义LOG_D类型
#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)  // 定义LOG_I类型
#define LOG_W(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)  // 定义LOG_W类型
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)  // 定义LOG_E类型
#define LOG_F(...) __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, __VA_ARGS__)  // 定义LOG_F类型

#define L_T_V(TAG, ...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)  // 定义L_T_V类型
#define L_T_D(TAG, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)  // 定义L_T_D类型
#define L_T_I(TAG, ...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)  // 定义L_T_I类型
#define L_T_W(TAG, ...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)  // 定义L_T_W类型
#define L_T_E(TAG, ...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)  // 定义L_T_E类型
#define L_T_F(TAG, ...) __android_log_print(ANDROID_LOG_FATAL, TAG, __VA_ARGS__)  // 定义L_T_F类型
#endif
