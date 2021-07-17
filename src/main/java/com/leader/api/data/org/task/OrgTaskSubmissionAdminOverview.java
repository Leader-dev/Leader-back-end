package com.leader.api.data.org.task;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class OrgTaskSubmissionAdminOverview{

    // Displayed dat
    public String title;
    public String coverUrl;
    public OrgMemberInfoOverview receiverMemberInfo;

    // Stats data
    public Date publicationDate;
    public Date latestSubmissionDate;
    public int submissionCount;
    public ArrayList<OrgTaskSubmissionAttempt> submissionAttempts; // Temp variable


    public void calculateLatestSubmission (){
        // A temp fix, consider add size to database later?
        this.submissionCount = this.submissionAttempts.size();
        if (submissionCount > 0) {
            this.latestSubmissionDate = submissionAttempts.get(submissionCount-1).submissionDate;
        }
        this.submissionAttempts = null; // Clear temp variable
    }
}
