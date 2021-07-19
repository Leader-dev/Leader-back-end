package com.leader.api.data.org;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "org_list")
public class Organization {

    // states
    public static final String PENDING = "pending";
    public static final String RUNNING = "running";

    @Id
    public ObjectId id;

    // auto controlled
    public String numberId;
    public long memberCount;
    public String status;
    public String instituteAuth;
    public String presidentName;

    // info
    public String name;
    public String address;
    public String instituteName;
    public String introduction;
    public ArrayList<String> phone;
    public ArrayList<String> email;
    public ArrayList<String> typeAliases;
    public String posterUrl;

    // application
    public OrgApplicationScheme applicationScheme;
    public int receivedApplicationCount;
}
