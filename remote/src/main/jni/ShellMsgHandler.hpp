//
// Created by 32494 on 2019/9/29.
//

#ifndef ANDROID_TEST_SHELLMSGHANDLER_HPP
#define ANDROID_TEST_SHELLMSGHANDLER_HPP

#include <unistd.h>
#include <stack>
#include "RemoteManager.hpp"

#define TAG_SMH "ShellMsgHandlerHpp"
char SHELL_REQ[] = "shellReq";
char SHELL_RES[] = "shellRes";

namespace shell {
#define BUF_SZ 256

    const char *COMMAND_EXIT = "exit";
    const char *COMMAND_CD = "cd";
    const char *COMMAND_CONTROLLERS[] = {"&&", "||"};

    // 内置的状态码
    enum {
        RESULT_NORMAL,
        ERROR_FORK,
        ERROR_COMMAND,
        ERROR_WRONG_PARAMETER,
        ERROR_MISS_PARAMETER,
        ERROR_TOO_MANY_PARAMETER,
        ERROR_CD,
        ERROR_SYSTEM,
        ERROR_EXIT,

        ERROR_MANY_IN,  /* 重定向的错误信息 */
        ERROR_MANY_OUT,
        ERROR_FILE_NOT_EXIST,

        ERROR_PIPE,  /* 管道的错误信息 */
        ERROR_PIPE_MISS_PARAMETER
    };

    class ShellMsgHandler : public remote::MsgHandler {
    private:
        // char username[BUF_SZ];
        // char hostname[BUF_SZ];
        char curPath[BUF_SZ];
        char totalCom[BUF_SZ * BUF_SZ];  // 获取的远程输入指令
        char splitCom[BUF_SZ][BUF_SZ];  // 切割出来的远程输入指令
    protected:
        int getCurWorkDir() {
            char *result = getcwd(curPath, BUF_SZ);
            return result == nullptr ? ERROR_SYSTEM : RESULT_NORMAL;
        }

        void prepareShell(ws::WebSocket &webSocket) {
            if (getCurWorkDir() == ERROR_SYSTEM) {
                L_T_E(TAG_SMH, "Error: System error while getting current work directory.");
                json11::Json jsonObj = json11::Json::object{
                        {"result",  ERROR_SYSTEM},
                        {"content", "Error: System error while getting current work directory."},
                };
                this->send(webSocket, jsonObj);
            }
        }

        void closeShell() {
            // TODO
        }

        int splitCommandAndArgs(char command[BUF_SZ]) {
            int num = 0;
            int len = strlen(command);
            int flag = 0;
            return num;
        }

        int splitCommandsByControllers(char command[BUF_SZ * BUF_SZ]) {  // 以空格分割命令， 返回分割得到的字符串个数
            int num = 0;
            int len = strlen(command);
            if (len == 0) {
                return num;
            }
            std::stack<char> s;
            int j = 0;
            for (int i = 0; i < len; i++) {
                char ch = command[i];
                int last = i - 1;
                if ((ch == '"' || ch == '\'') && (i == 0 || command[last] != '\\')) {
                    if (!s.empty() && ch == s.top()) s.pop(); else s.push(ch);
                } else if (s.empty() && i > 0 && (ch == '&' && command[last] == '&' || ch == '|' && command[last] == '|')) {
                    splitCom[num++][j] = '\0';
                    j = 0;
                } else {
                    splitCom[num][j++] = ch;
                }
            }
            if (s.empty() && num != 0 && j != 0) {
                splitCom[num][j] = '\0';
            }
            return num;
        }

        int callCommand(int commandNum);

        int callCommandWithPipe(int left, int right);

        int callCommandWithRedi(int left, int right);

        int callCd(int commandNum);

        int callExit(ws::WebSocket &webSocket) {
            // webSocket.close();
            // closeShell();
            L_T_D(TAG_SMH, "call exit");
        }

    public:
        ShellMsgHandler() : MsgHandler(SHELL_REQ, SHELL_RES) {}

        void onOpen(remote::RemoteClient *remoteClient) override {
            prepareShell(*(remoteClient->webSocket));
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
            strcpy(totalCom, data["shell"].dump().c_str());
            int len = strlen(totalCom);
            if (len != BUF_SZ) {
                totalCom[len - 1] = '\0';
            }
            // int commandNum = splitCommands(totalCom);
            // if (commandNum == 0) {
            //     return;
            // }
            // TODO
        }

        void onClose() override {
            closeShell();
        }
    };
}

#endif //ANDROID_TEST_SHELLMSGHANDLER_HPP
