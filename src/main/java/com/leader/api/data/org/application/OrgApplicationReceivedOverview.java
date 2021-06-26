package com.leader.api.data.org.application;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import com.leader.api.data.user.UserOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OrgApplicationReceivedOverview {

    @Id
    public ObjectId id;
    public String name;
    public ObjectId applicantUserId;
    public UserOverview applicantUserInfo;
    public OrgMemberInfoOverview operateMemberInfo;
    public Date sendDate;
    public String status;
    public ObjectId operateMemberId;
}
