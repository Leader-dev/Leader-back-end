package com.leader.api.data.org;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public class OrgDetail {

    @Id
    public ObjectId id;

    // auto controlled
    public String numberId;
    public Long memberCount;
    public String status;
    public String addressAuth;
    public String presidentName;

    // info
    public String name;
    public String address;
    public String introduction;
    public ArrayList<String> phone;
    public ArrayList<String> email;
    public ArrayList<String> typeAliases;
    public String posterUrl;

    // application
    public OrgApplicationScheme applicationScheme;
}
