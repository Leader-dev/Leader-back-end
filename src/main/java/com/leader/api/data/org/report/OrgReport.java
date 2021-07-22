package com.leader.api.data.org.report;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "org_report")
public class OrgReport {

    @Id
    public ObjectId id;
    public ObjectId orgId;
    public ObjectId senderUserId;
    public Date sendDate;
    public String description;
    public ArrayList<String> imageUrls;
}
