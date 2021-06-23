package com.leader.api.data.org.department;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("org_department")
public class OrgDepartment {

    @Id
    public ObjectId id;
    public ObjectId orgId;
    public ObjectId parentId;
    public String name;
}
