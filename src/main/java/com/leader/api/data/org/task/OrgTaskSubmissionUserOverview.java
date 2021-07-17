package com.leader.api.data.org.task;

import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class OrgTaskSubmissionUserOverview {
    // Class to return a overview of taskSubmissions
    // Related to task OrgTaskSubmission, theoretically a subclass but split because _id refers to taskId instead of the announcementId

    @Id
    public ObjectId id;

    // Task Submission details
    public String status;

    // Task details
    public OrgMemberInfoOverview senderMemberInfo; // id, name, numberId, title only
    public String title;
    public String coverUrl;
    public Date publicationDate;
}
