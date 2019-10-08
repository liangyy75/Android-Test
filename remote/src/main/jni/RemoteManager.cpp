#include <unistd.h>
#include "RemoteManager.hpp"
#include "Json.hpp"

void *remote::wsPoll(void *data) {
    auto *remoteClient = (RemoteClient *) data;
    char buf[100]; // NOLINT(cppcoreguidelines-avoid-magic-numbers)
    L_T_D(TAG_RM_CPP, "begin wsPoll with uid: %ld, guid: %s, serverUrl: %s", remoteClient->uid, remoteClient->guid,
          remoteClient->serverUrl);
    sprintf(buf, R"({"UserId":%ld,"Guid":"%s"})", remoteClient->uid, remoteClient->guid);
    // 将webSocket的创建搬到这里，如果在_startNewClient中进行会发送段错误
    remoteClient->webSocket = ws::WebSocket::from_url(remoteClient->serverUrl);
    if (remoteClient->webSocket != nullptr) {
        std::map<char *, remote::MsgHandler *, remote::ComByStr> handlers = RemoteManager::getInstance()->getMsgHandlers();
        try {
            remoteClient->webSocket->setCallable(&handleMsg);
            remoteClient->webSocket->send(buf);
            ws::WebSocket::ConnectionState state = remoteClient->webSocket->getReadyState();
            L_T_D(TAG_RM_CPP, "continue wsPoll with uid: %ld, guid: %s, serverUrl: %s and state: %d", remoteClient->uid,
                  remoteClient->guid, remoteClient->serverUrl, state);
            for (auto it = handlers.begin(); it != handlers.end(); it++) {
                it->second->onOpen(remoteClient);
            }
            while (state != ws::WebSocket::CLOSED && state != ws::WebSocket::CLOSING) {
                L_T_D(TAG_RM_CPP, "wsPoll continue, state is %d", state);
                remoteClient->webSocket->pollWithHandle(remoteClient->timeout);
                state = remoteClient->webSocket->getReadyState();
            }
            L_T_D(TAG_RM_CPP, "begin close");
            for (auto it = handlers.begin(); it != handlers.end(); it++) {
                it->second->onClose();
            }
        } catch (std::exception &ex) {
            L_T_E(TAG_RM_CPP, "fatal error: %s", ex.what());
            for (auto it = handlers.begin(); it != handlers.end(); it++) {
                it->second->onFatalError(ex);
            }
        }
    } else {
        L_T_D(TAG_RM_CPP, "create connection failed!!! with uid: %ld, guid: %s, serverUrl: %s", remoteClient->uid, remoteClient->guid,
              remoteClient->serverUrl);
    }
    L_T_D(TAG_RM_CPP, "finish wsPoll with uid: %ld, guid: %s, serverUrl: %s", remoteClient->uid, remoteClient->guid,
          remoteClient->serverUrl);
    RemoteManager::getInstance()->stopClient(remoteClient);
    return nullptr;
}

pthread_mutex_t remote::RemoteManager::mutex;  // 初始化锁
int result = remote::RemoteManager::initMutex();
remote::RemoteManager *remote::RemoteManager::instance = nullptr;  // 初始化单例
remote::RemoteManager *remote::RemoteManager::getInstance() {  // 懒汉式线程安全单例模式
    if (instance == nullptr) {
        pthread_mutex_lock(&mutex);
        if (instance == nullptr) {
            instance = new RemoteManager();
        }
        pthread_mutex_unlock(&mutex);
    }
    return instance;
}

// TODO: 支持协议替换，现在只支持Json
void remote::handleMsg(ws::WebSocket &webSocket, const std::string &message) {
    L_T_D(TAG_RM_CPP, "handleMsg: msg(%s)", message.c_str());
    std::map<char *, remote::MsgHandler *, remote::ComByStr> handlers = RemoteManager::getInstance()->getMsgHandlers();
    try {
        std::string err;
        const auto jsonObj = json11::Json::parse(message, err);
        json11::Json value = jsonObj["type"];
        L_T_D(TAG_RM_CPP, "jsonObj's string format: %s", jsonObj.dump().c_str());
        if (value.type() != json11::Json::NUL) {
            const char *trueType = value.string_value().c_str();
            L_T_D(TAG_RM_CPP, "parse json successfully, and type is '%s'", trueType);
            for (auto it = handlers.begin(); it != handlers.end(); it++) {
                if (strcmp(it->second->getReqType(), trueType) == 0) {
                    json11::Json data = jsonObj["data"];
                    it->second->handleMsg(webSocket, message, data);
                }
            }
        } else {
            L_T_D(TAG_RM_CPP, "exception occurred while parse json: %s, or the jsonString have no 'type' key", err.c_str());
        }
    } catch (std::exception &ex) {
        L_T_E(TAG_RM_CPP, "exception: %s", ex.what());
        for (auto it = handlers.begin(); it != handlers.end(); it++) {
            it->second->onError(webSocket, ex);
        }
    }
}
