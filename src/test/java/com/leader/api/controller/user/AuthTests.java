package com.leader.api.controller.user;

import com.leader.api.service.user.UserAuthService;
import com.leader.api.service.util.AuthCodeService;
import com.leader.api.test.util.JSONErrorResponse;
import com.leader.api.test.util.JSONInternalErrorResponse;
import com.leader.api.test.util.JSONSuccessResponse;
import com.leader.api.service.util.SessionService;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static com.leader.api.test.util.Util.performRestTestAndExpectResponse;
import static org.mockito.Mockito.*;

@WebMvcTest(Auth.class)
public class AuthTests {

    final String TEST_PHONE = "13360097989";
    final String TEST_AUTHCODE = "123456";
    final String TEST_PASSWORD = "xxxxxxxx";
    final String TEST_PUBLIC_KEY = "1234567890";
    final ObjectId TEST_USER_ID = new ObjectId();

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserAuthService userAuthService;

    @MockBean
    AuthCodeService authCodeService;

    @MockBean
    SessionService sessionService;

    JSONObject request;
    JSONObject response;
    MockHttpSession session;

    @BeforeEach
    void setup() {
        // reset variables and mock instance call each time
        request = new JSONObject();
        response = new JSONObject();
        session = new MockHttpSession();
        Mockito.clearInvocations(userAuthService);
    }

    @Test
    public void userExistTest() throws Exception {

        // setup

        request.put("phone", TEST_PHONE);

        // exist

        response = new JSONSuccessResponse();
        response.put("exist", true);

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        performRestTestAndExpectResponse(mockMvc, "/user/exist", request, response);

        // not exist

        response.put("exist", false);

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/exist", request, response);
    }

    @Test
    public void publicKeyTest() throws Exception {
        response = new JSONSuccessResponse();
        response.put("publicKey", TEST_PUBLIC_KEY);

        when(userAuthService.generateKeyPair(any())).thenReturn(TEST_PUBLIC_KEY);
        performRestTestAndExpectResponse(mockMvc, "/user/key", response);
    }

    @Test
    public void checkTest() throws Exception {

        request.put("password", TEST_PASSWORD);

        response = new JSONSuccessResponse();
        response.put("text", TEST_PASSWORD);

        when(userAuthService.decryptPassword(any(), eq(TEST_PASSWORD))).thenReturn(TEST_PASSWORD);
        performRestTestAndExpectResponse(mockMvc, "/user/check", request, response);
    }

    @Test
    public void sendAuthCodeTest() throws Exception {

        // setup

        request.put("phone", TEST_PHONE);

        // normal case

        response = new JSONSuccessResponse();

        when(authCodeService.sendAuthCode(TEST_PHONE)).thenReturn(true);
        performRestTestAndExpectResponse(mockMvc, "/user/authcode", request, response);

        // error case

        response = new JSONErrorResponse("request_too_frequent");

        when(authCodeService.sendAuthCode(TEST_PHONE)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/authcode", request, response);
    }

    @Test
    public void registerTest() throws Exception {

        // setup

        request.put("phone", TEST_PHONE);
        request.put("authcode", TEST_AUTHCODE);
        request.put("password", TEST_PASSWORD);

        // normal case

        response = new JSONSuccessResponse();

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);
        when(userAuthService.decryptPassword(any(), eq(TEST_PASSWORD))).thenReturn(TEST_PASSWORD);
        performRestTestAndExpectResponse(mockMvc, "/user/register", request, response);
        verify(userAuthService, atLeastOnce()).createUser(TEST_PHONE, TEST_PASSWORD);
        verify(authCodeService, atLeastOnce()).removeAuthCodeRecord(TEST_PHONE);
        clearInvocations(userAuthService);

        // error case 1

        response = new JSONErrorResponse("phone_exist");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        performRestTestAndExpectResponse(mockMvc, "/user/register", request, response);
        verify(userAuthService, never()).createUser(TEST_PHONE, TEST_PASSWORD);

        // error case 2

        response = new JSONErrorResponse("authcode_incorrect");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/register", request, response);
        verify(userAuthService, never()).createUser(TEST_PHONE, TEST_PASSWORD);
    }

    @Test
    public void loginPasswordTest() throws Exception {

        // setup

        request.put("phone", TEST_PHONE);
        request.put("password", TEST_PASSWORD);

        // normal case

        response = new JSONSuccessResponse();

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(userAuthService.decryptPassword(any(), eq(TEST_PASSWORD))).thenReturn(TEST_PASSWORD);
        when(userAuthService.validateUser(TEST_PHONE, TEST_PASSWORD)).thenReturn(true);
        when(userAuthService.getUserIdByPhone(TEST_PHONE)).thenReturn(TEST_USER_ID);
        performRestTestAndExpectResponse(mockMvc, "/user/login", request, response, session);
        verify(sessionService, atLeastOnce()).saveUserIdToSession(session, TEST_USER_ID);
        clearInvocations(sessionService);

        // error case 1

        response = new JSONErrorResponse("user_not_exist");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/login", request, response, session);
        verify(sessionService, never()).saveUserIdToSession(session, TEST_USER_ID);

        // error case 2

        response = new JSONErrorResponse("password_incorrect");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(userAuthService.decryptPassword(any(), eq(TEST_PASSWORD))).thenReturn(TEST_PASSWORD);
        when(userAuthService.validateUser(TEST_PHONE, TEST_PASSWORD)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/login", request, response, session);
        verify(sessionService, never()).saveUserIdToSession(session, TEST_USER_ID);
    }

    @Test
    public void loginAuthcodeTest() throws Exception {

        // setup

        request.put("phone", TEST_PHONE);
        request.put("authcode", TEST_AUTHCODE);

        // normal case

        response = new JSONSuccessResponse();

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);
        when(userAuthService.getUserIdByPhone(TEST_PHONE)).thenReturn(TEST_USER_ID);
        performRestTestAndExpectResponse(mockMvc, "/user/login", request, response, session);
        verify(authCodeService, atLeastOnce()).removeAuthCodeRecord(TEST_PHONE);
        verify(sessionService, atLeastOnce()).saveUserIdToSession(session, TEST_USER_ID);
        clearInvocations(sessionService);

        // error case 1

        response = new JSONErrorResponse("user_not_exist");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/login", request, response, session);
        verify(sessionService, never()).saveUserIdToSession(session, TEST_USER_ID);

        // error case 2

        response = new JSONErrorResponse("authcode_incorrect");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/login", request, response, session);
        verify(sessionService, never()).saveUserIdToSession(session, TEST_USER_ID);
    }

    @Test
    public void loginErrorParam() throws Exception {

        request.put("phone", TEST_PHONE);

        response = new JSONInternalErrorResponse("Expect password or authcode attribute in request");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        performRestTestAndExpectResponse(mockMvc, "/user/login", request, response, session);
    }

    @Test
    public void logoutTest() throws Exception {
        session.setAttribute(SessionService.USER_ID, TEST_USER_ID);
        performRestTestAndExpectResponse(mockMvc, "/user/logout", response, session);
        verify(sessionService, atLeastOnce()).removeUserIdFromSession(session);
    }

    @Test
    public void useridTest() throws Exception {

        // normal case

        when(sessionService.getUserIdFromSession(session)).thenReturn(TEST_USER_ID);

        response = new JSONSuccessResponse();
        response.put("userid", TEST_USER_ID);

        performRestTestAndExpectResponse(mockMvc, "/user/userid", response, session);

        // null case

        when(sessionService.getUserIdFromSession(session)).thenReturn(null);

        response = new JSONSuccessResponse();
        response.put("userid", null);

        performRestTestAndExpectResponse(mockMvc, "/user/userid", response, session);
    }

    @Test
    public void changePasswordTest() throws Exception {

        // setup

        request.put("phone", TEST_PHONE);
        request.put("authcode", TEST_AUTHCODE);
        request.put("password", TEST_PASSWORD);

        // normal case

        response = new JSONSuccessResponse();

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(true);
        when(userAuthService.decryptPassword(any(), eq(TEST_PASSWORD))).thenReturn(TEST_PASSWORD);
        performRestTestAndExpectResponse(mockMvc, "/user/changepassword", request, response);
        verify(userAuthService, atLeastOnce()).updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);
        verify(authCodeService, atLeastOnce()).removeAuthCodeRecord(TEST_PHONE);
        clearInvocations(userAuthService);

        // error case 1

        response = new JSONErrorResponse("phone_not_exist");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/changepassword", request, response);
        verify(userAuthService, never()).updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);

        // error case 2

        response = new JSONErrorResponse("authcode_incorrect");

        when(userAuthService.phoneExists(TEST_PHONE)).thenReturn(true);
        when(authCodeService.validateAuthCode(TEST_PHONE, TEST_AUTHCODE)).thenReturn(false);
        performRestTestAndExpectResponse(mockMvc, "/user/changepassword", request, response);
        verify(userAuthService, never()).updateUserPasswordByPhone(TEST_PHONE, TEST_PASSWORD);
    }
}
