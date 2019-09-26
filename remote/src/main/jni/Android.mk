LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := remote-jni
LOCAL_SRC_FILES := Utils.cpp RemoteNative.cpp
APP_STL         := c++_shared
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)