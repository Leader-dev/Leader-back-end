package com.leader.api.controller.app;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.leader.api.util.response.SuccessResponse.success;

@RestController
@RequestMapping("/app")
public class AppHostnameController {

    @Value("${leader.web-app-hostname}")
    private String hostname;

    @PostMapping("/web-app-hostname")
    public Document getWebAppHostname() {
        return success().data(
                "hostname", hostname
        );
    }
}
