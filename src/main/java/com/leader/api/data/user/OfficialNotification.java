package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "official_notification")
public class OfficialNotification {

    @Id
    public ObjectId id;
    public ObjectId userId;
    public Date sendDate;
    public String title;
    public String content;
    public String coverUrl;
    public boolean read;
}
