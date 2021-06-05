package com.leader.api.service.util;

import com.leader.api.util.component.ClientDataUtil;
import com.leader.api.util.component.DateUtil;
import com.leader.api.util.component.RandomUtil;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PasswordServiceTests {

    private static final String TEST_PUBLIC_KEY_STRING = "n7cy328cfgn74x34tg4v567ch";
    private static final String TEST_PRIVATE_KEY_STRING = "kj98xd349ymfg4togcn5g33gc6";

    private static class PrivateKeyAdaptor implements PrivateKey {

        @Override
        public String getAlgorithm() {
            return null;
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public byte[] getEncoded() {
            return TEST_PUBLIC_KEY_STRING.getBytes(StandardCharsets.UTF_8);
        }
    }

    private static class PublicKeyAdaptor implements PublicKey {

        @Override
        public String getAlgorithm() {
            return null;
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public byte[] getEncoded() {
            return TEST_PRIVATE_KEY_STRING.getBytes(StandardCharsets.UTF_8);
        }
    }

    private static final Date TEST_DATE = new Date();
    private static final PublicKey TEST_PUBLIC_KEY = new PublicKeyAdaptor();
    private static final PrivateKey TEST_PRIVATE_KEY = new PrivateKeyAdaptor();
    private static final KeyPair TEST_KEY_PAIR = new KeyPair(TEST_PUBLIC_KEY, TEST_PRIVATE_KEY);
    private static final String TEST_CIPHER_TEXT = "f2ctgn5tnuclulgmctfhu3xft";
    private static final String TEST_PLAIN_TEXT = "yy278ybtn9fox3wituhmrfvct";
    private static UUID TEST_UUID = UUID.randomUUID();

    @Autowired
    private PasswordService passwordService;

    @MockBean
    private SecureService secureService;

    @MockBean
    private DateUtil dateUtil;

    @MockBean
    private ClientDataUtil clientDataUtil;

    @MockBean
    private RandomUtil randomUtil;

    @BeforeEach
    public void setup() {
        TEST_UUID = UUID.randomUUID();
        when(dateUtil.getCurrentTime()).thenCallRealMethod();
        when(dateUtil.getCurrentDate()).thenCallRealMethod();
        clientDataUtil.setClientData(new Document());
        clearInvocations(clientDataUtil);
    }

    @Test
    public void savePrivateKeyTest() {
        when(dateUtil.getCurrentDate()).thenReturn(TEST_DATE);
        when(randomUtil.nextUUID()).thenReturn(TEST_UUID);

        passwordService.savePrivateKey(TEST_PRIVATE_KEY);

        verify(clientDataUtil, times(1)).set(PasswordService.PRIVATE_KEY_ID, TEST_UUID);
        verify(clientDataUtil, times(1)).set(PasswordService.PRIVATE_KEY_TIMESTAMP, TEST_DATE);
    }

    @Test
    public void getPrivateKeyTest() {
        when(randomUtil.nextUUID()).thenReturn(TEST_UUID);
        passwordService.savePrivateKey(TEST_PRIVATE_KEY);
        when(clientDataUtil.get(PasswordService.PRIVATE_KEY_ID, UUID.class)).thenReturn(TEST_UUID);
        when(clientDataUtil.get(PasswordService.PRIVATE_KEY_TIMESTAMP, Date.class)).thenReturn(TEST_DATE);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime());

        PrivateKey privateKey = passwordService.getPrivateKey();

        assertEquals(TEST_PRIVATE_KEY, privateKey);
        verify(clientDataUtil, times(1)).remove(PasswordService.PRIVATE_KEY_ID);
        verify(clientDataUtil, times(1)).remove(PasswordService.PRIVATE_KEY_TIMESTAMP);
    }

    @Test
    public void getPrivateKeyNullTest() {
        // no pre-actions

        PrivateKey privateKey = passwordService.getPrivateKey();

        assertNull(privateKey);
    }

    @Test
    public void getPrivateKeyExpiredTest() {
        when(randomUtil.nextUUID()).thenReturn(TEST_UUID);
        passwordService.savePrivateKey(TEST_PRIVATE_KEY);
        when(clientDataUtil.get(PasswordService.PRIVATE_KEY_ID, UUID.class)).thenReturn(TEST_UUID);
        when(clientDataUtil.get(PasswordService.PRIVATE_KEY_TIMESTAMP, Date.class)).thenReturn(TEST_DATE);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 60001);

        PrivateKey privateKey = passwordService.getPrivateKey();

        assertNull(privateKey);
        verify(clientDataUtil, times(1)).remove(PasswordService.PRIVATE_KEY_ID);
        verify(clientDataUtil, times(1)).remove(PasswordService.PRIVATE_KEY_TIMESTAMP);
    }

    @Test
    public void generateKeyTest() {
        when(dateUtil.getCurrentDate()).thenReturn(TEST_DATE);
        when(secureService.generateRSAKeyPair(anyInt())).thenReturn(TEST_KEY_PAIR);

        byte[] publicKey = passwordService.generateKey();

        assertArrayEquals(TEST_PUBLIC_KEY.getEncoded(), publicKey);
        verify(clientDataUtil, times(1)).set(eq(PasswordService.PRIVATE_KEY_ID), any());
        verify(clientDataUtil, times(1)).set(eq(PasswordService.PRIVATE_KEY_TIMESTAMP), eq(TEST_DATE));
    }

    @Test
    public void decryptTest() {
        when(randomUtil.nextUUID()).thenReturn(TEST_UUID);
        passwordService.savePrivateKey(TEST_PRIVATE_KEY);
        when(clientDataUtil.get(PasswordService.PRIVATE_KEY_ID, UUID.class)).thenReturn(TEST_UUID);
        when(clientDataUtil.get(PasswordService.PRIVATE_KEY_TIMESTAMP, Date.class)).thenReturn(TEST_DATE);
        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime());
        when(secureService.decryptRSA(TEST_CIPHER_TEXT, TEST_PRIVATE_KEY)).thenReturn(TEST_PLAIN_TEXT);

        String decrypted = passwordService.decrypt(TEST_CIPHER_TEXT);

        assertEquals(TEST_PLAIN_TEXT, decrypted);
        verify(clientDataUtil, times(1)).remove(PasswordService.PRIVATE_KEY_ID);
        verify(clientDataUtil, times(1)).remove(PasswordService.PRIVATE_KEY_TIMESTAMP);
    }

    @Test
    public void decryptNoKeyTest() {
        when(clientDataUtil.get(PasswordService.PRIVATE_KEY_ID, UUID.class)).thenReturn(null);

        Executable action = () -> passwordService.decrypt(TEST_CIPHER_TEXT);

        assertThrows(RuntimeException.class, action);
    }
}
