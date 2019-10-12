#include "com_liang_example_nativeremote_AbsRemoteMsgHandler.h"
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
    // const char *command = "/system/bin/sh -c ps && cd /sdcard && ls && ls >> smg.txt";
    // FILE *fp;
    // char buffer[80];
    // fp = popen(command, "r");
    // L_T_D(TAG_SMH, "testCommandResult: begin");
    // while (fgets(buffer, sizeof(buffer), fp)) {
    //     L_T_D(TAG_SMH, "testCommandResult: %s", buffer);
    // }
    // pclose(fp);
    // L_T_D(TAG_SMH, "testCommandResult: end");
    //
    // int fd[2];
    // pid_t pid;
    // int status;
    // int n, count;
    // if (pipe(fd) < 0 || (pid = fork()) < 0) {
    //     status = -1;
    //     L_T_E(TAG_SMH, "testCommandResult: pipe error or fork error");
    // } else if (pid == 0) {
    //     close(fd[0]);
    //     if (fd[1] != STDOUT_FILENO) {
    //         if (dup2(fd[1], STDOUT_FILENO) != STDOUT_FILENO) {
    //             L_T_E(TAG_SMH, "testCommandResult: executing error1");
    //         }
    //         close(fd[1]);
    //     }
    //     L_T_D(TAG_SMH, "testCommandResult: executing command");
    //     execl("/system/bin/sh", "sh", "-c", "cd /sdcard && echo \"msg\" >> smg.txt", (char *) 0);
    // } else {
    //     close(fd[1]);
    //     if (waitpid(pid, nullptr, 0) > 0) {
    //         L_T_E(TAG_SMH, "testCommandResult: executing error");
    //     }
    //     count = 0;
    //     char buf[80 * 1000];
    //     while ((n = read(fd[0], buf + count, 80 * 1000)) > 0 && count < 80 * 1000) {
    //         count += n;
    //     }
    //     L_T_D(TAG_SMH, "testCommandResult: result -- %s", buf);
    //     close(fd[0]);
    // }

    const char *guidStr = jniEnv->GetStringUTFChars(guid, nullptr);
    const char *serverUrlStr = jniEnv->GetStringUTFChars(serverUrl, nullptr);
    char guidBuf[JTC_BUF_LEN];
    char serverUrlBuf[JTC_BUF_LEN];
    strcpy(guidBuf, guidStr);
    strcpy(serverUrlBuf, serverUrlStr);
    remote::RemoteManager::getInstance()->addMsgHandler(new EchoMsgHandler());
    remote::RemoteManager::getInstance()->addMsgHandler(new shell::ShellMsgHandler());
    remote::RemoteManager::getInstance()->startNewClient((long) uid, guidBuf, serverUrlBuf);
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

JNIEXPORT void JNICALL Java_com_liang_example_nativeremote_AbsRemoteMsgHandler_send__Ljava_lang_Object_2
        (JNIEnv *, jobject, jobject) {
    // TODO:
}

JNIEXPORT void JNICALL Java_com_liang_example_nativeremote_AbsRemoteMsgHandler_send__Ljava_lang_String_2
        (JNIEnv *, jobject, jstring) {
    // TODO:
}

#ifdef __cplusplus
}
#endif

// WSClient的多线程安全问题，可以使用封装在Utils.hpp里面 -- c++11的mutex
// RemoteManager的线程安全懒汉单例模式 -- https://zhuanlan.zhihu.com/p/37469260
// cJson / json11 -- [json11 c++ 用法](https://blog.csdn.net/yangzm/article/details/71552609)
// RemoteShell.hpp -- TODO: 多线程的Shell
// TODO: 扩展要在java层
// TODO: 多协议扩展 -- 不仅仅是 json
// TODO: posix c++ 替换 c++ 11
//     stack / vector / string / map -- 自己实现(其他还好，就是map会很难，当然如果不用红黑树当我没说！！！)
//     memory -- ? 四大智能指针如何替换？
//     initializer_list -- 只要改变 json11:Json 类以及其他 container 的初始化方法
//     mutex -- pthread_mutex_t
