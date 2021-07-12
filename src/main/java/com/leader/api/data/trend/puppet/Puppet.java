package com.leader.api.data.trend.puppet;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "puppet_list")
public class Puppet {

    @Id
    public ObjectId id;
    public ObjectId userId;

    // info
    public String nickname;
    public String avatarUrl;
    public String introduction;
    public ArrayList<String> contacts;
}
