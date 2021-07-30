package com.leader.api.controller.admin.app;

import com.leader.api.service.app.AppInfoService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/app")
public class AdminAppInfoController {

    private final AppInfoService appInfoService;

    @Autowired
    public AdminAppInfoController(AppInfoService appInfoService) {
        this.appInfoService = appInfoService;
    }

    public static class QueryObject {
        public String md;
    }

    @PostMapping("/set-agreement")
    public Document getAgreement(@RequestBody QueryObject queryObject) {
        appInfoService.setAgreement(queryObject.md);
        return new SuccessResponse();
    }

    @PostMapping("/set-privacy")
    public Document getPrivacy(@RequestBody QueryObject queryObject) {
        appInfoService.setPrivacy(queryObject.md);
        return new SuccessResponse();
    }
}
