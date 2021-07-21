package com.leader.api.data.admin;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "admin_list")
public class Admin {

    @Id
    public ObjectId id;
    public String username;
    public String password;

    public String avatarUrl;
}
