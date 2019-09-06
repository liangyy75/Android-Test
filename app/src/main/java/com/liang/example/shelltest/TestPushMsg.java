package com.liang.example.shelltest;

import com.google.gson.annotations.SerializedName;

/**
 * @author liangyuyin
 * @since 2019/7/16
 */
public class TestPushMsg {
    @SerializedName("command")
    private String command;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
