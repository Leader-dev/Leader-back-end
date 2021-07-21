package com.leader.api.data.org.leave;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "org_leave")
public class OrgLeave {
    public static String PENDING = "pending";
    public static String APPROVED = "approved";
    public static String REJECTED = "rejected";

    public static String SICK_LEAVE = "sick_leave";
    public static String REASON_LEAVE = "reason_leave";

    @Id
    public ObjectId id;
    public String status; // Pending, approved or rejected

    // Submission related
    public ObjectId applicationMemberId;
    public Date submittedDate;

    public String leaveTitle;
    public String leaveType;
    public String leaveDetail;
    public Date leaveStartDate;
    public Date leaveEndDate;
    public ArrayList<String> leaveImageUrls;

    // Admin Reply
    public ObjectId reviewMemberId;
    public String reviewNote;
    public Date reviewDate;
}
