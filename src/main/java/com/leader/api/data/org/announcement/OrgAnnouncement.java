package com.leader.api.data.org.announcement;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "org_announce")
public class OrgAnnouncement {

    @Id
    public ObjectId id;
    public ObjectId senderMemberId;
    public Date sendDate;
    public long notConfirmedCount;
    public long confirmedCount;
    public String title;
    public String content;
    public String coverUrl;
    public ArrayList<String> imageUrls;
}
