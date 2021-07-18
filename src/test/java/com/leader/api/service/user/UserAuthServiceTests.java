package com.leader.api.service.user;

import com.leader.api.data.user.User;
import com.leader.api.data.user.UserRepository;
import com.leader.api.service.util.SecureService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserAuthServiceTests {

    private static final String TEST_NICKNAME = "Raymond";
    private static final String TEST_PHONE = "13360097989";
    private static final String TEST_PASSWORD = "abcdefg";
    private static final String TEST_INCORRECT_PASSWORD = "xxxxxxxy";
    private static final String TEST_UID = "654321";
    private static final String TEST_ENCRYPTED_PASSWORD = "qfiibrcop98q34mfpphorxenwfvacvegskfxns";
    private static final ObjectId TEST_USER_ID = new ObjectId();

    @Autowired
    private UserAuthService userAuthService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SecureService secureService;

    @Test
    public void assertPhoneExistsTest() {
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);

        Executable action = () -> userAuthService.assertPhoneExists(TEST_PHONE);

        assertDoesNotThrow(action);
    }

    @Test
    public void assertPhoneNotExistsTest() {
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(false);

        Executable action = () -> userAuthService.assertPhoneExists(TEST_PHONE);

        assertThrows(RuntimeException.class, action);
    }

    @Test
    public void assertUidExistsTest() {
        when(userRepository.existsByUid(TEST_UID)).thenReturn(true);

        Executable action = () -> userAuthService.assertUidExists(TEST_UID);

        assertDoesNotThrow(action);
    }

    @Test
    public void assertUidNotExistsTest() {
        when(userRepository.existsByUid(TEST_UID)).thenReturn(false);

        Executable action = () -> userAuthService.assertUidExists(TEST_UID);

        assertThrows(RuntimeException.class, action);
    }

    @Test
    public void createUserSuccessTest() {
        when(userRepository.count()).thenReturn(0L);
        when(secureService.generateRandomNumberId(anyInt(), any())).thenReturn(TEST_UID);
        when(secureService.encodePassword(TEST_PASSWORD)).thenReturn(TEST_ENCRYPTED_PASSWORD);

        Executable action = () -> userAuthService.createUser(TEST_PHONE, TEST_PASSWORD, TEST_NICKNAME);

        assertDoesNotThrow(action);
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).insert(argThat((User user) -> user.password.equals(TEST_ENCRYPTED_PASSWORD)));
    }

    @Test
    public void createUserCapacityReachedTest() {
        when(userRepository.count()).thenReturn(50000010L);

        Executable action = () -> userAuthService.createUser(TEST_PHONE, TEST_PASSWORD, TEST_NICKNAME);

        assertThrows(RuntimeException.class, action);
        verify(userRepository, never()).insert((User) any());
    }

    @Test
    public void validateUserSuccessTest() {
        User user = new User();
        user.password = TEST_ENCRYPTED_PASSWORD;
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(user);
        when(secureService.matchesPassword(TEST_PASSWORD, TEST_ENCRYPTED_PASSWORD)).thenReturn(true);

        boolean result = userAuthService.validateUser(TEST_PHONE, TEST_PASSWORD);

        assertTrue(result);
    }

    @Test
    public void validateUserFailTest() {
        User user = new User();
        user.password = TEST_ENCRYPTED_PASSWORD;
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(user);

        boolean result = userAuthService.validateUser(TEST_PHONE, TEST_INCORRECT_PASSWORD);

        assertFalse(result);
    }

    @Test
    public void updatePasswordTest() {
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(new User());

        userAuthService.updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);
    }

    @Test
    public void getUserIdTest() {
        User user = new User();
        user.id = TEST_USER_ID;
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(user);

        ObjectId result = userAuthService.getUserIdByPhone(TEST_PHONE);

        assertEquals(TEST_USER_ID, result);
    }
}
