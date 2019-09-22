package com.liang.example.fragmenttest.fragmentbase;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;

public class IntentHelper {
    private Intent intent;

    public Intent getIntent() {
        return intent;
    }

    public IntentHelper setIntent(Intent intent) {
        this.intent = intent;
        return this;
    }

    public IntentHelper createIntent() {
        this.intent = new Intent();
        return this;
    }

    public IntentHelper putAll(String key, Bundle value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putBoolean(String key, Boolean value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putBooleanArray(String key, boolean[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putBundle(String key, Bundle value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putByte(String key, Byte value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putByteArray(String key, byte[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putChar(String key, Character value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putCharArray(String key, char[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putCharSequence(String key, CharSequence value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putCharSequenceArray(String key, CharSequence[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        this.intent.putCharSequenceArrayListExtra(key, value);
        return this;
    }

    public IntentHelper putDouble(String key, Double value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putDoubleArray(String key, double[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putFloat(String key, Float value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putFloatArray(String key, float[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putInt(String key, Integer value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putIntArray(String key, int[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putIntegerArrayList(String key, ArrayList<Integer> value) {
        this.intent.putIntegerArrayListExtra(key, value);
        return this;
    }

    public IntentHelper putLong(String key, Long value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putLongArray(String key, long[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putParcelable(String key, Parcelable value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putParcelableArray(String key, Parcelable[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putParcelableArrayList(String key, ArrayList<Parcelable> value) {
        this.intent.putParcelableArrayListExtra(key, value);
        return this;
    }

    public IntentHelper putSerializable(String key, Serializable value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putShort(String key, Short value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putShortArray(String key, short[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putString(String key, String value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putStringArray(String key, String[] value) {
        this.intent.putExtra(key, value);
        return this;
    }

    public IntentHelper putStringArrayList(String key, ArrayList<String> value) {
        this.intent.putStringArrayListExtra(key, value);
        return this;
    }

    public IntentHelper putExtras(Bundle bundle) {
        this.intent.putExtras(bundle);
        return this;
    }

    public IntentHelper putExtras(Intent intent) {
        this.intent.putExtras(intent);
        return this;
    }
}
