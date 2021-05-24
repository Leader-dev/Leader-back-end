package com.leader.api.data.org;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class OrganizationPosterOverview {

    @Id
    public ObjectId id;
    public String posterUrl;
}
