package com.leader.api.controller.user;

import com.leader.api.service.user.UserAuthService;
import com.leader.api.service.util.AuthCodeService;
import com.leader.api.service.util.SessionService;
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
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

import static com.leader.api.test.util.Util.assertErrorResponse;
import static com.leader.api.test.util.Util.assertSuccessResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserAuthControllerTests {

    private static final String TEST_PHONE = "13360097989";
    private static final String TEST_AUTHCODE = "123456";
    private static final String TEST_PASSWORD = "xxxxxxxx";
    private static final byte[] TEST_PUBLIC_KEY = "1234567890".getBytes(StandardCharsets.UTF_8);
    private static final ObjectId TEST_USER_ID = new ObjectId();
    private static final HttpSession TEST_SESSION = new MockHttpSession();

    @Autowired
    private UserAuthController userAuthController;

    @MockBean
    private UserAuthService userAuthService;

    @MockBean
    private AuthCodeService authCodeService;

    @MockBean
    private SessionService sessionService;

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
        when(userAuthService.generateKeyPair(TEST_SESSION)).thenReturn(TEST_PUBLIC_KEY);

        response = userAuthController.getPublicKey(TEST_SESSION);

        assertSuccessResponse(response);
        assertEquals(TEST_PUBLIC_KEY, response.get("publicKey"));
    }

    @Test
    public void checkTest() {
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.decryptPassword(TEST_SESSION, TEST_PASSWORD)).thenReturn(TEST_PASSWORD);

        response = userAuthController.checkText(queryObject, TEST_SESSION);

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
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);
        when(userAuthService.decryptPassword(TEST_SESSION, TEST_PASSWORD)).thenReturn(TEST_PASSWORD);

        response = userAuthController.registerUser(queryObject, TEST_SESSION);

        assertSuccessResponse(response);
        verify(userAuthService, atLeastOnce()).createUser(TEST_PHONE, TEST_PASSWORD);
        verify(authCodeService, atLeastOnce()).removeAuthCodeRecord(TEST_PHONE);
    }

    @Test
    public void registerNoPasswordTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);

        response = userAuthController.registerUser(queryObject, TEST_SESSION);

        assertSuccessResponse(response);
        verify(userAuthService, never()).decryptPassword(any(), any());
        verify(userAuthService, atLeastOnce()).createUser(TEST_PHONE, null);
        verify(authCodeService, atLeastOnce()).removeAuthCodeRecord(TEST_PHONE);
        clearInvocations(userAuthService);
    }

    @Test
    public void registerPhoneExistTest() {
        queryObject.phone = TEST_PHONE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);

        response = userAuthController.registerUser(queryObject, TEST_SESSION);

        assertErrorResponse(response, "phone_exist");
        verify(userAuthService, never()).createUser(any(), any());
        verify(authCodeService, never()).removeAuthCodeRecord(TEST_PHONE);
    }

    @Test
    public void registerAuthCodeIncorrectTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);

        response = userAuthController.registerUser(queryObject, TEST_SESSION);

        assertErrorResponse(response, "authcode_incorrect");
        verify(userAuthService, never()).createUser(any(), any());
        verify(authCodeService, never()).removeAuthCodeRecord(TEST_PHONE);
    }

    @Test
    public void loginPasswordTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(userAuthService.decryptPassword(TEST_SESSION, TEST_PASSWORD)).thenReturn(TEST_PASSWORD);
        when(userAuthService.validateUser(TEST_PHONE, TEST_PASSWORD)).thenReturn(true);
        when(userAuthService.getUserIdByPhone(TEST_PHONE)).thenReturn(TEST_USER_ID);

        response = userAuthController.login(queryObject, TEST_SESSION);

        assertSuccessResponse(response);
        verify(sessionService, atLeastOnce()).saveUserIdToSession(TEST_SESSION, TEST_USER_ID);
        clearInvocations(sessionService);
    }

    @Test
    public void loginUserNotExistTest() {
        queryObject.phone = TEST_PHONE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);

        response = userAuthController.login(queryObject, TEST_SESSION);

        assertErrorResponse(response, "user_not_exist");
        verify(sessionService, never()).saveUserIdToSession(TEST_SESSION, TEST_USER_ID);
    }

    @Test
    public void loginPasswordIncorrectTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(userAuthService.decryptPassword(TEST_SESSION, TEST_PASSWORD)).thenReturn(TEST_PASSWORD);
        when(userAuthService.validateUser(TEST_PHONE, TEST_PASSWORD)).thenReturn(false);

        response = userAuthController.login(queryObject, TEST_SESSION);

        assertErrorResponse(response, "password_incorrect");
        verify(sessionService, never()).saveUserIdToSession(TEST_SESSION, TEST_USER_ID);
    }

    @Test
    public void loginAuthCodeTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);
        when(userAuthService.getUserIdByPhone(TEST_PHONE)).thenReturn(TEST_USER_ID);

        response = userAuthController.login(queryObject, TEST_SESSION);

        assertSuccessResponse(response);
        verify(authCodeService, atLeastOnce()).removeAuthCodeRecord(TEST_PHONE);
        verify(sessionService, atLeastOnce()).saveUserIdToSession(TEST_SESSION, TEST_USER_ID);
        clearInvocations(sessionService);
    }

    @Test
    public void loginAuthCodeIncorrectTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);

        response = userAuthController.login(queryObject, TEST_SESSION);

        assertErrorResponse(response, "authcode_incorrect");
        verify(sessionService, never()).saveUserIdToSession(TEST_SESSION, TEST_USER_ID);
    }

    @Test
    public void loginParamErrorTest() {
        queryObject.phone = TEST_PHONE;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);

        Executable executable = () -> userAuthController.login(queryObject, TEST_SESSION);

        Assertions.assertThrows(RuntimeException.class, executable);
    }

    @Test
    public void logoutTest() {
        // no Given-actions

        userAuthController.logout(TEST_SESSION);

        verify(sessionService, atLeastOnce()).removeUserIdFromSession(TEST_SESSION);
    }

    @Test
    public void useridTest() {
        when(sessionService.getUserIdFromSession(TEST_SESSION)).thenReturn(TEST_USER_ID);

        response = userAuthController.userid(TEST_SESSION);

        assertSuccessResponse(response);
        assertEquals(TEST_USER_ID, response.get("userid"));
    }


    @Test
    public void changePasswordTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);
        when(userAuthService.decryptPassword(TEST_SESSION, TEST_PASSWORD)).thenReturn(TEST_PASSWORD);

        response = userAuthController.changePassword(queryObject, TEST_SESSION);

        assertSuccessResponse(response);
        verify(userAuthService, atLeastOnce()).updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);
        verify(authCodeService, atLeastOnce()).removeAuthCodeRecord(TEST_PHONE);
        clearInvocations(userAuthService);
    }

    @Test
    public void changePasswordPhoneNotExistTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);

        response = userAuthController.changePassword(queryObject, TEST_SESSION);

        assertErrorResponse(response, "phone_not_exist");
        verify(userAuthService, never()).updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);
    }

    @Test
    public void changePasswordAuthCodeIncorrectTest() {
        queryObject.phone = TEST_PHONE;
        queryObject.authcode = TEST_AUTHCODE;
        queryObject.password = TEST_PASSWORD;
        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);

        response = userAuthController.changePassword(queryObject, TEST_SESSION);

        assertErrorResponse(response, "authcode_incorrect");
        verify(userAuthService, never()).updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);
    }
}
