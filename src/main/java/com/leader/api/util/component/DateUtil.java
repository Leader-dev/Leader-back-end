package com.leader.api.util.component;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DateUtil {

    public Date getCurrentDate() {
        return new Date();
    }

    public long getCurrentTime() {
        return new Date().getTime();
    }
}
