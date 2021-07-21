package com.leader.api.controller.admin.user;

import com.leader.api.service.admin.user.AdminUserService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    private final AdminUserService userService;

    @Autowired
    public AdminUserController(AdminUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/list")
    public Document list() {
        Document response = new SuccessResponse();
        response.append("list", userService.getAllUsers().getContent());
        return response;
    }
}
