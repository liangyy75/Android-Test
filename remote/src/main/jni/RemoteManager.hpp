#ifndef _Included_RemoteManager
#define _Included_RemoteManager

#include "WSClient.hpp"
#include "Json.hpp"
#include <map>
#include <pthread.h>
#include <string.h>
#include <exception>

#define TAG_RM_HPP "RMHpp"
#define TAG_RM_CPP "RMCpp"

namespace remote {
    struct RemoteClient {
        int timeout;
        long uid;
        char *guid;
        char *serverUrl;
        ws::WebSocket::pointer webSocket;

        RemoteClient(long uid, char *guid, char *serverUrl) : timeout(0), uid(uid), webSocket(nullptr) {
            this->guid = new char[strlen(guid) + 1];
            strcpy(this->guid, guid);
            this->serverUrl = new char[strlen(serverUrl) + 1];
            strcpy(this->serverUrl, serverUrl);
        }

        ~RemoteClient() {
            delete this->guid;
            delete this->serverUrl;
            if (this->webSocket != nullptr) {
                delete this->webSocket;
            }
        }
    };

    // TODO: 将RemoteManager规范化，即可扩展。。。
    // TODO: template <class Callable> -> msgHandler
    class MsgHandler {
    protected:
        char *reqType;
        char *resType;

        MsgHandler(char *reqType, char *resType) {
            this->reqType = new char[strlen(reqType) + 1];
            strcpy(this->reqType, reqType);
            this->resType = new char[strlen(resType) + 1];
            strcpy(this->resType, resType);
        }  // 构造函数
    public:
        char *getReqType() {
            return reqType;
        }  // getter
        char *getResType() {
            return resType;
        }  // getter
        virtual ~MsgHandler() {
            delete this->reqType;
            delete this->resType;
        }

        void send(ws::WebSocket &webSocket, json11::Json &data) {
            json11::Json jsonObj = json11::Json::object{
                    {"type", this->resType},
                    {"data", data},
            };
            webSocket.send(jsonObj.dump());
        }  // 应该用这个方法来发送

        virtual void onOpen(RemoteClient *remoteClient) {};  //
        virtual void handleMsg(ws::WebSocket &webSocket, const std::string &msg, const json11::Json &data) = 0;  // 必须实现的方法
        virtual void onError(ws::WebSocket &webSocket, std::exception &ex) {};  //
        virtual void onFatalError(std::exception &ex) {};  //
        virtual void onClose() {};
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
        RemoteManager &operator=(const RemoteManager &) { return *this; }  // 为了单例模式
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
                L_T_D(TAG_RM_HPP, "have created remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                      remoteClient->guid, remoteClient->serverUrl);
                return false;
            }
            pthread_t newThread;
            if (pthread_create(&newThread, nullptr, wsPoll, remoteClient) != 0) {
                L_T_D(TAG_RM_HPP, "failed to create remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                      remoteClient->guid, remoteClient->serverUrl);
                return false;
            }
            clients.insert(std::pair<RemoteClient *, pthread_t>(remoteClient, newThread));
            L_T_D(TAG_RM_HPP, "successfully create remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                  remoteClient->guid, remoteClient->serverUrl);
            return true;
        }

        bool stopClient(RemoteClient *remoteClient) {
            auto it = clients.find(remoteClient);
            if (it == clients.end()) {
                L_T_D(TAG_RM_HPP, "no such remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
                      remoteClient->guid, remoteClient->serverUrl);
                return false;
            }
            if (it->first->webSocket != nullptr) {
                it->first->webSocket->close();
            }
            if (remoteClient->webSocket != nullptr) {
                remoteClient->webSocket->close();
            }
            clients.erase(it);
            L_T_D(TAG_RM_HPP, "stop remote client: uid(%ld), guid(%s), serverUrl(%s)", remoteClient->uid,
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
            L_T_D(TAG_RM_HPP, "addMsgHandler: reqType(%s)", reqType);
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
