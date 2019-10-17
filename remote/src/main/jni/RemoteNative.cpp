#include "com_liang_example_nativeremote_AbsRemoteMsgHandler.h"
#include "com_liang_example_nativeremote_RemoteManager.h"
#include "Utils.hpp"
#include "Json.hpp"
#include "WSClient.hpp"
#include "RemoteManager.hpp"
#include "ShellMsgHandler.hpp"
#include "HandlerJniHelper.hpp"

#ifdef __cplusplus
extern "C" {
#endif

// #define TAG_RN "RemoteNativeCpp"
constexpr char TAG_RN[] = "RemoteNativeCpp";
char ECHO_REQ[] = "echoReq";
char ECHO_RES[] = "echoRes";

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

remote::RemoteClient *makeRemoteClient(JNIEnv *jniEnv, jlong uid, jstring guid, jstring serverUrl) {
    char guidBuf[JTC_BUF_LEN];
    char serverUrlBuf[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, guid, guidBuf);
    jStringToCharArray(jniEnv, serverUrl, serverUrlBuf);
    auto *remoteClient = new remote::RemoteClient((long) uid, guidBuf, serverUrlBuf);
    return remoteClient;
}

JNIEXPORT void JNICALL Java_com_liang_example_nativeremote_RemoteManager_init
        (JNIEnv *jniEnv, jobject obj, jboolean useShell, jboolean useEcho) {
    if (useEcho) {
        remote::RemoteManager::getInstance()->addMsgHandler(new EchoMsgHandler());
    }
    if (useShell) {
        remote::RemoteManager::getInstance()->addMsgHandler(new shell::ShellMsgHandler());
    }
}

JNIEXPORT void JNICALL Java_com_liang_example_nativeremote_RemoteManager_startRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    remote::RemoteManager::getInstance()->startNewClient(makeRemoteClient(jniEnv, uid, guid, serverUrl));
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_stopRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    return boolToJBoolean(remote::RemoteManager::getInstance()->stopClient(makeRemoteClient(jniEnv, uid, guid, serverUrl)));
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_hasRemoteClient
        (JNIEnv *jniEnv, jobject obj, jlong uid, jstring guid, jstring serverUrl) {
    return boolToJBoolean(remote::RemoteManager::getInstance()->hasClient(makeRemoteClient(jniEnv, uid, guid, serverUrl)));
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_hasRemoteClientByUrl
        (JNIEnv * jniEnv, jobject thisObj, jstring serverUrl) {
    char serverUrlBuf[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, serverUrl, serverUrlBuf);
    return boolToJBoolean(remote::RemoteManager::getInstance()->hasClient(serverUrlBuf));
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_addRemoteMsgHandler
        (JNIEnv *jniEnv, jobject obj, jobject msgHandler) {
    return boolToJBoolean(remote::RemoteManager::getInstance()->addMsgHandler(remote::jObjectToJavaMsgHandler(jniEnv, msgHandler)));
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_removeRemoteMsgHandler
        (JNIEnv *jniEnv, jobject obj, jstring reqType) {
    char reqTypeBuf[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, reqType, reqTypeBuf);
    return boolToJBoolean(remote::RemoteManager::getInstance()->removeMsgHandler(reqTypeBuf));
}

JNIEXPORT jboolean JNICALL Java_com_liang_example_nativeremote_RemoteManager_hasRemoteMsgHandler
        (JNIEnv *jniEnv, jobject obj, jstring reqType) {
    char reqTypeBuf[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, reqType, reqTypeBuf);
    return boolToJBoolean(remote::RemoteManager::getInstance()->hasMsgHandler(reqTypeBuf));
}

JNIEXPORT void JNICALL Java_com_liang_example_nativeremote_AbsRemoteMsgHandler_sendObj
        (JNIEnv *jniEnv, jobject obj, jstring serverUrl, jobject resObj, jstring resClass, jstring resType) {
    char serverUrlBuf[JTC_BUF_LEN];
    char resClassBuf[JTC_BUF_LEN];
    char resTypeBuf[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, serverUrl, serverUrlBuf);
    jStringToCharArray(jniEnv, resClass, resClassBuf);
    jStringToCharArray(jniEnv, resType, resTypeBuf);
    replace(resClassBuf, '.', '/');
    json11::Json result = json11::Json::object{
            {"type", resTypeBuf},
            {"data", jObjectToJson11(jniEnv, resObj, resClassBuf)}
    };
    std::string strMsg = result.dump();
    remote::RemoteManager::getInstance()->sendToServerUrl(serverUrlBuf, strMsg);
}

JNIEXPORT void JNICALL Java_com_liang_example_nativeremote_AbsRemoteMsgHandler_sendMsg
        (JNIEnv *jniEnv, jobject obj, jstring serverUrl, jstring msg, jstring resType) {
    char serverUrlBuf[JTC_BUF_LEN];
    char msgBuf[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, serverUrl, serverUrlBuf);
    jStringToCharArray(jniEnv, msg, msgBuf);
    std::string strMsg = std::string(serverUrlBuf);
    remote::RemoteManager::getInstance()->sendToServerUrl(serverUrlBuf, strMsg);
}

JNIEXPORT jobject JNICALL Java_com_liang_example_nativeremote_RemoteManager_getObjectFromJni
        (JNIEnv *jniEnv, jobject thisObj, jstring className, jstring jsonStr) {
    char jsonStrBuf[JTC_BUF_LEN * 10];
    char classNameBuf[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, jsonStr, jsonStrBuf);
    jStringToCharArray(jniEnv, className, classNameBuf);
    replace(classNameBuf, '.', '/');
    std::string err;
    json11::Json json = json11::Json::parse(jsonStrBuf, err);
    // return json11ToJObject(jniEnv, json, classNameBuf);

    auto *classFieldsMap = new std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr>();
    auto *targetClasses = new std::map<const char *, jclass, ComByStr>();
    auto globalFieldClass = (jclass) jniEnv->NewGlobalRef(jniEnv->FindClass("java/lang/reflect/Field"));
    auto globalClassClass = (jclass) jniEnv->NewGlobalRef(jniEnv->FindClass("java/lang/Class"));
    getFieldsFromJClass(jniEnv, classFieldsMap, classNameBuf, targetClasses, globalFieldClass, globalClassClass);
    jobject result = json11ToJObject(jniEnv, json, classFieldsMap, targetClasses, classNameBuf, globalFieldClass,
                                     globalClassClass);
    // remote::JavaMsgHandler::release(jniEnv, reqClassFieldsMap, reqTargetClasses);
    // jniEnv->DeleteGlobalRef(globalFieldClass);
    // jniEnv->DeleteGlobalRef(globalClassClass);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_liang_example_nativeremote_RemoteManager_getStringFromJni
        (JNIEnv *jniEnv, jobject thisObj, jobject transformObj, jstring className) {
    char classNameBuf[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, className, classNameBuf);
    // json11::Json json = jObjectToJson11(jniEnv, transformObj, classNameBuf);

    auto *classFieldsMap = new std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr>();
    auto *targetClasses = new std::map<const char *, jclass, ComByStr>();
    auto globalFieldClass = (jclass) jniEnv->NewGlobalRef(jniEnv->FindClass("java/lang/reflect/Field"));
    auto globalClassClass = (jclass) jniEnv->NewGlobalRef(jniEnv->FindClass("java/lang/Class"));
    getFieldsFromJClass(jniEnv, classFieldsMap, classNameBuf, targetClasses, globalFieldClass, globalClassClass);
    json11::Json json = jObjectToJson11(jniEnv, transformObj, classFieldsMap, classNameBuf, globalFieldClass, globalClassClass);
    // remote::JavaMsgHandler::release(jniEnv, reqClassFieldsMap, reqTargetClasses);
    // jniEnv->DeleteGlobalRef(globalFieldClass);
    // jniEnv->DeleteGlobalRef(globalClassClass);

    return jniEnv->NewStringUTF(json.dump().c_str());
}

#ifdef __cplusplus
}
#endif

// WSClient的多线程安全问题，可以使用封装在Utils.hpp里面 -- c++11的mutex
// RemoteManager的线程安全懒汉单例模式 -- https://zhuanlan.zhihu.com/p/37469260
// cJson / json11 -- [json11 c++ 用法](https://blog.csdn.net/yangzm/article/details/71552609)
// RemoteShell.hpp -- TODO: 多线程的Shell
// 扩展要在java层
// TODO: 多协议扩展 -- 不仅仅是 json
// TODO: posix c++ 替换 c++ 11
//     stack / vector / string / map -- 自己实现(其他还好，就是map会很难，当然如果不用红黑树当我没说！！！)
//     memory -- ? 四大智能指针如何替换？
//     initializer_list -- 只要改变 json11:Json 类以及其他 container 的初始化方法
//     mutex -- pthread_mutex_t

// 现有逻辑
// 1. 提供了 ws::WebSocket ，能够进行 c++ 层的 WebSocket 连接，主要接口有
//     1. poll: 不断循环从后台拉取消息并发送消息给后台
//     2. dispatch / dispatchBinary: 将拉取到的消息进行处理，需要传入处理函数
//         或者使用 setCallable / setByteCallable 这两个函数设置的处理函数，但这个时候需要调用 pollWithHandle
//         注意，不要同时 setCallable 和 setByteCallable ，两者只会调用其一，而且 setCallable 的优先级高于 setByteCallable
//     3. send / sendBinary / sendPing / sendPong / close
//     4. 基本使用
//     ```cpp
//     ws::WebSocket::pointer webSocket = WebSocket::from_url("ws://localhost:8126/foo");
//     webSocket->send("First message.");
//     webSocket->send("Second message.");
//     bool flag = false;
//     while (webSocket->getReadyState() != ws::WebSocket::CLOSED) {
//         ws->poll();
//         ws->dispatch(flag ? &handle_message1 : &handle_message2);
//     }
//     delete webSocket;
//     ```
//     ```cpp
//     ws::WebSocket::pointer webSocket = WebSocket::from_url("ws://localhost:8126/foo");
//     webSocket->send("First message.");
//     webSocket->send("Second message.");
//     webSocket->setCallable(&handle_message);
//     while (webSocket->getReadyState() != ws::WebSocket::CLOSED) {
//         ws->pollWithHandle();
//     }
//     delete webSocket;
//     ```

// 2. json11::Json
// 3. utils
// 4.