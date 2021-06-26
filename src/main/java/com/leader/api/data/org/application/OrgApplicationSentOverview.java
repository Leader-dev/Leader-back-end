package com.leader.api.data.org.application;

import com.leader.api.data.org.OrgLobbyOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OrgApplicationSentOverview {

    @Id
    public ObjectId id;
    public ObjectId orgId;
    public OrgLobbyOverview orgInfo;
    public Date sendData;
    public Integer unreadCount;
    public String status;
}
