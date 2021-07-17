package com.leader.api.data.org.task;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class OrgTaskSubmissionAttempt {
    public String submissionDescription;
    public ArrayList<String> submissionImageUrls;
    public Date submissionDate;

    public String reviewNote;
    public Date reviewDate;
    public ObjectId reviewPersonId;
}
