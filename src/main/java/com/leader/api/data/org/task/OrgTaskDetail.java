package com.leader.api.data.org.task;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class OrgTaskDetail {
    // Task Information
    public String status;
    public ObjectId id; // Task Id
    public ObjectId submissionId; // Submission Id

    public Date publicationDate;
    public String title;
    public String description;

    public OrgMemberInfoOverview senderMemberInfo;
    // public OrgMemberInfoOverview receiverMemberInfo; // Not included as portrait is required, use another interface
    // Submission Information
    public ArrayList<OrgTaskSubmissionAttempt> submissionAttempts;

}
