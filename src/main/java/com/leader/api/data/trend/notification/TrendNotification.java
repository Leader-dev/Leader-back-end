package com.leader.api.data.trend.notification;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "trend_notification")
public class TrendNotification {

    public static final String LIKE = "like";

    @Id
    public ObjectId id;
    public ObjectId toPuppetId;
    public Date sendDate;
    public String type;
    public ObjectId puppetId;
    public ObjectId trendItemId;
    public boolean read;
}
