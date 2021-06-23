package com.leader.api.service.util;

import com.leader.api.util.component.ClientDataUtil;
import com.leader.api.util.component.DateUtil;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PhoneValidatedService {

    public static final String PHONE_VALIDATED = "phone_validated";
    public static final String PHONE_VALIDATED_TIMESTAMP = "phone_validated_timestamp";

    private static final long VALIDATION_EXPIRE = 300000;

    private final ClientDataUtil clientDataUtil;
    private final DateUtil dateUtil;

    public PhoneValidatedService(ClientDataUtil clientDataUtil, DateUtil dateUtil) {
        this.clientDataUtil = clientDataUtil;
        this.dateUtil = dateUtil;
    }

    public void setPhoneValidated(String phone) {
        clientDataUtil.set(PHONE_VALIDATED, phone);
        clientDataUtil.set(PHONE_VALIDATED_TIMESTAMP, dateUtil.getCurrentDate());
    }

    public void assertValidated(String phone) {
        String validatedPhone = clientDataUtil.get(PHONE_VALIDATED, String.class);

        if (validatedPhone == null || !validatedPhone.equals(phone)) {
            throw new RuntimeException("Validation failed");
        }

        Date validatedDate = clientDataUtil.get(PHONE_VALIDATED_TIMESTAMP, Date.class);
        long timePassed = dateUtil.getCurrentTime() - validatedDate.getTime();

        if (timePassed > VALIDATION_EXPIRE) {
            throw new RuntimeException("Validation failed");
        }

        clientDataUtil.remove(PHONE_VALIDATED);
        clientDataUtil.remove(PHONE_VALIDATED_TIMESTAMP);
    }
}
