package com.leader.api.util.component;

import com.leader.api.util.InternalErrorException;
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

    public void assertDateIsAfterNow(Date thisDate) {
        Date currentDate = this.getCurrentDate();
        if (thisDate.getTime() < currentDate.getTime()) {
            throw new InternalErrorException("Invalid date! Must be after current date.");
        }
    }
}
