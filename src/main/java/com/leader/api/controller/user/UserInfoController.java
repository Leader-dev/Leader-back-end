package com.leader.api.controller.user;

import com.leader.api.data.user.UserInfo;
import com.leader.api.service.service.ImageService;
import com.leader.api.service.user.UserInfoService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/info")
public class UserInfoController {

    private final UserInfoService userInfoService;
    private final UserIdService userIdService;
    private final ImageService imageService;

    @Autowired
    public UserInfoController(UserInfoService userInfoService, UserIdService userIdService, ImageService imageService) {
        this.userInfoService = userInfoService;
        this.userIdService = userIdService;
        this.imageService = imageService;
    }

    public static class QueryObject {
        public String nickname;
    }

    @PostMapping("/get")
    public Document getUserInfo() {
        ObjectId userId = userIdService.getCurrentUserId();
        UserInfo info = userInfoService.getUserInfo(userId);

        Document response = new SuccessResponse();
        response.append("info", info);
        return response;
    }

    @PostMapping("/update-nickname")
    public Document updateNickname(@RequestBody QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        userInfoService.updateNickname(userId, queryObject.nickname);

        return new SuccessResponse();
    }

    @PostMapping("/update-portrait")
    public Document updatePortrait() {
        String portraitUrl = imageService.getUploadedTempImage();

        ObjectId userId = userIdService.getCurrentUserId();
        String prevPortraitUrl = userInfoService.getPortrait(userId);
        userInfoService.updatePortrait(userId, portraitUrl);

        imageService.confirmUploadImage(portraitUrl);
        imageService.deleteImage(prevPortraitUrl);

        return new SuccessResponse();
    }
}
