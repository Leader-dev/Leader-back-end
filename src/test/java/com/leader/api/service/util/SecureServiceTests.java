package com.leader.api.service.util;

import com.leader.api.util.component.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class SecureServiceTests {

    private static final String TEST_PLAIN_TEXT = "jiqtngrc43xfn89yfmtgt";
    private static final String TEST_SHA1 = "db13e81dacc1cc0cadf6570fcb3449f0f5bf72b8";

    @Autowired
    private SecureService secureService;

    @MockBean
    private RandomUtil randomUtil;

    @BeforeEach
    public void setup() {
        clearInvocations(randomUtil);
    }

    @Test
    public void generateRandomSaltTest() {
        when(randomUtil.nextInt(anyInt())).thenReturn(1, 2, 3, 4, 5);

        String result = secureService.generateRandomSalt(5);

        assertEquals("BCDEF", result);
    }

    @Test
    public void generateRandomAuthCodeTest() {
        when(randomUtil.nextDouble()).thenReturn(0.04238143619);

        String result = secureService.generateRandomAuthCode(6);

        assertEquals("042381", result);
    }

    @Test
    public void generateRandomNumberIdTest() {
        when(randomUtil.nextDouble()).thenReturn(0.04238143619, 0.0148263143, 0.1273172389142);

        String result = secureService.generateRandomNumberId(8, numberId -> false);

        assertEquals("12731723", result);
        verify(randomUtil, times(3)).nextDouble();
    }

    @Test
    public void encryptDecryptTest() {
        KeyPair keyPair = secureService.generateRSAKeyPair(1024);
        String cipherText = secureService.encryptRSA(TEST_PLAIN_TEXT, keyPair.getPublic());
        String result = secureService.decryptRSA(cipherText, keyPair.getPrivate());
        assertEquals(TEST_PLAIN_TEXT, result);
    }
}
