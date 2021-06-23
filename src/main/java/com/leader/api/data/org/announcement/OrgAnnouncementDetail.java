package com.leader.api.data.org.announcement;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class OrgAnnouncementDetail {

    @Id
    public ObjectId id;
    public OrgMemberInfoOverview senderMemberInfo;
    public Date sendDate;
    public String title;
    public String content;
    public String coverUrl;
    public ArrayList<String> imageUrls;
    public String status;
}
