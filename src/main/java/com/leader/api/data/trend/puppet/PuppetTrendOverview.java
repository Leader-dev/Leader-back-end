package com.leader.api.data.trend.puppet;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class PuppetTrendOverview {

    @Id
    public ObjectId id;

    // info
    public String nickname;
    public String avatarUrl;
}
