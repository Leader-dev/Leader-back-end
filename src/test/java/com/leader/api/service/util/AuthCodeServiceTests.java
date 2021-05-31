package com.leader.api.service.util;

import com.leader.api.data.user.AuthCodeRecord;
import com.leader.api.data.user.AuthCodeRecordRepository;
import com.leader.api.util.component.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuthCodeServiceTests {

    private static final String TEST_PHONE = "13360097989";
    private static final String TEST_AUTHCODE = "123456";
    private static final String TEST_INCORRECT_AUTHCODE = "123457";
    private static final Date TEST_DATE = new Date();

    @Autowired
    private AuthCodeService authCodeService;
    
    @MockBean
    private AuthCodeRecordRepository authCodeRecordRepository;

    @MockBean
    private SecureService secureService;

    @MockBean
    private DateUtil dateUtil;

    @Test
    public void sendAuthCodeSuccessTest() {
        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(null);
        when(secureService.generateRandomAuthCode(anyInt())).thenReturn(TEST_AUTHCODE);
        assertTrue(authCodeService.sendAuthCode(TEST_PHONE), "Should send successfully");
        verify(authCodeRecordRepository, atLeastOnce()).deleteByPhone(TEST_PHONE);
        verify(authCodeRecordRepository, atLeastOnce()).insert(
                argThat((AuthCodeRecord record) -> record.authcode.equals(TEST_AUTHCODE))
        );
    }

    @Test
    public void sendAuthCodeRejectTest() {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.timestamp = TEST_DATE;

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 59990);
        assertFalse(authCodeService.sendAuthCode(TEST_PHONE), "Should reject send");
        verify(authCodeRecordRepository, never()).deleteByPhone(TEST_PHONE);
        verify(authCodeRecordRepository, never()).insert((AuthCodeRecord) any());
    }

    @Test
    public void validateAuthCodeTest() {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.authcode = TEST_AUTHCODE;
        authCodeRecord.timestamp = TEST_DATE;

        // success case

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 299990);
        assertTrue(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE), "Should success");

        // incorrect case

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 299990);
        assertFalse(authCodeService.validateAuthCode(TEST_PHONE, TEST_INCORRECT_AUTHCODE), "Should fail");

        // phone not exist case

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(null);
        assertFalse(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE), "Should fail");

        // timed out case

        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 300010);
        assertFalse(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE), "Should fail");
    }

    @Test
    public void removeAuthCodeRecordTest() {
        authCodeService.removeAuthCodeRecord(TEST_PHONE);
        verify(authCodeRecordRepository, atLeastOnce()).deleteByPhone(TEST_PHONE);
    }
}
