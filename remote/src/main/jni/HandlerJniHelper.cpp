#include "HandlerJniHelper.hpp"

remote::JavaMsgHandler *remote::jObjectToJavaMsgHandler(JNIEnv *jniEnv, jobject msgHandler) {
    char reqTypeStr[JTC_BUF_LEN];
    char resTypeStr[JTC_BUF_LEN];
    jStringToCharArray(jniEnv, (jstring) getFieldFromJObject(
            jniEnv, msgHandler, (char *) "reqTypeStr", (char *) "Ljava/lang/String;"), reqTypeStr);
    jStringToCharArray(jniEnv, (jstring) getFieldFromJObject(
            jniEnv, msgHandler, (char *) "resTypeStr", (char *) "Ljava/lang/String;"), resTypeStr);
    return new JavaMsgHandler(reqTypeStr, resTypeStr, jniEnv, msgHandler);
}
