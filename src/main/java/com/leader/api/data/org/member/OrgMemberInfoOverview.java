package com.leader.api.data.org.member;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class OrgMemberInfoOverview {

    @Id
    public ObjectId id;
    public String name;
    public String numberId;
    public String title;
    public String avatarUrl;
}
