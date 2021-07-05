package com.leader.api.resource.storage;

import com.leader.api.util.MultitaskUtil;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface StaticResourceStorage {

    URL generatePresignedUploadUrl(String url, Date expiration);

    void storeFile(String url, InputStream inputStream);

    boolean fileExists(String url);

    void deleteFile(String url);

    String getAccessStartUrl();

    default boolean allFilesExist(List<String> urls) {
        AtomicBoolean allExists = new AtomicBoolean(true);
        MultitaskUtil.forEach(urls, url -> {
            if (!fileExists(url)) {
                allExists.set(false);
            }
        });
        return allExists.get();
    }
}
