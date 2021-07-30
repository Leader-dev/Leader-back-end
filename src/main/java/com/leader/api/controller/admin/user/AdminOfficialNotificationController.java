package com.leader.api.controller.admin.user;

import com.leader.api.data.user.OfficialNotificationSentDetail;
import com.leader.api.data.user.UserInfo;
import com.leader.api.service.service.ImageService;
import com.leader.api.service.user.OfficialNotificationService;
import com.leader.api.service.user.UserInfoService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user/official-notification")
public class AdminOfficialNotificationController {

    private final UserInfoService infoService;
    private final OfficialNotificationService notificationService;
    private final ImageService imageService;

    @Autowired
    public AdminOfficialNotificationController(UserInfoService infoService, OfficialNotificationService notificationService,
                                               ImageService imageService) {
        this.infoService = infoService;
        this.notificationService = notificationService;
        this.imageService = imageService;
    }

    public static class QueryObject {
        public String uid;
        public Boolean toAll;
        public ObjectId userId;
        public String title;
        public String content;
        public String coverUrl;
        public ObjectId notificationId;
    }

    @PostMapping("/user-info")
    public Document getUserInfo(@RequestBody QueryObject queryObject) {
        Document response = new SuccessResponse();
        response.append("info", infoService.getUserInfo(queryObject.uid));
        return response;
    }

    @PostMapping("/list")
    public Document listSentNotifications(@RequestBody QueryObject queryObject) {
        Document response = new SuccessResponse();
        response.append("list", notificationService.getNotifications(queryObject.toAll));
        return response;
    }

    @PostMapping("/detail")
    public Document getNotificationDetail(@RequestBody QueryObject queryObject) {
        Document response = new SuccessResponse();
        response.append("detail", notificationService.getNotificationDetail(queryObject.notificationId));
        return response;
    }

    @PostMapping("/send")
    public Document sendNotification(@RequestBody QueryObject queryObject) {
        imageService.assertUploadedTempImage(queryObject.coverUrl);

        UserInfo info = infoService.getUserInfo(queryObject.uid);
        ObjectId userId = info == null ? null : info.id;
        notificationService.sendNotification(queryObject.toAll, userId, queryObject.title, queryObject.content, queryObject.coverUrl);

        imageService.confirmUploadImage(queryObject.coverUrl);

        return new SuccessResponse();
    }

    @PostMapping("/update")
    public Document updateNotification(@RequestBody QueryObject queryObject) {
        imageService.assertUploadedTempImage(queryObject.coverUrl);

        notificationService.updateNotification(queryObject.notificationId, queryObject.title, queryObject.content);
        if (queryObject.coverUrl != null) {
            notificationService.updateNotificationCover(queryObject.notificationId, queryObject.coverUrl);
        }

        imageService.confirmUploadImage(queryObject.coverUrl);
        return new SuccessResponse();
    }

    @PostMapping("/delete")
    public Document deleteNotification(@RequestBody QueryObject queryObject) {
        OfficialNotificationSentDetail detail = notificationService.getNotificationDetail(queryObject.notificationId);
        notificationService.deleteNotification(queryObject.notificationId);
        imageService.deleteImage(detail.coverUrl);
        return new SuccessResponse();
    }
}
