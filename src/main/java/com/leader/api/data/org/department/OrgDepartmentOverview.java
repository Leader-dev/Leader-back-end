package com.leader.api.data.org.department;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class OrgDepartmentOverview {

    @Id
    public ObjectId id;
    public String name;
}
