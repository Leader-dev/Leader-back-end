package com.leader.api.data.org.task;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class OrgTaskOverview {

    @Id
    public ObjectId id;
    public ObjectId publishUserId;
    public ArrayList<String> imageUrls;
    public Date publicationDate;
}
