//
// Created by 32494 on 2019/9/29.
//

#ifndef ANDROID_TEST_SHELLMSGHANDLER_HPP
#define ANDROID_TEST_SHELLMSGHANDLER_HPP

#include <pthread.h>
#include <sys/wait.h>
#include <stack>
#include <time.h>
#include <unistd.h>
#include "RemoteManager.hpp"

#define TAG_SMH "ShellMsgHandlerHpp"

namespace shell {
    char SHELL_REQ[] = "shellReq";
    char SHELL_RES[] = "shellRes";

    constexpr int COM_MAX_NUM = 10;  // cd ../ && cat smg.txt | wc -c || echo "some error" 这样的就有3个指令，{"cd ../", "cat smg.txt | wc -c", "echo \"some error\""}
    constexpr int COM_BUF_SIZE = 256;
    constexpr int ARG_MAX_NUM = 20;  // 其实包含命令在内的，如 ls -l -a 则会有 {"ls", "-l", "-a"} 这样算，即3个
    constexpr int ARG_BUF_SIZE = 128;
    constexpr int RESULT_BUF_SIZE = 100;
    constexpr int RESULT_MAX_SIZE = 100 * RESULT_BUF_SIZE;  // 这是传输数据的最大字节数，注意因为使用的是json解析，超大量级别的数据不好传输
    constexpr int SINGLE_TIMEOUT_LIMIT = 20;
    constexpr int TOTAL_TIMEOUT_LIMIT = 100;

    const char COMMAND_EXIT[] = "exit";
    const char COMMAND_CD[] = "cd";
    const char COMMAND_AND[] = "&&";
    const char COMMAND_OR[] = "||";
    const bool RETURN_PATH = true;

    // 内置的状态码
    enum {
        RESULT_NORMAL,
        ERROR_WRONG_COMMAND,
        ERROR_EXECUTE,
        ERROR_CD,
        ERROR_EXIT,
        ERROR_SYSTEM,
        ERROR_TIMEOUT,
        ERROR_FORK,
        ERROR_PIPE,
        ERROR_TWO_MANY_COMMAND,
        ERROR_TWO_MANY_PARAMETER,
        ERROR_RESULT_TOO_LONG,  // 表示result太长，超过限制
        ERROR_COMMAND_TOO_LONG,
        ERROR_PARAMETER_TOO_LONG,
    };

    enum {
        MODE_SINGLE_SHELL,
        MODE_MULTIPLE_SHELL,
    };

    void *executeCommandInPThread(void *data);

    class ShellMsgHandler : public remote::MsgHandler {
    private:
        char curPath[COM_BUF_SIZE];
        char remoteCommand[COM_MAX_NUM * COM_BUF_SIZE + 1];  // 获取的远程输入指令
        char splitCommands[COM_MAX_NUM][COM_BUF_SIZE + 1];  // 切割出来的远程输入指令(通过&&和||切割，这些指令控制间可能要用chdir改变执行路径)
        char splitControllers[COM_MAX_NUM][3];  // shell指令控制部分: && 和 ||
        char executeCommands[ARG_MAX_NUM][ARG_BUF_SIZE + 1];  // 每次真正执行的指令，切割为命令(index=0)和它的参数(index=1,2,...)，注意管道|也会当成一个参数
        char sendResult[RESULT_MAX_SIZE + 20];  // 需要传输的数据 -- 目前最大大小为10KB，并预留20个字节作为错误信息的位置
        int resultLen = 0;  // 需要传输的数据的长度
        pthread_mutex_t singleMutex;
        int mode = MODE_SINGLE_SHELL;
        bool returnPath = RETURN_PATH;
    protected:
        int getCurWorkDir() {
            char *result = getcwd(curPath, COM_BUF_SIZE);
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

        // 以&&和||分割命令，返回分割得到的字符串个数
        // 返回-1表示ERROR_WRONG_COMMAND；返回-2表示ERROR_TWO_MANY_COMMAND，返回-3表示ERROR_COMMAND_TOO_LONG
        int splitCommandsByControllers() {
            int num = 0;
            int len = strlen(remoteCommand);
            if (len == 0) {
                L_T_D(TAG_SMH, "splitCommandsByControllers -- len = 0");
                return num;
            }
            L_T_D(TAG_SMH, "splitCommandsByControllers -- len = %d, remote-command: %s", len, remoteCommand);
            std::stack<char> s;
            int j = 0;
            int i = 0;
            while (i < len && remoteCommand[i] == ' ') {
                ++i;
            }
            bool flag = false;
            for (; i < len; i++) {
                char ch = remoteCommand[i];
                int next = i + 1;
                if (s.empty() && next < len && (ch == '&' && remoteCommand[next] == '&' || ch == '|' && remoteCommand[next] == '|')) {
                    while (j > 0 && splitCommands[num][j - 1] == ' ' && (j < 2 || splitCommands[num][j - 2] != '\\')) {
                        j--;
                    }
                    if (j == 0) {
                        return -1;
                    }
                    splitControllers[num][0] = ch;
                    splitControllers[num][1] = remoteCommand[next];
                    splitControllers[num][2] = '\0';
                    splitCommands[num++][j] = '\0';
                    j = 0;
                    flag = true;
                    i = next;
                    next++;
                    while (next < len && remoteCommand[next] == ' ') {
                        i = next;
                        ++next;
                    }
                } else {
                    if (num >= COM_MAX_NUM) {
                        return -2;
                    }
                    if (j >= COM_BUF_SIZE) {
                        return -3;
                    }
                    splitCommands[num][j++] = ch;
                    if (ch != ' ' && (i == 0 || remoteCommand[i - 1] != '\\')) {
                        flag = false;
                    }
                    if ((ch == '"' || ch == '\'') && (i == 0 || remoteCommand[i - 1] != '\\')) {
                        if (!s.empty() && ch == s.top()) {
                            s.pop();
                        } else {
                            s.push(ch);
                        }
                    }
                }
            }
            if (flag) {
                return -1;
            }
            if (s.empty() && j != 0) {
                while (j > 0 && splitCommands[num][j - 1] == ' ' && (j < 2 || splitCommands[num][j - 2] != '\\')) {
                    j--;
                }
                splitControllers[num][0] = '\0';
                splitCommands[num++][j] = '\0';
            }
            return num;
        }

        // 以空格分割命令和它的参数(管道|也当成一个命令)，返回分割得到的字符串个数
        // 返回-1表示ERROR_TWO_MANY_PARAMETER
        int splitCommandByParameters(char singleCommand[COM_BUF_SIZE]) {
            int num = 0;
            int len = strlen(singleCommand);
            if (len == 0) {
                return num;
            }
            std::stack<char> s;
            int j = 0;
            int i = 0;
            while (i < len && singleCommand[i] == ' ') {
                ++i;
            }
            for (; i < len; i++) {
                char ch = singleCommand[i];
                int last = i - 1;
                if (s.empty() && ch == ' ' && (i == 0 || singleCommand[last] != '\\')) {
                    executeCommands[num++][j] = '\0';
                    j = 0;
                    int next = i + 1;
                    while (next < len && singleCommand[next] == ' ') {
                        i = next;
                        ++next;
                    }
                } else if (num >= ARG_BUF_SIZE) {
                    return -1;
                } else if (s.empty() && ch == '|' && (i == 0 || singleCommand[last] != '\\')) {
                    if (j > 0) {
                        executeCommands[num++][j] = '\0';
                        j = 0;
                    }
                    executeCommands[num][0] = '|';
                    executeCommands[num++][1] = '\0';
                } else {
                    if (j >= ARG_BUF_SIZE) {
                        return -2;
                    }
                    executeCommands[num][j++] = ch;
                    if ((ch == '"' || ch == '\'') && (i == 0 || singleCommand[last] != '\\')) {
                        if (!s.empty() && ch == s.top()) {
                            s.pop();
                        } else {
                            s.push(ch);
                        }
                    }
                }
            }
            if (s.empty() && j != 0) {
                executeCommands[num++][j] = '\0';
            }
            return num;
        }

        int executeSingleCommand(char command[COM_BUF_SIZE], long startOfAllCommand) {
            int num = splitCommandByParameters(command);
            if (num == -1) {
                L_T_E(TAG_SMH, "executeSingleCommand -- too many parameters but only one command");
                return ERROR_TWO_MANY_PARAMETER;
            } else if (num == -2) {
                L_T_E(TAG_SMH, "executeSingleCommand -- ERROR_PARAMETER_TOO_LONG");
                return ERROR_PARAMETER_TOO_LONG;
            }
            if (strcmp(executeCommands[0], COMMAND_CD) == 0) {
                if (num != 2) {
                    L_T_E(TAG_SMH, "executeSingleCommand -- wrong command about cd");
                    return ERROR_WRONG_COMMAND;
                }
                if (chdir(executeCommands[1])) {
                    return ERROR_CD;
                } else {
                    if (getCurWorkDir() != RESULT_NORMAL) {
                        L_T_E(TAG_SMH, "executeSingleCommand -- error about getting current path, but change directory successfully");
                    } else {
                        L_T_D(TAG_SMH, "executeSingleCommand -- call cd and current path: %s", curPath);
                    }
                }
                return RESULT_NORMAL;
            } else if (strcmp(executeCommands[0], COMMAND_EXIT) == 0) {
                if (num != 1) {
                    L_T_E(TAG_SMH, "executeSingleCommand -- wrong command about exit");
                    return ERROR_WRONG_COMMAND;
                }
                L_T_E(TAG_SMH, "executeSingleCommand -- call exit, but don't exit");
                return ERROR_EXIT;
            }

            char comBuf[COM_BUF_SIZE + 6 * ARG_BUF_SIZE];
            int start = 0;
            for (int i = 0; i < num; i++) {
                char tempBuf[ARG_BUF_SIZE * 2];
                sprintf(tempBuf, (i == 0 ? "%d: %s" : ", %d: %s"), i, executeCommands[i]);
                strcpy(comBuf + start, tempBuf);
                start += strlen(tempBuf);
            }
            L_T_D(TAG_SMH,
                  "executeSingleCommand -- command(%s) -- parameter numbers(include command): %d, command and it's args: [%s]",
                  command, num, comBuf);

            int fd[2];
            if (pipe(fd) < 0) {
                L_T_E(TAG_SMH, "executeSingleCommand -- error when pipe");
                return ERROR_PIPE;
            }
            pid_t pid = fork();
            if (pid == -1) {
                L_T_E(TAG_SMH, "executeSingleCommand -- error when fork");
                return ERROR_FORK;
            } else if (pid == 0) {
                close(fd[0]);
                char trueCommand[COM_BUF_SIZE + 20];
                sprintf(trueCommand, "/system/bin/sh -c %s", command);
                FILE *fp = popen(trueCommand, "r");
                char writeBuffer[RESULT_BUF_SIZE];
                while (fgets(writeBuffer, sizeof(writeBuffer), fp) != nullptr) {
                    L_T_D(TAG_SMH, "executeSingleCommand -- child process send msg: %s", writeBuffer);
                    write(fd[1], writeBuffer, strlen(writeBuffer));
                }
                pclose(fp);
                L_T_D(TAG_SMH, "executeSingleCommand -- child process finish executing command: (%s)", trueCommand);
                // close(fd[1]);
                exit(0);
            } else {
                close(fd[1]);
                int status;
                int timeCounter = 0;
                while (true) {
                    int result = waitpid(pid, &status, WNOHANG);
                    if (result == 0) {
                        L_T_D(TAG_SMH, "executeSingleCommand -- child process is still running");
                    } else if (result == pid) {
                        L_T_D(TAG_SMH, "executeSingleCommand -- child process has exited.");
                        status = 0;
                        break;
                    } else {
                        L_T_D(TAG_SMH, "executeSingleCommand -- result = %d", result);
                    }
                    timeCounter++;
                    if (timeCounter >= SINGLE_TIMEOUT_LIMIT || time(nullptr) - startOfAllCommand >= TOTAL_TIMEOUT_LIMIT) {
                        kill(pid, 9);
                        // close(fd[0]);
                        L_T_D(TAG_SMH, "executeSingleCommand -- kill child process because of timeout.");
                        return ERROR_TIMEOUT;
                    }
                    sleep(1);
                };
                int exitCode = WEXITSTATUS(status);
                if (exitCode != RESULT_NORMAL) {
                    // close(fd[0]);
                    L_T_E(TAG_SMH, "executeSingleCommand -- error while execute");
                    return ERROR_EXECUTE;
                }
                char readBuffer[RESULT_BUF_SIZE];
                int ret = 0;
                while ((ret = read(fd[0], readBuffer, RESULT_BUF_SIZE - 1)) > 0) {
                    if (resultLen + ret >= RESULT_MAX_SIZE) {
                        return ERROR_RESULT_TOO_LONG;
                    }
                    readBuffer[ret] = '\0';
                    // printf("%s", readBuffer);
                    L_T_D(TAG_SMH, "%s -- %s", command, readBuffer);
                    strcpy(sendResult + resultLen, readBuffer);
                    resultLen += ret;
                }
                // close(fd[0]);
            }
            L_T_D(TAG_SMH, "executeSingleCommand -- execute command successfully");
            return RESULT_NORMAL;
        }

    public:
        ShellMsgHandler() : MsgHandler(SHELL_REQ, SHELL_RES) {
            pthread_mutex_init(&singleMutex, nullptr);
            resultLen = 0;
            sendResult[0] = '\0';
        }

        ~ShellMsgHandler() {
            pthread_mutex_destroy(&singleMutex);
        }

        void onOpen(remote::RemoteClient *remoteClient) override {
            prepareShell(*(remoteClient->webSocket));
        }

        void onFatalError(std::exception &ex) override {
            L_T_E(TAG_SMH, "error occurred in EchoMsgHandler: %s", ex.what());
            closeShell();
        }

        void handleMsg(ws::WebSocket &webSocket, const std::string &msg, const json11::Json &data) override {
            L_T_D(TAG_SMH, "received msg: %s, and data: %s", msg.c_str(), data.dump().c_str());
            if (mode == MODE_SINGLE_SHELL) {
                pthread_mutex_lock(&singleMutex);
            }
            std::string temp = data["shell"][0].dump();
            strcpy(remoteCommand, temp.substr(1, temp.size() - 2).c_str());
            int result = executeMultipleCommand();
            char errorMsg[50];
            if (result == RESULT_NORMAL) { errorMsg[0] = '\0'; }
            else if (result == ERROR_WRONG_COMMAND) { strcpy(errorMsg, "\nerror msg: ERROR_WRONG_COMMAND"); }
            else if (result == ERROR_EXECUTE) { strcpy(errorMsg, "\nerror msg: ERROR_EXECUTE"); }
            else if (result == ERROR_CD) { strcpy(errorMsg, "\nerror msg: ERROR_CD"); }
            else if (result == ERROR_EXIT) { strcpy(errorMsg, "\nerror msg: ERROR_EXIT"); }
            else if (result == ERROR_SYSTEM) { strcpy(errorMsg, "\nerror msg: ERROR_SYSTEM"); }
            else if (result == ERROR_TIMEOUT) { strcpy(errorMsg, "\nerror msg: ERROR_TIMEOUT"); }
            else if (result == ERROR_FORK) { strcpy(errorMsg, "\nerror msg: ERROR_FORK"); }
            else if (result == ERROR_PIPE) { strcpy(errorMsg, "\nerror msg: ERROR_PIPE"); }
            else if (result == ERROR_TWO_MANY_COMMAND) { strcpy(errorMsg, "\nerror msg: ERROR_TWO_MANY_COMMAND"); }
            else if (result == ERROR_TWO_MANY_PARAMETER) { strcpy(errorMsg, "\nerror msg: ERROR_TWO_MANY_PARAMETER"); }
            else if (result == ERROR_RESULT_TOO_LONG) { strcpy(errorMsg, "\nerror msg: ERROR_RESULT_TOO_LONG"); }
            else if (result == ERROR_COMMAND_TOO_LONG) { strcpy(errorMsg, "\nerror msg: ERROR_COMMAND_TOO_LONG"); }
            else if (result == ERROR_PARAMETER_TOO_LONG) { strcpy(errorMsg, "\nerror msg: ERROR_PARAMETER_TOO_LONG"); }
            else { strcpy(errorMsg, "\nerror msg: ERROR_UNKNOWN"); }
            if (returnPath) {
                char pathBuf[COM_BUF_SIZE + 10];
                sprintf(pathBuf, resultLen > 0 && sendResult[resultLen - 1] != '\n' ? "\npath: %s" : "path: %s", curPath);
                int ret = strlen(pathBuf);
                strcpy(sendResult + resultLen, pathBuf);
                resultLen += ret;
            }
            if (errorMsg[0] != '\0') {
                int ret = strlen(errorMsg);
                strcpy(sendResult + resultLen, errorMsg);
                resultLen += ret;
                L_T_E(TAG_SMH, "handleMsg -- error: %s && len = %d", errorMsg, ret);
            }
            sendResult[resultLen] = '\0';
            json11::Json jsonObj = json11::Json::object{
                    {"result",  result},
                    {"content", sendResult},
            };
            this->send(webSocket, jsonObj);
            resultLen = 0;
            sendResult[0] = '\0';
            if (mode == MODE_SINGLE_SHELL) {
                pthread_mutex_unlock(&singleMutex);
            }
        }

        void onClose() override {
            closeShell();
        }

        int executeMultipleCommand() {
            int num = splitCommandsByControllers();
            if (num == -1) {
                L_T_E(TAG_SMH, "executeMultipleCommand -- ERROR_WRONG_COMMAND");
                return ERROR_WRONG_COMMAND;
            } else if (num == -2) {
                L_T_E(TAG_SMH, "executeMultipleCommand -- ERROR_TWO_MANY_COMMAND");
                return ERROR_TWO_MANY_COMMAND;
            } else if (num == -3) {
                L_T_E(TAG_SMH, "executeMultipleCommand -- ERROR_COMMAND_TOO_LONG");
                return ERROR_COMMAND_TOO_LONG;
            }
            long startOfAllCommand = time(nullptr);
            char comBuf[COM_MAX_NUM * COM_BUF_SIZE + 6 * COM_MAX_NUM];
            char tempBuf[COM_BUF_SIZE + 6];
            int start = 0;
            for (int i = 0; i < num; i++) {
                sprintf(tempBuf, i == 0 ? "%d: %s" : ", %d: %s", i, splitCommands[i]);
                int len = strlen(tempBuf);
                strcpy(comBuf + start, tempBuf);
                start += len;
                int result = executeSingleCommand(splitCommands[i], startOfAllCommand);
                int flag1 = strcmp(splitControllers[i], COMMAND_OR);
                int flag2 = strcmp(splitControllers[i], COMMAND_AND);
                bool flag3 = result != RESULT_NORMAL;
                if (flag3 && flag2 == 0) {
                    L_T_E(TAG_SMH, "executeMultipleCommand -- execute command error: %d, and terminate because of '&&'", result);
                    return result;
                } else if (flag3 && flag1 == 0) {
                    L_T_E(TAG_SMH, "executeMultipleCommand -- execute command error: %d", result);
                } else if (flag1 == 0) {
                    L_T_D(TAG_SMH, "executeMultipleCommand -- terminate because of '||'");
                    break;
                } else if (flag2 != 0 && i != num - 1) {
                    L_T_E(TAG_SMH, "executeMultipleCommand -- error split controller: %s", splitControllers[i]);
                    break;
                }
            }
            L_T_D(TAG_SMH, "executeMultipleCommand -- end -- finish executing (%s), which is split as [%s]", remoteCommand, comBuf);
            return RESULT_NORMAL;
        }
    };

    void *executeCommandInPThread(void *data) {
        // TODO: 问题是如何将 webSocket 传入到 executeCommandInPThread 中(考虑使用struct，但是这个好像非常不靠谱)，而且使用多个shell的话，那么多个result也是必须的了
    }
}

#endif //ANDROID_TEST_SHELLMSGHANDLER_HPP
