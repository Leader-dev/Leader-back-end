package com.leader.api.controller.service;

import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/service/upload")
public class Upload {

    @PostMapping("/single")
    public Document uploadSingle(@RequestParam("file") MultipartFile file) {
        Document response = new SuccessResponse();
        response.append("url", "");
        return response;
    }
}
