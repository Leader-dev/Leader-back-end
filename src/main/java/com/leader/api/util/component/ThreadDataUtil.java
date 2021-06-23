package com.leader.api.util.component;

import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ThreadDataUtil {

    private final ThreadLocal<Document> threadLocal = new ThreadLocal<>();

    public void initThreadData() {
        threadLocal.set(new Document());
    }

    public Document getThreadData() {
        return threadLocal.get();
    }

    public void removeThreadData() {
        threadLocal.remove();
    }

    public Object get(String key) {
        return getThreadData().get(key);
    }

    public <T> T get(String key, Class<T> type) {
        return getThreadData().get(key, type);
    }

    public void set(String key, Object value) {
        getThreadData().put(key, value);
    }

    public void remove(String key) {
        getThreadData().remove(key);
    }
}
