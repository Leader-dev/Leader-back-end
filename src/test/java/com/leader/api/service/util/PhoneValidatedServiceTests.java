package com.leader.api.service.util;

import com.leader.api.util.component.ClientDataUtil;
import com.leader.api.util.component.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Date;

import static com.leader.api.service.util.PhoneValidatedService.PHONE_VALIDATED;
import static com.leader.api.service.util.PhoneValidatedService.PHONE_VALIDATED_TIMESTAMP;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PhoneValidatedServiceTests {

    private static final String TEST_PHONE = "13360097989";
    private static final String TEST_INCORRECT_PHONE = "13360097987";
    private static final Date TEST_DATE = new Date();

    @Autowired
    private PhoneValidatedService phoneValidatedService;

    @MockBean
    private ClientDataUtil clientDataUtil;

    @MockBean
    private DateUtil dateUtil;

    @BeforeEach
    public void setup() {
        clearInvocations(clientDataUtil);
    }

    @Test
    public void setPhoneValidatedTest() {
        when(dateUtil.getCurrentDate()).thenReturn(TEST_DATE);

        phoneValidatedService.setPhoneValidated(TEST_PHONE);

        verify(clientDataUtil, times(1)).set(PHONE_VALIDATED, TEST_PHONE);
        verify(clientDataUtil, times(1)).set(PHONE_VALIDATED_TIMESTAMP, TEST_DATE);
    }

    @Test
    public void assertValidatedSuccessTest() {
        when(clientDataUtil.get(PHONE_VALIDATED, String.class)).thenReturn(TEST_PHONE);
        when(clientDataUtil.get(PHONE_VALIDATED_TIMESTAMP, Date.class)).thenReturn(TEST_DATE);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 300000);

        Executable action = () -> phoneValidatedService.assertValidated(TEST_PHONE);

        assertDoesNotThrow(action);
        verify(clientDataUtil, times(1)).remove(PHONE_VALIDATED);
        verify(clientDataUtil, times(1)).remove(PHONE_VALIDATED_TIMESTAMP);
    }

    @Test
    public void assertValidatedPhoneNullTest() {
        when(clientDataUtil.get(PHONE_VALIDATED, String.class)).thenReturn(null);

        Executable action = () -> phoneValidatedService.assertValidated(TEST_PHONE);

        assertThrows(RuntimeException.class, action);
        verify(clientDataUtil, never()).remove(PHONE_VALIDATED);
        verify(clientDataUtil, never()).remove(PHONE_VALIDATED_TIMESTAMP);
    }

    @Test
    public void assertValidatedPhoneInvalidTest() {
        when(clientDataUtil.get(PHONE_VALIDATED, String.class)).thenReturn(TEST_PHONE);

        Executable action = () -> phoneValidatedService.assertValidated(TEST_INCORRECT_PHONE);

        assertThrows(RuntimeException.class, action);
        verify(clientDataUtil, never()).remove(PHONE_VALIDATED);
        verify(clientDataUtil, never()).remove(PHONE_VALIDATED_TIMESTAMP);
    }

    @Test
    public void assertValidatedPhoneExpiredTest() {
        when(clientDataUtil.get(PHONE_VALIDATED, String.class)).thenReturn(TEST_PHONE);
        when(clientDataUtil.get(PHONE_VALIDATED_TIMESTAMP, Date.class)).thenReturn(TEST_DATE);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 300001);

        Executable action = () -> phoneValidatedService.assertValidated(TEST_PHONE);

        assertThrows(RuntimeException.class, action);
        verify(clientDataUtil, never()).remove(PHONE_VALIDATED);
        verify(clientDataUtil, never()).remove(PHONE_VALIDATED_TIMESTAMP);
    }
}
