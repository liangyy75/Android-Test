LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := remote-jni
APP_STL         := stlport_static
APP_ALLOW_MISSING_DEPS=true
LOCAL_LDLIBS    := -llog
LOCAL_CPPFLAGS    := $(MY_CPPFLAGS) -frtti -std=c++11 -fsigned-char
LOCAL_SRC_FILES := Utils.cpp WSClient.cpp RemoteNative.cpp

include $(BUILD_SHARED_LIBRARY)
