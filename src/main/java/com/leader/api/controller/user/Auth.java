package com.leader.api.controller.user;

import com.leader.api.data.user.AuthCodeRecordRepository;
import com.leader.api.data.user.UserRepository;
import com.leader.api.response.ErrorResponse;
import com.leader.api.response.SuccessResponse;
import com.leader.api.util.Util;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class Auth {

    private static final long AUTHCODE_REQUEST_INTERVAL = 60000;
    private static final long AUTHCODE_EXPIRE = 300000;
    private static final int SALT_LENGTH = 16;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthCodeRecordRepository authCodeRecordRepository;

    private static class UserQueryObject {
        public String username;
        public String password;
        public String phone;
        public String authcode;
    }

    @PostMapping("/exist")
    public Document userExist(@RequestBody UserQueryObject queryObject) {
        boolean exist = false;
        if (queryObject.username != null) {
            exist = userRepository.existsByUsername(queryObject.username);
        } else if (queryObject.phone != null) {
            exist = userRepository.existsByPhone(queryObject.phone);
        }
        Document response = new SuccessResponse();
        response.append("exist", exist);
        return response;
    }

    @PostMapping("/authcode")
    public Document getAuthCode(@RequestBody UserQueryObject queryObject) {
        long timePassed = authCodeRecordRepository.timePassedSinceLastAuthCode(queryObject.phone);
        if (timePassed != -1 && timePassed < AUTHCODE_REQUEST_INTERVAL) {
            return new ErrorResponse("request_too_frequent");
        }
        authCodeRecordRepository.generateAuthCode(queryObject.phone);
        // TODO Actually send the authcode to phone
        return new SuccessResponse();
    }

    @PostMapping("/register")
    public Document createUser(@RequestBody UserQueryObject queryObject) {
        // check authcode
        if (!authCodeRecordRepository.isAuthCodeValid(queryObject.phone, queryObject.authcode, AUTHCODE_EXPIRE)) {
            return new ErrorResponse("authcode_incorrect");
        }
        // check username
        if (userRepository.existsByUsername(queryObject.username)) {
            return new ErrorResponse("username_exist");
        }
        // check phone
        if (userRepository.existsByPhone(queryObject.phone)) {
            return new ErrorResponse("phone_exist");
        }

        // add user
        userRepository.insertUser(queryObject.username, queryObject.password, queryObject.phone, SALT_LENGTH);

        // delete authcode record
        authCodeRecordRepository.deleteByPhone(queryObject.phone);

        return new SuccessResponse();
    }

    @PostMapping("/login")
    public Document login(@RequestBody UserQueryObject queryObject, HttpSession session) {
        if (!userRepository.existsByUsername(queryObject.username)) {
            return new ErrorResponse("user_not_exist");
        }
        if (!userRepository.validateUser(queryObject.username, queryObject.password)) {
            return new ErrorResponse("password_incorrect");
        }
        session.setAttribute("user_id", userRepository.getIdByUsername(queryObject.username));
        return new SuccessResponse();
    }

    @PostMapping("/logout")
    public Document logout(HttpSession session) {
        session.invalidate();
        return new SuccessResponse();
    }

    @PostMapping("/userid")
    public ObjectId userid(HttpSession session) {
        return Util.getUserIdFromSession(session);
    }

    @PostMapping("/changepassword")
    public Document changePassword(@RequestBody UserQueryObject queryObject) {
        // check phone
        if (!userRepository.existsByPhone(queryObject.phone)) {
            return new ErrorResponse("phone_not_exist");
        }
        // check authcode
        if (!authCodeRecordRepository.isAuthCodeValid(queryObject.phone, queryObject.authcode, AUTHCODE_EXPIRE)) {
            return new ErrorResponse("authcode_incorrect");
        }

        // update user
        userRepository.updatePasswordByPhone(queryObject.phone, queryObject.password, SALT_LENGTH);

        // delete authcode record
        authCodeRecordRepository.deleteByPhone(queryObject.phone);

        return new SuccessResponse();
    }

    @PostMapping("/test")
    public UserQueryObject test(@RequestParam("value") UserQueryObject queryObject) {
        return queryObject;
    }
}
