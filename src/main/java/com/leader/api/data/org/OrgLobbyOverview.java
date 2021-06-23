package com.leader.api.data.org;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

public class OrgLobbyOverview {

    @Id
    public ObjectId id;
    public String numberId;
    public String addressAuth;

    public String name;
    public String address;
    public List<String> typeAliases;
    public String posterUrl;
    public Long memberCount;
}
