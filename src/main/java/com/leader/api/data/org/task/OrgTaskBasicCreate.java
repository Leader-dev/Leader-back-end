package com.leader.api.data.org.task;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class OrgTaskBasicCreate {
    // Used to accept a new task
    public String title;
    public String description;

    // DDL
    public Date dueDate;

    // Urls
    public String coverUrl;
    public ArrayList<String> imageUrls;
}
