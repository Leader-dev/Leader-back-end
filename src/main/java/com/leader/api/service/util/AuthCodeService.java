package com.leader.api.service.util;

import com.leader.api.data.user.AuthCodeRecord;
import com.leader.api.data.user.AuthCodeRecordRepository;
import com.leader.api.util.component.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthCodeService {

    private static final int AUTHCODE_LENGTH = 6;
    private static final long AUTHCODE_REQUEST_INTERVAL = 60000;
    private static final long AUTHCODE_EXPIRE = 300000;

    private final AuthCodeRecordRepository authCodeRecordRepository;

    private final SecureService secureService;

    private final DateUtil dateUtil;

    @Autowired
    public AuthCodeService(AuthCodeRecordRepository authCodeRecordRepository, SecureService secureService,
                           DateUtil dateUtil) {
        this.authCodeRecordRepository = authCodeRecordRepository;
        this.secureService = secureService;
        this.dateUtil = dateUtil;
    }

    private long timePassedSinceLastAuthCode(String phone) {
        AuthCodeRecord authCodeRecord = authCodeRecordRepository.findByPhone(phone);
        if (authCodeRecord == null) {
            return -1;
        }
        return dateUtil.getCurrentTime() - authCodeRecord.timestamp.getTime();
    }

    private void insertAuthCodeRecord(String phone, String authcode) {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.phone = phone;
        authCodeRecord.authcode = authcode;
        authCodeRecord.timestamp = dateUtil.getCurrentDate();
        authCodeRecordRepository.deleteByPhone(phone);  // make sure previous ones are deleted
        authCodeRecordRepository.insert(authCodeRecord);
    }

    public boolean sendAuthCode(String phone) {
        long timePassed = timePassedSinceLastAuthCode(phone);
        if (timePassed != -1 && timePassed < AUTHCODE_REQUEST_INTERVAL) {
            return false;
        }

        // randomly generate authcode
        String authcode = secureService.generateRandomAuthCode(AUTHCODE_LENGTH);

        // insert record to database
        insertAuthCodeRecord(phone, authcode);

        // TODO Actually send the authcode to phone

        return true;
    }

    public boolean validateAuthCode(String phone, String authcode) {
        AuthCodeRecord authCodeRecord = authCodeRecordRepository.findByPhone(phone);
        if (authCodeRecord == null) {
            return false;
        }
        long timePassed = dateUtil.getCurrentTime() - authCodeRecord.timestamp.getTime();
        return timePassed <= AUTHCODE_EXPIRE && authCodeRecord.authcode.equals(authcode);
    }

    public void removeAuthCodeRecord(String phone) {
        authCodeRecordRepository.deleteByPhone(phone);
    }
}
