#include "Utils.hpp"
#include <cstring>

void replace(char *str, char o, char n) {
    if (!str) {
        return;
    }
    int len = strlen(str);
    for (int i = 0; i < len; i++) {
        if (str[i] == o) {
            str[i] = n;
        }
    }
}

bool startsWith(const char *str, const char *start) {
    if (!str || !start) {
        return false;
    }
    int len1 = strlen(str);
    int len2 = strlen(start);
    if (len2 > len1) {
        return false;
    }
    int i;
    for (i = 0; i < len2 && str[i] == start[i]; i++);
    return i == len2;
}

jboolean boolToJBoolean(bool value) {
    // if (value) return JNI_TRUE; else return JNI_FALSE;
    return (uint8_t) (value ? JNI_TRUE : JNI_FALSE);
}

void jStringToCharArray(JNIEnv *jniEnv, jstring jStr, char buf[]) {
    const char *strBuf = jniEnv->GetStringUTFChars(jStr, nullptr);
    strcpy(buf, strBuf);
    jniEnv->ReleaseStringUTFChars(jStr, strBuf);
}

jobject getFieldFromJObject(JNIEnv *jniEnv, jobject obj, char name[], char signature[]) {
    jclass cls = jniEnv->GetObjectClass(obj);
    jfieldID fId = jniEnv->GetFieldID(cls, name, signature);
    if (fId == nullptr) {
        return nullptr;
    }
    jniEnv->DeleteLocalRef(cls);
    return jniEnv->GetObjectField(obj, fId);
}

// 例子 getFieldIdsFromJClass(jniEnv, "com/huya/example/TestReq", reqClassFieldIdsMap);
void getFieldIdsFromJClass(JNIEnv *jniEnv, const char *className, std::map<const char *, jobject, ComByStr> *fieldsMap) {
    try {
        jclass targetClass = jniEnv->FindClass(className);
        jclass classClass = jniEnv->FindClass("java/lang/Class");
        jmethodID getDeclaredFieldsMethodId = jniEnv->GetMethodID(classClass, "getDeclaredFields", "()[Ljava/lang/reflect/Field;");
        auto fields = (jobjectArray) jniEnv->CallObjectMethod(targetClass, getDeclaredFieldsMethodId);

        jclass fieldClass = jniEnv->FindClass("java/lang/reflect/Field");
        jmethodID setAccessibleMethodId = jniEnv->GetMethodID(fieldClass, "setAccessible", "(Z)V");
        jmethodID getNameMethodId = jniEnv->GetMethodID(fieldClass, "getName", "()Ljava/lang/String;");

        int len = jniEnv->GetArrayLength(fields);
        for (int i = 0; i < len; i++) {
            jobject field = jniEnv->GetObjectArrayElement(fields, i);
            jniEnv->CallVoidMethod(field, setAccessibleMethodId, JNI_TRUE);
            char *fieldName = new char[JTC_BUF_LEN];
            jStringToCharArray(jniEnv, (jstring) jniEnv->CallObjectMethod(field, getNameMethodId), fieldName);
            fieldsMap->insert(std::make_pair(fieldName, field));
        }

        jniEnv->DeleteLocalRef(targetClass);
        jniEnv->DeleteLocalRef(classClass);
        jniEnv->DeleteLocalRef(fieldClass);
    } catch (std::exception &e) {
        L_T_E(LOG_TAG, "getFieldIdsFromJClass -- %s", e.what());
    }
}

void getValidClassName(const char *fieldType, char *className, int end, int start) {
    int n = strlen(fieldType) - end;
    strncpy(className, fieldType + start, n);
    className[n] = '\0';
    replace(className, '.', '/');
}

#define SET_VALUE(methodName, methodSig, type, isMethod, valueMethod, defaultValue) \
    jmethodID setValueMethodId = jniEnv->GetMethodID(fieldClass, methodName, methodSig); \
    jniEnv->CallVoidMethod(field, setValueMethodId, resultJObject, (type) (value.isMethod() ? value.valueMethod() : defaultValue));/* \
    L_T_D(LOG_TAG, "json11ToJObject -- %s", name)*/

#define SET_CHAR_VALUE() SET_VALUE("setChar", "(Ljava/lang/Object;C)V", jchar, is_number, int_value, 0)
#define SET_BYTE_VALUE() SET_VALUE("setByte", "(Ljava/lang/Object;B)V", jbyte, is_number, int_value, 0)
#define SET_SHORT_VALUE() SET_VALUE("setShort", "(Ljava/lang/Object;S)V", jshort, is_number, int_value, 0)
#define SET_INT_VALUE() SET_VALUE("setInt", "(Ljava/lang/Object;I)V", jint, is_number, int_value, 0)
#define SET_LONG_VALUE() SET_VALUE("setLong", "(Ljava/lang/Object;J)V", jlong, is_number, number_value, 0)
#define SET_FLOAT_VALUE() SET_VALUE("setFloat", "(Ljava/lang/Object;F)V", jfloat, is_number, number_value, 0)
#define SET_DOUBLE_VALUE() SET_VALUE("setDouble", "(Ljava/lang/Object;D)V", jdouble, is_number, number_value, 0)
#define SET_BOOLEAN_VALUE() SET_VALUE("setBoolean", "(Ljava/lang/Object;Z)V", jboolean, is_bool, bool_value, false)

#define SET_ARRAY_VALUE(basicType, arrType, arrMethod, isMethod, valueMethod, defaultValue, setRegion, name) \
    basicType valuesBuf[JTC_BUF_LEN]; \
    arrType finalArray = jniEnv->arrMethod(len); \
    for (int i = 0; i < len; i++) { \
        json11::Json temp = valueArray[i]; \
        valuesBuf[i] = (basicType) (temp.isMethod() ? temp.valueMethod() : defaultValue); \
    } \
    jniEnv->setRegion(finalArray, 0, len, valuesBuf); \
    finalValue = finalArray;/* \
    L_T_D(LOG_TAG, "json11ToJObject -- %s", name)*/

#define SET_CHAR_ARRAY_VALUE() SET_ARRAY_VALUE(jchar, jcharArray, NewCharArray, is_number, int_value, 0, SetCharArrayRegion, "SetCharArrayRegion")
#define SET_BYTE_ARRAY_VALUE() SET_ARRAY_VALUE(jbyte, jbyteArray, NewByteArray, is_number, int_value, 0, SetByteArrayRegion, "SetByteArrayRegion")
#define SET_SHORT_ARRAY_VALUE() SET_ARRAY_VALUE(jshort, jshortArray, NewShortArray, is_number, int_value, 0, SetShortArrayRegion, "SetShortArrayRegion")
#define SET_INT_ARRAY_VALUE() SET_ARRAY_VALUE(jint, jintArray, NewIntArray, is_number, int_value, 0, SetIntArrayRegion, "SetIntArrayRegion")
#define SET_LONG_ARRAY_VALUE() SET_ARRAY_VALUE(jlong, jlongArray, NewLongArray, is_number, number_value, 0, SetLongArrayRegion, "SetLongArrayRegion")
#define SET_FLOAT_ARRAY_VALUE() SET_ARRAY_VALUE(jfloat, jfloatArray, NewFloatArray, is_number, number_value, 0, SetFloatArrayRegion, "SetFloatArrayRegion")
#define SET_DOUBLE_ARRAY_VALUE() SET_ARRAY_VALUE(jdouble, jdoubleArray, NewDoubleArray, is_number, number_value, 0, SetDoubleArrayRegion, "SetDoubleArrayRegion")
#define SET_BOOLEAN_ARRAY_VALUE() SET_ARRAY_VALUE(jboolean, jbooleanArray, NewBooleanArray, is_bool, bool_value, 0, SetBooleanArrayRegion, "SetBooleanArrayRegion")

// className必须是com/huya/mtp/TestReq这样的形式
jobject json11ToJObject(JNIEnv *jniEnv, const json11::Json &json, const char *className) {
    try {
        L_T_D(LOG_TAG, "json11ToJObject -- className is %s", className);
        auto fieldsMap = new std::map<const char *, jobject, ComByStr>();
        getFieldIdsFromJClass(jniEnv, className, fieldsMap);
        jclass fieldClass = jniEnv->FindClass("java/lang/reflect/Field");
        jmethodID getTypeMethodId = jniEnv->GetMethodID(fieldClass, "getType", "()Ljava/lang/Class;");
        // jmethodID getNameMethodId = jniEnv->GetMethodID(fieldClass, "getName", "()Ljava/lang/String;");
        jclass classClass = jniEnv->FindClass("java/lang/Class");
        jmethodID toStringMethodId = jniEnv->GetMethodID(classClass, "toString", "()Ljava/lang/String;");
        // char fieldsMapStr[JTC_BUF_LEN * 100];
        int len = 0;
        auto fieldsTypeMap = new std::map<const char *, const char *, ComByStr>();
        for (auto it = fieldsMap->begin(); it != fieldsMap->end(); it++) {
            char *fieldType = new char[JTC_BUF_LEN];
            jobject typeObj = jniEnv->CallObjectMethod(it->second, getTypeMethodId);
            jStringToCharArray(jniEnv, (jstring) jniEnv->CallObjectMethod(typeObj, toStringMethodId), fieldType);
            jniEnv->DeleteLocalRef(typeObj);
            fieldsTypeMap->insert(std::make_pair(it->first, fieldType));
            // char temp[JTC_BUF_LEN];
            // sprintf(temp, (it == fieldsMap->begin() ? "%s: %s" : ", %s: %s"), it->first, fieldType);
            // strcpy(fieldsMapStr + len, temp);
            // len += strlen(temp);
        }
        // L_T_D(LOG_TAG,
        //       "json11ToJObject -- get fieldsMap fieldClass, getTypeMethod, classClass, toStringMethod, getNameMethod; and fieldsMapStr is {%s}",
        //       fieldsMapStr);

        jclass targetClass = jniEnv->FindClass(className);
        jmethodID defaultConstructorMethodId = jniEnv->GetMethodID(targetClass, "<init>", "()V");
        jobject resultJObject = jniEnv->NewObject(targetClass, defaultConstructorMethodId);
        // L_T_D(LOG_TAG, "json11ToJObject -- finish allocObj about class: %s, and json is %s", className, json.dump().c_str());

        if (json.is_object()) {
            json11::Json::object m = json.object_items();
            for (auto it = m.begin(); it != m.end(); it++) {
                const char *key = it->first.c_str();
                auto fieldIt = fieldsMap->find(key);
                if (fieldIt == fieldsMap->end()) {
                    continue;
                }
                json11::Json value = it->second;
                jobject field = fieldIt->second;
                char fieldTypeStrBuf[JTC_BUF_LEN];
                strcpy(fieldTypeStrBuf, fieldsTypeMap->find(key)->second);
                // L_T_D(LOG_TAG,
                //       "json11ToJObject -- begin process jsonKey: [%s], jsonValue: [%s], and fieldType: [%s]", key,
                //       value.dump().c_str(), fieldTypeStrBuf);

                bool valueIsString = value.is_string();
                bool fieldIsChar = strcmp(fieldTypeStrBuf, "char") == 0;
                if (valueIsString && fieldIsChar) {
                    int charValue = value.string_value()[0];
                    value = json11::Json(charValue);
                    SET_CHAR_VALUE();
                } else if (value.is_number()) {
                    if (fieldIsChar) {
                        SET_CHAR_VALUE();
                    } else if (strcmp(fieldTypeStrBuf, "byte") == 0) {
                        // jmethodID setValueMethodId = jniEnv->GetMethodID(fieldClass, "setShort", "(Ljava/lang/Object;S)V");
                        // jniEnv->CallVoidMethod(field, setValueMethodId, (jshort) (value.is_number() ? value.int_value() : 0));
                        // continue;
                        SET_BYTE_VALUE();
                    } else if (strcmp(fieldTypeStrBuf, "short") == 0) {
                        SET_SHORT_VALUE();
                    } else if (strcmp(fieldTypeStrBuf, "int") == 0) {
                        SET_INT_VALUE();
                    } else if (strcmp(fieldTypeStrBuf, "long") == 0) {
                        SET_LONG_VALUE();
                    } else if (strcmp(fieldTypeStrBuf, "float") == 0) {
                        SET_FLOAT_VALUE();
                    } else if (strcmp(fieldTypeStrBuf, "double") == 0) {
                        SET_DOUBLE_VALUE();
                    }
                    // L_T_D(LOG_TAG,
                    //       "json11ToJObject -- finish process jsonKey: [%s], jsonValue: [%s], and fieldType: [%s]", key,
                    //       value.dump().c_str(), fieldTypeStrBuf);
                    continue;
                } else if (value.is_bool() && strcmp(fieldTypeStrBuf, "boolean") == 0) {
                    SET_BOOLEAN_VALUE();
                    // L_T_D(LOG_TAG,
                    //       "json11ToJObject -- finish process jsonKey: [%s], jsonValue: [%s], and fieldType: [%s]", key,
                    //       value.dump().c_str(), fieldTypeStrBuf);
                    continue;
                }

                jmethodID setValueMethodId = jniEnv->GetMethodID(fieldClass, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                jobject finalValue = nullptr;
                if (valueIsString) {
                    if (strcmp(fieldTypeStrBuf, "class java.lang.String") == 0) {
                        finalValue = jniEnv->NewStringUTF(value.is_string() ? value.string_value().c_str() : "");
                    }
                } else if (value.is_array()) {
                    json11::Json::array valueArray = value.array_items();
                    int len = valueArray.size();
                    json11::Json first = valueArray[0];
                    if (strcmp(fieldTypeStrBuf, "class [C") == 0) {
                        // auto valueArray = value.array_items();
                        // int len = valueArray.size();
                        // jchar charsBuf[JTC_BUF_LEN];
                        // jcharArray charArray = jniEnv->NewCharArray(len);
                        // for (int i = 0; i < len; i++) {
                        //     json11::Json temp = valueArray[i];
                        //     charsBuf[i] = (jchar) (temp.is_number() ? temp.int_value() : 0);
                        // }
                        // jniEnv->SetCharArrayRegion(charArray, 0, len, charsBuf);
                        // finalValue = charArray;
                        if (first.is_string()) {
                            json11::Json::array tempArray = json11::Json::array();
                            for (unsigned int i = 0; i < len; i++) {
                                int charValue = valueArray[i].string_value()[0];
                                tempArray.push_back(json11::Json(charValue));
                            }
                            // L_T_D(LOG_TAG, "json11ToJObject -- tempArray: %s, and valueArray: %s",
                            //       json11::Json(tempArray).dump().c_str(), value.dump().c_str());
                            valueArray = tempArray;
                            SET_CHAR_ARRAY_VALUE();
                        } else if (first.is_number()) {
                            SET_CHAR_ARRAY_VALUE();
                        }
                    } else if (first.is_number()) {
                        if (strcmp(fieldTypeStrBuf, "class [B") == 0) {
                            SET_BYTE_ARRAY_VALUE();
                        } else if (strcmp(fieldTypeStrBuf, "class [S") == 0) {
                            SET_SHORT_ARRAY_VALUE();
                        } else if (strcmp(fieldTypeStrBuf, "class [I") == 0) {
                            SET_INT_ARRAY_VALUE();
                        } else if (strcmp(fieldTypeStrBuf, "class [J") == 0) {
                            SET_LONG_ARRAY_VALUE();
                        } else if (strcmp(fieldTypeStrBuf, "class [F") == 0) {
                            SET_FLOAT_ARRAY_VALUE();
                        } else if (strcmp(fieldTypeStrBuf, "class [D") == 0) {
                            SET_DOUBLE_ARRAY_VALUE();
                        }
                    } else if (first.is_bool() && strcmp(fieldTypeStrBuf, "class [Z") == 0) {
                        SET_BOOLEAN_ARRAY_VALUE();
                    } else if (first.is_string() && strcmp(fieldTypeStrBuf, "class [Ljava.lang.String;") == 0) {
                        jobjectArray finalArray = jniEnv->NewObjectArray(len, jniEnv->FindClass("java/lang/String"), nullptr);
                        for (int i = 0; i < len; i++) {
                            json11::Json temp = valueArray[i];
                            jniEnv->SetObjectArrayElement(finalArray, i, jniEnv->NewStringUTF(
                                    temp.is_string() ? temp.string_value().c_str() : ""));
                        }
                        finalValue = finalArray;
                    } else if (first.is_object() && startsWith(fieldTypeStrBuf, "class [L")) {
                        char fieldElementClassName[JTC_BUF_LEN];
                        getValidClassName(fieldTypeStrBuf, fieldElementClassName, 9, 8);
                        jclass tempClass = jniEnv->FindClass(fieldElementClassName);
                        jobjectArray finalArray = jniEnv->NewObjectArray(len, tempClass, nullptr);
                        for (int i = 0; i < len; i++) {
                            json11::Json temp = valueArray[i];
                            jniEnv->SetObjectArrayElement(finalArray, i, json11ToJObject(jniEnv, temp, fieldElementClassName));
                        }
                        finalValue = finalArray;
                        jniEnv->DeleteLocalRef(tempClass);
                    }
                } else if (value.is_object() && startsWith(fieldTypeStrBuf, "class ")) {
                    char fieldElementClassName[JTC_BUF_LEN];
                    getValidClassName(fieldTypeStrBuf, fieldElementClassName, 6, 6);
                    finalValue = json11ToJObject(jniEnv, value, fieldElementClassName);
                }
                if (finalValue) {
                    jniEnv->CallVoidMethod(field, setValueMethodId, resultJObject, finalValue);
                } else {
                    L_T_D(LOG_TAG, "json11ToJObject -- finalValue is null, so can't set value for field: %s", key);
                }
                // L_T_D(LOG_TAG,
                //       "json11ToJObject -- finish process jsonKey: [%s], jsonValue: [%s], and fieldType: [%s]", key,
                //       value.dump().c_str(), fieldTypeStrBuf);
            }
        } else {
            L_T_E(LOG_TAG, "json11ToJObject -- json str is not a valid object json string");
        }

        for (auto it = fieldsTypeMap->begin(); it != fieldsTypeMap->end(); it++) {
            delete[] it->second;
        }
        delete fieldsTypeMap;
        for (auto it = fieldsMap->begin(); it != fieldsMap->end(); it++) {
            delete[] it->first;
            jniEnv->DeleteLocalRef(it->second);
        }
        delete fieldsMap;
        jniEnv->DeleteLocalRef(fieldClass);
        jniEnv->DeleteLocalRef(classClass);
        jniEnv->DeleteLocalRef(targetClass);
        return resultJObject;
    } catch (std::exception &e) {
        L_T_E(LOG_TAG, "json11ToJObject -- %s", e.what());
    }
    return nullptr;
}

#define GET_VALUE(methodName, methodSig, initType, callMethod) \
    jmethodID getValueMethodId = jniEnv->GetMethodID(fieldClass, methodName, methodSig); \
    initType value = (initType) jniEnv->callMethod(field, getValueMethodId, obj); \
    json11::Json jValue = json11::Json(value); \
    resultJson.insert(std::make_pair(fieldNameStr, jValue)); /*resultJson[fieldName] = json11::Json(value);*/ \
    delete[] fieldName; \
    delete[] fieldType; \
    continue;

#define GET_CHAR_VALUE() GET_VALUE("getChar", "(Ljava/lang/Object;)C", int, CallCharMethod)
#define GET_BYTE_VALUE() GET_VALUE("getByte", "(Ljava/lang/Object;)B", int, CallByteMethod)
#define GET_SHORT_VALUE() GET_VALUE("getShort", "(Ljava/lang/Object;)S", int, CallShortMethod)
#define GET_INT_VALUE() GET_VALUE("getInt", "(Ljava/lang/Object;)I", int, CallIntMethod)
#define GET_LONG_VALUE() GET_VALUE("getLong", "(Ljava/lang/Object;)J", double, CallLongMethod)
#define GET_FLOAT_VALUE() GET_VALUE("getFloat", "(Ljava/lang/Object;)F", double, CallFloatMethod)
#define GET_DOUBLE_VALUE() GET_VALUE("getDouble", "(Ljava/lang/Object;)D", double, CallDoubleMethod)
#define GET_BOOLEAN_VALUE() GET_VALUE("getBoolean", "(Ljava/lang/Object;)Z", bool, CallBooleanMethod)

#define GET_ARRAY_VALUE(jArrayType, jItemType, getArrayMethod, itemType) \
    auto value = (jArrayType) jniEnv->CallObjectMethod(field, getValueMethodId, obj); \
    if (value != nullptr) { \
        jItemType *jvalue = jniEnv->getArrayMethod(value, JNI_FALSE); \
        int len = jniEnv->GetArrayLength(value); \
        json11::Json::array trueValue = json11::Json::array(); \
        for (int i = 0; i < len; i++) { \
            itemType valueI = jvalue[i]; \
            trueValue.push_back(json11::Json(valueI)); \
        } \
        json11::Json jTrueValue = json11::Json(trueValue); \
        resultJson.insert(std::make_pair(fieldNameStr, jTrueValue)); /*resultJson[fieldName] = json11::Json(trueValue);*/ \
        jniEnv->DeleteLocalRef(value); \
    }

#define GET_CHAR_ARRAY_VALUE() GET_ARRAY_VALUE(jcharArray, jchar, GetCharArrayElements, int)
#define GET_BYTE_ARRAY_VALUE() GET_ARRAY_VALUE(jbyteArray, jbyte, GetByteArrayElements, int)
#define GET_SHORT_ARRAY_VALUE() GET_ARRAY_VALUE(jshortArray, jshort, GetShortArrayElements, int)
#define GET_INT_ARRAY_VALUE() GET_ARRAY_VALUE(jintArray, jint, GetIntArrayElements, int)
#define GET_LONG_ARRAY_VALUE() GET_ARRAY_VALUE(jlongArray, jlong, GetLongArrayElements, double)
#define GET_FLOAT_ARRAY_VALUE() GET_ARRAY_VALUE(jfloatArray, jfloat, GetFloatArrayElements, double)
#define GET_DOUBLE_ARRAY_VALUE() GET_ARRAY_VALUE(jdoubleArray, jdouble, GetDoubleArrayElements, double)
#define GET_BOOLEAN_ARRAY_VALUE() GET_ARRAY_VALUE(jbooleanArray, jboolean, GetBooleanArrayElements, bool)

// className必须是像"com/huya/example/TestReq"这样的形式
json11::Json jObjectToJson11(JNIEnv *jniEnv, jobject obj, const char *className) {
    try {
        auto fieldsMap = new std::map<const char *, jobject, ComByStr>();
        getFieldIdsFromJClass(jniEnv, className, fieldsMap);
        jclass fieldClass = jniEnv->FindClass("java/lang/reflect/Field");

        jmethodID getTypeMethodId = jniEnv->GetMethodID(fieldClass, "getType", "()Ljava/lang/Class;");
        jmethodID getNameMethodId = jniEnv->GetMethodID(fieldClass, "getName", "()Ljava/lang/String;");
        jclass classClass = jniEnv->FindClass("java/lang/Class");
        jmethodID toStringMethodId = jniEnv->GetMethodID(classClass, "toString", "()Ljava/lang/String;");

        json11::Json::object resultJson = json11::Json::object();
        for (auto it = fieldsMap->begin(); it != fieldsMap->end(); it++) {
            char *fieldName = new char[JTC_BUF_LEN];
            char *fieldType = new char[JTC_BUF_LEN];
            jobject field = it->second;
            jStringToCharArray(jniEnv, (jstring) jniEnv->CallObjectMethod(field, getNameMethodId), fieldName);
            jobject typeObj = jniEnv->CallObjectMethod(field, getTypeMethodId);
            jStringToCharArray(jniEnv, (jstring) jniEnv->CallObjectMethod(typeObj, toStringMethodId), fieldType);
            jniEnv->DeleteLocalRef(typeObj);
            std::string fieldNameStr = std::string(fieldName);

            if (strcmp(fieldType, "char") == 0) {
                GET_CHAR_VALUE();
            } else if (strcmp(fieldType, "byte") == 0) {
                GET_BYTE_VALUE();
            } else if (strcmp(fieldType, "short") == 0) {
                GET_SHORT_VALUE();
            } else if (strcmp(fieldType, "int") == 0) {
                GET_INT_VALUE();
            } else if (strcmp(fieldType, "long") == 0) {
                GET_LONG_VALUE();
            } else if (strcmp(fieldType, "float") == 0) {
                GET_FLOAT_VALUE();
            } else if (strcmp(fieldType, "double") == 0) {
                GET_DOUBLE_VALUE();
            } else if (strcmp(fieldType, "boolean") == 0) {
                GET_BOOLEAN_VALUE();
            }

            jmethodID getValueMethodId = jniEnv->GetMethodID(fieldClass, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            if (strcmp(fieldType, "class java.lang.String") == 0) {
                auto value = (jstring) jniEnv->CallObjectMethod(field, getValueMethodId, obj);
                char valueBuf[JTC_BUF_LEN];
                jStringToCharArray(jniEnv, value, valueBuf);
                json11::Json jValueBuf = json11::Json(std::string(valueBuf));
                resultJson.insert(
                        std::make_pair(fieldNameStr, jValueBuf)); /*resultJson[fieldName] = json11::Json(std::string(valueBuf));*/
            } else if (strcmp(fieldType, "class [C") == 0) {
                GET_CHAR_ARRAY_VALUE();
            } else if (strcmp(fieldType, "class [B") == 0) {
                GET_BYTE_ARRAY_VALUE();
            } else if (strcmp(fieldType, "class [S") == 0) {
                GET_SHORT_ARRAY_VALUE();
            } else if (strcmp(fieldType, "class [I") == 0) {
                GET_INT_ARRAY_VALUE();
            } else if (strcmp(fieldType, "class [J") == 0) {
                GET_LONG_ARRAY_VALUE();
            } else if (strcmp(fieldType, "class [F") == 0) {
                GET_FLOAT_ARRAY_VALUE();
            } else if (strcmp(fieldType, "class [D") == 0) {
                GET_DOUBLE_ARRAY_VALUE();
            } else if (strcmp(fieldType, "class [Z") == 0) {
                GET_BOOLEAN_ARRAY_VALUE();
            } else if (strcmp(fieldType, "class [Ljava.lang.String;") == 0) {
                auto value = (jobjectArray) jniEnv->CallObjectMethod(field, getValueMethodId, obj);
                if (value != nullptr) {
                    int len = jniEnv->GetArrayLength(value);
                    json11::Json::array trueValue = json11::Json::array();
                    for (int i = 0; i < len; i++) {
                        auto valueI = (jstring) jniEnv->GetObjectArrayElement(value, i);
                        char valueIBuf[JTC_BUF_LEN];
                        jStringToCharArray(jniEnv, valueI, valueIBuf);
                        trueValue.push_back(json11::Json(std::string(valueIBuf)));
                    }
                    json11::Json jTrueValue = json11::Json(trueValue);
                    resultJson.insert(std::make_pair(fieldNameStr, jTrueValue));
                    jniEnv->DeleteLocalRef(value);
                }
            } else if (startsWith(fieldType, "class [L")) {
                auto value = (jobjectArray) jniEnv->CallObjectMethod(field, getValueMethodId, obj);
                if (value != nullptr) {
                    char fieldElementClassName[JTC_BUF_LEN];
                    getValidClassName(fieldType, fieldElementClassName, 9, 8);
                    int len = jniEnv->GetArrayLength(value);
                    json11::Json::array trueValue = json11::Json::array();
                    for (int i = 0; i < len; i++) {
                        json11::Json valueI = jObjectToJson11(jniEnv, jniEnv->GetObjectArrayElement(value, i), fieldElementClassName);
                        trueValue.push_back(valueI);
                    }
                    json11::Json jTrueValue = json11::Json(trueValue);
                    resultJson.insert(std::make_pair(fieldNameStr, jTrueValue));
                    jniEnv->DeleteLocalRef(value);
                }
            } else if (startsWith(fieldType, "class ")) {
                jobject value = jniEnv->CallObjectMethod(field, getValueMethodId, obj);
                if (value != nullptr) {
                    char fieldElementClassName[JTC_BUF_LEN];
                    getValidClassName(fieldType, fieldElementClassName, 6, 6);
                    json11::Json jValue = jObjectToJson11(jniEnv, value, fieldElementClassName);
                    resultJson.insert(std::make_pair(fieldNameStr, jValue));
                    /*resultJson[fieldName] = jObjectToJson11(jniEnv, value, fieldElementClassName);*/
                }
            } else {
                L_T_D(LOG_TAG, "jObjectToJson11 -- fieldType is invalid: %s, and fieldName is %s", fieldType, fieldName);
            }

            delete[] fieldName;
            delete[] fieldType;
        }

        for (auto it = fieldsMap->begin(); it != fieldsMap->end(); it++) {
            delete[] it->first;
            jniEnv->DeleteLocalRef(it->second);
        }
        delete fieldsMap;
        jniEnv->DeleteLocalRef(fieldClass);
        jniEnv->DeleteLocalRef(obj);
        return resultJson;
    } catch (std::exception &e) {
        L_T_E(LOG_TAG, "json11ToJObject -- %s", e.what());
    }
    return json11::Json();
}

void getFieldsFromJClass(JNIEnv *jniEnv, std::map<const char *, std::map<const char *, jobject, ComByStr> *, ComByStr> *allFieldsMap,
                         const char *className, std::map<const char *, jclass, ComByStr> *targetClasses, jclass fieldClass,
                         jclass classClass) {
    if (allFieldsMap->find(className) != allFieldsMap->end()) {
        return;
    }
    try {
        L_T_D(LOG_TAG, "className is %s", className);
        auto *fieldsMap = new std::map<const char *, jobject, ComByStr>();
        allFieldsMap->insert(std::make_pair(className, fieldsMap));
        jmethodID getDeclaredFieldsMethodId = jniEnv->GetMethodID(classClass, "getDeclaredFields", "()[Ljava/lang/reflect/Field;");
        jclass targetClass = (jclass) jniEnv->NewGlobalRef(jniEnv->FindClass(className));
        auto fields = (jobjectArray) jniEnv->CallObjectMethod(targetClass, getDeclaredFieldsMethodId);
        jmethodID setAccessibleMethodId = jniEnv->GetMethodID(fieldClass, "setAccessible", "(Z)V");
        jmethodID getNameMethodId = jniEnv->GetMethodID(fieldClass, "getName", "()Ljava/lang/String;");
        jmethodID getTypeMethodId = jniEnv->GetMethodID(fieldClass, "getType", "()Ljava/lang/Class;");
        jmethodID toStringMethodId = jniEnv->GetMethodID(classClass, "toString", "()Ljava/lang/String;");
        int len = jniEnv->GetArrayLength(fields);
        for (int i = 0; i < len; i++) {
            jobject field = jniEnv->GetObjectArrayElement(fields, i);
            jniEnv->CallVoidMethod(field, setAccessibleMethodId, JNI_TRUE);
            char *fieldName = new char[JTC_BUF_LEN];
            jStringToCharArray(jniEnv, (jstring) jniEnv->CallObjectMethod(field, getNameMethodId), fieldName);
            fieldsMap->insert(std::make_pair(fieldName, field));

            jobject fieldType = jniEnv->CallObjectMethod(field, getTypeMethodId);
            char fieldTypeStr[JTC_BUF_LEN];
            jStringToCharArray(jniEnv, (jstring) jniEnv->CallObjectMethod(fieldType, toStringMethodId), fieldTypeStr);
            if (startsWith(fieldTypeStr, "class ") && strcmp(fieldTypeStr, "class java.lang.String") != 0) {
                char fieldClass2[JTC_BUF_LEN];
                getValidClassName(fieldTypeStr, fieldClass2, 6, 6);
                getFieldsFromJClass(jniEnv, allFieldsMap, fieldClass2, targetClasses, fieldClass, classClass);
            }
            jniEnv->DeleteLocalRef(fieldType);
        }
    } catch (std::exception &e) {
        L_T_E(LOG_TAG, "getFieldsFromJClass -- %s", e.what());
    }
}

jobject
json11ToJObject(JNIEnv *jniEnv, const json11::Json &json, std::map<const char *, std::map<const char *, jobject>> *allFieldsMap,
                std::map<const char *, jclass> targetClasses, const char *className, jclass fieldClass, jclass classClass) {
    try {
    } catch (std::exception &ex) {
        L_T_E(LOG_TAG, "json11ToJObject -- exception: %s", ex.what());
    }
    return nullptr;
}

json11::Json
jObjectToJson11(JNIEnv *jniEnv, jobject obj, std::map<const char *, std::map<const char *, jobject>> *allFieldsMap,
                std::map<const char *, jclass> targetClasses, const char *className, jclass fieldClass, jclass classClass) {
    try {
    } catch (std::exception &ex) {
        L_T_E(LOG_TAG, "jObjectToJson11 -- exception: %s", ex.what());
    }
    return json11::Json();
}
