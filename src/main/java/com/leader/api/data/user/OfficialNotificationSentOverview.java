package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class OfficialNotificationSentOverview {

    @Id
    public ObjectId id;
    public boolean toAll;
    public ObjectId userId;
    public UserInfo userInfo;
    public Date sendDate;
    public String title;
    public String coverUrl;
    public ArrayList<OfficialNotificationRead> reads;
}
