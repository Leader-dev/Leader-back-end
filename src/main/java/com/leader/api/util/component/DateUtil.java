package com.leader.api.util.component;

import com.leader.api.util.InternalErrorException;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class DateUtil {

    public Date getCurrentDate() {
        return new Date();
    }

    public long getCurrentTime() {
        return new Date().getTime();
    }

    public Date getTodayZero() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long todayZero = calendar.getTimeInMillis();
        return new Date(todayZero);
    }

    public void assertDateIsAfterNow(Date thisDate) {
        Date currentDate = this.getCurrentDate();
        if (thisDate.getTime() < currentDate.getTime()) {
            throw new InternalErrorException("Invalid date! Must be after current date.");
        }
    }
}
