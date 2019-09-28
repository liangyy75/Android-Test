#include "com_liang_example_nativeremote_RemoteManager.h"
#include "Utils.hpp"
#include "WSClient.hpp"
#include "RemoteManager.hpp"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_startRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    const char *guidStr = jniEnv->GetStringUTFChars(guid, JNI_FALSE);
    const char *serverUrlStr = jniEnv->GetStringUTFChars(serverUrl, JNI_FALSE);
    char guidBuf[100];
    char serverUrlBuf[100];
    strcpy(guidBuf, guidStr);
    strcpy(serverUrlBuf, serverUrlStr);
    // remote::RemoteManager::getInstance()->startNewClient((long) uid, guidBuf, serverUrlBuf);
    remote::RemoteManager::getInstance()->startNewClient(50042533l, "0e74af97aa48135d0c5528db29dbb6fe", "ws://172.21.87.227:9001");
    jniEnv->ReleaseStringUTFChars(guid, guidStr);
    jniEnv->ReleaseStringUTFChars(serverUrl, serverUrlStr);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_stopRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    const char *guidStr = jniEnv->GetStringUTFChars(guid, nullptr);
    const char *serverUrlStr = jniEnv->GetStringUTFChars(serverUrl, nullptr);
    char guidBuf[100];
    char serverUrlBuf[100];
    strcpy(guidBuf, guidStr);
    strcpy(serverUrlBuf, serverUrlStr);
    remote::RemoteManager::getInstance()->stopClient((long) uid, guidBuf, serverUrlBuf);
    jniEnv->ReleaseStringUTFChars(guid, guidStr);
    jniEnv->ReleaseStringUTFChars(serverUrl, serverUrlStr);
    return JNI_TRUE;
}
#ifdef __cplusplus
}
#endif

// TODO: WSClient的多线程安全问题，可以使用封装在Utils.hpp里面
// RemoteManager的线程安全懒汉单例模式 -- https://zhuanlan.zhihu.com/p/37469260
// TODO: cJson / json11
// TODO: RemoteShell.hpp / RemoteShell.cpp
