package com.leader.api.resource.storage;

import com.leader.api.util.InternalErrorException;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public interface StaticResourceStorage {

    URL generatePresignedUploadUrl(String url, Date expiration);

    void storeFile(String url, InputStream inputStream);

    boolean fileExists(String url);

    void deleteFile(String url);

    String getAccessStartUrl();

    default boolean allFilesExist(List<String> urls) {
        AtomicBoolean allExists = new AtomicBoolean(true);
        AtomicReference<RuntimeException> exception = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(urls.size());
        for (String url: urls) {
            new Thread(() -> {
                try {
                    if (!fileExists(url)) {
                        allExists.set(false);
                    }
                } catch (RuntimeException e) {
                    exception.set(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new InternalErrorException("InterruptedException occur.", exception.get());
        }
        if (exception.get() != null) {
            throw exception.get();
        }
        return allExists.get();
    }
}
