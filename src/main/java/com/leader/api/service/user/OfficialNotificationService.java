package com.leader.api.service.user;

import com.leader.api.data.user.OfficialNotification;
import com.leader.api.data.user.OfficialNotificationRepository;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficialNotificationService {

    private final OfficialNotificationRepository notificationRepository;
    private final DateUtil dateUtil;

    @Autowired
    public OfficialNotificationService(OfficialNotificationRepository notificationRepository, DateUtil dateUtil) {
        this.notificationRepository = notificationRepository;
        this.dateUtil = dateUtil;
    }

    public List<OfficialNotification> getReceivedNotifications(ObjectId userId) {
        return notificationRepository.findByUserId(userId);
    }

    public void sendNotification(ObjectId userId, String title, String content, String coverUrl) {
        OfficialNotification notification = new OfficialNotification();
        notification.userId = userId;
        notification.sendDate = dateUtil.getCurrentDate();
        notification.title = title;
        notification.content = content;
        notification.coverUrl = coverUrl;
        notification.read = false;
        notificationRepository.insert(notification);
    }

    public void readNotification(ObjectId userId, ObjectId notificationId) {
        OfficialNotification notification = notificationRepository.findByUserIdAndId(userId, notificationId);
        notification.read = true;
        notificationRepository.save(notification);
    }
}
