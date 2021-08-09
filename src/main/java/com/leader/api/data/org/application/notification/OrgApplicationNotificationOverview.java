package com.leader.api.data.org.application.notification;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OrgApplicationNotificationOverview {

    @Id
    public ObjectId id;
    public String title;
    public boolean unread;
    public Date sendDate;
}
