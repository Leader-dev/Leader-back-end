package com.leader.api.data.org.application;

import com.leader.api.data.org.OrgLobbyOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OrgApplicationSentOverview {

    @Id
    public ObjectId id;
    public ObjectId organizationId;
    public OrgLobbyOverview organizationInfo;
    public Date timestamp;
    public String status;
}
