#ifndef _Included_RemoteManager
#define _Included_RemoteManager

#include "WSClient.hpp"
#include <map>
#include <pthread.h>
#include <string.h>

#define RM_TAG_HPP "RMHpp"
#define RM_TAG_CPP "RMCpp"

namespace remote {
    struct RemoteClient {
        int timeout;
        long uid;
        char *guid;
        char *serverUrl;
        ws::WebSocket::pointer webSocket;

        RemoteClient(long uid, char *guid, char *serverUrl) :
                timeout(0), uid(uid), guid(guid), serverUrl(serverUrl), webSocket(nullptr) {}
    };

    // TODO: 将RemoteManager规范化，即可扩展。。。
    // TODO: template <class Callable> -> msgHandler
    class MsgHandler {
    private:
        char *reqType;
        char *resType;
    protected:
        virtual void handleMsg(ws::WebSocket &webSocket, const std::string &msg) = 0;  // 必须实现的方法
        MsgHandler(char *reqType, char *resType) : reqType(reqType), resType(resType) {}  // 构造函数
    public:
        char *getReqType() {
            return reqType;
        }  // getter
        char *getResType() {
            return resType;
        }  // getter
    };

    struct ComByRC {
        bool operator()(const RemoteClient *a, const RemoteClient *b) const {
            return a->uid < b->uid || strcmp(a->guid, b->guid) < 0 || strcmp(a->serverUrl, b->serverUrl) < 0;
        }
    };

    struct ComByStr {
        bool operator()(const char *a, const char *b) const {
            return strcmp(a, b) < 0;
        }
    };

    void *wsPoll(void *data);

    void handleMsg(ws::WebSocket &webSocket, const std::string &message);

    class RemoteManager {
    private:
        static RemoteManager *instance;

        RemoteManager() {};  // 为了单例模式
        RemoteManager(const RemoteManager &other) {}  // 为了单例模式
        RemoteManager &operator=(const RemoteManager &) {}  // 为了单例模式
        ~RemoteManager() {
            for (auto it = clients.begin(); it != clients.end();) {
                delete it->first;
                clients.erase(it++);
            }
            for (auto it = handlers.begin(); it != handlers.end();) {
                delete it->second;
                handlers.erase(it++);
            }
            pthread_mutex_destroy(&mutex);
        } // destroy lock and release
    protected:
        std::map<RemoteClient *, pthread_t, ComByRC> clients;  // clients
        std::map<char *, MsgHandler *, ComByStr> handlers;  // handlers

        // ws::WebSocket::pointer _startNewClient(remote::RemoteClient clientInfo) {
        //     char buf[100];
        //     sprintf(buf, R"({"UserId":%ld,"Guid":":%s)", clientInfo.uid, clientInfo.guid);
        //     ws::WebSocket::pointer webSocket = ws::WebSocket::from_url(clientInfo.serverUrl, buf);
        //     if (webSocket != nullptr) {
        //         webSocket->setCallable(&handleMsg);
        //     }
        //     return webSocket;
        // };  // 启动
        // void _stopClient(remote::RemoteClient clientInfo) {
        //     // 这里好像不需要做什么
        // };  // 销毁
    public:
        static int initMutex() {
            return pthread_mutex_init(&mutex, nullptr);
        }  // init lock
        static pthread_mutex_t mutex;  // 锁
        static RemoteManager *getInstance();// 单例模式

        bool startNewClient(long uid, char *guid, char *serverUrl) {
            return startNewClient(new RemoteClient(uid, guid, serverUrl));
        }  // 开启新Client
        bool stopClient(long uid, char *guid, char *serverUrl) {
            return stopClient(new RemoteClient(uid, guid, serverUrl));
        }  // 销毁新Client

        bool startNewClient(RemoteClient *remoteClient) {
            if (clients.find(remoteClient) != clients.end()) {
                L_T_D(RM_TAG_HPP, "have created remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                      remoteClient->guid, remoteClient->serverUrl);
                return false;
            }
            // ws::WebSocket::pointer webSocket = _startNewClient(clientInfo);
            // clientInfo.webSocket = webSocket;
            pthread_t newThread;
            if (pthread_create(&newThread, nullptr, wsPoll, remoteClient) != 0) {
                L_T_D(RM_TAG_HPP, "failed to create remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                      remoteClient->guid, remoteClient->serverUrl);
                return false;
            }
            clients.insert(std::pair<RemoteClient *, pthread_t>(remoteClient, newThread));
            L_T_D(RM_TAG_HPP, "successfully create remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                  remoteClient->guid, remoteClient->serverUrl);
            return true;
        }

        bool stopClient(RemoteClient *remoteClient) {
            auto it = clients.find(remoteClient);
            if (it == clients.end()) {
                L_T_D(RM_TAG_HPP, "no such remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                      remoteClient->guid, remoteClient->serverUrl);
                return false;
            }
            it->first->webSocket->close();
            // _stopClient(it->first);
            clients.erase(it);
            L_T_D(RM_TAG_HPP, "stop remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                  remoteClient->guid, remoteClient->serverUrl);
            return true;
        }

        std::map<RemoteClient *, pthread_t, ComByRC> getClients() {
            return clients;
        }

        bool addMsgHandler(MsgHandler *msgHandler) {
            char *reqType = msgHandler->getReqType();
            if (handlers.find(reqType) == handlers.end()) {
                return false;
            }
            handlers.insert(std::pair<char *, MsgHandler *>(reqType, msgHandler));
            L_T_D(RM_TAG_HPP, "addMsgHandler: reqType(%s)", reqType);
            return true;
        }  // 添加消息处理类
        bool removeMsgHandler(MsgHandler *msgHandler) {
            return removeMsgHandler(msgHandler->getReqType());
        }  // 删除消息处理类
        bool removeMsgHandler(char *reqType) {
            auto it = handlers.find(reqType);
            if (it == handlers.end()) {
                return false;
            }
            handlers.erase(it);
            return true;
        }  // 删除消息处理类
        std::map<char *, MsgHandler *, ComByStr> getMsgHandlers() {
            return handlers;
        }  // 获取所有的消息处理类
        MsgHandler *getMsgHandler(char *reqType) {
            return handlers[reqType];
        }  // 获取单个消息处理类
    };
}

#endif  // _Included_RemoteManager
