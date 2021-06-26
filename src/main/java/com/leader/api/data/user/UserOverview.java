package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class UserOverview {

    @Id
    public ObjectId id;
    public String uid;
    public String nickname;
}
