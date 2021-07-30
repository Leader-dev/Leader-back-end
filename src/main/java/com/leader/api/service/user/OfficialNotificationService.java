package com.leader.api.service.user;

import com.leader.api.data.user.*;
import com.leader.api.util.component.DateUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficialNotificationService {

    private final OfficialNotificationRepository notificationRepository;
    private final OfficialNotificationReadRepository readRepository;
    private final DateUtil dateUtil;

    @Autowired
    public OfficialNotificationService(OfficialNotificationRepository notificationRepository,
                                       OfficialNotificationReadRepository readRepository, DateUtil dateUtil) {
        this.notificationRepository = notificationRepository;
        this.readRepository = readRepository;
        this.dateUtil = dateUtil;
    }

    public List<OfficialNotificationReceivedOverview> getReceivedNotifications(ObjectId userId) {
        return notificationRepository.lookupReceived(userId, OfficialNotificationReceivedOverview.class);
    }

    public OfficialNotificationSentDetail getNotificationDetail(ObjectId notificationId) {
        return notificationRepository.lookupById(notificationId);
    }

    public OfficialNotification getNotificationDetail(ObjectId userId, ObjectId notificationId) {
        OfficialNotification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null || (!notification.toAll && notification.userId != userId)) {
            return null;
        }
        return notification;
    }

    public List<OfficialNotificationSentOverview> getNotifications(Boolean toAll) {
        Document query;
        if (toAll == null) {
            query = new Document();
        } else {
            query = new Document("toAll", toAll);
        }
        return notificationRepository.lookupSent(query, OfficialNotificationSentOverview.class);
    }

    public void sendNotification(boolean toAll, ObjectId userId, String title, String content, String coverUrl) {
        OfficialNotification notification = new OfficialNotification();
        notification.toAll = toAll;
        notification.userId = userId;
        notification.sendDate = dateUtil.getCurrentDate();
        notification.title = title;
        notification.content = content;
        notification.coverUrl = coverUrl;
        notificationRepository.insert(notification);
    }

    public void updateNotification(ObjectId notificationId, String title, String content) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.title = title;
            notification.content = content;
            notificationRepository.save(notification);
        });
    }

    public void updateNotificationCover(ObjectId notificationId, String coverUrl) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.coverUrl = coverUrl;
            notificationRepository.save(notification);
        });
    }

    public void deleteNotification(ObjectId notificationId) {
        synchronized (notificationRepository) {
            notificationRepository.deleteById(notificationId);
            readRepository.deleteByNotificationId(notificationId);
        }
    }

    public void readNotification(ObjectId userId, ObjectId notificationId) {
        synchronized (notificationRepository) {
            if (notificationRepository.existsById(notificationId) && !readRepository.existsByUserIdAndNotificationId(userId, notificationId)) {
                OfficialNotificationRead read = new OfficialNotificationRead();
                read.notificationId = notificationId;
                read.userId = userId;
                readRepository.insert(read);
            }
        }
    }
}
