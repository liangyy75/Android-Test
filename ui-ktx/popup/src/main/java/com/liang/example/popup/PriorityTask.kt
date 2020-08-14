package com.liang.example.popup

import android.os.Handler
import android.os.HandlerThread
import com.liang.example.popup.PriorityTask.Companion.INVALID_PRIORITY
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author liangyuying.lyy75
 * @date 2020/8/14
 * <p>
 * 弹窗管理
 */

interface PriorityTask {
    fun canUse(useful: PriorityManager.UsefulCallback)
    fun use()
    fun priority(): Int

    companion object {
        const val INVALID_PRIORITY = 0
    }
}

interface PriorityManager {
    fun add(task: PriorityTask)
    fun remove(task: PriorityTask)
    fun remove(priority: Int)
    fun get(priority: Int): List<PriorityTask>
    fun addCallback(callback: LifecycleCallback)
    fun removeCallback(callback: LifecycleCallback)
    fun run()

    interface LifecycleCallback {
        fun onUsed(task: PriorityTask, canUse: Boolean)
        fun onAdded(task: PriorityTask)
        fun onRemoved(task: PriorityTask)
        fun targetPriority(): Int = INVALID_PRIORITY
    }

    interface UsefulCallback {
        fun onResult(result: Boolean)
    }
}

open class PriorityManagerImpl : PriorityManager, PriorityManager.UsefulCallback {
    open var currentPriority: Int = INVALID_PRIORITY + 1
    open val tasks = CopyOnWriteArrayList<PriorityTask>()
    open val callbacks = CopyOnWriteArrayList<PriorityManager.LifecycleCallback>()
    open var handlerThread: HandlerThread? = null
    open var handler: Handler? = null
    open var async: Boolean = true
        set(value) {
            field = value
            if (field && handlerThread == null) {
                handlerThread = HandlerThread("PriorityManagerImpl")
                handler = Handler(handlerThread!!.looper)
            } else if (!field && handlerThread != null) {
                handlerThread!!.quit()
                handlerThread = null
                handler?.removeCallbacksAndMessages(null)
                handler = null
            }
        }

    override fun add(task: PriorityTask) {
        tasks.add(task)
        val taskPriority = task.priority()
        callbacks.forEach {
            if (it.targetPriority() == INVALID_PRIORITY || it.targetPriority() == taskPriority) {
                it.onAdded(task)
            }
        }
    }

    override fun remove(task: PriorityTask) {
        tasks.remove(task)
        val taskPriority = task.priority()
        callbacks.forEach {
            if (it.targetPriority() == INVALID_PRIORITY || it.targetPriority() == taskPriority) {
                it.onRemoved(task)
            }
        }
    }

    override fun remove(priority: Int) {
        get(priority).forEach {
            remove(it)
        }
    }

    override fun get(priority: Int): List<PriorityTask> {
        return tasks.filter { it.priority() == priority }
    }

    override fun addCallback(callback: PriorityManager.LifecycleCallback) {
        callbacks.remove(callback)
    }

    override fun removeCallback(callback: PriorityManager.LifecycleCallback) {
        callbacks.remove(callback)
    }

    override fun onResult(result: Boolean) {
        val task = tasks.find { it.priority() == currentPriority }
        if (result) {
            if (task != null) {
                task.use()  // 转到主线程执行
            } else {
                currentPriority++
            }
        }
        if (task != null) {
            remove(task)
        }
        run()
    }

    override fun run() {
        if (async && handler != null) {
            handler!!.post {
                val task = tasks.find { it.priority() == currentPriority }
                if (task != null) {
                    task.canUse(this)
                } else {
                    currentPriority++
                    run()
                }
            }
        }
    }
}
