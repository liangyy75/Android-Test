package com.liang.example.shell;

import com.liang.example.remote.AbsRemoteMsgHandler;
import com.liang.example.remote.RemoteMsgManager;

import java.util.Arrays;

/**
 * @author liangyuyin
 * @since 2019/7/16
 */
public class ShellMsgHandler extends AbsRemoteMsgHandler<ShellPushReqMsg, ShellPushResMsg> {
    private static final String TAG = "ShellMsgHandler";
    private static final String TYPE_REMOTE_SHELL_REQ = "shell";
    private static final String TYPE_REMOTE_SHELL_RES = "output";
    private static final long EXECUTE_INTERVAL = 20 * 1000;

    private Shell.Console shell = null;
    private final Object shellLock = new Object();
    private long lastCommandExecuteTime = 0;

    public ShellMsgHandler() {
        super(TYPE_REMOTE_SHELL_REQ, TYPE_REMOTE_SHELL_RES);
    }

    @Override
    public void onOpen() {
        getShell("get shell successfully");
    }

    @Override
    public void onMessage(String msg, ShellPushReqMsg reqMsg) {
        if (reqMsg == null) {
            return;
        }
        // long now = System.currentTimeMillis();
        // if (now - lastCommandExecuteTime < EXECUTE_INTERVAL) {
        //     KLog.debug(TAG, "the next instruction should be delayed by 30s from the previous instruction.");
        //     return;
        // }
        // lastCommandExecuteTime = now;
        onMessage(reqMsg);
    }

    @Override
    public void onMessage(final ShellPushReqMsg reqMsg) {
        RemoteMsgManager.logger.d(TAG, "request: " + Arrays.toString(reqMsg.getShell()));
        getShell("reget shell successfully -- onMessage");
        ShellPushResMsg resMsg = null;
        synchronized (shellLock) {
            // if (!shell.isCommandRunning()) {
            CommandResult result = shell.run(reqMsg.getShell());
            // } else {
            //     result = new CommandResult(Collections.singletonList("last command is still running."), null, 0);
            // }
            if (result != null) {
                resMsg = resultToResponse(result);
            }
        }
        RemoteMsgManager.logger.d(TAG, "response: " + (resMsg != null ? resMsg.getContent() : "empty resMsg"));
        this.send(resMsg);
    }

    @Override
    public void onClose() {
        clearShell();
    }

    @Override
    public void onError(Throwable e) {
        clearShell();
    }

    private void clearShell() {
        if (shell != null) {
            synchronized (shellLock) {
                if (shell != null) {
                    shell.close();
                    shell = null;
                    RemoteMsgManager.logger.d(TAG, "clear shell successfully.");
                }
            }
        }
    }

    private ShellPushResMsg resultToResponse(CommandResult result) {
        ShellPushResMsg resMsg = new ShellPushResMsg();
        resMsg.setResult(result.exitCode);
        String content = result.toString();
        String exitMsg = null;
        if (result.exitCode == ShellExitCode.WATCHDOG_EXIT) {
            exitMsg = "timeout";
            getShell("reget shell successfully -- timeout");
        } else if (result.exitCode == ShellExitCode.SHELL_EXEC_FAILED) {
            exitMsg = "execute shell failed";
        } else if (result.exitCode == ShellExitCode.SHELL_WRONG_UID) {
            exitMsg = "wrong shell's uid";
        } else if (result.exitCode == ShellExitCode.SHELL_NOT_FOUND) {
            exitMsg = "shell not found";
        } else if (result.exitCode == ShellExitCode.COMMAND_NOT_EXECUTABLE) {
            exitMsg = "command not executable";
        } else if (result.exitCode == ShellExitCode.COMMAND_NOT_FOUND) {
            exitMsg = "command not found";
        } else if (result.exitCode == ShellExitCode.SHELL_DIED) {
            exitMsg = "shell died";
            getShell("reget shell successfully -- shell died");
        } else if (result.exitCode == ShellExitCode.TERMINATED) {
            exitMsg = "terminated";
        }
        if (exitMsg != null) {
            if (content.length() == 0) {
                content = exitMsg;
            } else {
                content = exitMsg + " : " + content;
            }
        }
        resMsg.setContent(content);
        return resMsg;
    }

    private void getShell(String s) {
        RemoteMsgManager.logger.d(TAG, "getShell -- shell is null:" + (shell == null) + ";shell.isClosed():" + (shell != null ? shell.isClosed() : "false"));
        if (shell == null || shell.isClosed()) {
            try {
                synchronized (shellLock) {
                    if (shell == null || shell.isClosed()) {
                        shell = new Shell.Console.Builder().useSH().setWatchdogTimeout(20).build();
                        RemoteMsgManager.logger.d(TAG, s);
                    }
                }
            } catch (ShellNotFoundException e) {
                RemoteMsgManager.logger.e(TAG, "could not get shell.", e);
            }
        }
    }
}
