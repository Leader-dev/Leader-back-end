package com.leader.api.data.org.application;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "org_application")
public class OrganizationApplication {

    @Id
    public ObjectId id;
    public ObjectId organizationId;
    public ObjectId applicantUserId;
    public ObjectId departmentId;
    public ObjectId auditUserId;
    public OrganizationApplicationForm applicationForm;
    public Date timestamp;
    public String status;
}
