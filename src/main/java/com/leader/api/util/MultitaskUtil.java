package com.leader.api.util;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MultitaskUtil {

    private static void awaitAndCheck(CountDownLatch latch, AtomicReference<RuntimeException> exception) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new InternalErrorException("Interrupted exception occur.", e);
        }
        if (exception.get() != null) {
            throw exception.get();
        }
    }

    public static void forI(int count, Consumer<Integer> consumer) {
        AtomicReference<RuntimeException> exception = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int currentI = i;
            new Thread(() -> {
                try {
                    consumer.accept(currentI);
                } catch (RuntimeException e) {
                    exception.set(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        awaitAndCheck(latch, exception);
    }

    public static <T> void forEach(Collection<T> items, Consumer<T> consumer) {
        AtomicReference<RuntimeException> exception = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(items.size());
        for (T item : items) {
            new Thread(() -> {
                try {
                    consumer.accept(item);
                } catch (RuntimeException e) {
                    exception.set(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        awaitAndCheck(latch, exception);
    }
}
