package com.liang.example.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Parcelable
import android.util.SparseArray

var applicationInfo: ApplicationInfo? = null

fun init(context: Context) {
    applicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
}

fun release() {
    applicationInfo = null
}

fun getAnyMetaData(name: String) = applicationInfo?.metaData?.get(name)

@JvmOverloads
fun getCharMetaData(name: String, default: Char = ' ') =
        applicationInfo?.metaData?.getChar(name, default) ?: default

@JvmOverloads
fun getCharSequenceMetaData(name: String, default: CharSequence = "") =
        applicationInfo?.metaData?.getCharSequence(name, default) ?: default

@JvmOverloads
fun getStringMetaData(name: String, default: String = "") =
        applicationInfo?.metaData?.getString(name, default) ?: default

@JvmOverloads
fun getBooleanMetaData(name: String, default: Boolean = false) =
        applicationInfo?.metaData?.getBoolean(name, default) ?: default

@JvmOverloads
fun getByteMetaData(name: String, default: Byte = 0) =
        applicationInfo?.metaData?.getByte(name, default) ?: default

@JvmOverloads
fun getShortMetaData(name: String, default: Short = 0) =
        applicationInfo?.metaData?.getShort(name, default) ?: default

@JvmOverloads
fun getIntMetaData(name: String, default: Int = 0) =
        applicationInfo?.metaData?.getInt(name, default) ?: default

@JvmOverloads
fun getLongMetaData(name: String, default: Long = 0) =
        applicationInfo?.metaData?.getLong(name, default) ?: default

@JvmOverloads
fun getFloatMetaData(name: String, default: Float = 0.0f) =
        applicationInfo?.metaData?.getFloat(name, default) ?: default

@JvmOverloads
fun getDoubleMetaData(name: String, default: Double = 0.0) =
        applicationInfo?.metaData?.getDouble(name, default) ?: default

fun getSerializableMetaData(name: String) = applicationInfo?.metaData?.getSerializable(name)
fun getBinderMetaData(name: String) = applicationInfo?.metaData?.getBinder(name)
fun getBundleMetaData(name: String) = applicationInfo?.metaData?.getBundle(name)
fun <T> getParcelableMetaData(name: String) where T : Parcelable = applicationInfo?.metaData?.getParcelable<T>(name)
fun getSizeFMetaData(name: String) = applicationInfo?.metaData?.getSizeF(name)
fun getSizeMetaData(name: String) = applicationInfo?.metaData?.getSize(name)

fun getCharArrayMetaData(name: String) = applicationInfo?.metaData?.getCharArray(name)
fun getCharSequenceArrayMetaData(name: String) = applicationInfo?.metaData?.getCharSequenceArray(name)
fun getStringArrayMetaData(name: String) = applicationInfo?.metaData?.getStringArray(name)
fun getParcelableArrayMetaData(name: String) = applicationInfo?.metaData?.getParcelableArray(name)
fun <T> getSparseParcelableArrayMetaData(name: String): SparseArray<T>? where T : Parcelable = applicationInfo?.metaData?.getSparseParcelableArray(name)

fun getBooleanArrayMetaData(name: String) = applicationInfo?.metaData?.getBooleanArray(name)
fun getByteArrayMetaData(name: String) = applicationInfo?.metaData?.getByteArray(name)
fun getShortArrayMetaData(name: String) = applicationInfo?.metaData?.getShortArray(name)
fun getIntArrayMetaData(name: String) = applicationInfo?.metaData?.getIntArray(name)
fun getLongArrayMetaData(name: String) = applicationInfo?.metaData?.getLongArray(name)
fun getDoubleArrayMetaData(name: String) = applicationInfo?.metaData?.getDoubleArray(name)

fun getCharSequenceArrayListMetaData(name: String) = applicationInfo?.metaData?.getCharSequenceArrayList(name)
fun getStringArrayListMetaData(name: String) = applicationInfo?.metaData?.getStringArrayList(name)
fun getIntegerArrayListMetaData(name: String) = applicationInfo?.metaData?.getIntegerArrayList(name)
fun <T> getParcelableArrayListMetaData(name: String): java.util.ArrayList<T>? where T : Parcelable = applicationInfo?.metaData?.getParcelableArrayList(name)
