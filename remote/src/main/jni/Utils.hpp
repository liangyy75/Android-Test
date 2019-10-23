#include <mutex>
#include <jni.h>
#include <string.h>
#include <android/log.h>
#include "Json.hpp"

#define JTC_BUF_LEN 256

#ifndef LOG_TAG
#define LOG_TAG "REMOTE_JNI"

struct LogCallback {
    virtual int operator()(int level, const char *tag, const char *format, va_list args) = 0;
};

class Logger {
private:
    static Logger *instance;

    LogCallback *logCallback = nullptr;

    Logger() = default;

    Logger(const Logger &other) = delete;

    Logger &operator=(const Logger &other) = delete;

    ~Logger() {
        if (logCallback) {
            delete logCallback;
        }
        pthread_mutex_destroy(&mutex);
    }

protected:
    template<class Callable>
    struct MyLogCallback : public LogCallback {
        Callable *callable;  // 这个Callable可以是C++11的lambda表达式或者functors以及C语言的function pointers
        MyLogCallback(Callable *callable) : callable(callable) {}  // 构造函数
        int operator()(int level, const char *tag, const char *format, va_list args) {
            return callable(level, tag, format, args);
        }
    };

public:
    static pthread_mutex_t mutex;

    static int initMutex() {
        return pthread_mutex_init(&mutex, nullptr);
    }

    static Logger *getInstance() {
        if (instance == nullptr) {
            pthread_mutex_lock(&mutex);
            if (instance == nullptr) {
                instance = new Logger();
            }
            pthread_mutex_unlock(&mutex);
        }
        return instance;
    }

    template<class Callable>
    void setLogCallable(Callable *callable) {
        this->logCallback = new MyLogCallback<Callable>(callable);
    }

    int logPrint(int level, const char *tag, const char *format, va_list args) {
        int ret = 0;
        if (logCallback) {
            ret = (*logCallback)(level, tag, format, args);
        }
        return ret;
    }
};

typedef enum LogPriority {
    /** For internal use only.  */
            LOG_UNKNOWN = 0,
    /** The default priority, for internal use only.  */
            LOG_DEFAULT, /* only for SetMinPriority() */
    /** Verbose logging. Should typically be disabled for a release apk. */
            LOG_VERBOSE,
    /** Debug logging. Should typically be disabled for a release apk. */
            LOG_DEBUG,
    /** Informational logging. Should typically be disabled for a release apk. */
            LOG_INFO,
    /** Warning logging. For use with recoverable failures. */
            LOG_WARN,
    /** Error logging. For use with unrecoverable failures. */
            LOG_ERROR,
    /** Fatal logging. For use when aborting. */
            LOG_FATAL,
    /** For internal use only.  */
            LOG_SILENT, /* only for SetMinPriority(); must be last */
} LogPriority;

int __log_print(int level, const char *tag, const char *format, ...);

#define LOG_V(...) __log_print(LOG_VERBOSE, LOG_TAG, __VA_ARGS__)  // 定义LOG_V类型
#define LOG_D(...) __log_print(LOG_DEBUG, LOG_TAG, __VA_ARGS__)    // 定义LOG_D类型
#define LOG_I(...) __log_print(LOG_INFO, LOG_TAG, __VA_ARGS__)     // 定义LOG_I类型
#define LOG_W(...) __log_print(LOG_WARN, LOG_TAG, __VA_ARGS__)     // 定义LOG_W类型
#define LOG_E(...) __log_print(LOG_ERROR, LOG_TAG, __VA_ARGS__)    // 定义LOG_E类型
#define LOG_F(...) __log_print(LOG_FATAL, LOG_TAG, __VA_ARGS__)    // 定义LOG_F类型

#define L_T_V(TAG, ...) __log_print(LOG_VERBOSE, TAG, __VA_ARGS__)  // 定义L_T_V类型
#define L_T_D(TAG, ...) __log_print(LOG_DEBUG, TAG, __VA_ARGS__)    // 定义L_T_D类型
#define L_T_I(TAG, ...) __log_print(LOG_INFO, TAG, __VA_ARGS__)     // 定义L_T_I类型
#define L_T_W(TAG, ...) __log_print(LOG_WARN, TAG, __VA_ARGS__)     // 定义L_T_W类型
#define L_T_E(TAG, ...) __log_print(LOG_ERROR, TAG, __VA_ARGS__)    // 定义L_T_E类型
#define L_T_F(TAG, ...) __log_print(LOG_FATAL, TAG, __VA_ARGS__)    // 定义L_T_F类型

template<class T>
class MutexVector {
private:
    std::mutex mMutex;
    std::vector<T> mVector;
public:
    MutexVector() {}

    MutexVector(const MutexVector &other) = delete;

    MutexVector &operator()(const MutexVector &other) = delete;

    void lock() {
        mMutex.lock();
    }

    void unlock() {
        mMutex.unlock();
    }

    std::vector<T> getVector() {
        return mVector;
    }

    void insertEnd(std::vector<T> v) {
        std::lock_guard<std::mutex> lg(mMutex);
        mVector.insert(mVector.end(), v.begin(), v.end());
    }

    template<class Iterator>
    void normalInsertEnd(Iterator message_begin, Iterator message_end) {
        mVector.insert(mVector.end(), message_begin, message_end);
    }

    void insertBegin(std::vector<T> v) {
        std::lock_guard<std::mutex> lg(mMutex);
        mVector.insert(mVector.begin(), v.begin(), v.end());
    }

    bool empty() {
        std::lock_guard<std::mutex> lg(mMutex);
        return mVector.empty();
    }

    int size() {
        std::lock_guard<std::mutex> lg(mMutex);
        return mVector.size();
    }

    int normalSize() {
        return mVector.size();
    }

    void erase(int ret) {
        std::lock_guard<std::mutex> lg(mMutex);
        mVector.erase(mVector.begin(), mVector.begin() + ret);
    }

    void normalErase(int ret) {
        mVector.erase(mVector.begin(), mVector.begin() + ret);
    }

    void normalXor(int pos, T t) {
        mVector[pos] ^= t;
    }
};

struct ComByStr {
    bool operator()(const char *a, const char *b) const {
        return strcmp(a, b) < 0;
    }
};

void replace(char *str, char o, char n);

bool startsWith(const char *str, const char *start);

jboolean boolToJBoolean(bool value);

void jStringToCharArray(JNIEnv *jniEnv, jstring jStr, char buf[]);

jobject getFieldFromJObject(JNIEnv *jniEnv, jobject obj, char name[], char signature[]);

void getFieldIdsFromJClass(JNIEnv *jniEnv, const char *className, std::map<const char *, jobject, ComByStr> *fieldsMap);

jobject json11ToJObject(JNIEnv *jniEnv, const json11::Json &json, const char *className);

json11::Json jObjectToJson11(JNIEnv *jniEnv, jobject obj, const char *className);

// 为了跨线程使用jni，只能这样了

void getFieldsFromJClass(JNIEnv *jniEnv, std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr> *allFieldsMap,
                         const char *className, std::map<const char *, jclass, ComByStr> *targetClasses, jclass fieldClass,
                         jclass classClass);

jobject
json11ToJObject(JNIEnv *jniEnv, const json11::Json &json,
                std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr> *allFieldsMap,
                std::map<const char *, jclass, ComByStr> *targetClasses, const char *className, jclass fieldClass, jclass classClass);

json11::Json
jObjectToJson11(JNIEnv *jniEnv, jobject obj,
                std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr> *allFieldsMap,
                const char *className, jclass fieldClass, jclass classClass);

#endif
