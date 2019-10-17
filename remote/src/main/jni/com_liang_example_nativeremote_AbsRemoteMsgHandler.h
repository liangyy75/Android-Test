#include <jni.h>
/* Header for class com_liang_example_nativeremote_AbsRemoteMsgHandler */

#ifndef _Included_com_liang_example_nativeremote_AbsRemoteMsgHandler
#define _Included_com_liang_example_nativeremote_AbsRemoteMsgHandler
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_liang_example_nativeremote_AbsRemoteMsgHandler_sendObj
        (JNIEnv *, jobject, jstring, jobject, jstring);

JNIEXPORT void JNICALL Java_com_liang_example_nativeremote_AbsRemoteMsgHandler_sendMsg
        (JNIEnv *, jobject, jstring, jstring);

#ifdef __cplusplus
}
#endif
#endif
