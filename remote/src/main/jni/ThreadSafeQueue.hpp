#ifndef ANDROID_TEST_THREADSAFE_QUEUE_HPP
#define ANDROID_TEST_THREADSAFE_QUEUE_HPP

#include <mutex>
#include <queue>

template<class T>
class ThreadSafeQueue {
private:
    mutable std::mutex mMutex;
    std::queue<T> mQueue;
    std::condition_variable mDataCond;
public:
    ThreadSafeQueue() = default;

    ThreadSafeQueue(const ThreadSafeQueue &other) = delete;

    ThreadSafeQueue &operator=(const ThreadSafeQueue &other) = delete;

    /*
     * 使用迭代器为参数的构造函数,适用所有容器对象
     * */
    template<typename _InputIterator>
    ThreadSafeQueue(_InputIterator first, _InputIterator last) {
        for (auto iterator = first; iterator != last; ++iterator) {
            mQueue.push(*iterator);
        }
    }

    explicit ThreadSafeQueue(const std::queue<T> &c) : mQueue(c) {}

    /*
     * 使用初始化列表为参数的构造函数
     * */
    ThreadSafeQueue(std::initializer_list<T> list) : mQueue(list.begin(), list.end()) {}

    void push(T data) {
        std::lock_guard<std::mutex> lg(mMutex);
        mQueue.push(std::move(data));
        mDataCond.notify_one();
    }

    void waitAndPop(T &t) {
        std::unique_lock<std::mutex> ul(mMutex);
        mDataCond.wait(ul, [this] { return !this->mQueue.empty(); });
        t = std::move(mQueue.front());
        mQueue.pop();
    }

    std::shared_ptr<T> waitAndPop() {
        std::unique_lock<std::mutex> ul(mMutex);
        mDataCond.wait(ul, [this] { return !mQueue.empty(); });
        std::shared_ptr<T> t(std::make_shared<T>(mQueue.front()));
        mQueue.pop();
        return t;
    }

    bool tryPop(T &t) {
        std::lock_guard<std::mutex> lg(mMutex);
        if (mQueue.empty()) {
            return false;
        }
        t = mQueue.front();
        mQueue.pop();
        return true;
    }

    std::shared_ptr<T> tryPop() {
        std::lock_guard<std::mutex> lg(mMutex);
        if (mQueue.empty()) {
            return std::shared_ptr<T>();
        }
        std::shared_ptr<T> t(std::make_shared<T>(mQueue.front()));
        mQueue.pop();
        return t;
    }

    bool empty() {
        std::lock_guard<std::mutex> lg(mMutex);
        return mQueue.empty();
    }

    int size() {
        std::lock_guard<std::mutex> lg(mMutex);
        return mQueue.size();
    }
};

#endif

// [C/C++ 线程安全队列](https://blog.csdn.net/what951006/article/details/77916490)
// [线程安全的队列](https://blog.csdn.net/hwz119/article/details/1663253)
// [C++11:基于std::queue和std::mutex构建一个线程安全的队列](https://blog.csdn.net/10km/article/details/52067929)

// TODO: [C++并发实战17：线程安全的stack和queue](https://blog.csdn.net/liuxuejiang158blog/article/details/17523477)
//  上面的采用一个mutex保护整个queue使得线程迫使线程顺序化，为了减小锁的粒度，使用链表作为queue的底层数据结构，并采用一个虚拟节点使head和tail分开