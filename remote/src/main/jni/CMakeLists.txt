# https://github.com/dhbaird/easywsclient
# https://github.com/dropbox/json11/blob/master/test.cpp or
# https://github.com/DaveGamble/cJSON

cmake_minimum_required(VERSION 3.4.1)
add_library(remote-jni SHARED Json.cpp Utils.cpp WSClient.cpp RemoteManager.cpp ShellMsgHandler.cpp HandlerJniHelper.cpp TinyXml2.cpp RemoteNative.cpp)
find_library(log-lib log)
target_link_libraries(remote-jni ${log-lib})