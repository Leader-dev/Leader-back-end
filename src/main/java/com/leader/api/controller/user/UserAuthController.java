package com.leader.api.controller.user;

import com.leader.api.service.user.UserAuthService;
import com.leader.api.service.util.AuthCodeService;
import com.leader.api.service.util.SessionService;
import com.leader.api.util.response.ErrorResponse;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserAuthController {

    private final UserAuthService userAuthService;

    private final AuthCodeService authCodeService;

    private final SessionService sessionService;

    @Autowired
    public UserAuthController(UserAuthService userAuthService, AuthCodeService authCodeService, SessionService sessionService) {
        this.userAuthService = userAuthService;
        this.authCodeService = authCodeService;
        this.sessionService = sessionService;
    }

    static class UserQueryObject {
        public String password;
        public String phone;
        public String authcode;
    }

    @PostMapping("/exist")
    public Document userExist(@RequestBody UserQueryObject queryObject) {
        boolean exist = userAuthService.phoneExists(queryObject.phone);

        Document response = new SuccessResponse();
        response.append("exist", exist);
        return response;
    }

    @PostMapping("/key")
    public Document getPublicKey(HttpSession session) {
        // generate public key
        byte[] publicKey = userAuthService.generateKeyPair(session);

        // put public key in response
        Document response = new SuccessResponse();
        response.append("publicKey", publicKey);
        return response;
    }

    @PostMapping("/check")
    public Document checkText(@RequestBody UserQueryObject queryObject, HttpSession session) {
        // decrypt password
        String text = userAuthService.decryptPassword(session, queryObject.password);

        Document response = new SuccessResponse();
        response.append("text", text);
        return response;
    }

    @PostMapping("/authcode")
    public Document sendAuthCode(@RequestBody UserQueryObject queryObject) {
        boolean sendSuccess = authCodeService.sendAuthCode(queryObject.phone);
        if (!sendSuccess) {
            return new ErrorResponse("request_too_frequent");
        }

        return new SuccessResponse();
    }

    @PostMapping("/register")
    public Document registerUser(@RequestBody UserQueryObject queryObject, HttpSession session) {
        // check phone
        if (userAuthService.phoneExists(queryObject.phone)) {
            return new ErrorResponse("phone_exist");
        }

        // check authcode
        if (!authCodeService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
            return new ErrorResponse("authcode_incorrect");
        }

        String password;
        if (queryObject.password == null) {
            password = null;
        } else {
            // decrypt password
            password = userAuthService.decryptPassword(session, queryObject.password);
        }

        // actually create user
        userAuthService.createUser(queryObject.phone, password);

        // delete authcode record
        authCodeService.removeAuthCodeRecord(queryObject.phone);

        return new SuccessResponse();
    }

    @PostMapping("/login")
    public Document login(@RequestBody UserQueryObject queryObject, HttpSession session) {
        // check phone
        if (!userAuthService.phoneExists(queryObject.phone)) {
            return new ErrorResponse("user_not_exist");
        }

        if (queryObject.password != null) {  // if chose to use password
            // decrypt password
            String password = userAuthService.decryptPassword(session, queryObject.password);

            // check password
            if (!userAuthService.validateUser(queryObject.phone, password)) {
                return new ErrorResponse("password_incorrect");
            }
        } else if (queryObject.authcode != null) {  // if chose to use phone authcode
            // check authcode
            if (!authCodeService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
                return new ErrorResponse("authcode_incorrect");
            }

            // invalidate current authcode
            authCodeService.removeAuthCodeRecord(queryObject.phone);
        } else {
            throw new RuntimeException("Expect password or authcode attribute in request");
        }

        // update session
        ObjectId userid = userAuthService.getUserIdByPhone(queryObject.phone);
        sessionService.saveUserIdToSession(session, userid);

        return new SuccessResponse();
    }

    @PostMapping("/logout")
    public Document logout(HttpSession session) {
        sessionService.removeUserIdFromSession(session);

        return new SuccessResponse();
    }

    @PostMapping("/userid")
    public Document userid(HttpSession session) {
        ObjectId userid = sessionService.getUserIdFromSession(session);

        Document response = new SuccessResponse();
        response.append("userid", userid);
        return response;
    }

    @PostMapping("/changepassword")
    public Document changePassword(@RequestBody UserQueryObject queryObject, HttpSession session) {
        // check phone
        if (!userAuthService.phoneExists(queryObject.phone)) {
            return new ErrorResponse("phone_not_exist");
        }
        // check authcode
        if (!authCodeService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
            return new ErrorResponse("authcode_incorrect");
        }

        // decrypt password
        String password = userAuthService.decryptPassword(session, queryObject.password);

        // update user
        userAuthService.updateUserPasswordByPhone(queryObject.phone, password);

        // delete authcode record
        authCodeService.removeAuthCodeRecord(queryObject.phone);

        return new SuccessResponse();
    }
}
