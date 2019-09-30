//
// Created by 32494 on 2019/9/29.
//

#ifndef ANDROID_TEST_SHELLMSGHANDLER_HPP
#define ANDROID_TEST_SHELLMSGHANDLER_HPP

#include <unistd.h>
#include "RemoteManager.hpp"

#define TAG_SMH "ShellMsgHandlerHpp"
char SHELL_REQ[] = "shellReq";
char SHELL_RES[] = "shellRes";

class ShellMsgHandler : public remote::MsgHandler {
protected:
    void prepareShell() {
        // TODO
    }

    void closeShell() {
        // TODO
    }

public:
    ShellMsgHandler() : MsgHandler(SHELL_REQ, SHELL_RES) {}

    void onOpen(remote::RemoteClient *remoteClient) override {
        prepareShell();
    }

    // void onError(ws::WebSocket &webSocket, std::exception &ex) override {
    //     L_T_E(TAG_SMH, "error occurred in EchoMsgHandler: %s", ex.what());
    // }

    void onFatalError(std::exception &ex) override {
        L_T_E(TAG_SMH, "error occurred in EchoMsgHandler: %s", ex.what());
        closeShell();
    }

    void handleMsg(ws::WebSocket &webSocket, const std::string &msg, const json11::Json &data) override {
        L_T_D(TAG_SMH, "received msg: %s, and data: %s", msg.c_str(), data.dump().c_str());
    }

    void onClose() override {
        closeShell();
    }
};

#endif //ANDROID_TEST_SHELLMSGHANDLER_HPP
