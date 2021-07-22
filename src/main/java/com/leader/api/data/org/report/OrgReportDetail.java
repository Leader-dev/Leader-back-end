package com.leader.api.data.org.report;

import com.leader.api.data.org.OrgDetail;
import com.leader.api.data.user.UserInfo;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class OrgReportDetail {

    @Id
    public ObjectId id;
    public ObjectId orgId;
    public OrgDetail orgInfo;
    public ObjectId senderUserId;
    public UserInfo senderUserInfo;
    public Date sendDate;
    public String description;
    public ArrayList<String> imageUrls;
}
