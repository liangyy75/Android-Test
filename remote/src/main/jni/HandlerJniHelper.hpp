//
// Created by 32494 on 2019/10/11.
//

#ifndef ANDROID_TEST_HANDLERJNIHELPER_HPP
#define ANDROID_TEST_HANDLERJNIHELPER_HPP

#include <jni.h>
#include "RemoteManager.hpp"

namespace remote {
#define TAG_HJH "HandlerJniHelper"

    class JavaMsgHandler : public remote::RemoteMsgHandler {
    private:
        JavaVM *globalJvm;  // 多线程共享变量
        jobject globalMsgHandler;  // 多线程共享JObject
        char *reqClassStr, *resClassStr;
        std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr> *reqClassFieldsMap, *resClassFieldsMap;
        std::map<const char *, jclass, ComByStr> *reqTargetClasses, *resTargetClasses;
        jmethodID onOpenId, handleMsgId, onErrorId, onFatalErrorId, onCloseId;
        jclass globalFieldClass, globalClassClass;
    public:
        JavaMsgHandler(char *reqType, char *resType, JNIEnv *jniEnv, jobject msgHandler) : RemoteMsgHandler(reqType, resType) {
            jclass cls = jniEnv->GetObjectClass(msgHandler);
            this->reqClassStr = new char[JTC_BUF_LEN];
            this->resClassStr = new char[JTC_BUF_LEN];
            jStringToCharArray(jniEnv, (jstring) getFieldFromJObject(
                    jniEnv, msgHandler, (char *) "reqClassStr", (char *) "Ljava/lang/String;"), this->reqClassStr);
            jStringToCharArray(jniEnv, (jstring) getFieldFromJObject(
                    jniEnv, msgHandler, (char *) "resClassStr", (char *) "Ljava/lang/String;"), this->resClassStr);

            char *reqClassStrTemp = new char[JTC_BUF_LEN];
            char *resClassStrTemp = new char[JTC_BUF_LEN];
            strcpy(reqClassStrTemp, reqClassStr);
            strcpy(resClassStrTemp, resClassStr);
            replace(reqClassStrTemp, '.', '/');
            replace(resClassStrTemp, '.', '/');
            this->globalFieldClass = (jclass) jniEnv->NewGlobalRef(jniEnv->FindClass("java/lang/reflect/Field"));
            this->globalClassClass = (jclass) jniEnv->NewGlobalRef(jniEnv->FindClass("java/lang/Class"));

            this->reqClassFieldsMap = new std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr>();
            this->resClassFieldsMap = new std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr>();
            this->reqTargetClasses = new std::map<const char *, jclass, ComByStr>();
            this->resTargetClasses = new std::map<const char *, jclass, ComByStr>();
            getFieldsFromJClass(jniEnv, reqClassFieldsMap, reqClassStrTemp, reqTargetClasses, globalFieldClass, globalClassClass);
            getFieldsFromJClass(jniEnv, resClassFieldsMap, resClassStrTemp, resTargetClasses, globalFieldClass, globalClassClass);

            jniEnv->GetJavaVM(&(this->globalJvm));
            this->globalMsgHandler = jniEnv->NewGlobalRef(msgHandler);
        }

        static void
        release(JNIEnv *jniEnv, std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr> *allFieldsMap,
                std::map<const char *, jclass, ComByStr> *targetClasses) {
            for (auto it = targetClasses->begin(); it != targetClasses->end(); it++) {
                jniEnv->DeleteGlobalRef(it->second);
            }
            for (auto it = allFieldsMap->begin(); it != allFieldsMap->end(); it++) {
                std::map<const char *, jobject, ComByStr> *fieldsMap = it->second;
                for (auto ij = fieldsMap->begin(); ij != fieldsMap->end(); ij++) {
                    delete[] ij->first;
                    jniEnv->DeleteGlobalRef(ij->second);
                }
                delete fieldsMap;
                delete[] it->first;
            }
            delete targetClasses;
            delete allFieldsMap;
        }

        ~JavaMsgHandler() {
            JNIEnv *jniEnv;
            if (globalJvm->AttachCurrentThread(&jniEnv, nullptr) == JNI_OK) {
                release(jniEnv, reqClassFieldsMap, reqTargetClasses);
                release(jniEnv, resClassFieldsMap, resTargetClasses);
                jniEnv->DeleteGlobalRef(globalMsgHandler);
                jniEnv->DeleteGlobalRef(globalFieldClass);
                jniEnv->DeleteGlobalRef(globalClassClass);
            };
            globalJvm = nullptr;
            delete[] reqClassStr;
            delete[] resClassStr;
        }

        void onOpen(RemoteClient *remoteClient) override {
            JNIEnv *jniEnv;
            if (globalJvm->AttachCurrentThread(&jniEnv, nullptr) == JNI_OK) {
                jclass cls = jniEnv->GetObjectClass(globalMsgHandler);
                this->onOpenId = jniEnv->GetMethodID(cls, "onOpen", "()V");
                jniEnv->CallVoidMethod(globalMsgHandler, onOpenId);
                globalJvm->DetachCurrentThread();
            } else {
                L_T_D(TAG_HJH, "onOpen -- get jniEnv failed");
            }
        }

        void handleMsg(ws::WebSocket &webSocket, const std::string &msg, const json11::Json &data) override {
            JNIEnv *jniEnv;
            if (globalJvm->AttachCurrentThread(&jniEnv, nullptr) == JNI_OK) {
                jstring jServerUrl = jniEnv->NewStringUTF(webSocket.getUrl().c_str());
                jstring jMsg = jniEnv->NewStringUTF(msg.c_str());
                jclass cls = jniEnv->GetObjectClass(globalMsgHandler);
                char reqClassStrTemp[JTC_BUF_LEN];
                strcpy(reqClassStrTemp, reqClassStr);
                replace(reqClassStrTemp, '.', '/');
                this->handleMsgId = jniEnv->GetMethodID(
                        cls, "onMessage", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V");  // 泛型的本质
                jniEnv->CallVoidMethod(globalMsgHandler, handleMsgId, jServerUrl, jMsg, json11ToJObject(
                        jniEnv, data, reqClassFieldsMap, reqTargetClasses, reqClassStrTemp, globalFieldClass, globalClassClass));
                globalJvm->DetachCurrentThread();
            } else {
                L_T_D(TAG_HJH, "handleMsg -- get jniEnv failed, and msg is (%s), json data is (%s)", msg.c_str(), data.dump().c_str());
            }
        }

        void onError(ws::WebSocket &webSocket, std::exception &ex) override {
            JNIEnv *jniEnv;
            if (globalJvm->AttachCurrentThread(&jniEnv, nullptr) == JNI_OK) {
                jstring errorMsg = jniEnv->NewStringUTF(ex.what());
                jclass cls = jniEnv->GetObjectClass(globalMsgHandler);
                this->onErrorId = jniEnv->GetMethodID(cls, "onError", "(Ljava/lang/String;)V");
                jniEnv->CallVoidMethod(globalMsgHandler, onErrorId, errorMsg);
                globalJvm->DetachCurrentThread();
            } else {
                L_T_D(TAG_HJH, "onError -- get jniEnv failed, and exception is (%s)", ex.what());
            }
        }

        void onFatalError(std::exception &ex) override {
            JNIEnv *jniEnv;
            if (globalJvm->AttachCurrentThread(&jniEnv, nullptr) == JNI_OK) {
                jstring errorMsg = jniEnv->NewStringUTF(ex.what());
                jclass cls = jniEnv->GetObjectClass(globalMsgHandler);
                this->onFatalErrorId = jniEnv->GetMethodID(cls, "onFatalError", "(Ljava/lang/String;)V");
                jniEnv->CallVoidMethod(globalMsgHandler, onFatalErrorId, errorMsg);
                globalJvm->DetachCurrentThread();
            } else {
                L_T_D(TAG_HJH, "onFatalError -- get jniEnv failed, and exception is (%s)", ex.what());
            }
        }

        void onClose() override {
            JNIEnv *jniEnv;
            if (globalJvm->AttachCurrentThread(&jniEnv, nullptr) == JNI_OK) {
                jclass cls = jniEnv->GetObjectClass(globalMsgHandler);
                this->onOpenId = jniEnv->GetMethodID(cls, "onClose", "()V");
                jniEnv->CallVoidMethod(globalMsgHandler, onCloseId);
                globalJvm->DetachCurrentThread();
            } else {
                L_T_D(TAG_HJH, "onClose -- get jniEnv failed");
            }
        }
    };

    JavaMsgHandler *jObjectToJavaMsgHandler(JNIEnv *jniEnv, jobject msgHandler);
}

#endif //ANDROID_TEST_HANDLERJNIHELPER_HPP
