package com.liang.example.basic_block

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor

var mainHandler: Handler? = null

/**
 * 1. 能根据 msg.what 指定 Handler.Callback
 * 2. 能根据 msg.arg1 指定线程
 * 3. 有对应的 release 释放资源
 */
@Suppress("MemberVisibilityCanBePrivate")
open class BlockHandler : Handler {
    companion object {
        const val TYPE_IMMEDIATE = 0  // Handler所在线程执行
        const val TYPE_NEW_THREAD = 1  // 新线程执行
        const val TYPE_SINGLE_THREAD = 2  // 所有这种类型的callback会放在一起，顺序执行
        const val TYPE_MAIN_THREAD = 3  // 主线程执行
        const val TYPE_ASYNC_TASK_POOL = 4  // asyncTask自带的线程池执行
    }

    /**
     * 带有token的Handler.Callback
     *
     * @param callback
     * @param token
     */
    open class TokenCallback(callback: Callback, token: Any) : Callback, WithToken<Callback>(callback, token) {
        override fun handleMessage(msg: Message): Boolean = data.handleMessage(msg)
    }

    /**
     * 带有token的executor
     *
     * @param executor
     * @param token
     */
    open class TokenExecutor(executor: Executor, token: Any) : Executor, WithToken<Executor>(executor, token) {
        override fun execute(command: Runnable) = data.execute(command)
    }

    // message的处理器，根据message.what和message.obj来分配message给对应的callback
    protected val callbackMap: MutableMap<Int, Callback> = ConcurrentHashMap()
    // message的线程提供者，根据message.arg1和message.obj来制定message的线程
    protected val executorMap: MutableMap<Int, Executor> = ConcurrentHashMap()

    constructor() : super()
    constructor(looper: Looper) : super(looper)

    /**
     * 添加 / 删除 executor
     * 1. executor 为 null ，删除
     * 2. token 不为 null ，executor 转化为 TokenExecutor ，然后添加
     * 3. 直接添加 executor
     *
     * @param arg1 message.arg1 和这一个相同的 message 会被分配到这个 executor
     * @param executor 真正的线程提供者
     * @param token 方便删除 executor
     */
    @JvmOverloads
    open fun dealExecutor(arg1: Int, executor: Executor? = null, token: Any? = null): Executor? = when {
        executor == null -> executorMap.remove(arg1)
        token == null -> executorMap.put(arg1, executor)
        else -> executorMap.put(arg1, TokenExecutor(executor, token))
    }

    /**
     * 添加/删除 Handler.Callback
     * 1. callback 为 null ，删除
     * 2. token 不为 null ，callback 转化为 TokenCallback ，然后添加
     * 3. 直接添加 callback
     *
     * @param what message.what 和它相同的 message 会被分配到这个 callback
     * @param callback message 处理者
     * @param token 方便删除 callback
     */
    @JvmOverloads
    open fun dealCallback(what: Int, callback: Callback? = null, token: Any? = null): Callback? = when {
        callback == null -> callbackMap.remove(what)
        token == null -> callbackMap.put(what, callback)
        else -> callbackMap.put(what, TokenCallback(callback, token))
    }

    /**
     * @param what 指定消息处理者 callback
     * @param delayMillis
     * @param arg1 指定线程提供者 executor
     * @param token 方便删除
     */
    @JvmOverloads
    open fun sendEmptyMessage(what: Int, delayMillis: Long = 0L, arg1: Int = TYPE_IMMEDIATE, token: Any? = null) {
        sendMessageDelayed(Message.obtain().apply {
            this.what = what
            this.arg1 = arg1
            this.obj = token
        }, delayMillis)
    }

    /**
     * @param what 指定消息处理者 callback
     * @param msg
     * @param delayMillis
     * @param arg1 指定线程提供者 executor
     * @param token 方便删除
     */
    @JvmOverloads
    open fun sendMessage(what: Int, msg: Message, delayMillis: Long = 0L, arg1: Int = TYPE_IMMEDIATE, token: Any? = null) {
        sendMessageDelayed(msg.apply {
            this.what = what
            this.arg1 = arg1
            this.obj = token
        }, delayMillis)
    }

    /**
     * @param r
     * @param delayMillis
     * @param arg1 指定线程提供者 executor
     * @param token 方便删除
     */
    @JvmOverloads
    open fun post(r: Runnable, delayMillis: Long = 0L, arg1: Int = TYPE_IMMEDIATE, token: Any? = null) {
        sendMessageDelayed(Message.obtain(this, r).apply {
            this.arg1 = arg1
            this.obj = token
        }, delayMillis)
    }

    /**
     * 重写了 Handler 的 dispatchMessage ，将 msg.callback 交给指定线程执行
     *
     * @param msg
     */
    override fun dispatchMessage(msg: Message) {
        if (msg.callback != null) {
            when (msg.arg1) {
                TYPE_IMMEDIATE -> msg.callback.run()
                TYPE_MAIN_THREAD -> mainHandler?.post(msg.callback)
                TYPE_NEW_THREAD -> Thread(msg.callback).start()
                TYPE_SINGLE_THREAD -> AsyncTask.SERIAL_EXECUTOR.execute(msg.callback)
                TYPE_ASYNC_TASK_POOL -> AsyncTask.THREAD_POOL_EXECUTOR.execute(msg.callback)
                else -> executorMap[msg.arg1]?.execute(msg.callback)
            }
            return
        }
        super.dispatchMessage(msg)
    }

    /**
     * 重写了 Handler 的 handleMessage ，将 message 都交给 callback 处理，并指定线程执行
     *
     * @param msg
     */
    override fun handleMessage(msg: Message) {
        if (callbackMap.containsKey(msg.what)) {
            val callback = callbackMap[msg.what]!!
            val runnable = Runnable { callback.handleMessage(msg) }
            when (msg.arg1) {
                TYPE_IMMEDIATE -> runnable.run()
                TYPE_MAIN_THREAD -> mainHandler?.post(runnable)
                TYPE_NEW_THREAD -> Thread(runnable).start()
                TYPE_SINGLE_THREAD -> AsyncTask.SERIAL_EXECUTOR.execute(runnable)
                TYPE_ASYNC_TASK_POOL -> AsyncTask.THREAD_POOL_EXECUTOR.execute(runnable)
                else -> executorMap[msg.arg1]?.execute(runnable)
            }
            return
        }
        super.handleMessage(msg)
    }

    /**
     * 根据 token 删除 callbacks / executors ，如果 token 为 null ，删除全部
     *
     * @param token
     */
    open fun release(token: Any?): List<Pair<Int, Executor>> {
        removeCallbacksAndMessages(token)
        return if (token == null) {
            callbackMap.clear()
            val result = executorMap.toList()
            executorMap.clear()
            return result
        } else {
            removeIf(callbackMap) {
                val callback = it.value
                callback is TokenExecutor && callback.token == token
            }
            removeIf(executorMap) {
                val executor = it.value
                executor is TokenCallback && executor.token == token
            }
        }
    }
}
