package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_list")
public class User {

    @Id
    public ObjectId id;
    public String uid;

    // auth
    public String password;
    public String phone;

    // info
    public String nickname;
    public String avatarUrl;
}
