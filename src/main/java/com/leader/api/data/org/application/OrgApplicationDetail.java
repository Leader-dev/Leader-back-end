package com.leader.api.data.org.application;

import com.leader.api.data.org.application.notification.OrgApplicationNotification;
import com.leader.api.data.org.department.OrgDepartment;
import org.bson.types.ObjectId;

import java.util.List;

public class OrgApplicationDetail extends OrgApplicationSentOverview {

    public String name;
    public ObjectId applicantUserId;
    public OrgApplicationForm applicationForm;
    public ObjectId departmentId;
    public OrgDepartment departmentInfo;
    public List<OrgApplicationNotification> notifications;
}
