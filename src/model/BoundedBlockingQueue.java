package model;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBlockingQueue<T> {

    private final Queue<T> queue;
    private final int capacity;
    private final ReentrantLock lock;
    private final Condition notFullCondition;
    private final Condition nonEmptyCondition;

    public BoundedBlockingQueue(int capacity) {
        this.queue = new ArrayDeque<>(capacity);
        this.capacity = capacity;
        this.lock = new ReentrantLock(true);
        this.notFullCondition = lock.newCondition();
        this.nonEmptyCondition = lock.newCondition();
    }

    public boolean enqueue(T item) throws InterruptedException {
        lock.lock();
        try {
            while (capacity == queue.size()) {
                notFullCondition.await();
            }

            queue.offer(item);
            nonEmptyCondition.signal();
        } finally {
            lock.unlock();
        }
        return true;
    }

    public T dequeue() throws InterruptedException {
        lock.lock();

        try {
            while (queue.isEmpty()) {
                nonEmptyCondition.await();
            }

            T item =  queue.poll();
            notFullCondition.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }
}
