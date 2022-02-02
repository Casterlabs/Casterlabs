package co.casterlabs.caffeinated.webview.impl;

import java.util.function.Consumer;

import org.eclipse.swt.widgets.Display;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.Producer;
import co.casterlabs.caffeinated.util.async.Lock;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

public class SwtPromise<T> {
    private final Lock lock = new Lock();

    private @Getter boolean isCompleted;

    private T result;
    private Throwable err;

    private Consumer<T> then;
    private Consumer<Throwable> catcher = (t) -> t.printStackTrace();

//    private AsyncTask productionTask;

    public SwtPromise() {
        // For those that want to fulfill using the methods.
    }

    public SwtPromise(@NonNull Producer<T> producer) {
        Display.getDefault().asyncExec(() -> {
            try {
                T result = producer.produce();

                this.fulfill0(result);
            } catch (Throwable t) {
                this.fulfill0(t);
            }
        });
    }

    public void fulfill(@Nullable T result) {
//        if (this.productionTask != null) {
//            this.productionTask.cancel();
//        }

        this.fulfill0(result);
    }

    public void error(@NonNull Throwable err) {
//        if (this.productionTask != null) {
//            this.productionTask.cancel();
//        }

        this.fulfill0(err);
    }

    private void fulfill0(T result) {
        if (!this.isCompleted) {
            this.result = result;

            this.isCompleted = true;

            synchronized (this.lock) {
                this.lock.notifyAll();
            }

            if (this.then != null) {
                this.then.accept(this.result);
            }
        }
    }

    private void fulfill0(Throwable err) {
        if (!this.isCompleted) {
            this.err = err;

            this.isCompleted = true;

            synchronized (this.lock) {
                this.lock.notifyAll();
            }

            if (this.catcher != null) {
                this.catcher.accept(this.err);
            }
        }
    }

    public void then(Consumer<T> then) {
        this.then = then;

        if (this.isCompleted && this.isSuccess()) {
            this.then.accept(this.result);
        }
    }

    public void except(Consumer<Throwable> catcher) {
        this.catcher = catcher;

        if (this.isCompleted && !this.isSuccess()) {
            this.catcher.accept(this.err);
        }
    }

    @SneakyThrows
    public T await() /*throws Throwable, InterruptedException*/ {
        if (!this.isCompleted) {
            synchronized (this.lock) {
                this.lock.wait();
            }
        }

        if (this.isSuccess()) {
            return this.result;
        } else {
            throw this.err;
        }
    }

    public boolean isSuccess() {
        return this.err == null;
    }

}
