#ifndef _Included_RemoteManager
#define _Included_RemoteManager

#include "WSClient.hpp"
#include <map>
#include <pthread.h>
#include <string.h>

namespace remote {
    struct RemoteClient {
        int timeout;
        long uid;
        char *guid;
        char *serverUrl;
        ws::WebSocket *webSocket;

        RemoteClient(long uid, char *guid, char *serverUrl) : uid(uid), guid(guid), serverUrl(serverUrl) {}
    };

    struct ComByRC {
        bool operator()(const RemoteClient &a, const RemoteClient &b) const {
            return a.uid != b.uid && strcmp(a.guid, b.guid) != 0 && strcmp(a.serverUrl, b.serverUrl) != 0;
        }
    };

    void *wsPoll(void *data) {
        RemoteClient *remoteClient = (RemoteClient *) data;
        ws::WebSocket::ConnectionState state = remoteClient->webSocket->getReadyState();
        while (state != ws::WebSocket::CLOSED && state != ws::WebSocket::CLOSING) {
            remoteClient->webSocket->pollWithHandle(remoteClient->timeout);
            state = remoteClient->webSocket->getReadyState();
        }
        return nullptr;
    }

    class RemoteManager {
    protected:
        std::map<RemoteClient, pthread_t, ComByRC> clients;

        virtual ws::WebSocket _startNewClient(RemoteClient clientInfo) = 0;  // 启动
        virtual ws::WebSocket _stopClient(RemoteClient clientInfo) = 0;  // 销毁
    public:
        bool startNewClient(long uid, char *guid, char *serverUrl) {
            return startNewClient(RemoteClient(uid, guid, serverUrl));
        }

        bool stopClient(long uid, char *guid, char *serverUrl) {
            return stopClient(RemoteClient(uid, guid, serverUrl));
        }

        bool startNewClient(RemoteClient clientInfo) {
            if (clients.find(clientInfo) == clients.end()) {
                return false;
            }
            ws::WebSocket webSocket = _startNewClient(clientInfo);
            clientInfo.webSocket = &webSocket;
            pthread_t newThread;
            if (pthread_create(&newThread, nullptr, wsPoll, &clientInfo) != 0) {
                return false;
            }
            clients.insert(std::pair(clientInfo, newThread));
            return true;
        }

        bool stopClient(RemoteClient clientInfo) {
            std::map<RemoteClient, pthread_t, ComByRC>::iterator it = clients.find(clientInfo);
            if (it == clients.end()) {
                return false;
            }
            it->first.webSocket->close();
            _stopClient(it->first);
            clients.erase(it);
            return true;
        }
    };
}

#endif  // _Included_RemoteManager
