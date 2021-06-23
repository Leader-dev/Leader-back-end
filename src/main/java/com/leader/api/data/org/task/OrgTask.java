package com.leader.api.data.org.task;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "org_task")
public class OrgTask {

    @Id
    public ObjectId id;
    public ObjectId publishUserId;
    public String description;
    public ArrayList<String> imageUrls;
    public Date publicationDate;
}
