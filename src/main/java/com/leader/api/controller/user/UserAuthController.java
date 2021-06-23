package com.leader.api.controller.user;

import com.leader.api.data.user.User;
import com.leader.api.service.user.UserAuthService;
import com.leader.api.service.util.AuthCodeService;
import com.leader.api.service.util.PasswordService;
import com.leader.api.service.util.PhoneValidatedService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.response.ErrorResponse;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final AuthCodeService authCodeService;
    private final PasswordService passwordService;
    private final UserIdService userIdService;
    private final PhoneValidatedService phoneValidatedService;

    @Autowired
    public UserAuthController(UserAuthService userAuthService, AuthCodeService authCodeService,
                              PasswordService passwordService, UserIdService userIdService,
                              PhoneValidatedService phoneValidatedService) {
        this.userAuthService = userAuthService;
        this.authCodeService = authCodeService;
        this.passwordService = passwordService;
        this.userIdService = userIdService;
        this.phoneValidatedService = phoneValidatedService;
    }

    public static class UserQueryObject {
        public String password;
        public String phone;
        public String authcode;
        public String nickname;
    }

    @PostMapping("/exist")
    public Document userExist(@RequestBody UserQueryObject queryObject) {
        boolean exist = userAuthService.phoneExists(queryObject.phone);

        Document response = new SuccessResponse();
        response.append("exist", exist);
        return response;
    }

    @PostMapping("/key")
    public Document getPublicKey() {
        // generate public key
        byte[] publicKey = passwordService.generateKey();

        // put public key in response
        Document response = new SuccessResponse();
        response.append("publicKey", publicKey);
        return response;
    }

    @PostMapping("/check")
    public Document checkText(@RequestBody UserQueryObject queryObject) {
        // decrypt password
        String text = passwordService.decrypt(queryObject.password);

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
    public Document registerUser(@RequestBody UserQueryObject queryObject) {
        // check phone
        if (userAuthService.phoneExists(queryObject.phone)) {
            return new ErrorResponse("phone_exist");
        }

        // check authcode
        if (!authCodeService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
            return new ErrorResponse("authcode_incorrect");
        }

        // decrypt password
        String password = passwordService.decrypt(queryObject.password);

        // actually create user
        User registeredUser = userAuthService.createUser(queryObject.phone, password, queryObject.nickname);

        // delete authcode record
        authCodeService.removeAuthCodeRecord(queryObject.phone);

        // save user id
        userIdService.setCurrentUserId(registeredUser.id);

        return new SuccessResponse();
    }

    @PostMapping("/login")
    public Document login(@RequestBody UserQueryObject queryObject) {
        // check phone
        if (!userAuthService.phoneExists(queryObject.phone)) {
            return new ErrorResponse("user_not_exist");
        }

        if (queryObject.password != null) {  // if chose to use password
            // decrypt password
            String password = passwordService.decrypt(queryObject.password);

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
        userIdService.setCurrentUserId(userid);

        return new SuccessResponse();
    }

    @PostMapping("/logout")
    public Document logout() {
        userIdService.clearCurrentUserId();

        return new SuccessResponse();
    }

    @PostMapping("/userid")
    public Document userid() {
        ObjectId userid = userIdService.getCurrentUserId();

        Document response = new SuccessResponse();
        response.append("userid", userid);
        return response;
    }

    @PostMapping("/check-authcode")
    public Document checkAuthCode(@RequestBody UserQueryObject queryObject) {
        // check phone
        if (!userAuthService.phoneExists(queryObject.phone)) {
            return new ErrorResponse("phone_not_exist");
        }
        // check authcode
        if (!authCodeService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
            return new ErrorResponse("authcode_incorrect");
        }

        // delete authcode record
        authCodeService.removeAuthCodeRecord(queryObject.phone);

        phoneValidatedService.setPhoneValidated(queryObject.phone);

        return new SuccessResponse();
    }

    @PostMapping("/change-password")
    public Document changePassword(@RequestBody UserQueryObject queryObject) {
        // check phone validated
        phoneValidatedService.assertValidated(queryObject.phone);

        // decrypt password
        String password = passwordService.decrypt(queryObject.password);

        // update user
        userAuthService.updateUserPasswordByPhone(queryObject.phone, password);

        return new SuccessResponse();
    }
}
