package com.leader.api.util.component;

import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ClientDataUtil {

    private final ThreadLocal<Document> clientDataThreadLocal = new ThreadLocal<>();

    public void setClientData(Document clientData) {
        clientDataThreadLocal.set(clientData);
    }

    public Document getClientData() {
        return clientDataThreadLocal.get();
    }

    public Document popClientData() {
        Document clientData = clientDataThreadLocal.get();
        clientDataThreadLocal.remove();
        return clientData;
    }

    public Object get(String key) {
        return getClientData().get(key);
    }

    public <T> T get(String key, Class<T> type) {
        return getClientData().get(key, type);
    }

    public void set(String key, Object value) {
        getClientData().put(key, value);
    }

    public void remove(String key) {
        getClientData().remove(key);
    }
}
