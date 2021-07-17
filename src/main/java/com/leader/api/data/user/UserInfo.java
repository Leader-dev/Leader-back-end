package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class UserInfo {

    @Id
    public ObjectId id;
    public String uid;
    public String nickname;
    public String avatarUrl;
}
