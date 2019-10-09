//
// Created by 32494 on 2019/10/8.
//

#ifndef ANDROID_TEST_THREADSAFEQUEUE3_HPP
#define ANDROID_TEST_THREADSAFEQUEUE3_HPP

#include <queue>
#include <mutex>

template<class T>
class ThreadSafeQueue3 {
private:
    struct QueueNode3 {
        std::shared_ptr<T> data;
        std::unique_ptr<QueueNode3> next;
    };

    std::mutex headMutex;
    std::unique_ptr<QueueNode3> head;
    std::mutex tailMutex;
    QueueNode3 *tail;
    std::condition_variable dataCond;

    QueueNode3 *getTail() {
        std::lock_guard<std::mutex> tailLock(tailMutex);
        return tail;
    }

    std::unique_ptr<QueueNode3> popHead() {
        std::unique_ptr<QueueNode3> oldHead = std::move(head);
        head = std::move(oldHead->next);
        return oldHead;
    }

    std::unique_lock<std::mutex> waitForData() {
        std::unique_lock<std::mutex> headLock(headMutex);
        dataCond.wait(headLock, [this] { return this->head != this->getTail(); });
        return std::move(headLock);
    }

    std::unique_ptr<QueueNode3> waitPopHead() {
        std::unique_lock<std::mutex> headLock(waitForData());
        return popHead();
    }

    std::unique_ptr<QueueNode3> waitPopHead(T &value) {
        std::unique_lock<std::mutex> headLock(waitForData());
        value = std::move(*head->data);
        return popHead();
    }

    std::unique_ptr<QueueNode3> tryPopHead() {
        std::lock_guard<std::mutex> headLock(headMutex);
        if (head.get() == getTail()) {
            return std::unique_ptr<QueueNode3>();
        }
        return popHead();
    }

    std::unique_ptr<QueueNode3> tryPopHead(T &value) {
        std::lock_guard<std::mutex> headLock(headMutex);
        if (head.get() == getTail()) {
            return std::unique_ptr<QueueNode3>();
        }
        value = std::move(*head->data);
        return popHead();
    }

public:
    ThreadSafeQueue3() : head(new QueueNode3), tail(head.get()) {}

    ThreadSafeQueue3(const ThreadSafeQueue3 &other) = delete;

    ThreadSafeQueue3 &operator=(const ThreadSafeQueue3 &other) = delete;

    std::shared_ptr<T> tryPop() {
        std::unique_ptr<QueueNode3> const oldHead = tryPopHead();
        return oldHead ? oldHead->data : std::shared_ptr<T>();
    }

    bool tryPop(T &value) {
        std::unique_ptr<QueueNode3> oldHead = tryPopHead(value);
        return oldHead;
    }

    std::shared_ptr<T> waitAndPop() {
        std::unique_ptr<QueueNode3> const oldHead = waitPopHead();
        return oldHead->data;
    }

    void waitAndPop(T &value) {
        std::unique_ptr<QueueNode3> const oldHead = waitPopHead(value);
    }

    bool empty() {
        std::lock_guard<std::mutex> headLock(headMutex);
        return head == getTail();
    }

    void push(T value) {
        std::shared_ptr<T> newData(std::make_shared<T>(std::move(value)));
        std::unique_ptr<QueueNode3> p(new QueueNode3);
        std::lock_guard<std::mutex> tailLock(tailMutex);
        tail->data = newData;
        QueueNode3 *const newTail = p.get();
        tail->next = std::move(p);
        tail = newTail;
        dataCond.notify_one();
    }
};

#endif //ANDROID_TEST_THREADSAFEQUEUE3_HPP
