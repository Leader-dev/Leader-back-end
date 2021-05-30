package com.leader.api.util;

import java.util.concurrent.Callable;

public class ExceptionUtil {

    public static <T> T ignoreException(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
