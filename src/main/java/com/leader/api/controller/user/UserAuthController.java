package com.leader.api.controller.user;

import com.leader.api.service.user.UserAuthService;
import com.leader.api.service.util.AuthCodeService;
import com.leader.api.service.util.PasswordService;
import com.leader.api.service.util.PhoneValidatedService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.response.ErrorResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.leader.api.util.response.ErrorResponse.error;
import static com.leader.api.util.response.SuccessResponse.success;

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

    private String phoneOrCurrentUserPhone(String phone) {
        if (phone == null) {
            ObjectId userid = userIdService.getCurrentUserId();
            return userAuthService.getUserPhoneById(userid);
        }
        return phone;
    }

    @PostMapping("/exist")
    public Document userExist(@RequestBody UserQueryObject queryObject) {
        boolean exist = userAuthService.phoneExists(queryObject.phone);

        return success(
                "exist", exist
        );
    }

    @PostMapping("/key")
    public Document getPublicKey() {
        // generate public key
        byte[] publicKey = passwordService.generateKey();

        // put public key in response
        return success(
                "publicKey", publicKey
        );
    }

    @PostMapping("/check")
    public Document checkText(@RequestBody UserQueryObject queryObject) {
        // decrypt password
        String text = passwordService.decrypt(queryObject.password);

        return success(
                "text", text
        );
    }

    @PostMapping("/authcode")
    public Document sendAuthCode(@RequestBody UserQueryObject queryObject) {
        String phone = phoneOrCurrentUserPhone(queryObject.phone);
        boolean sendSuccess = authCodeService.sendAuthCode(phone);
        if (!sendSuccess) {
            return error("request_too_frequent");
        }

        return success();
    }

    @PostMapping("/quick-login")
    public Document quickLogin(@RequestBody UserQueryObject queryObject) {
        // check authcode
        if (!authCodeService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
            return error("authcode_incorrect");
        }

        ObjectId userid;
        if (userAuthService.phoneExists(queryObject.phone)) {
            userid = userAuthService.getUserIdByPhone(queryObject.phone);
        } else {
            userid = userAuthService.createUser(queryObject.phone).id;
        }
        userIdService.setCurrentUserId(userid);

        // invalidate current authcode
        authCodeService.removeAuthCodeRecord(queryObject.phone);

        return success();
    }

    @PostMapping("/register")
    public Document registerUser(@RequestBody UserQueryObject queryObject) {
        // check authcode
        if (!authCodeService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
            return error("authcode_incorrect");
        }

        // decrypt password
        String password = passwordService.decrypt(queryObject.password);

        ObjectId userId;
        if (userAuthService.phoneExists(queryObject.phone)) {
            if (userAuthService.hasPassword(queryObject.phone)) {
                return error("user_exist");
            }
            userAuthService.updateUserPasswordByPhone(queryObject.phone, password);
            userAuthService.updateUserNicknameByPhone(queryObject.phone, queryObject.nickname);
            userId = userAuthService.getUserIdByPhone(queryObject.phone);
        } else {
            userId = userAuthService.createUser(queryObject.phone, password, queryObject.nickname).id;
        }
        userIdService.setCurrentUserId(userId);

        // invalidate current authcode
        authCodeService.removeAuthCodeRecord(queryObject.phone);

        return success();
    }

    @PostMapping("/login")
    public Document login(@RequestBody UserQueryObject queryObject) {
        // check phone
        if (!userAuthService.phoneExists(queryObject.phone)) {
            return error("user_not_exist");
        }

        if (queryObject.password != null) {  // if chose to use password
            // decrypt password
            String password = passwordService.decrypt(queryObject.password);

            // check password
            if (!userAuthService.validateUser(queryObject.phone, password)) {
                return error("password_incorrect");
            }
        } else if (queryObject.authcode != null) {  // if chose to use phone authcode
            // check authcode
            if (!authCodeService.validateAuthCode(queryObject.phone, queryObject.authcode)) {
                return error("authcode_incorrect");
            }

            if (!userAuthService.hasPassword(queryObject.phone)) {
                return error("need_info");
            }

            // invalidate current authcode
            authCodeService.removeAuthCodeRecord(queryObject.phone);
        } else {
            throw new InternalErrorException("Expect password or authcode attribute in request");
        }

        // update session
        ObjectId userid = userAuthService.getUserIdByPhone(queryObject.phone);
        userIdService.setCurrentUserId(userid);

        return success();
    }

    @PostMapping("/logout")
    public Document logout() {
        userIdService.clearCurrentUserId();

        return success();
    }

    @PostMapping("/phone")
    public Document getUserPhone() {
        ObjectId userId = userIdService.getCurrentUserId();
        String phone = userAuthService.getUserPhoneById(userId);

        return success(
                "phone", phone
        );
    }

    @PostMapping("/userid")
    public Document userid() {
        ObjectId userid = userIdService.getCurrentUserId();

        return success(
                "userid", userid
        );
    }

    @PostMapping("/check-authcode")
    public Document checkAuthCode(@RequestBody UserQueryObject queryObject) {
        String phone = phoneOrCurrentUserPhone(queryObject.phone);

        if (!userAuthService.phoneExists(phone)) {
            return new ErrorResponse("phone_not_exist");
        }
        // check authcode
        if (!authCodeService.validateAuthCode(phone, queryObject.authcode)) {
            return new ErrorResponse("authcode_incorrect");
        }

        // delete authcode record
        authCodeService.removeAuthCodeRecord(phone);

        phoneValidatedService.setPhoneValidated(phone);

        return success();
    }

    @PostMapping("/change-password")
    public Document changePassword(@RequestBody UserQueryObject queryObject) {
        String phone = phoneOrCurrentUserPhone(queryObject.phone);

        // check phone validated
        phoneValidatedService.assertValidated(phone);

        // decrypt password
        String password = passwordService.decrypt(queryObject.password);

        // update user
        userAuthService.updateUserPasswordByPhone(phone, password);

        return success();
    }
}
