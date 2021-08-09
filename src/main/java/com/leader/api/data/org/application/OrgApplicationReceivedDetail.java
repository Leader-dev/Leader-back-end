package com.leader.api.data.org.application;

import com.leader.api.data.org.application.notification.OrgApplicationNotificationOverview;
import com.leader.api.data.org.department.OrgDepartmentOverview;
import org.bson.types.ObjectId;

import java.util.List;

public class OrgApplicationReceivedDetail extends OrgApplicationReceivedOverview {

    public OrgApplicationForm applicationForm;
    public ObjectId departmentId;
    public OrgDepartmentOverview departmentInfo;
    public List<OrgApplicationNotificationOverview> notifications;
}
