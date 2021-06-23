package com.leader.api.data.org.member;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class OrgJoinedOverview {

    @Id
    public ObjectId id;
    public String numberId;
    public String name;
    public String address;
    public String addressAuth;
    public String typeAlias;
    public String posterUrl;
    public String status;
    public Long memberCount;
}
