package com.liang.example.basic_ktx

import java.lang.Exception
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

fun Class<*>.isExtendExclusive(cls: Class<*>): Boolean {
    val parentCls = superclass ?: return false
    if (parentCls == cls) {
        return true
    }
    return parentCls.isExtendExclusive(cls)
}

fun Class<*>.isExtendInclusive(cls: Class<*>): Boolean {
    if (this == cls) {
        return true
    }
    return isExtendExclusive(cls)
}

fun Class<*>.isImplementExclusive(cls: Class<*>): Boolean {
    val parentInterfaces = interfaces
    if (parentInterfaces.isEmpty()) {
        return false
    }
    if (cls in parentInterfaces) {
        return true
    }
    parentInterfaces.forEach {
        if (it.isImplementExclusive(cls)) {
            return true
        }
    }
    return false
}

fun Class<*>.isImplementInclusive(cls: Class<*>): Boolean {
    if (this == cls) {
        return true
    }
    return isImplementExclusive(cls)
}

@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")
object ReflectHelper {
    val fields: ConcurrentHashMap<String, Field> = ConcurrentHashMap()
    val constructors: ConcurrentHashMap<String, Constructor<*>> = ConcurrentHashMap()
    val methods: ConcurrentHashMap<String, Method> = ConcurrentHashMap()
    val classes: ConcurrentHashMap<String, Class<*>> = ConcurrentHashMap()

    fun <T> tryAction(action: () -> T): T? = try {
        action()
    } catch (e: Exception) {
        println(e)
        if (ConfigApi.debug) {
            throw e
        }
        null
    }

    fun findCls(name: String): Class<*>? {
        var cls = classes[name]
        if (cls == null) {
            try {
                cls = Class.forName(name)
                classes[name] = cls
            } catch (e: Exception) {
                if (ConfigApi.debug) {
                    throw e
                }
                return null
            }
        }
        return cls
    }

    fun findCtor(cls: Class<*>?, vararg args: Class<*>?): Constructor<*>? {
        if (cls == null) {
            return null
        }
        val key = cls.name
        var constructor = constructors[key]
        if (constructor == null) {
            try {
                constructor = cls.getDeclaredConstructor(*args)
                constructor.isAccessible = true
                constructors[key] = constructor
            } catch (e: Exception) {
                if (ConfigApi.debug) {
                    throw e
                }
                return null
            }
        }
        return constructor
    }

    fun calculateName(thisObj: Any, name: String) = thisObj::class.java.toString() + name  // todo: 其实不对，因为方法可能name一样，但参数列表不一样
    fun calculateName(cls: Class<*>, name: String) = cls.toString() + name  // todo: 其实不对，因为方法可能name一样，但参数列表不一样

    fun findMethod(name: String, cls: Class<*>?, vararg args: Class<*>?): Method? {
        if (cls == null) {
            return null
        }
        val key = calculateName(cls, name)
        var method = methods[key]
        if (method == null) {
            try {
                method = cls.getDeclaredMethod(name, *args)
                method.isAccessible = true
                methods[key] = method
            } catch (e: Exception) {
                if (ConfigApi.debug) {
                    throw e
                }
                return null
            }
        }
        return method
    }

    fun findField(name: String, cls: Class<*>?): Field? {
        if (cls == null) {
            return null
        }
        val key = calculateName(cls, name)
        var field = fields[key]
        if (field == null) {
            try {
                field = cls.getDeclaredField(name)
                field.isAccessible = true
                fields[key] = field
            } catch (e: Exception) {
                if (ConfigApi.debug) {
                    throw e
                }
                return null
            }
        }
        return field
    }

    // 下面都是封装

    fun findMethod(name: String, thisObj: Any?, vararg args: Any?): Method? =
            findMethod(name, if (thisObj != null) thisObj::class.java else null, *argClasses(args))

    fun findField(name: String, thisObj: Any?): Field? =
            findField(name, if (thisObj != null) thisObj::class.java else null)

    fun <T> newInstance(cls: Class<*>?, vararg args: Any?): T? =
            tryAction { findCtor(cls, *argClasses(args))?.newInstance(*args) } as? T

    fun <T> newInstance(name: String, vararg args: Any?): T? =
            tryAction { findCls(name)?.let { clazz -> findCtor(clazz, *argClasses(args))?.newInstance(*args) } } as? T

    fun <T> invoke(name: String, thisObj: Any?, vararg args: Any?): T? =
            tryAction { findMethod(name, if (thisObj != null) thisObj::class.java else null, *argClasses(args))?.invoke(thisObj, *args) } as? T

    fun <T> invokeS(name: String, cls: Class<*>?, vararg args: Any?): T? =
            tryAction { findMethod(name, cls, *argClasses(args))?.invoke(null, *args) } as? T

    fun invokeN(name: String, thisObj: Any?, vararg args: Any?) {
        tryAction { findMethod(name, if (thisObj != null) thisObj::class.java else null, *argClasses(args))?.invoke(thisObj, *args) }
    }

    fun argClasses(args: Array<out Any?>): Array<Class<*>> = args.map {
        when (it) {
            is Byte -> Byte::class.java
            is Short -> Short::class.java
            is Int -> Int::class.java
            is Long -> Long::class.java
            is Float -> Float::class.java
            is Double -> Double::class.java
            is Char -> Char::class.java
            else -> it!!::class.java
        }
    }.toTypedArray()

    fun get(name: String, thisObj: Any?): Any? = tryAction { findField(name, thisObj)?.get(thisObj) }
    fun getByte(name: String, thisObj: Any?): Byte = tryAction { findField(name, thisObj)?.getByte(thisObj) } ?: 0
    fun getShort(name: String, thisObj: Any?): Short = tryAction { findField(name, thisObj)?.getShort(thisObj) } ?: 0
    fun getInt(name: String, thisObj: Any?): Int = tryAction { findField(name, thisObj)?.getInt(thisObj) } ?: 0
    fun getLong(name: String, thisObj: Any?): Long = tryAction { findField(name, thisObj)?.getLong(thisObj) } ?: 0
    fun getFloat(name: String, thisObj: Any?): Float = tryAction { findField(name, thisObj)?.getFloat(thisObj) } ?: 0f
    fun getDouble(name: String, thisObj: Any?): Double = tryAction { findField(name, thisObj)?.getDouble(thisObj) } ?: 0.0
    fun getChar(name: String, thisObj: Any?): Char = tryAction { findField(name, thisObj)?.getChar(thisObj) } ?: '\u0000'
    fun getBoolean(name: String, thisObj: Any?): Boolean = tryAction { findField(name, thisObj)?.getBoolean(thisObj) } ?: false

    fun set(name: String, thisObj: Any?, value: Any?) = tryAction { findField(name, thisObj)?.set(thisObj, value) }
    fun setByte(name: String, thisObj: Any?, value: Byte?) = tryAction { findField(name, thisObj)?.setByte(thisObj, value ?: 0) }
    fun setShort(name: String, thisObj: Any?, value: Short?) = tryAction { findField(name, thisObj)?.setShort(thisObj, value ?: 0) }
    fun setInt(name: String, thisObj: Any?, value: Int?) = tryAction { findField(name, thisObj)?.setInt(thisObj, value ?: 0) }
    fun setLong(name: String, thisObj: Any?, value: Long?) = tryAction { findField(name, thisObj)?.setLong(thisObj, value ?: 0L) }
    fun setFloat(name: String, thisObj: Any?, value: Float?) = tryAction { findField(name, thisObj)?.setFloat(thisObj, value ?: 0f) }
    fun setDouble(name: String, thisObj: Any?, value: Double?) = tryAction { findField(name, thisObj)?.setDouble(thisObj, value ?: 0.0) }
    fun setChar(name: String, thisObj: Any?, value: Char?) = tryAction { findField(name, thisObj)?.setChar(thisObj, value ?: '\u0000') }
    fun setBoolean(name: String, thisObj: Any?, value: Boolean?) = tryAction { findField(name, thisObj)?.setBoolean(thisObj, value ?: false) }

    fun getStatic(name: String, cls: Class<*>?): Any? = tryAction { findField(name, cls)?.get(null) }
    fun getByteStatic(name: String, cls: Class<*>?): Byte = tryAction { findField(name, cls)?.getByte(null) } ?: 0
    fun getShortStatic(name: String, cls: Class<*>?): Short = tryAction { findField(name, cls)?.getShort(null) } ?: 0
    fun getIntStatic(name: String, cls: Class<*>?): Int = tryAction { findField(name, cls)?.getInt(null) } ?: 0
    fun getLongStatic(name: String, cls: Class<*>?): Long = tryAction { findField(name, cls)?.getLong(null) } ?: 0
    fun getFloatStatic(name: String, cls: Class<*>?): Float = tryAction { findField(name, cls)?.getFloat(null) } ?: 0f
    fun getDoubleStatic(name: String, cls: Class<*>?): Double = tryAction { findField(name, cls)?.getDouble(null) } ?: 0.0
    fun getCharStatic(name: String, cls: Class<*>?): Char = tryAction { findField(name, cls)?.getChar(null) } ?: '\u0000'
    fun getBooleanStatic(name: String, cls: Class<*>?): Boolean = tryAction { findField(name, cls)?.getBoolean(null) } ?: false

    fun setStatic(name: String, cls: Class<*>?, value: Any?) = tryAction { findField(name, cls)?.set(null, value) }
    fun setByteStatic(name: String, cls: Class<*>?, value: Byte?) = tryAction { findField(name, cls)?.setByte(null, value ?: 0) }
    fun setShortStatic(name: String, cls: Class<*>?, value: Short?) = tryAction { findField(name, cls)?.setShort(null, value ?: 0) }
    fun setIntStatic(name: String, cls: Class<*>?, value: Int?) = tryAction { findField(name, cls)?.setInt(null, value ?: 0) }
    fun setLongStatic(name: String, cls: Class<*>?, value: Long?) = tryAction { findField(name, cls)?.setLong(null, value ?: 0L) }
    fun setFloatStatic(name: String, cls: Class<*>?, value: Float?) = tryAction { findField(name, cls)?.setFloat(null, value ?: 0f) }
    fun setDoubleStatic(name: String, cls: Class<*>?, value: Double?) = tryAction { findField(name, cls)?.setDouble(null, value ?: 0.0) }
    fun setCharStatic(name: String, cls: Class<*>?, value: Char?) = tryAction { findField(name, cls)?.setChar(null, value ?: '\u0000') }
    fun setBooleanStatic(name: String, cls: Class<*>?, value: Boolean?) = tryAction { findField(name, cls)?.setBoolean(null, value ?: false) }
}
