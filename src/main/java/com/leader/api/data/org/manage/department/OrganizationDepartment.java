package com.leader.api.data.org.manage.department;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "org_department")
public class OrganizationDepartment {

    @Id
    public ObjectId id;
    public ObjectId organizationId;
    public ObjectId parentDepartmentId;
    public String name;
    public ObjectId applicationAuditUserId;
}
