package com.leader.api.data.org.task;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class OrgTaskOverview {

    @Id
    public ObjectId id; // TaskId
    public OrgMemberInfoOverview senderMemberInfo;
    public String title;
    public String coverUrl;
    public Date publicationDate;
    public int submittedCount;
    public int notSubmittedCount;
}
