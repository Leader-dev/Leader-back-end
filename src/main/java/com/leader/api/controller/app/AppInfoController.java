package com.leader.api.controller.app;

import com.leader.api.service.app.AppInfoService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class AppInfoController {

    private final AppInfoService appInfoService;

    @Autowired
    public AppInfoController(AppInfoService appInfoService) {
        this.appInfoService = appInfoService;
    }

    @PostMapping("/agreement")
    public Document getAgreement() {
        return new SuccessResponse(
                "md", appInfoService.getAgreement()
        );
    }

    @PostMapping("/privacy")
    public Document getPrivacy() {
        return new SuccessResponse(
                "md", appInfoService.getPrivacy()
        );
    }
}
