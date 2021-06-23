package com.leader.api.data.org.task;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "org_task_submission")
public class OrgTaskSubmission {

    public static String NOT_SUBMITTED = "not-submitted";
    public static String PENDING = "submitted";
    public static String PASSED = "passed";
    public static String WITHDRAWN = "withdrawn";

    @Id
    public ObjectId id;
    public ObjectId taskId;
    public ObjectId submitUserId;

    public String status;

    public String description;
    public ArrayList<String> imageUrls;
    public Date submissionDate;

    public String reviewNote;
    public Date reviewDate;
}
