#ifndef ANDROID_TEST_THREADSAFE_QUEUE2_HPP
#define ANDROID_TEST_THREADSAFE_QUEUE2_HPP

#include <mutex>
#include <queue>

template<class T>
class ThreadSafeQueue2 {
private:
    struct QueueNode {
        std::shared_ptr<T> data;  // 通过shared_ptr管理资源T，那么资源的初始化和析构都在临界区外进行
        std::unique_ptr<QueueNode> next;
    };
    std::mutex headMutex;
    std::unique_ptr<QueueNode> head;
    std::mutex tailMutex;
    QueueNode *tail;

    QueueNode *getTail() {  // 返回tail用于判断head == tail
        std::lock_guard<std::mutex> tailLock(tailMutex);
        return tail;
    }

    std::unique_ptr<QueueNode> popHead() {  // 删除队首元素并返回该元素
        std::lock_guard<std::mutex> headLock(headMutex);
        if (head.get() == getTail()) {  // 判断队是否为空，get_tail()必选在head_mutex保护下，试想多个线程都在pop那么会出现什么情形?
            return nullptr;
        }
        std::unique_ptr<QueueNode> oldHead = std::move(head);
        head = std::move(oldHead->next);
        return oldHead;
    }

public:
    ThreadSafeQueue2() : head(new QueueNode()), tail(head.get()) {}

    ThreadSafeQueue2(const ThreadSafeQueue2 &other) = delete;

    ThreadSafeQueue2 &operator=(const ThreadSafeQueue2 &other) = delete;

    std::shared_ptr<T> tryPop() {
        std::unique_ptr<QueueNode> oldHead = popHead();
        return oldHead ? oldHead->data : std::shared_ptr<T>();
    }

    void push(T value) {  // 向队列添加一个元素，T的实例在临界区外创建即使抛出异常queue也没有被修改，而且加速多个线程的添加操作
        std::shared_ptr<T> newData(std::make_shared<T>(std::move(value)));
        // 注意make_shared可以提高效率，make_shared()函数要比直接创建shared_ptr对象的方式快且高效，因为它内部仅分配一次内存，消除了shared_ptr 构造时的开销
        std::unique_ptr<QueueNode> p(new QueueNode());
        // 创建一个虚拟节点，tail始终指向一个虚拟节点从而和head分开(队列中有元素时)，防止队列中只有元素时pop和top都操作的tail和head(若没有虚拟节点此时tail和head都是同一个节点)
        QueueNode *const newTail = p.get();
        std::lock_guard<std::mutex> tailLock(tailMutex);
        tail->data = newData;
        tail->next = std::move(p);
        tail = newTail;
    }
};

#endif

// [C/C++ 线程安全队列](https://blog.csdn.net/what951006/article/details/77916490)
// [线程安全的队列](https://blog.csdn.net/hwz119/article/details/1663253)
// [C++11:基于std::queue和std::mutex构建一个线程安全的队列](https://blog.csdn.net/10km/article/details/52067929)

// TODO: [C++并发实战17：线程安全的stack和queue](https://blog.csdn.net/liuxuejiang158blog/article/details/17523477)
//  上面的采用一个mutex保护整个queue使得线程迫使线程顺序化，为了减小锁的粒度，使用链表作为queue的底层数据结构，并采用一个虚拟节点使head和tail分开