package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class UserAdminOverview {

    @Id
    public ObjectId id;
    public String uid;
    public String phone;
    public String nickname;
    public String avatarUrl;
}
