package com.leader.api.controller.service;

import com.leader.api.service.service.ImageService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

@RestController
@RequestMapping("/service/image")
public class ImageUploadController {

    private final ImageService imageService;

    @Autowired
    public ImageUploadController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/access-start-url")
    public Document getAccessStartUrl() {
        String startUrl = imageService.getAccessStartUrl();

        Document response = new SuccessResponse();
        response.append("start", startUrl);
        return response;
    }

    @PostMapping("/upload-single")
    public Document uploadSingle(MultipartHttpServletRequest request) throws IOException {
        imageService.deleteTemp();

        String originalFilename = request.getFileNames().next();
        MultipartFile file = request.getFile(originalFilename);
        imageService.uploadTempImage(file);

        return new SuccessResponse();
    }

    @PostMapping("/upload-multiple")
    public Document uploadMultiple(MultipartHttpServletRequest request) throws IOException {
        imageService.deleteTemp();

        ArrayList<MultipartFile> files = new ArrayList<>();
        for (Iterator<String> it = request.getFileNames(); it.hasNext();) {
            String filename = it.next();
            files.addAll(request.getFiles(filename));
        }
        imageService.uploadTempImages(files);

        return new SuccessResponse();
    }

    @PostMapping("/delete-temp")
    public Document deleteTempFiles() {
        imageService.deleteTemp();

        return new SuccessResponse();
    }
}
