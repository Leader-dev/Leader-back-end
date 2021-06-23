package com.leader.api.controller.user;

import com.leader.api.data.user.User;
import com.leader.api.service.user.UserAuthService;
import com.leader.api.service.util.AuthCodeService;
import com.leader.api.service.util.PasswordService;
import com.leader.api.service.util.PhoneValidatedService;
import com.leader.api.service.util.UserIdService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.nio.charset.StandardCharsets;

import static com.leader.api.test.util.Util.assertErrorResponse;
import static com.leader.api.test.util.Util.assertSuccessResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserAuthControllerTests {

    private static final String TEST_NICKNAME = "Raymond";
    private static final String TEST_PHONE = "13360097989";
    private static final String TEST_AUTHCODE = "123456";
    private static final String TEST_PASSWORD = "xxxxxxxx";
    private static final byte[] TEST_PUBLIC_KEY = "1234567890".getBytes(StandardCharsets.UTF_8);
    private static final ObjectId TEST_USER_ID = new ObjectId();

    @Autowired
    private UserAuthController userAuthController;

    @MockBean
    private UserAuthService userAuthService;

    @MockBean
    private AuthCodeService authCodeService;

    @MockBean
    private PasswordService passwordService;

    @MockBean
    private UserIdService userIdService;

    @MockBean
    private PhoneValidatedService phoneValidatedService;

    private UserAuthController.UserQueryObject queryObject;
    private Document response;

    @BeforeEach
    void setup() {
        // reset variables and mock instance call each time
        queryObject = new UserAuthController.UserQueryObject();
        response = new Document();
        Mockito.clearInvocations(userAuthService);
    }

    @Test
    public void userExistTest() {
        queryObject.phone = TEST_PHONE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);

        response = userAuthController.userExist(queryObject);

        assertSuccessResponse(response);
        assertEquals(true, response.get("exist"));
    }

    @Test
    public void userNotExistTest() {
        queryObject.phone = TEST_PHONE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);

        response = userAuthController.userExist(queryObject);

        assertSuccessResponse(response);
        assertEquals(false, response.get("exist"));
    }

    @Test
    public void publicKeyTest() {
        when(passwordService.generateKey()).thenReturn(TEST_PUBLIC_KEY);

        response = userAuthController.getPublicKey();

        assertSuccessResponse(response);
        assertEquals(TEST_PUBLIC_KEY, response.get("publicKey"));
    }

    @Test
    public void checkTest() {
        queryObject.password = TEST_PASSWORD;
        when(passwordService.decrypt(TEST_PASSWORD)).thenReturn(TEST_PASSWORD);

        response = userAuthController.checkText(queryObject);

        assertSuccessResponse(response);
        assertEquals(TEST_PASSWORD, response.get("text"));
    }

    @Test
    public void sendAuthCodeTest() {
        queryObject.phone = TEST_PHONE;
        when(authCodeService.sendAuthCode(TEST_PHONE)).thenReturn(true);

        response = userAuthController.sendAuthCode(queryObject);

        assertSuccessResponse(response);
    }

    @Test
    public void sendAuthCodeTooFrequentTest() {
        queryObject.phone = TEST_PHONE;
        when(authCodeService.sendAuthCode(TEST_PHONE)).thenReturn(false);

        response = userAuthController.sendAuthCode(queryObject);

        assertErrorResponse(response, "request_too_frequent");
    }

    @Test
    public void registerSuccessTest() {
        User user = new User();
        user.id = TEST_USER_ID;
        queryObject.nickname = TEST_NICKNAME;
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);
        when(passwordService.decrypt(TEST_PASSWORD)).thenReturn(TEST_PASSWORD);
        when(userAuthService.createUser(TEST_PHONE, TEST_PASSWORD, TEST_NICKNAME)).thenReturn(user);

        response = userAuthController.registerUser(queryObject);

        assertSuccessResponse(response);
        verify(userAuthService, times(1)).createUser(TEST_PHONE, TEST_PASSWORD, TEST_NICKNAME);
        verify(authCodeService, times(1)).removeAuthCodeRecord(TEST_PHONE);
        verify(userIdService, times(1)).setCurrentUserId(TEST_USER_ID);
    }

    @Test
    public void registerPhoneExistTest() {
        queryObject.phone = TEST_PHONE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);

        response = userAuthController.registerUser(queryObject);

        assertErrorResponse(response, "phone_exist");
        verify(userAuthService, never()).createUser(any(), any(), any());
        verify(authCodeService, never()).removeAuthCodeRecord(any());
    }

    @Test
    public void registerAuthCodeIncorrectTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);

        response = userAuthController.registerUser(queryObject);

        assertErrorResponse(response, "authcode_incorrect");
        verify(userAuthService, never()).createUser(any(), any(), any());
        verify(authCodeService, never()).removeAuthCodeRecord(any());
    }

    @Test
    public void loginPasswordTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(passwordService.decrypt(TEST_PASSWORD)).thenReturn(TEST_PASSWORD);
        when(userAuthService.validateUser(TEST_PHONE, TEST_PASSWORD)).thenReturn(true);
        when(userAuthService.getUserIdByPhone(TEST_PHONE)).thenReturn(TEST_USER_ID);

        response = userAuthController.login(queryObject);

        assertSuccessResponse(response);
        verify(userIdService, times(1)).setCurrentUserId(TEST_USER_ID);
    }

    @Test
    public void loginUserNotExistTest() {
        queryObject.phone = TEST_PHONE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);

        response = userAuthController.login(queryObject);

        assertErrorResponse(response, "user_not_exist");
        verify(userIdService, never()).setCurrentUserId(any());
    }

    @Test
    public void loginPasswordIncorrectTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(passwordService.decrypt(TEST_PASSWORD)).thenReturn(TEST_PASSWORD);
        when(userAuthService.validateUser(TEST_PHONE, TEST_PASSWORD)).thenReturn(false);

        response = userAuthController.login(queryObject);

        assertErrorResponse(response, "password_incorrect");
        verify(userIdService, never()).setCurrentUserId(any());
    }

    @Test
    public void loginAuthCodeTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);
        when(userAuthService.getUserIdByPhone(TEST_PHONE)).thenReturn(TEST_USER_ID);

        response = userAuthController.login(queryObject);

        assertSuccessResponse(response);
        verify(authCodeService, times(1)).removeAuthCodeRecord(TEST_PHONE);
        verify(userIdService, times(1)).setCurrentUserId(TEST_USER_ID);
    }

    @Test
    public void loginAuthCodeIncorrectTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);

        response = userAuthController.login(queryObject);

        assertErrorResponse(response, "authcode_incorrect");
        verify(userIdService, never()).setCurrentUserId(any());
    }

    @Test
    public void loginParamErrorTest() {
        queryObject.phone = TEST_PHONE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);

        Executable executable = () -> userAuthController.login(queryObject);

        Assertions.assertThrows(RuntimeException.class, executable);
    }

    @Test
    public void logoutTest() {
        // no pre-actions

        userAuthController.logout();

        verify(userIdService, times(1)).clearCurrentUserId();
    }

    @Test
    public void useridTest() {
        when(userIdService.getCurrentUserId()).thenReturn(TEST_USER_ID);

        response = userAuthController.userid();

        assertSuccessResponse(response);
        assertEquals(TEST_USER_ID, response.get("userid"));
    }

    @Test
    public void checkAuthCodeTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);

        response = userAuthController.checkAuthCode(queryObject);

        assertSuccessResponse(response);
        verify(authCodeService, times(1)).removeAuthCodeRecord(TEST_PHONE);
        verify(phoneValidatedService, times(1)).setPhoneValidated(TEST_PHONE);
    }

    @Test
    public void checkAuthCodePhoneNotExistTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);

        response = userAuthController.checkAuthCode(queryObject);

        assertErrorResponse(response, "phone_not_exist");
        verify(authCodeService, never()).removeAuthCodeRecord(TEST_PHONE);
        verify(phoneValidatedService, never()).setPhoneValidated(TEST_PHONE);
    }

    @Test
    public void checkAuthCodeIncorrectTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);

        response = userAuthController.checkAuthCode(queryObject);

        assertErrorResponse(response, "authcode_incorrect");
        verify(authCodeService, never()).removeAuthCodeRecord(TEST_PHONE);
        verify(phoneValidatedService, never()).setPhoneValidated(TEST_PHONE);
    }

    @Test
    public void changePasswordTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.password = TEST_PASSWORD;
        doNothing().when(phoneValidatedService).assertValidated(TEST_PHONE);
        when(passwordService.decrypt(TEST_PASSWORD)).thenReturn(TEST_PASSWORD);

        response = userAuthController.changePassword(queryObject);

        assertSuccessResponse(response);
        verify(userAuthService, times(1)).updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);
        verify(phoneValidatedService, times(1)).assertValidated(TEST_PHONE);
    }
}
