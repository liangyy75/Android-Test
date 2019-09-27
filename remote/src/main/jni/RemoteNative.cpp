#include <istream>
#include "com_liang_example_nativeremote_RemoteManager.h"
#include "Utils.hpp"
#include "WSClient.hpp"
#include "RemoteManager.hpp"

#ifdef __cplusplus
extern "C" {
#endif

#include <map>
#include <string>

std::map<std::string, ws::WebSocket> clientMap = std::map();

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_startRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    ;
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_stopRemoteClient
        (JNIEnv *jniEnv, jobject obj, jstring serverUrl) {
    return JNI_FALSE;
}
#ifdef __cplusplus
}
#endif