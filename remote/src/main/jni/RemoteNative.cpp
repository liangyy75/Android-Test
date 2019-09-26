#include "com_liang_example_nativeremote_RemoteManager.h"
#include "WebSocket.h"
#include <Utils.cpp>

#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_startRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_stopRemoteClient
        (JNIEnv *jniEnv, jobject obj, jstring serverUrl) {
    return JNI_FALSE;
}
#ifdef __cplusplus
}