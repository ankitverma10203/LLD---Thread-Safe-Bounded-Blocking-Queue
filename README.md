# Thread Safe Bounded Blocking Queue

## Overview
This is a low-level design (LLD) implementation of a **Thread-Safe Bounded Blocking Queue** in Java. It's a generic data structure that safely handles concurrent producer-consumer scenarios with a fixed capacity.

## What is it?
A Bounded Blocking Queue is a thread-safe queue with the following characteristics:
- **Bounded**: Has a fixed maximum capacity
- **Blocking**: Operations block when the queue is full (enqueue) or empty (dequeue)
- **Thread-Safe**: Can be safely accessed by multiple threads simultaneously

## Implementation Details

### Core Components

#### 1. **ArrayDeque<T> - Internal Storage**
- Used as the underlying data structure to store queue elements
- Provides O(1) enqueue and dequeue operations
- Efficient memory usage with array-based implementation

#### 2. **ReentrantLock**
- Provides mutual exclusion for thread-safe access
- `fair = true`: Ensures fairness - threads acquire the lock in FIFO order
- Prevents race conditions when multiple threads access the queue simultaneously

#### 3. **Condition Variables**
- `notFullCondition`: Signals when space becomes available in the queue
  - Producers wait on this when queue is at capacity
  - Signaled after dequeue operation
- `nonEmptyCondition`: Signals when items become available in the queue
  - Consumers wait on this when queue is empty
  - Signaled after enqueue operation

### Why This Design?

#### **Why ReentrantLock instead of synchronized?**
- More fine-grained control over locking behavior
- Ability to create multiple condition variables for separate producer/consumer states
- Fairness guarantee ensures no thread starvation
- Better performance in high-contention scenarios

#### **Why Condition Variables?**
- More efficient than busy-waiting or polling
- `await()`: Thread releases lock and waits for notification
- `signal()`: Wakes up one waiting thread (better than notifyAll in terms of efficiency)
- Separates producer and consumer waiting states for optimal performance

#### **Why while loops instead of if statements?**
```java
while (capacity == queue.size()) {  // Instead of if
    notFullCondition.await();
}
```
- Protects against spurious wakeups (threads can wake up without being signaled)
- Ensures the condition is still true after reacquiring the lock
- Prevents race conditions in multi-threaded scenarios

#### **Why ArrayDeque?**
- More efficient than LinkedList for queue operations
- Better cache locality due to contiguous memory
- No extra memory overhead from node pointers

## Methods

### `enqueue(T item): boolean`
- **Purpose**: Add an item to the queue
- **Behavior**: 
  - Blocks if queue is at capacity
  - Wakes up waiting dequeue operations
- **Returns**: `true` on successful insertion

### `dequeue(): T`
- **Purpose**: Remove and return an item from the queue
- **Behavior**: 
  - Blocks if queue is empty
  - Wakes up waiting enqueue operations
- **Returns**: The dequeued item

## Thread Safety Guarantees
- **Mutual Exclusion**: Only one thread can modify the queue at a time
- **Visibility**: All operations are properly synchronized
- **Progress**: Threads waiting on conditions will eventually be notified
- **Fairness**: Lock acquisition follows FIFO order due to fair ReentrantLock

## Usage Pattern
```java
BoundedBlockingQueue<Integer> queue = new BoundedBlockingQueue<>(10);

// Producer Thread
queue.enqueue(5);  // Blocks if queue is full

// Consumer Thread
int item = queue.dequeue();  // Blocks if queue is empty
```

## Key Design Decisions Summary
| Aspect | Choice | Reason |
|--------|--------|--------|
| Synchronization | ReentrantLock | Fine-grained control + fairness |
| Conditions | Two separate conditions | Optimize producer/consumer wake-ups |
| Storage | ArrayDeque | O(1) operations + memory efficiency |
| Waiting Logic | while loops | Handle spurious wakeups safely |
| Lock Fairness | Fair = true | Prevent thread starvation |
