package com.liang.example.remote;

import com.google.gson.annotations.SerializedName;

/**
 * @author liangyuyin
 * @since 2019/7/16
 */
public class RemoteBasicMsg<T> {
    @SerializedName("type")
    private String type;
    @SerializedName("data")
    private T data;

    public RemoteBasicMsg(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public RemoteBasicMsg() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
