package com.leader.api.data.org.membership;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "org_membership")
public class  OrganizationMembership {

    @Id
    public ObjectId id;
    public ObjectId organizationId;
    public ObjectId userId;
}
