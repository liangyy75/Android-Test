package com.liang.example.shelltest;

import com.liang.example.remote.AbsRemoteMsgHandler;
import com.liang.example.utils.ApiManager;

public class TestMsgHandler extends AbsRemoteMsgHandler<TestPushMsg, TestPushMsg> {
    private static final String TYPE_REMOTE_TEST_REQ = "testReq";
    private static final String TYPE_REMOTE_TEST_RES = "testRes";

    public TestMsgHandler() {
        super(TYPE_REMOTE_TEST_REQ, TYPE_REMOTE_TEST_RES);
    }

    @Override
    public void onMessage(TestPushMsg testPushMsg) {
        ApiManager.LOGGER.d("TestMsgHandler", "response: " + (testPushMsg != null ? testPushMsg.getCommand() : "empty resMsg"));
        this.send(testPushMsg);
    }
}
