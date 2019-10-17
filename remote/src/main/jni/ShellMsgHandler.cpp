#include "ShellMsgHandler.hpp"

void *shell::executeCommandInPThread(void *data) {
    // TODO: 问题是如何将 webSocket 传入到 executeCommandInPThread 中(考虑使用struct，但是这个好像非常不靠谱)，而且使用多个shell的话，那么多个result也是必须的了
    return nullptr;
}
