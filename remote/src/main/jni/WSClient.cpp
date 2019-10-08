#include <cstdlib>
#include <cstring>
#include <fcntl.h>
#include <linux/in.h>
#include <netdb.h>
#include <netinet/tcp.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#include <cstdint>
#include <pthread.h>

#ifndef _SOCKET_T_DEFINED
typedef int socket_t;
#define _SOCKET_T_DEFINED
#endif
#ifndef INVALID_SOCKET
#define INVALID_SOCKET (-1)
#endif
#ifndef SOCKET_ERROR
#define SOCKET_ERROR   (-1)
#endif
#define closeSocket(s) ::close(s)

#include <cerrno>

#define socketerrno errno
#define SOCKET_EAGAIN_EINPROGRESS EAGAIN
#define SOCKET_EWOULDBLOCK EWOULDBLOCK

#include "ThreadSafeQueue.hpp"
#include "ThreadSafeQueue2.hpp"
#include "Utils.hpp"
#include "WSClient.hpp"

using ws::Callback_Imp;
using ws::BytesCallback_Imp;

namespace {
    const int SPORT_LEN = 16;
    const int MILLI_TIME_STEP = 1000;

    socket_t hostname_connect(const std::string &hostname, int port) {
        struct addrinfo hints = addrinfo();
        struct addrinfo *result;
        struct addrinfo *p;
        int ret;
        socket_t sockFd = INVALID_SOCKET;
        char sport[SPORT_LEN];
        memset(&hints, 0, sizeof(hints));
        hints.ai_family = AF_UNSPEC;
        hints.ai_socktype = SOCK_STREAM;
        snprintf(sport, SPORT_LEN, "%d", port);
        if ((ret = getaddrinfo(hostname.c_str(), sport, &hints, &result)) != 0) {
            L_T_E(TAG_WS_CLIENT_CPP, "hostname_connect -- getAddressInfo: %s", gai_strerror(ret));
            return 1;
        }
        L_T_D(TAG_WS_CLIENT_CPP, "begin create socket");
        for (p = result; p != nullptr; p = p->ai_next) {
            sockFd = socket(p->ai_family, p->ai_socktype, p->ai_protocol);
            L_T_D(TAG_WS_CLIENT_CPP, "during create socket -- sockFd: %d", sockFd);
            if (sockFd == INVALID_SOCKET) {
                L_T_D(TAG_WS_CLIENT_CPP, "during create socket -- invalid sockFd: %d", sockFd);
                continue;
            }
            if (connect(sockFd, p->ai_addr, p->ai_addrlen) != SOCKET_ERROR) {
                L_T_D(TAG_WS_CLIENT_CPP, "during create socket -- connect sockFd: %d successfully", sockFd);
                break;
            }
            closeSocket(sockFd);
            sockFd = INVALID_SOCKET;
        }
        freeaddrinfo(result);
        return sockFd;
    }

    class _DummyWebSocket : public ws::WebSocket {
    public:
        void poll(int timeout) override {}  // 1
        void send(const std::string &message) override {}  // 2
        void sendBinary(const std::string &message) override {}  // 3
        void sendBinary(const std::vector<uint8_t> &message) override {} // 4
        void sendPing() override {}  // 5
        void sendPong() override {}  // 6
        void close() override {}  // 7
        ConnectionState getReadyState() const override { return CLOSED; }  // 8
        void _dispatch(Callback_Imp &callable) override {}  // 9
        void _dispatchBinary(BytesCallback_Imp &callable) override {}  // 10
    };

    class _RealWebSocket : public ws::WebSocket {
    private:
        std::vector<uint8_t> rxBuf;  // received buffer
        std::vector<uint8_t> txBuf;  // send buffer
        std::vector<uint8_t> receivedData;  // true received data

        socket_t sockFd;  // true socket's fileId
        ConnectionState state;  // state
        bool useMask;
        bool isRxBad;
    public:
        // http://tools.ietf.org/html/rfc6455#section-5.2  Base Framing Protocol
        //
        //  0                   1                   2                   3
        //  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
        // +-+-+-+-+-------+-+-------------+-------------------------------+
        // |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
        // |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
        // |N|V|V|V|       |S|             |   (if payload len==126/127)   |
        // | |1|2|3|       |K|             |                               |
        // +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
        // |     Extended payload length continued, if payload len == 127  |
        // + - - - - - - - - - - - - - - - +-------------------------------+
        // |                               |Masking-key, if MASK set to 1  |
        // +-------------------------------+-------------------------------+
        // | Masking-key (continued)       |          Payload Data         |
        // +-------------------------------- - - - - - - - - - - - - - - - +
        // :                     Payload Data continued ...                :
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
        // |                     Payload Data continued ...                |
        // +---------------------------------------------------------------+
        struct WsHeaderType {
            unsigned header_size;
            bool fin;
            bool mask;
            enum OpCodeType {
                CONTINUATION = 0x0,
                TEXT_FRAME = 0x1,
                BINARY_FRAME = 0x2,
                CLOSE = 8,
                PING = 9,
                PONG = 0xa,
            } opcode;
            int N0;
            uint64_t N;
            uint8_t masking_key[4];
        };

        _RealWebSocket(socket_t sockfd, bool useMask) : sockFd(sockfd), state(OPEN), useMask(useMask), isRxBad(false) {
        }

        ConnectionState getReadyState() const override {
            return state;
        }

        void poll(int timeout) override {
            // L_T_D(WS_CLIENT_TAG_CPP, "poll -- begin timeout: %d", timeout);
            if (state == CLOSED) {
                if (timeout > 0) {
                    timeval tv = {timeout / MILLI_TIME_STEP, (timeout % MILLI_TIME_STEP) * MILLI_TIME_STEP};
                    select(0, nullptr, nullptr, nullptr, &tv);
                }
                L_T_D(TAG_WS_CLIENT_CPP, "poll -- state is 'closed'");
                return;
            }
            if (timeout > 0) {
                fd_set readFds;
                fd_set writeFds;
                timeval tv = {timeout / MILLI_TIME_STEP, (timeout % MILLI_TIME_STEP) * MILLI_TIME_STEP};
                FD_ZERO(&readFds);
                FD_ZERO(&writeFds);
                FD_SET(sockFd, &readFds);
                if (!txBuf.empty()) { FD_SET(sockFd, &writeFds); }
                select(sockFd + 1, &readFds, &writeFds, nullptr, timeout > 0 ? &tv : nullptr);
            }
            // L_T_D(WS_CLIENT_TAG_CPP, "poll -- begin receive");
            while (true) {
                unsigned N = rxBuf.size();
                ssize_t ret;
                rxBuf.resize(N + 1500);
                ret = recv(sockFd, (char *) &rxBuf[0] + N, 1500, 0);
                if (ret < 0 && (socketerrno == SOCKET_EWOULDBLOCK || socketerrno == SOCKET_EAGAIN_EINPROGRESS)) {
                    rxBuf.resize(N);
                    break;
                } else if (ret <= 0) {
                    rxBuf.resize(N);
                    closeSocket(sockFd);
                    state = CLOSED;
                    L_T_D(TAG_WS_CLIENT_CPP, ret < 0 ? "poll -- recv -- Connection error!" : "poll -- recv -- Connection closed!");
                    return;
                } else {
                    rxBuf.resize(N + ret);
                }
            }
            // L_T_D(WS_CLIENT_TAG_CPP, "poll -- begin send");
            while (!txBuf.empty()) {
                int ret = ::send(sockFd, (char *) &txBuf[0], txBuf.size(), 0);
                if (ret < 0 && (socketerrno == SOCKET_EWOULDBLOCK || socketerrno == SOCKET_EAGAIN_EINPROGRESS)) {
                    break;
                } else if (ret <= 0) {
                    closeSocket(sockFd);
                    state = CLOSED;
                    L_T_D(TAG_WS_CLIENT_CPP, ret < 0 ? "poll -- send -- Connection error!" : "poll -- send -- Connection closed!");
                    break;
                } else {
                    txBuf.erase(txBuf.begin(), txBuf.begin() + ret);
                }
            }
            if (txBuf.empty() && state == CLOSING) {
                closeSocket(sockFd);
                state = CLOSED;
                L_T_D(TAG_WS_CLIENT_CPP, "poll -- now close connection");
            }
        }  // 轮询发送和接受消息

        // Callable must have signature: void(const std::string & message).
        // Should work with C functions, C++ functors, and C++11 std::function and
        // lambda:
        //template<class Callable>
        //void dispatch(Callable callback)
        virtual void _dispatch(Callback_Imp &callable) {
            struct CallbackAdapter : public BytesCallback_Imp {  // Adapt void(const std::string<uint8_t>&) to void(const std::string&)
                Callback_Imp &callable;  // 1
                explicit CallbackAdapter(Callback_Imp &callable) : callable(callable) {}  // 2
                void operator()(ws::WebSocket &webSocket, const std::vector<uint8_t> &message) override {
                    std::string stringMessage(message.begin(), message.end());
                    L_T_D(TAG_WS_CLIENT_CPP, "_dispatch -- msg: %s", stringMessage.c_str());
                    callable(webSocket, stringMessage);
                }
            };
            CallbackAdapter bytesCallback(callable);
            _dispatchBinary(bytesCallback);
        }

        virtual void _dispatchBinary(BytesCallback_Imp &callable) {
            if (isRxBad) {
                L_T_E(TAG_WS_CLIENT_CPP, "_dispatchBinary -- isRxBad is true");
                return;
            }
            while (true) {
                WsHeaderType ws = WsHeaderType();
                if (rxBuf.size() < 2) { return; /* Need at least 2 */ }
                const uint8_t *data = (uint8_t *) &rxBuf[0]; // peek, but don't consume
                ws.fin = (data[0] & 0x80) == 0x80;
                ws.opcode = (WsHeaderType::OpCodeType) (data[0] & 0x0f);
                ws.mask = (data[1] & 0x80) == 0x80;
                ws.N0 = (data[1] & 0x7f);
                ws.header_size = 2 + (ws.N0 == 126 ? 2 : 0) + (ws.N0 == 127 ? 8 : 0) + (ws.mask ? 4 : 0);
                if (rxBuf.size() < ws.header_size) { return; /* Need: ws.header_size - rxBuf.size() */ }
                int i = 0;
                if (ws.N0 < 126) {
                    ws.N = ws.N0;
                    i = 2;
                } else if (ws.N0 == 126) {
                    ws.N = 0;
                    ws.N |= ((uint64_t) data[2]) << 8;
                    ws.N |= ((uint64_t) data[3]) << 0;
                    i = 4;
                } else if (ws.N0 == 127) {
                    ws.N = 0;
                    ws.N |= ((uint64_t) data[2]) << 56;
                    ws.N |= ((uint64_t) data[3]) << 48;
                    ws.N |= ((uint64_t) data[4]) << 40;
                    ws.N |= ((uint64_t) data[5]) << 32;
                    ws.N |= ((uint64_t) data[6]) << 24;
                    ws.N |= ((uint64_t) data[7]) << 16;
                    ws.N |= ((uint64_t) data[8]) << 8;
                    ws.N |= ((uint64_t) data[9]) << 0;
                    i = 10;
                    if (ws.N & 0x8000000000000000ull) {
                        // https://tools.ietf.org/html/rfc6455 writes the "the most
                        // significant bit MUST be 0."
                        //
                        // We can't drop the frame, because (1) we don't we don't
                        // know how much data to skip over to find the next header,
                        // and (2) this would be an impractically long length, even
                        // if it were valid. So just close() and return immediately
                        // for now.
                        isRxBad = true;
                        L_T_E(TAG_WS_CLIENT_CPP, "_dispatchBinary -- Frame has invalid frame length. Closing.");
                        close();
                        return;
                    }
                }
                if (ws.mask) {
                    ws.masking_key[0] = ((uint8_t) data[i + 0]) << 0;
                    ws.masking_key[1] = ((uint8_t) data[i + 1]) << 0;
                    ws.masking_key[2] = ((uint8_t) data[i + 2]) << 0;
                    ws.masking_key[3] = ((uint8_t) data[i + 3]) << 0;
                } else {
                    ws.masking_key[0] = 0;
                    ws.masking_key[1] = 0;
                    ws.masking_key[2] = 0;
                    ws.masking_key[3] = 0;
                }

                // Note: The checks above should hopefully ensure this addition
                //       cannot overflow:
                if (rxBuf.size() < ws.header_size + ws.N) { return; /* Need: ws.header_size+ws.N - rxBuf.size() */ }

                // We got a whole message, now do something with it:
                if (ws.opcode == WsHeaderType::TEXT_FRAME
                    || ws.opcode == WsHeaderType::BINARY_FRAME
                    || ws.opcode == WsHeaderType::CONTINUATION
                        ) {
                    if (ws.mask) { for (size_t j = 0; j != ws.N; ++j) { rxBuf[j + ws.header_size] ^= ws.masking_key[j & 0x3]; }}
                    receivedData.insert(receivedData.end(), rxBuf.begin() + ws.header_size,
                                        rxBuf.begin() + ws.header_size + (size_t) ws.N);// just feed
                    if (ws.fin) {
                        callable(*this, receivedData);
                        receivedData.erase(receivedData.begin(), receivedData.end());
                        std::vector<uint8_t>().swap(receivedData);// free memory
                    }
                    L_T_D(TAG_WS_CLIENT_CPP, "_dispatchBinary -- received data frame");
                } else if (ws.opcode == WsHeaderType::PING) {
                    if (ws.mask) { for (size_t j = 0; j != ws.N; ++j) { rxBuf[j + ws.header_size] ^= ws.masking_key[j & 0x3]; }}
                    std::string data(rxBuf.begin() + ws.header_size, rxBuf.begin() + ws.header_size + (size_t) ws.N);
                    sendData(WsHeaderType::PONG, data.size(), data.begin(), data.end());
                    L_T_D(TAG_WS_CLIENT_CPP, "_dispatchBinary -- received ping frame");
                } else if (ws.opcode == WsHeaderType::PONG) {
                    L_T_D(TAG_WS_CLIENT_CPP, "_dispatchBinary -- received pong frame");
                } else if (ws.opcode == WsHeaderType::CLOSE) {
                    close();
                } else {
                    L_T_E(TAG_WS_CLIENT_CPP, "_dispatchBinary -- Got unexpected WebSocket message.");
                    close();
                }
                rxBuf.erase(rxBuf.begin(), rxBuf.begin() + ws.header_size + (size_t) ws.N);
            }
        }

        void sendPing() override {
            std::string empty;
            sendData(WsHeaderType::PING, empty.size(), empty.begin(), empty.end());
            L_T_D(TAG_WS_CLIENT_CPP, "sendPing");
        }

        void sendPong() override {
            std::string empty;
            sendData(WsHeaderType::PING, empty.size(), empty.begin(), empty.end());
            L_T_D(TAG_WS_CLIENT_CPP, "sendPong");
        }

        void send(const std::string &message) {
            L_T_D(TAG_WS_CLIENT_CPP, "send -- msg: %s", message.c_str());
            sendData(WsHeaderType::TEXT_FRAME, message.size(), message.begin(), message.end());
        }

        void sendBinary(const std::string &message) {
            L_T_D(TAG_WS_CLIENT_CPP, "sendBinary -- msg: %s", message.c_str());
            sendData(WsHeaderType::BINARY_FRAME, message.size(), message.begin(), message.end());
        }

        void sendBinary(const std::vector<uint8_t> &message) {
            L_T_D(TAG_WS_CLIENT_CPP, "sendBinary -- msg: %s", std::string(message.begin(), message.end()).c_str());
            sendData(WsHeaderType::BINARY_FRAME, message.size(), message.begin(), message.end());
        }

        template<class Iterator>
        void sendData(WsHeaderType::OpCodeType type, uint64_t message_size, Iterator message_begin, Iterator message_end) {
            const uint8_t masking_key[4] = {0x12, 0x34, 0x56, 0x78};
            if (state == CLOSING || state == CLOSED) {
                L_T_D(TAG_WS_CLIENT_CPP, "sendData -- state is 'closing' or 'closed', so return");
                return;
            }
            std::vector<uint8_t> header;
            header.assign(2 + (message_size >= 126 ? 2 : 0) + (message_size >= 65536 ? 6 : 0) + (useMask ? 4 : 0), 0);
            header[0] = 0x80 | type;
            L_T_D(TAG_WS_CLIENT_CPP, "sendData -- useMask: %d", useMask);
            if (message_size < 126) {
                header[1] = (message_size & 0xff) | (useMask ? 0x80 : 0);
                if (useMask) {
                    header[2] = masking_key[0];
                    header[3] = masking_key[1];
                    header[4] = masking_key[2];
                    header[5] = masking_key[3];
                }
                L_T_D(TAG_WS_CLIENT_CPP, "sendData -- msg_size < 126");
            } else if (message_size < 65536) {
                header[1] = 126 | (useMask ? 0x80 : 0);
                header[2] = (message_size >> 8) & 0xff;
                header[3] = (message_size >> 0) & 0xff;
                if (useMask) {
                    header[4] = masking_key[0];
                    header[5] = masking_key[1];
                    header[6] = masking_key[2];
                    header[7] = masking_key[3];
                }
                L_T_D(TAG_WS_CLIENT_CPP, "sendData -- msg_size < 65536");
            } else {
                header[1] = 127 | (useMask ? 0x80 : 0);
                header[2] = (message_size >> 56) & 0xff;
                header[3] = (message_size >> 48) & 0xff;
                header[4] = (message_size >> 40) & 0xff;
                header[5] = (message_size >> 32) & 0xff;
                header[6] = (message_size >> 24) & 0xff;
                header[7] = (message_size >> 16) & 0xff;
                header[8] = (message_size >> 8) & 0xff;
                header[9] = (message_size >> 0) & 0xff;
                if (useMask) {
                    header[10] = masking_key[0];
                    header[11] = masking_key[1];
                    header[12] = masking_key[2];
                    header[13] = masking_key[3];
                }
                L_T_D(TAG_WS_CLIENT_CPP, "sendData -- msg_size >= 65536");
            }
            // N.B. - txBuf will keep growing until it can be transmitted over the socket:
            txBuf.insert(txBuf.end(), header.begin(), header.end());
            txBuf.insert(txBuf.end(), message_begin, message_end);
            if (useMask) {
                size_t message_offset = txBuf.size() - message_size;
                for (size_t i = 0; i != message_size; ++i) {
                    txBuf[message_offset + i] ^= masking_key[i & 0x3];
                }
            }
        }

        void close() override {
            if (state == CLOSING || state == CLOSED) { return; }
            state = CLOSING;
            uint8_t closeFrame[6] = {0x88, 0x80, 0x00, 0x00, 0x00, 0x00};  // last 4 bytes are a masking key
            std::vector<uint8_t> header(closeFrame, closeFrame + 6);
            txBuf.insert(txBuf.end(), header.begin(), header.end());
            L_T_D(TAG_WS_CLIENT_CPP, "close");
        }

        ~_RealWebSocket() {
            close();
        }
    };

    ws::WebSocket::pointer from_url(const std::string &url, bool useMask, const std::string &origin) {
        char host[512];
        int port;
        char path[512];
        if (url.size() >= 512) {
            L_T_E(TAG_WS_CLIENT_CPP, "create ws -- url size limit exceeded: %s", url.c_str());
            return nullptr;
        }
        if (origin.size() >= 200) {
            L_T_E(TAG_WS_CLIENT_CPP, "create ws -- origin size limit exceeded: %s", origin.c_str());
            return nullptr;
        }
        if (sscanf(url.c_str(), "ws://%[^:/]:%d/%s", host, &port, path) == 3) {
        } else if (sscanf(url.c_str(), "ws://%[^:/]/%s", host, path) == 2) {
            port = 80;
        } else if (sscanf(url.c_str(), "ws://%[^:/]:%d", host, &port) == 2) {
            path[0] = '\0';
        } else if (sscanf(url.c_str(), "ws://%[^:/]", host) == 1) {
            port = 80;
            path[0] = '\0';
        } else {
            L_T_W(TAG_WS_CLIENT_CPP, "create ws -- Could not parse WebSocket url: %s", url.c_str());
            return nullptr;
        }
        L_T_D(TAG_WS_CLIENT_CPP, "ws: connecting: host=%s port=%d path=/%s", host, port, path);
        socket_t sockFd = hostname_connect(host, port);
        if (sockFd == INVALID_SOCKET) {
            L_T_D(TAG_WS_CLIENT_CPP, "create ws -- Unable to connect to %s:%d", host, port);
            return nullptr;
        } else {
            L_T_D(TAG_WS_CLIENT_CPP, "create ws -- create socket_t successfully");
        }
        {
            // XXX: this should be done non-blocking,
            char line[1024];
            int status;
            int i;
            snprintf(line, 1024, "GET /%s HTTP/1.1\r\n", path);
            ::send(sockFd, line, strlen(line), 0);
            if (port == 80) {
                snprintf(line, 1024, "Host: %s\r\n", host);
                ::send(sockFd, line, strlen(line), 0);
            } else {
                snprintf(line, 1024, "Host: %s:%d\r\n", host, port);
                ::send(sockFd, line, strlen(line), 0);
            }
            snprintf(line, 1024, "Upgrade: websocket\r\n");
            ::send(sockFd, line, strlen(line), 0);
            snprintf(line, 1024, "Connection: Upgrade\r\n");
            ::send(sockFd, line, strlen(line), 0);
            if (!origin.empty()) {
                snprintf(line, 1024, "Origin: %s\r\n", origin.c_str());
                ::send(sockFd, line, strlen(line), 0);
            }
            snprintf(line, 1024, "Sec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\r\n");
            ::send(sockFd, line, strlen(line), 0);
            snprintf(line, 1024, "Sec-WebSocket-Version: 13\r\n");
            ::send(sockFd, line, strlen(line), 0);
            snprintf(line, 1024, "\r\n");
            ::send(sockFd, line, strlen(line), 0);
            for (i = 0; i < 2 || (i < 1023 && line[i - 2] != '\r' && line[i - 1] != '\n'); ++i) {
                if (recv(sockFd, line + i, 1, 0) == 0) { return nullptr; }
            }
            line[i] = 0;
            if (i == 1023) {
                L_T_E(TAG_WS_CLIENT_CPP, "create ws -- Got invalid status line connecting to: %s", url.c_str());
                return nullptr;
            }
            if (sscanf(line, "HTTP/1.1 %d", &status) != 1 || status != 101) {
                L_T_E(TAG_WS_CLIENT_CPP, "create ws -- Got bad status connecting to %s: %s", url.c_str(), line);
                return nullptr;
            }
            while (true) {
                for (i = 0; i < 2 || (i < 1023 && line[i - 2] != '\r' && line[i - 1] != '\n'); ++i) {
                    if (recv(sockFd, line + i, 1, 0) == 0) { return nullptr; }
                }
                if (line[0] == '\r' && line[1] == '\n') { break; }
            }
        }
        int flag = 1;
        setsockopt(sockFd, IPPROTO_TCP, TCP_NODELAY, (char *) &flag, sizeof(flag)); // Disable Nagle's algorithm
        fcntl(sockFd, F_SETFL, O_NONBLOCK);
        L_T_D(TAG_WS_CLIENT_CPP, "create ws -- Connected to: %s", url.c_str());
        return ws::WebSocket::pointer(new _RealWebSocket(sockFd, useMask));
    }
}  // end of module-only namespace

namespace ws {
    WebSocket::pointer WebSocket::create_dummy() {
        static pointer dummy = pointer(new _DummyWebSocket);
        return dummy;
    }

    WebSocket::pointer WebSocket::from_url(const std::string &url, const std::string &origin) {
        return ::from_url(url, true, origin);
    }

    WebSocket::pointer WebSocket::from_url_no_mask(const std::string &url, const std::string &origin) {
        return ::from_url(url, false, origin);
    }
}
