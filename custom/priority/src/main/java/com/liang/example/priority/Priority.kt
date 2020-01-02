package com.liang.example.priority

/**
 * 1. 异步
 * 2. 超时中断
 * 3. 优先级
 * 4. 重复次数
 * 5. 自主取消
 */
abstract class PriorityTask<Priority : Comparator<*>, Result : Any> {
    open var async = false
    open var timeout = -1
    open var repeatCount = -1
    open lateinit var priority: Priority
    open lateinit var manager: PriorityManager

    abstract fun call(): Result

    open fun cancel() {
        TODO()
    }
}

/**
 * 1. 优先级
 * 2. TODO
 */
open class PriorityObjectHolder<Priority : Comparator<*>, Value : Any> {
    open lateinit var priority: Priority
    open lateinit var manager: PriorityManager
    open lateinit var value: Value
}

/**
 * 1. 任务选择的策略，即如何选取任务并执行
 * 2. 任务执行的策略，即决定一些最长时间、并行执行任务的数量、TODO
 */
interface PriorityPolicy {}

/**
 * 1. 缓存 -- 保存 、 获取 、 查看
 * 2. 缓存上限、丢弃策略
 */
interface PriorityTargetStore {}

/**
 * 1. whiteBoard -- rxJava / handler / android-lifecycle-extension
 * 2. 多级优先队列
 * 3. 优先级别任务的同步、异步执行、超时、重复等等
 * 4. 优先级别任务的 增、删、改、查
 * 5. 记录各个优先级的各个任务执行的次数(application / activity / fragment)
 *
 *                    use by
 * PriorityManager <--------- PriorityPolicy
 *        |                     |
 *        |                     | use by
 *        |         use by      |
 * use by |------------------ PriorityTask / PriorityObjectHolder
 *        |                     |
 *        |                     | use by
 * PriorityTargetStore <--------|
 */
open class PriorityManager {}
