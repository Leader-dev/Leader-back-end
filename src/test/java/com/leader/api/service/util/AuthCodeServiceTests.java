package com.leader.api.service.util;

import com.leader.api.data.user.AuthCodeRecord;
import com.leader.api.data.user.AuthCodeRecordRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@SpringBootTest
public class AuthCodeServiceTests {

    final String TEST_PHONE = "13360097989";
    final String TEST_AUTHCODE = "123456";
    final String TEST_INCORRECT_AUTHCODE = "123457";

    @Autowired
    AuthCodeService authCodeService;
    
    @MockBean
    AuthCodeRecordRepository authCodeRecordRepository;

    @Test
    public void sendAuthCodeSuccessTest() {
        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(null);
        Assertions.assertTrue(authCodeService.sendAuthCode(TEST_PHONE), "Should send successfully");
        verify(authCodeRecordRepository, atLeastOnce()).deleteByPhone(TEST_PHONE);
        verify(authCodeRecordRepository, atLeastOnce()).insert((AuthCodeRecord) any());
    }

    @Test
    public void sendAuthCodeRejectTest() {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.timestamp = new Date();

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        Assertions.assertFalse(authCodeService.sendAuthCode(TEST_PHONE), "Should reject send");
        verify(authCodeRecordRepository, never()).deleteByPhone(TEST_PHONE);
        verify(authCodeRecordRepository, never()).insert((AuthCodeRecord) any());
    }

    @Test
    public void validateAuthCodeTest() {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.authcode = TEST_AUTHCODE;
        authCodeRecord.timestamp = new Date();

        // success case

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        Assertions.assertTrue(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE), "Should success");

        // incorrect case

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        Assertions.assertFalse(authCodeService.validateAuthCode(TEST_PHONE, TEST_INCORRECT_AUTHCODE), "Should fail");

        // phone not exist case

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(null);
        Assertions.assertFalse(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE), "Should fail");

        // timed out case

        authCodeRecord.timestamp = new Date(new Date().getTime() - 500000);

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        Assertions.assertFalse(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE), "Should fail");
    }

    @Test
    public void removeAuthCodeRecordTest() {
        authCodeService.removeAuthCodeRecord(TEST_PHONE);
        verify(authCodeRecordRepository, atLeastOnce()).deleteByPhone(TEST_PHONE);
    }
}
