package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "user_list")
public class User {

    @Id
    public ObjectId id;
    public String uid;

    // auth
    public String password;
    public String phone;
    public String salt;

    // info
    public String nickname;
    public String avatarUrl;
    public String introduction;
    public ArrayList<String> contacts;
}
