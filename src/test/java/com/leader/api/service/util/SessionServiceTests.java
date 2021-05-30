package com.leader.api.service.util;

import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest
public class SessionServiceTests {

    static class PrivateKeyAdaptor implements PrivateKey {

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
            return "n7cy328cfgn74x34tg4v567ch".getBytes(StandardCharsets.UTF_8);
        }
    }

    static class PublicKeyAdaptor implements PublicKey {

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
            return "kj98xd349ymfg4togcn5g33gc6".getBytes(StandardCharsets.UTF_8);
        }
    }

    final ObjectId TEST_USER_ID = new ObjectId();
    final Date TEST_DATE = new Date();
    final PublicKey TEST_PUBLIC_KEY = new PublicKeyAdaptor();
    final PrivateKey TEST_PRIVATE_KEY = new PrivateKeyAdaptor();
    final KeyPair TEST_KEY_PAIR = new KeyPair(TEST_PUBLIC_KEY, TEST_PRIVATE_KEY);
    final String TEST_CIPHER_TEXT = "f2ctgn5tnuclulgmctfhu3xft";
    final String TEST_PLAIN_TEXT = "yy278ybtn9fox3wituhmrfvct";

    @Autowired
    SessionService sessionService;

    @MockBean
    SecureService secureService;

    @MockBean
    DateUtil dateUtil;

    HttpSession session;

    @BeforeEach
    public void setup() {
        session = new MockHttpSession();
    }

    @Test
    public void userIdTest() {

        // save

        sessionService.saveUserIdToSession(session, TEST_USER_ID);
        assertEquals(session.getAttribute("user_id"), TEST_USER_ID);

        // get

        assertEquals(sessionService.getUserIdFromSession(session), TEST_USER_ID);

        // remove

        sessionService.removeUserIdFromSession(session);
        assertNull(session.getAttribute("user_id"));
    }

    @Test
    public void privateKeyTest() {
        when(dateUtil.getCurrentDate()).thenReturn(TEST_DATE);

        // save

        sessionService.savePrivateKeyToSession(session, TEST_PRIVATE_KEY);
        assertEquals(session.getAttribute("private_key"), TEST_PRIVATE_KEY);
        assertEquals(session.getAttribute("private_key_timestamp"), TEST_DATE);

        // get and remove

        assertEquals(sessionService.getPrivateKeyFromSession(session, 10), TEST_PRIVATE_KEY);
        assertNull(session.getAttribute("private_key"));
        assertNull(session.getAttribute("private_key_timestamp"));
    }

    @Test
    public void privateKeyNullTest() {
        assertNull(sessionService.getPrivateKeyFromSession(session, 10));
    }

    @Test
    public void privateKeyExpiredTest() {
        when(dateUtil.getCurrentDate()).thenReturn(TEST_DATE);
        sessionService.savePrivateKeyToSession(session, TEST_PRIVATE_KEY);

        when(dateUtil.getCurrentTime()).thenReturn(TEST_DATE.getTime() + 20);
        assertNull(sessionService.getPrivateKeyFromSession(session, 10));
        assertNull(session.getAttribute("private_key"));
        assertNull(session.getAttribute("private_key_timestamp"));
    }

    @Test
    public void generateKeyTest() {
        when(secureService.generateRSAKeyPair(anyInt())).thenReturn(TEST_KEY_PAIR);
        assertEquals("kj98xd349ymfg4togcn5g33gc6", sessionService.generateKey(session, 0));
        assertEquals(TEST_PRIVATE_KEY, session.getAttribute("private_key"));
    }

    @Test
    public void decryptTest() {
        when(dateUtil.getCurrentDate()).thenReturn(TEST_DATE);
        sessionService.savePrivateKeyToSession(session, TEST_PRIVATE_KEY);
        when(secureService.decryptRSA(TEST_CIPHER_TEXT, TEST_PRIVATE_KEY)).thenReturn(TEST_PLAIN_TEXT);
        when(secureService.generateRSAKeyPair(anyInt())).thenReturn(TEST_KEY_PAIR);

        assertEquals(TEST_PLAIN_TEXT, sessionService.decrypt(session, TEST_CIPHER_TEXT, 0));
        assertNull(session.getAttribute("private_key"));
        assertNull(session.getAttribute("private_key_timestamp"));
    }

    @Test
    public void decryptFailTest() {
        assertNull(sessionService.decrypt(session, TEST_CIPHER_TEXT, 0));
    }
}
