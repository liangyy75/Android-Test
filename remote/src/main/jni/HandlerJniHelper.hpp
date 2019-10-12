//
// Created by 32494 on 2019/10/11.
//

#ifndef ANDROID_TEST_HANDLERJNIHELPER_HPP
#define ANDROID_TEST_HANDLERJNIHELPER_HPP

#include <jni.h>

namespace remote {
    bool isValidRemoteMsgHandler(jobject obj);

    void callRemoteMsgHandler(jobject obj);
}

#endif //ANDROID_TEST_HANDLERJNIHELPER_HPP
