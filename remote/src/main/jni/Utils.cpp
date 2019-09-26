#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "REMOTE_JNI"
#define LOG_V(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__) // 定义LOGD类型
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__) // 定义LOGD类型
#define LOG_I(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__) // 定义LOGI类型
#define LOG_W(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__) // 定义LOGW类型
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__) // 定义LOGE类型
#define LOG_F(...) __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, __VA_ARGS__) // 定义LOGF类型
#endif
