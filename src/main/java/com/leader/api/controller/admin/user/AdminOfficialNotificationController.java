package com.leader.api.controller.admin.user;

import com.leader.api.data.user.UserInfo;
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

    @Autowired
    public AdminOfficialNotificationController(UserInfoService infoService, OfficialNotificationService notificationService) {
        this.infoService = infoService;
        this.notificationService = notificationService;
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
        UserInfo info = infoService.getUserInfo(queryObject.uid);
        ObjectId userId = info == null ? null : info.id;
        notificationService.sendNotification(queryObject.toAll, userId, queryObject.title, queryObject.content, queryObject.coverUrl);
        return new SuccessResponse();
    }

    @PostMapping("/update")
    public Document updateNotification(@RequestBody QueryObject queryObject) {
        notificationService.updateNotification(queryObject.notificationId, queryObject.title, queryObject.content, queryObject.coverUrl);
        return new SuccessResponse();
    }

    @PostMapping("/delete")
    public Document deleteNotification(@RequestBody QueryObject queryObject) {
        notificationService.deleteNotification(queryObject.notificationId);
        return new SuccessResponse();
    }
}
