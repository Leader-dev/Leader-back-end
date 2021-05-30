package com.leader.api.service.user;

import com.leader.api.data.user.AuthCodeRecordRepository;
import com.leader.api.data.user.User;
import com.leader.api.data.user.UserRepository;
import com.leader.api.service.util.SecureService;
import com.leader.api.service.util.SessionService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

@SpringBootTest
public class UserAuthServiceTests {

    final String TEST_PHONE = "13360097989";
    final String TEST_PASSWORD = "xxxxxxxx";
    final String TEST_INCORRECT_PASSWORD = "xxxxxxxy";
    final String TEST_PUBLIC_KEY = "1234567890";
    final String TEST_SALT = "fwgfv3843oiu43hfir3bb3";
    final String TEST_UID = "654321";
    final String TEST_SHA1 = "3128yr7n92tg3f7596fx5ncg524g";
    final String TEST_INCORRECT_SHA1 = "vharo78cnfiumx4qn34yxfv43498x";
    final String TEST_CIPHER_TEXT = "ny3872m94nr0c3fxnpmoir2fxmejkwxrw";
    final ObjectId TEST_USER_ID = new ObjectId();

    @Autowired
    UserAuthService authService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    AuthCodeRecordRepository authCodeRecordRepository;

    @MockBean
    SecureService secureService;

    @MockBean
    SessionService sessionService;

    @Test
    public void assertPhoneExistsTest() {
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        Assertions.assertDoesNotThrow(() -> authService.assertPhoneExists(TEST_PHONE), "Should not throw");

        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(false);
        Assertions.assertThrows(RuntimeException.class, () -> authService.assertPhoneExists(TEST_PHONE), "Should throw");
    }

    @Test
    public void assertUidExistsTest() {
        when(userRepository.existsByUid(TEST_UID)).thenReturn(true);
        Assertions.assertDoesNotThrow(() -> authService.assertUidExists(TEST_UID), "Should not throw");

        when(userRepository.existsByUid(TEST_UID)).thenReturn(false);
        Assertions.assertThrows(RuntimeException.class, () -> authService.assertUidExists(TEST_UID), "Should throw");
    }

    @Test
    public void generateKeyPairTest() {
        when(sessionService.generateKey(any(), anyInt())).thenReturn(TEST_PUBLIC_KEY);
        Assertions.assertEquals(authService.generateKeyPair(null), TEST_PUBLIC_KEY);
    }

    @Test
    public void decryptTest() {
        when(sessionService.decrypt(any(), eq(TEST_CIPHER_TEXT), anyLong())).thenReturn(TEST_PASSWORD);
        Assertions.assertEquals(authService.decryptPassword(null, TEST_CIPHER_TEXT), TEST_PASSWORD);

        when(sessionService.decrypt(any(), eq(TEST_CIPHER_TEXT), anyLong())).thenReturn(null);
        Assertions.assertThrows(RuntimeException.class, () -> authService.decryptPassword(null, TEST_CIPHER_TEXT), "Should throw");
    }

    @Test
    public void createUserSuccessTest() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.existsByUid(any())).thenReturn(true, true, false);
        when(secureService.SHA1(any())).thenReturn(TEST_SHA1);
        Assertions.assertDoesNotThrow(() -> authService.createUser(TEST_PHONE, TEST_PASSWORD), "Should success");
        verify(userRepository, times(1)).count();
        verify(userRepository, times(3)).existsByUid(any());
        verify(userRepository, times(1)).insert(argThat((User user) -> user.password.equals(TEST_SHA1)));
    }

    @Test
    public void createUserCapacityReachedTest() {
        when(userRepository.count()).thenReturn(50000010L);
        Assertions.assertThrows(RuntimeException.class, () -> authService.createUser(TEST_PHONE, TEST_PASSWORD), "Should throw");
        verify(userRepository, never()).insert((User) any());
    }

    @Test
    public void validateUserTest() {
        User user = new User();
        user.salt = TEST_SALT;
        user.password = TEST_SHA1;

        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(user);
        when(secureService.generateRandomSalt(anyInt())).thenReturn(TEST_SALT);

        // success case

        when(secureService.SHA1(TEST_PASSWORD + TEST_SALT)).thenReturn(TEST_SHA1);
        Assertions.assertTrue(authService.validateUser(TEST_PHONE, TEST_PASSWORD), "Should success");
        verify(secureService, times(1)).SHA1(TEST_PASSWORD + TEST_SALT);
        clearInvocations(secureService);

        // incorrect case

        when(secureService.SHA1(TEST_INCORRECT_PASSWORD + TEST_SALT)).thenReturn(TEST_INCORRECT_SHA1);
        Assertions.assertFalse(authService.validateUser(TEST_PHONE, TEST_INCORRECT_PASSWORD), "Should fail");
        verify(secureService, times(1)).SHA1(TEST_INCORRECT_PASSWORD + TEST_SALT);
    }

    @Test
    public void updatePasswordTest() {
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(new User());
        when(secureService.SHA1(any())).thenReturn(TEST_SHA1);

        authService.updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);
        verify(userRepository, atLeastOnce()).save(argThat(user1 -> user1.password.equals(TEST_SHA1)));
    }

    @Test
    public void getUserIdTest() {
        User user = new User();
        user.id = TEST_USER_ID;

        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(user);
        Assertions.assertEquals(authService.getUserIdByPhone(TEST_PHONE), TEST_USER_ID);
    }
}
