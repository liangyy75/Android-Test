package com.liang.example.shell;

import com.google.gson.annotations.SerializedName;

/**
 * @author liangyuyin
 * @since 2019/7/16
 */
public class ShellPushReqMsg {
    @SerializedName("shell")
    private String[] shell;

    public String[] getShell() {
        return shell;
    }

    public void setShell(String[] shell) {
        this.shell = shell;
    }
}
