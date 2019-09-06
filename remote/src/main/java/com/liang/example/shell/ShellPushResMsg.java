package com.liang.example.shell;

import com.google.gson.annotations.SerializedName;

/**
 * @author liangyuyin
 * @since 2019/7/16
 */
public class ShellPushResMsg {
    @SerializedName("result")
    private int result;
    @SerializedName("content")
    private String content;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
