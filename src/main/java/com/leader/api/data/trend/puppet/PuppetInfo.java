package com.leader.api.data.trend.puppet;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

public class PuppetInfo {

    @Id
    public ObjectId id;

    // info
    public String nickname;
    public String avatarUrl;
    public String introduction;
    public ArrayList<String> contacts;
}
