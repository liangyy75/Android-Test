#include "WSClient.hpp"
#include "RemoteManager.hpp"

namespace remote {
    class MsgHandler {
    private:
        char *reqType;
        char *resType;
    protected:
        virtual void handleMsg(ws::WebSocket &webSocket, const std::string &msg) = 0;

        MsgHandler(char *reqType, char *resType) : reqType(reqType), resType(resType) {}

    public:
        char *getReqType() {
            return reqType;
        }  // getter
        char *getResType() {
            return resType;
        }  // getter
    };

    struct ComByStr {
        bool operator()(const char *a, const char *b) const {
            return strcmp(a, b) != 0;
        }
    };

    class _RemoteManagerImpl1 : remote::RemoteManager {
    private:
        std::map<char *, MsgHandler, ComByStr> handlers;
    public:
        ws::WebSocket _startNewClient(remote::RemoteClient clientInfo) override {
            // TODO:
        };  // 启动
        ws::WebSocket _stopClient(remote::RemoteClient clientInfo) override {
            // 这里好像不需要做什么
        };  // 销毁

        bool addMsgHandler(MsgHandler msgHandler) {
            if (handlers.find(msgHandler.getReqType()) == handlers.end()) {
                return false;
            }
            handlers.insert(std::pair<char *, MsgHandler>(msgHandler.getReqType(), msgHandler));
            return true;
        }  // 添加消息处理类
        bool removeMsgHandler(MsgHandler msgHandler) {
            return removeMsgHandler(msgHandler.getReqType());
        }  // 删除消息处理类
        bool removeMsgHandler(char *reqType) {
            auto it = handlers.find(reqType);
            if (it == handlers.end()) {
                return false;
            }
            handlers.erase(it);
            return true;
        }  // 删除消息处理类
    };
}