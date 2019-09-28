#include "RemoteManager.hpp"

void *remote::wsPoll(void *data) {
    auto *remoteClient = (RemoteClient *) data;
    char buf[100];
    L_T_D(RM_TAG_CPP, "begin wsPoll with uid: %ld, guid: %s, serverUrl: %s", remoteClient->uid, remoteClient->guid,
          remoteClient->serverUrl);
    sprintf(buf, R"({"UserId":%ld,"Guid":"%s"})", remoteClient->uid, remoteClient->guid);
    // 将webSocket的创建搬到这里，如果在_startNewClient中进行会发送段错误
    remoteClient->webSocket = ws::WebSocket::from_url(remoteClient->serverUrl);
    if (remoteClient->webSocket != nullptr) {
        remoteClient->webSocket->setCallable(&handleMsg);
        remoteClient->webSocket->send(buf);
        ws::WebSocket::ConnectionState state = remoteClient->webSocket->getReadyState();
        L_T_D(RM_TAG_CPP, "continue wsPoll with uid: %ld, guid: %s, serverUrl: %s and state: %d", remoteClient->uid,
              remoteClient->guid, remoteClient->serverUrl, state);
        while (state != ws::WebSocket::CLOSED && state != ws::WebSocket::CLOSING) {
            remoteClient->webSocket->pollWithHandle(remoteClient->timeout);
            state = remoteClient->webSocket->getReadyState();
        }
    } else {
        L_T_D(RM_TAG_CPP, "create connection failed!!! with uid: %ld, guid: %s, serverUrl: %s", remoteClient->uid, remoteClient->guid,
              remoteClient->serverUrl);
        return nullptr;
    }
    L_T_D(RM_TAG_CPP, "finish wsPoll with uid: %ld, guid: %s, serverUrl: %s", remoteClient->uid, remoteClient->guid,
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

void remote::handleMsg(ws::WebSocket &webSocket, const std::string &message) {
    L_T_D(RM_TAG_CPP, "handleMsg: msg(%s)", message.c_str());
    std::map<char *, remote::MsgHandler *, remote::ComByStr> handlers = RemoteManager::getInstance()->getMsgHandlers();
    for (auto it = handlers.begin(); it != handlers.end(); it++) {
        // TODO: json 解析
        // if (strcmp(it->second->getReqType(), parseXxx()) == 0) {
        //     it->second.handleMsg(webSocket, message);
        // }
    }
}