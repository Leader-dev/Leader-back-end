package com.leader.api.data.org.type;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "org_type")
public class OrganizationType {

    @Id
    public ObjectId id;
    public String name;
    public String alias;
}
