// https://github.com/dhbaird/easywsclient
#ifndef _Included_WSClient
#define _Included_WSClient

#include "Utils.hpp"
#include <string>
#include <vector>

#define TAG_WS_CLIENT_HPP "WSClientHpp"
#define TAG_WS_CLIENT_CPP "WSClientCpp"

namespace ws {

    class WebSocket;

    struct Callback_Imp {
        virtual void operator()(WebSocket &webSocket, const std::string &message) = 0;
    };

    struct BytesCallback_Imp {
        virtual void operator()(WebSocket &webSocket, const std::vector<uint8_t> &message) = 0;
    };

    class WebSocket {
    protected:
        std::string url;
        Callback_Imp *callback = nullptr;
        BytesCallback_Imp *bytesCallback = nullptr;

        template<class Callable>
        struct _Callback : public Callback_Imp {
            Callable *callable;  // 这个Callable可以是C++11的lambda表达式或者functors以及C语言的function pointers
            _Callback(Callable *callable) : callable(callable) {}  // 构造函数
            void operator()(WebSocket &webSocket, const std::string &message) {
                callable(webSocket, message);
            }
        };

        template<class Callable>
        struct _BytesCallback : public BytesCallback_Imp {
            Callable *callable;  // 这个Callable可以是C++11的lambda表达式或者functors以及C语言的function pointers
            _BytesCallback(Callable *callable) : callable(callable) {}  // 构造函数
            void operator()(WebSocket &webSocket, const std::vector<uint8_t> &message) {
                callable(webSocket, message);
            }
        };

    public:
        typedef WebSocket *pointer;
        typedef enum ConnectionState {
            CLOSING, CLOSED, CONNECTING, OPEN
        } ConnectionState;

        // Factories:
        static pointer create_dummy();  // dummy
        static pointer from_url(const std::string &url, const std::string &origin = std::string());  // url
        static pointer from_url_no_mask(const std::string &url, const std::string &origin = std::string());  // url with no mask

        // Interfaces:
        WebSocket(std::string url) { this->url = url; }  // 构造函数
        virtual ~WebSocket() {
            delete this->bytesCallback;
            delete this->callback;
        }  // 析构函数
        virtual void poll(int timeout = 0) = 0; // 超时，单位是毫秒
        virtual void send(const std::string &message) = 0;  // 文本形式
        virtual void sendBinary(const std::string &message) = 0;  // 二进制形式
        virtual void sendBinary(const std::vector<uint8_t> &message) = 0;  // 二进制形式
        virtual void sendPing() = 0;  // 发送ping
        virtual void sendPong() = 0;  // 发送pong
        virtual void close() = 0;  // 关闭连接
        virtual ConnectionState getReadyState() const = 0;  // 获取状态

        void pollWithHandle(int timeout = 0) {
            poll(timeout);
            ConnectionState state = getReadyState();
            // L_T_D(WS_CLIENT_TAG_HPP, "pollWithHandle -- state: %d", state);
            if (state == OPEN || state == CONNECTING) {
                if (callback != nullptr) {
                    _dispatch(*callback);
                }
                if (bytesCallback != nullptr) {
                    _dispatchBinary(*bytesCallback);
                }
            }
        }

        template<class Callable>
        void setCallable(Callable *callable) {
            L_T_D(TAG_WS_CLIENT_HPP, "setCallable");
            this->callback = new _Callback<Callable>(callable);
        }  // 设置 callback
        void dispatch() {
            L_T_D(TAG_WS_CLIENT_HPP, "dispatch");
            _dispatch(*(this->callback));
        }  // 派分消息给设置的 callback
        template<class Callable>
        void dispatch(Callable *callable) {
            L_T_D(TAG_WS_CLIENT_HPP, "dispatch(callable)");
            _Callback<Callable> callback(callable);
            _dispatch(callback);
        }  // 派分消息给传入的 callback

        template<class Callable>
        void setByteCallback(Callable *callable) {
            L_T_D(TAG_WS_CLIENT_HPP, "setByteCallback");
            this->bytesCallback = new _BytesCallback<Callable>(callable);
        }  // 设置 byteCallback
        void dispatchBinary() {
            L_T_D(TAG_WS_CLIENT_HPP, "dispatchBinary");
            _dispatchBinary(*(this->bytesCallback));
        }  // 派分消息给设置的 byteCallback
        template<class Callable>
        void dispatchBinary(Callable *callable) {
            L_T_D(TAG_WS_CLIENT_HPP, "dispatchBinary(callable)");
            _BytesCallback<Callable> callback(callable);
            _dispatchBinary(callback);
        }  // 派分消息给传入的 byteCallback

        std::string getUrl() {
            return this->url.c_str();
        }  // 获取url
    protected:
        virtual void _dispatch(Callback_Imp &callable) = 0;  // 只有继承的类需要且必须重写
        virtual void _dispatchBinary(BytesCallback_Imp &callable) = 0;  // 只有继承的类需要且必须重写
    };
}

#endif  // _Included_WSClient
