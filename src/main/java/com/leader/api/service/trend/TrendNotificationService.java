package com.leader.api.service.trend;

import com.leader.api.data.trend.notification.TrendNotification;
import com.leader.api.data.trend.notification.TrendNotificationOverview;
import com.leader.api.data.trend.notification.TrendNotificationRepository;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.leader.api.data.trend.notification.TrendNotification.LIKE;

@Service
public class TrendNotificationService {

    private final TrendNotificationRepository notificationRepository;
    private final DateUtil dateUtil;

    @Autowired
    public TrendNotificationService(TrendNotificationRepository notificationRepository, DateUtil dateUtil) {
        this.notificationRepository = notificationRepository;
        this.dateUtil = dateUtil;
    }

    public long countUnreadNotifications(ObjectId toPuppetId) {
        return notificationRepository.countByToPuppetIdAndReadFalse(toPuppetId);
    }

    public List<TrendNotificationOverview> getReceivedNotifications(ObjectId toPuppetId, Pageable pageable) {
        return notificationRepository.lookupByToPuppetIdOrderBySendDateDesc(toPuppetId, pageable);
    }

    public void sendLikeNotification(ObjectId toPuppetId, ObjectId puppetId, ObjectId trendItemId) {
        if (!notificationRepository.existsByTypeAndPuppetIdAndTrendItemId(LIKE, puppetId, trendItemId)) {
            TrendNotification notification = new TrendNotification();
            notification.toPuppetId = toPuppetId;
            notification.sendDate = dateUtil.getCurrentDate();
            notification.type = LIKE;
            notification.toPuppetId = toPuppetId;
            notification.puppetId = puppetId;
            notification.trendItemId = trendItemId;
            notificationRepository.insert(notification);
        }
    }

    public void readNotificationsBefore(ObjectId toPuppetId, Date date) {
        List<TrendNotification> notifications = notificationRepository.findByToPuppetIdAndSendDateLessThanEqualAndReadFalse(toPuppetId, date);
        notifications.forEach(notification -> notification.read = true);
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(ObjectId toPuppetId, ObjectId notificationId) {
        notificationRepository.deleteByToPuppetIdAndId(toPuppetId, notificationId);
    }
}
