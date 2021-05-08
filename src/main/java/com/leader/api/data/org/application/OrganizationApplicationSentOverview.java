package com.leader.api.data.org.application;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationLobbyOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OrganizationApplicationSentOverview {

    @Id
    public ObjectId id;
    public ObjectId organizationId;
    public OrganizationLobbyOverview organizationInfo;
    public Date timestamp;
    public String status;
}
