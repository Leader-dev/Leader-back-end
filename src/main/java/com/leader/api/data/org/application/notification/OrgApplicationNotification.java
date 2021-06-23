package com.leader.api.data.org.application.notification;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "org_application_notification")
public class OrgApplicationNotification {

    @Id
    public ObjectId id;
    public ObjectId applicationId;
    public String title;
    public String content;
    public ArrayList<String> imageUrls;
    public boolean unread;
    public Date timestamp;
}
