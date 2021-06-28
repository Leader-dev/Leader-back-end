package com.leader.api.resource.storage;

import java.io.InputStream;

public interface StaticResourceStorage {

    void storeFile(String url, InputStream inputStream);

    boolean fileExists(String url);

    void deleteFile(String url);

    String getAccessStartUrl();
}
