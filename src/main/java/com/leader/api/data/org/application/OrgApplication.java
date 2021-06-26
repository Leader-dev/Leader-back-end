package com.leader.api.data.org.application;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "org_application")
public class OrgApplication {

    public static String PENDING = "pending";
    public static String PASSED = "passed";
    public static String REJECTED = "rejected";
    public static String ACCEPTED = "accepted";
    public static String DECLINED = "declined";

    @Id
    public ObjectId id;
    public ObjectId orgId;
    public String name;
    public ObjectId applicantUserId;
    public ObjectId departmentId;
    public OrgApplicationForm applicationForm;
    public Date sendDate;
    public String status;
    public ObjectId operateMemberId;
}
