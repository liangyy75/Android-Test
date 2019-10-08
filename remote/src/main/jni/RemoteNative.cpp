#include "com_liang_example_nativeremote_RemoteManager.h"
#include "Utils.hpp"
#include "Json.hpp"
#include "WSClient.hpp"
#include "RemoteManager.hpp"
#include "ShellMsgHandler.hpp"

#ifdef __cplusplus

extern "C" {
#endif

// #define TAG_RN "RemoteNativeCpp"
constexpr char TAG_RN[] = "RemoteNativeCpp";
char ECHO_REQ[] = "echoReq";
char ECHO_RES[] = "echoRes";

constexpr int JTC_BUF_LEN = 100;

// [json11 c++ 用法](https://blog.csdn.net/yangzm/article/details/71552609)
class EchoMsgHandler : public remote::MsgHandler {
public:
    EchoMsgHandler() : remote::MsgHandler(ECHO_REQ, ECHO_RES) {}

    void handleMsg(ws::WebSocket &webSocket, const std::string &msg, const json11::Json &data) override {
        L_T_D(TAG_RN, "received msg: %s, and data: %s", msg.c_str(), data.dump().c_str());
        json11::Json jsonObj = json11::Json::object{
                {"command", "msg from EchoMsgHandler: " + data["command"].dump()},
        };
        this->send(webSocket, jsonObj);
        // 这么简单的消息处理当然不用另起一个线程，但是如果是Shell/...等其他复杂的指令处理是需要另起线程的
    }
};

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_startRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    const char *guidStr = jniEnv->GetStringUTFChars(guid, nullptr);
    const char *serverUrlStr = jniEnv->GetStringUTFChars(serverUrl, nullptr);
    char guidBuf[JTC_BUF_LEN];
    char serverUrlBuf[JTC_BUF_LEN];
    strcpy(guidBuf, guidStr);
    strcpy(serverUrlBuf, serverUrlStr);
    remote::RemoteManager::getInstance()->addMsgHandler(new EchoMsgHandler());
    remote::RemoteManager::getInstance()->addMsgHandler(new shell::ShellMsgHandler());
    remote::RemoteManager::getInstance()->startNewClient((long) uid, guidBuf, serverUrlBuf);
    // remote::RemoteManager::getInstance()->startNewClient(50042533l, "0e74af97aa48135d0c5528db29dbb6fe", "ws://157");
    jniEnv->ReleaseStringUTFChars(guid, guidStr);
    jniEnv->ReleaseStringUTFChars(serverUrl, serverUrlStr);
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_stopRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    const char *guidStr = jniEnv->GetStringUTFChars(guid, nullptr);
    const char *serverUrlStr = jniEnv->GetStringUTFChars(serverUrl, nullptr);
    char guidBuf[JTC_BUF_LEN];
    char serverUrlBuf[JTC_BUF_LEN];
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
// cJson / json11 -- [json11 c++ 用法](https://blog.csdn.net/yangzm/article/details/71552609)
// TODO: RemoteShell.hpp
