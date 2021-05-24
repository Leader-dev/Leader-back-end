package com.leader.api.controller.user;

import com.leader.api.response.ErrorResponse;
import com.leader.api.response.SuccessResponse;
import com.leader.api.service.UserAuthService;
import com.leader.api.util.SessionUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class Auth {

    @Autowired
    private UserAuthService userAuthService;

    private static class UserQueryObject {
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
    public Document getAuthCode(@RequestBody UserQueryObject queryObject) {
        boolean sendSuccess = userAuthService.sendAuthCode(queryObject.phone);
        if (!sendSuccess) {
            return new ErrorResponse("request_too_frequent");
        }

        return new SuccessResponse();
    }

    @PostMapping("/register")
    public Document createUser(@RequestBody UserQueryObject queryObject, HttpSession session) {
        // check authcode
        if (!userAuthService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
            return new ErrorResponse("authcode_incorrect");
        }

        // check phone
        if (userAuthService.phoneExists(queryObject.phone)) {
            return new ErrorResponse("phone_exist");
        }

        // decrypt password
        String password = userAuthService.decryptPassword(session, queryObject.password);

        // actually create user
        userAuthService.createUser(password, queryObject.phone);

        // delete authcode record
        userAuthService.removeAuthCodeRecord(queryObject.phone);

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
            if (!userAuthService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
                return new ErrorResponse("authcode_incorrect");
            }
        } else {
            throw new RuntimeException("Expect password or authcode attribute in request");
        }

        // update session
        ObjectId userid = userAuthService.getUserIdByPhone(queryObject.phone);
        SessionUtil.saveUserIdToSession(session, userid);

        return new SuccessResponse();
    }

    @PostMapping("/logout")
    public Document logout(HttpSession session) {
        SessionUtil.removeUserIdFromSession(session);

        return new SuccessResponse();
    }

    @PostMapping("/userid")
    public Document userid(HttpSession session) {
        ObjectId userid = SessionUtil.getUserIdFromSession(session);

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
        if (!userAuthService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
            return new ErrorResponse("authcode_incorrect");
        }

        // decrypt password
        String password = userAuthService.decryptPassword(session, queryObject.password);

        // update user
        userAuthService.updateUserPasswordByPhone(queryObject.phone, password);

        // delete authcode record
        userAuthService.removeAuthCodeRecord(queryObject.phone);

        return new SuccessResponse();
    }
}
