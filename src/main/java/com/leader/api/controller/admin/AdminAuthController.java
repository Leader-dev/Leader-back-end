package com.leader.api.controller.admin;

import com.leader.api.data.admin.AdminInfo;
import com.leader.api.service.admin.AdminAuthService;
import com.leader.api.service.admin.AdminIdService;
import com.leader.api.service.util.PasswordService;
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
@RequestMapping("/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminIdService adminIdService;
    private final PasswordService passwordService;

    @Autowired
    public AdminAuthController(AdminAuthService adminAuthService, AdminIdService adminIdService, PasswordService passwordService) {
        this.adminAuthService = adminAuthService;
        this.adminIdService = adminIdService;
        this.passwordService = passwordService;
    }

    public static class QueryObject {
        public String username;
        public String password;
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

    @PostMapping("/login")
    public Document login(@RequestBody QueryObject queryObject) {
        if (!adminAuthService.usernameExists(queryObject.username)) {
            return new ErrorResponse("username_not_exist");
        }

        String password = passwordService.decrypt(queryObject.password);
        if (!adminAuthService.validateAdmin(queryObject.username, password)) {
            return new ErrorResponse("password_incorrect");
        }

        ObjectId adminId = adminAuthService.getAdminId(queryObject.username);
        adminIdService.setCurrentAdminId(adminId);

        return new SuccessResponse();
    }

    @PostMapping("/info")
    public Document info() {
        ObjectId adminId = adminIdService.getCurrentAdminId();

        AdminInfo info = adminAuthService.getAdminInfo(adminId);

        Document response = new SuccessResponse();
        response.append("data", info);
        return response;
    }

    @PostMapping("/logout")
    public Document logout() {
        adminIdService.clearCurrentAdminId();

        return new SuccessResponse();
    }

    @PostMapping("/change-password")
    public Document changePassword(@RequestBody QueryObject queryObject) {
        ObjectId adminId = adminIdService.getCurrentAdminId();

        String password = passwordService.decrypt(queryObject.password);
        adminAuthService.changePassword(adminId, password);

        return new SuccessResponse();
    }
}
