package com.leader.api.data.org.report;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "org_repost")
public class OrgReport {

    @Id
    public ObjectId id;
    public ObjectId organizationId;
    public ObjectId senderUserId;
    public String description;
    public ArrayList<String> imageUrls;
}
