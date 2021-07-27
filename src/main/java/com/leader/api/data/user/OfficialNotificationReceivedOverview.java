package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OfficialNotificationReceivedOverview {

    @Id
    public ObjectId id;
    public Date sendDate;
    public String title;
    public String content;
    public String coverUrl;
    public boolean read;
}
