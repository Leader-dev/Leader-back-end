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

        boolean result = authCodeService.sendAuthCode(TEST_PHONE);

        assertTrue(result);
        verify(authCodeRecordRepository, times(1)).deleteByPhone(TEST_PHONE);
        verify(authCodeRecordRepository, times(1)).insert(
                argThat((AuthCodeRecord record) -> record.authcode.equals(TEST_AUTHCODE))
        );
    }

    @Test
    public void sendAuthCodeRejectTest() {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.timestamp = TEST_DATE;
        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 59990);

        boolean result = authCodeService.sendAuthCode(TEST_PHONE);

        assertFalse(result);
        verify(authCodeRecordRepository, never()).deleteByPhone(TEST_PHONE);
        verify(authCodeRecordRepository, never()).insert((AuthCodeRecord) any());
    }

    @Test
    public void validateAuthCodeTest() {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.authcode = TEST_AUTHCODE;
        authCodeRecord.timestamp = TEST_DATE;
        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 300000);

        boolean result = authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE);

        assertTrue(result);
    }

    @Test
    public void validateAuthCodeIncorrectTest() {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.authcode = TEST_AUTHCODE;
        authCodeRecord.timestamp = TEST_DATE;
        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 300000);

        boolean result = authCodeService.validateAuthCode(TEST_PHONE, TEST_INCORRECT_AUTHCODE);

        assertFalse(result);
    }

    @Test
    public void validateAuthCodePhoneNotExistTest() {
        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(null);

        boolean result = authCodeService.validateAuthCode(TEST_PHONE, TEST_INCORRECT_AUTHCODE);

        assertFalse(result);
    }

    @Test
    public void validateAuthCodeTimedOutTest() {
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.authcode = TEST_AUTHCODE;
        authCodeRecord.timestamp = TEST_DATE;
        when(authCodeRecordRepository.findByPhone(TEST_PHONE)).thenReturn(authCodeRecord);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 300001);

        boolean result = authCodeService.validateAuthCode(TEST_PHONE, TEST_INCORRECT_AUTHCODE);

        assertFalse(result);
    }

    @Test
    public void removeAuthCodeRecordTest() {
        // no pre-actions

        authCodeService.removeAuthCodeRecord(TEST_PHONE);

        verify(authCodeRecordRepository, times(1)).deleteByPhone(TEST_PHONE);
    }
}
