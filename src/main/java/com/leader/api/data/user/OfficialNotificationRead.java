package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "official_notification_read")
public class OfficialNotificationRead {

    @Id
    public ObjectId id;
    public ObjectId notificationId;
    public ObjectId userId;
}
