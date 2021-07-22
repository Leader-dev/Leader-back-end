package com.leader.api.data.org.report;

import com.leader.api.data.org.OrgLobbyOverview;
import com.leader.api.data.user.UserInfo;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OrgReportOverview {

    @Id
    public ObjectId id;
    public ObjectId orgId;
    public OrgLobbyOverview orgInfo;
    public ObjectId senderUserId;
    public UserInfo senderUserInfo;
    public Date sendDate;
}
