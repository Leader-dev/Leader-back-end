package com.leader.api.controller;

import com.leader.api.ApiApplication;
import com.leader.api.response.SuccessResponse;
import org.bson.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiInfo {

    @RequestMapping("/api/info")
    public Document getApiInfo() {
        Document response = new SuccessResponse();
        response.append("processStartTime", ApiApplication.PROCESS_START_TIME.toString());
        return response;
    }
}
