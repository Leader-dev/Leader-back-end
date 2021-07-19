package com.leader.api.controller.user;

import com.leader.api.data.user.OfficialNotification;
import com.leader.api.service.user.OfficialNotificationService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/official-notification")
public class OfficialNotificationController {

    private final OfficialNotificationService notificationService;
    private final UserIdService userIdService;

    @Autowired
    public OfficialNotificationController(OfficialNotificationService notificationService, UserIdService userIdService) {
        this.notificationService = notificationService;
        this.userIdService = userIdService;
    }

    public static class QueryObject {
        public ObjectId notificationId;
    }

    @PostMapping("/list-received")
    public Document listReceivedNotifications() {
        ObjectId userId = userIdService.getCurrentUserId();
        List<OfficialNotification> notifications = notificationService.getReceivedNotifications(userId);

        Document response = new SuccessResponse();
        response.append("notifications", notifications);
        return response;
    }

    @PostMapping("/read")
    public Document readNotification(@RequestBody QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        notificationService.readNotification(userId, queryObject.notificationId);

        return new SuccessResponse();
    }
}
