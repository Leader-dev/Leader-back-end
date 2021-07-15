package com.leader.api.util;

public interface ThrowableConsumer<T> {
    void accept(T value) throws Exception;
}
