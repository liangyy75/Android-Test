package com.liang.example.gsontest;

import com.google.gson.annotations.SerializedName;

public class BasicMsg<T> {
    private String type;
    @SerializedName(value = "cmd", alternate = {"data"})
    private T data;

    public BasicMsg(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public BasicMsg() {
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
