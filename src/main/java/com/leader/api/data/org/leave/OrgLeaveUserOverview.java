package com.leader.api.data.org.leave;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;

import java.util.Date;

public class OrgLeaveUserOverview {
    public ObjectId id;
    public String status; // Pending, approved or rejected


    // Submission related
    public OrgMemberInfoOverview applicationMemberInfoOverview;
    public Date submittedDate;

    public String leaveTitle;
    public String leaveType;

    public Date leaveStartDate;
    public Date leaveEndDate;


    // Admin Reply
    public OrgMemberInfoOverview reviewMemberInfoOverview;

}
