package com.liang.example.nativeremote;

import android.util.Log;

import com.liang.example.remoteutils.JsonApiKt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

class TestReq {
    String command;
}

class TestRes {
    String command;
}

class TestB {
    private float f;
    private String str;

    TestB() {
    }

    TestB(float f, String str) {
        this.f = f;
        this.str = str;
    }
}

class TestA {
    private byte[] bs;
    private String str;
    private TestB tb;

    TestA(byte[] bs, String str, TestB tb) {
        this.bs = bs;
        this.str = str;
        this.tb = tb;
    }

    public TestA() {
    }
}

class TestC {
    private byte b;
    private short s;
    private int i;
    private long j;
    private char c;
    private float f;
    private double d;
    private boolean z;
    private String str;

    private byte[] bs;
    private short[] ss;
    private int[] is;
    private long[] js;
    private char[] cs;
    private float[] fs;
    private double[] ds;
    private boolean[] zs;
    private String[] strs;

    private TestA ta;
    private TestB tb;

    public TestC() {
    }

    public TestC(byte b, short s, int i, long j, char c, float f, double d, boolean z, String str, byte[] bs,
                 short[] ss, int[] is, long[] js, char[] cs, float[] fs, double[] ds, boolean[] zs, String[] strs, TestA ta,
                 TestB tb) {
        this.b = b;
        this.s = s;
        this.i = i;
        this.j = j;
        this.c = c;
        this.f = f;
        this.d = d;
        this.z = z;
        this.str = str;

        this.bs = bs;
        this.ss = ss;
        this.is = is;
        this.js = js;
        this.cs = cs;
        this.fs = fs;
        this.ds = ds;
        this.zs = zs;
        this.strs = strs;

        this.ta = ta;
        this.tb = tb;
    }
}

public class TestMsgHandler extends AbsMsgHandler<TestReq, TestRes> {
    private static final String TAG = "TestMsgHandler";

    public TestMsgHandler() {
        super("testReq", "testRes");
        Log.d(TAG, "reqTypeStr: testReq, resTypeStr: testRes; reqType: " + reqType + ", resType: " + resType + "; reqClassStr: "
                + reqClassStr + ", resClassStr: " + resClassStr);
        showFieldsMap(getFieldsMap(reqClassStr), reqClassStr);
        showFieldsMap(getFieldsMap(resClassStr), resClassStr);

        byte[] bs = {1, 2, 3};
        short[] ss = {4, 5, 6};
        int[] is = {7, 8, 9};
        long[] js = {10, 11, 12};
        char[] cs = {'a', 'b', 'c'};
        float[] fs = {13.1f, 14.2f, 15.3f};
        double[] ds = {16.4, 17.5, 18.6};
        boolean[] zs = {false, true, false};
        String[] strs = {"jstr2", "jstr3", "jstr4"};
        TestA ta = new TestA(new byte[]{19, 20, 21}, "jstr5", new TestB(22.7f, "jstr6"));
        TestB tb = new TestB(23.8f, "jstr7");
        TestC tc = new TestC((byte) 10, (short) 11, 12, 13L, 'c', 14.1f, 15.2, true, "jstr1", bs, ss, is, js, cs, fs, ds, zs, strs, ta, tb);
        Log.d(TAG, "testC: " + JsonApiKt.toJson(tc));
        TestC tc2 = (TestC) RemoteManager.getInstance().getObjectFromJni(TestC.class.toString().substring(6), JsonApiKt.toJson(tc));
        Log.d(TAG, "testC2: " + JsonApiKt.toJson(tc2));
        Log.d(TAG, "testC3: " + RemoteManager.getInstance().getStringFromJni(tc2, TestC.class.toString().substring(6)));
    }

    private void showFieldsMap(Map<String, Field> fieldsMap, String className) {
        StringBuilder sb = new StringBuilder(className + "[");
        if (fieldsMap != null) {
            boolean first = true;
            for (Map.Entry<String, Field> entry : fieldsMap.entrySet()) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                // entry.getValue().get(obj);
                // entry.getValue().set(obj, val);
                // entry.getValue().setByte(obj, val);
                // entry.getValue().setChar(obj, val);
                // entry.getValue().setShort(obj, val);
                // entry.getValue().setInt(obj, val);
                // entry.getValue().setLong(obj, val);
                // entry.getValue().setFloat(obj, val);
                // entry.getValue().setDouble(obj, val);
                // entry.getValue().setBoolean(obj, val);
                sb.append(entry.getKey()).append(": ").append(entry.getValue().getType().toString());
            }
        } else {
            sb.append("null-error");
        }
        sb.append("]");
        Log.d(TAG, "show fields -- " + sb.toString());
    }

    private Map<String, Field> getFieldsMap(String className) {
        try {
            Class<?> cls = Class.forName(className);
            Field[] fields = cls.getDeclaredFields();
            Map<String, Field> map = new HashMap<>();
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field);
            }
            return map;
        } catch (SecurityException e) {
            Log.e(TAG, "reqClass -- SecurityException", e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "reqClass -- ClassNotFoundException", e);
        }
        return null;
    }

    @Override
    public void onOpen() {
        Log.d(TAG, "onOpen");
    }

    @Override
    public void onClose() {
        Log.d(TAG, "onClose");
    }

    @Override
    public void onError(String what) {
        Log.d(TAG, "onError: " + what);
    }

    @Override
    public void onFatalError(String what) {
        Log.d(TAG, "onFatalError: " + what);
    }

    @Override
    public void onMessage(String serverUrl, TestReq testReq) {
        Log.d(TAG, "onMessage -- testReq.a: " + testReq.command);
        TestRes testRes = new TestRes();
        testRes.command = testReq.command;
        send(serverUrl, testRes);
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize");
        super.finalize();
    }
}
