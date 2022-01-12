package co.casterlabs.caffeinated.bootstrap;

import java.util.ArrayDeque;
import java.util.Deque;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.async.AsyncTask;
import co.casterlabs.caffeinated.util.async.Lock;
import co.casterlabs.caffeinated.util.async.Promise;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class MainThread {
    private static Deque<Runnable> taskQueue = new ArrayDeque<>();
    private static Thread mainThread;
    private static Lock lock;

    public static void park(@Nullable Runnable continued) {
        assert mainThread == null : "Already parked.";

        mainThread = Thread.currentThread();

        if (continued != null) {
            new AsyncTask(continued);
        }

        lock = new Lock(); // Create the resource on THIS thread.

        while (true) {

            // Process queue.
            while (!taskQueue.isEmpty()) {
                processOne();
            }

            synchronized (lock) {
                // Sleep until we get another task.
                try {
                    Thread.yield(); // The thread may lie dormant for a while.
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private static void processOne() {
        Runnable popped = taskQueue.pop();

        try {
            popped.run();
        } catch (Throwable t) {
            FastLogger.logStatic(LogLevel.SEVERE, "An exception occurred whilst processing task on the main thread:");
            FastLogger.logException(t);
        }
    }

    /**
     * This is here since the SWT event loop mandates special behavior.
     * 
     * @return true if the queue was processed.
     */
    @Deprecated
    public static boolean processTaskQueue() {
        assert isMainThread() : "This method may only be called on the main thread.";

        if (taskQueue.isEmpty()) {
            return false;
        } else {
            while (!taskQueue.isEmpty()) {
                processOne();
            }
            return true;
        }
    }

    public static void submitTask(@NonNull Runnable task) {
        assert !isMainThread() : "Submitting a task on the main thread and waiting for it will ALWAYS result in a deadlock! So, for your benefit, you are not allowed to submit tasks from the main thread.";

        taskQueue.add(task);

        synchronized (lock) {
            lock.notify();
        }
    }

    public static void submitTaskAndWait(@NonNull Runnable task) throws InterruptedException, Throwable {
        Promise<Void> promise = new Promise<>();

        submitTask(() -> {
            try {
                task.run();
                promise.fulfill(null);
            } catch (Throwable t) {
                promise.error(t);
                throw t;
            }
        });

        promise.await();
    }

    public static void executeOffOfMainThread(@NonNull Runnable task) {
        if (isMainThread()) {
            new AsyncTask(task);
        } else {
            task.run();
        }
    }

    public static boolean isMainThread() {
        return Thread.currentThread() == mainThread;
    }

}
