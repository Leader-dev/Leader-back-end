package com.leader.api.service.org.application;

import com.leader.api.data.org.application.OrgApplicationForm;
import org.bson.types.ObjectId;

public class OrgApplicationInfo {
    ObjectId orgId;
    ObjectId departmentId;
    OrgApplicationForm applicationForm;
    ObjectId userid;
}
