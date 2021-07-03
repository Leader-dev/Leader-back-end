package com.leader.api.controller.service;

import com.leader.api.service.service.ImageService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service/image")
public class ImageUploadController {

    private final ImageService imageService;

    @Autowired
    public ImageUploadController(ImageService imageService) {
        this.imageService = imageService;
    }

    public static class QueryObject {
        public Integer urlCount;
    }

    @PostMapping("/access-start-url")
    public Document getAccessStartUrl() {
        String startUrl = imageService.getAccessStartUrl();

        Document response = new SuccessResponse();
        response.append("start", startUrl);
        return response;
    }

    @PostMapping("/get-upload-url")
    public Document getUploadUrl() {
        imageService.cleanUp();

        String uploadUrl = imageService.generateNewUploadUrl();

        Document response = new SuccessResponse();
        response.append("url", uploadUrl);
        return response;
    }

    @PostMapping("/get-upload-url-multiple")
    public Document getUploadUrlMultiple(@RequestBody QueryObject queryObject) {
        imageService.cleanUp();

        List<String> uploadUrls = imageService.generateNewUploadUrls(queryObject.urlCount);

        Document response = new SuccessResponse();
        response.append("urls", uploadUrls);
        return response;
    }

    @PostMapping("/delete-temp")
    public Document deleteTempFiles() {
        imageService.cleanUp();

        return new SuccessResponse();
    }
}
