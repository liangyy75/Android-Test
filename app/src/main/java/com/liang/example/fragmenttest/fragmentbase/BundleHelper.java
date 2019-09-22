package com.liang.example.fragmenttest.fragmentbase;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;

public class BundleHelper {
    private Bundle bundle;

    public Bundle getBundle() {
        return bundle;
    }

    public BundleHelper setBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    public BundleHelper createBundle() {
        this.bundle = new Bundle();
        return this;
    }

    public BundleHelper putAll(Bundle bundle) {
        this.bundle.putAll(bundle);
        return this;
    }

    public BundleHelper putAll(PersistableBundle bundle) {
        this.bundle.putAll(bundle);
        return this;
    }

    public BundleHelper putBinder(String key, IBinder value) {
        this.bundle.putBinder(key, value);
        return this;
    }

    public BundleHelper putBoolean(String key, Boolean value) {
        this.bundle.putBoolean(key, value);
        return this;
    }

    public BundleHelper putBooleanArray(String key, boolean[] value) {
        this.bundle.putBooleanArray(key, value);
        return this;
    }

    public BundleHelper putBundle(String key, Bundle value) {
        this.bundle.putBundle(key, value);
        return this;
    }

    public BundleHelper putByte(String key, Byte value) {
        this.bundle.putByte(key, value);
        return this;
    }

    public BundleHelper putByteArray(String key, byte[] value) {
        this.bundle.putByteArray(key, value);
        return this;
    }

    public BundleHelper putChar(String key, Character value) {
        this.bundle.putChar(key, value);
        return this;
    }

    public BundleHelper putCharArray(String key, char[] value) {
        this.bundle.putCharArray(key, value);
        return this;
    }

    public BundleHelper putCharSequence(String key, CharSequence value) {
        this.bundle.putCharSequence(key, value);
        return this;
    }

    public BundleHelper putCharSequenceArray(String key, CharSequence[] value) {
        this.bundle.putCharSequenceArray(key, value);
        return this;
    }

    public BundleHelper putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        this.bundle.putCharSequenceArrayList(key, value);
        return this;
    }

    public BundleHelper putDouble(String key, Double value) {
        this.bundle.putDouble(key, value);
        return this;
    }

    public BundleHelper putDoubleArray(String key, double[] value) {
        this.bundle.putDoubleArray(key, value);
        return this;
    }

    public BundleHelper putFloat(String key, Float value) {
        this.bundle.putFloat(key, value);
        return this;
    }

    public BundleHelper putFloatArray(String key, float[] value) {
        this.bundle.putFloatArray(key, value);
        return this;
    }

    public BundleHelper putInt(String key, Integer value) {
        this.bundle.putInt(key, value);
        return this;
    }

    public BundleHelper putIntArray(String key, int[] value) {
        this.bundle.putIntArray(key, value);
        return this;
    }

    public BundleHelper putIntegerArrayList(String key, ArrayList<Integer> value) {
        this.bundle.putIntegerArrayList(key, value);
        return this;
    }

    public BundleHelper putLong(String key, Long value) {
        this.bundle.putLong(key, value);
        return this;
    }

    public BundleHelper putLongArray(String key, long[] value) {
        this.bundle.putLongArray(key, value);
        return this;
    }

    public BundleHelper putParcelable(String key, Parcelable value) {
        this.bundle.putParcelable(key, value);
        return this;
    }

    public BundleHelper putParcelableArray(String key, Parcelable[] value) {
        this.bundle.putParcelableArray(key, value);
        return this;
    }

    public BundleHelper putParcelableArrayList(String key, ArrayList<Parcelable> value) {
        this.bundle.putParcelableArrayList(key, value);
        return this;
    }

    public BundleHelper putSparseParcelableArray(String key, SparseArray<? extends Parcelable> value) {
        this.bundle.putSparseParcelableArray(key, value);
        return this;
    }

    public BundleHelper putSerializable(String key, Serializable value) {
        this.bundle.putSerializable(key, value);
        return this;
    }

    public BundleHelper putShort(String key, Short value) {
        this.bundle.putShort(key, value);
        return this;
    }

    public BundleHelper putShortArray(String key, short[] value) {
        this.bundle.putShortArray(key, value);
        return this;
    }

    public BundleHelper putSize(String key, Size value) {
        this.bundle.putSize(key, value);
        return this;
    }

    public BundleHelper putSizeF(String key, SizeF value) {
        this.bundle.putSizeF(key, value);
        return this;
    }

    public BundleHelper putString(String key, String value) {
        this.bundle.putString(key, value);
        return this;
    }

    public BundleHelper putStringArray(String key, String[] value) {
        this.bundle.putStringArray(key, value);
        return this;
    }

    public BundleHelper putStringArrayList(String key, ArrayList<String> value) {
        this.bundle.putStringArrayList(key, value);
        return this;
    }
}
