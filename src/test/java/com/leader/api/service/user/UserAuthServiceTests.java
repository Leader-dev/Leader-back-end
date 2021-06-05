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
    private static final String TEST_PASSWORD = "xxxxxxxx";
    private static final String TEST_INCORRECT_PASSWORD = "xxxxxxxy";
    private static final String TEST_SALT = "fwgfv3843oiu43hfir3bb3";
    private static final String TEST_UID = "654321";
    private static final String TEST_SHA1 = "3128yr7n92tg3f7596fx5ncg524g";
    private static final String TEST_INCORRECT_SHA1 = "vharo78cnfiumx4qn34yxfv43498x";
    private static final ObjectId TEST_USER_ID = new ObjectId();

    @Autowired
    private UserAuthService authService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SecureService secureService;

    @Test
    public void assertPhoneExistsTest() {
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);

        Executable action = () -> authService.assertPhoneExists(TEST_PHONE);

        assertDoesNotThrow(action);
    }

    @Test
    public void assertPhoneNotExistsTest() {
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(false);

        Executable action = () -> authService.assertPhoneExists(TEST_PHONE);

        assertThrows(RuntimeException.class, action);
    }

    @Test
    public void assertUidExistsTest() {
        when(userRepository.existsByUid(TEST_UID)).thenReturn(true);

        Executable action = () -> authService.assertUidExists(TEST_UID);

        assertDoesNotThrow(action);
    }

    @Test
    public void assertUidNotExistsTest() {
        when(userRepository.existsByUid(TEST_UID)).thenReturn(false);

        Executable action = () -> authService.assertUidExists(TEST_UID);

        assertThrows(RuntimeException.class, action);
    }

    @Test
    public void createUserSuccessTest() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.existsByUid(any())).thenReturn(true, true, false);
        when(secureService.SHA1(any())).thenReturn(TEST_SHA1);

        Executable action = () -> authService.createUser(TEST_PHONE, TEST_PASSWORD, TEST_NICKNAME);

        assertDoesNotThrow(action);
        verify(userRepository, times(1)).count();
        verify(userRepository, times(3)).existsByUid(any());
        verify(userRepository, times(1)).insert(argThat((User user) -> user.password.equals(TEST_SHA1)));
    }

    @Test
    public void createUserCapacityReachedTest() {
        when(userRepository.count()).thenReturn(50000010L);

        Executable action = () -> authService.createUser(TEST_PHONE, TEST_PASSWORD, TEST_NICKNAME);

        assertThrows(RuntimeException.class, action);
        verify(userRepository, never()).insert((User) any());
    }

    @Test
    public void validateUserSuccessTest() {
        User user = new User();
        user.salt = TEST_SALT;
        user.password = TEST_SHA1;
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(user);
        when(secureService.generateRandomSalt(anyInt())).thenReturn(TEST_SALT);
        when(secureService.SHA1(TEST_PASSWORD + TEST_SALT)).thenReturn(TEST_SHA1);

        boolean result = authService.validateUser(TEST_PHONE, TEST_PASSWORD);

        assertTrue(result);
        verify(secureService, times(1)).SHA1(TEST_PASSWORD + TEST_SALT);
    }

    @Test
    public void validateUserFailTest() {
        User user = new User();
        user.salt = TEST_SALT;
        user.password = TEST_SHA1;
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(user);
        when(secureService.generateRandomSalt(anyInt())).thenReturn(TEST_SALT);
        when(secureService.SHA1(TEST_INCORRECT_PASSWORD + TEST_SALT)).thenReturn(TEST_INCORRECT_SHA1);

        boolean result = authService.validateUser(TEST_PHONE, TEST_INCORRECT_PASSWORD);

        assertFalse(result);
        verify(secureService, times(1)).SHA1(TEST_INCORRECT_PASSWORD + TEST_SALT);
    }

    @Test
    public void updatePasswordTest() {
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(new User());
        when(secureService.SHA1(any())).thenReturn(TEST_SHA1);

        authService.updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);

        verify(userRepository, times(1)).save(argThat(user1 -> user1.password.equals(TEST_SHA1)));
    }

    @Test
    public void getUserIdTest() {
        User user = new User();
        user.id = TEST_USER_ID;
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(user);

        ObjectId result = authService.getUserIdByPhone(TEST_PHONE);

        assertEquals(TEST_USER_ID, result);
    }
}
