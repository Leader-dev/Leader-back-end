package com.leader.api.data.org.announcement;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OrgAnnouncementOverview {

    @Id
    public ObjectId id;
    public OrgMemberInfoOverview senderMemberInfo;
    public Date sendDate;
    public String title;
    public String coverUrl;
    public String status;
}
