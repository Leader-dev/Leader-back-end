package com.leader.api.data.trend.like;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "trend_like")
public class TrendLike {

    @Id
    public ObjectId id;
    public ObjectId trendItemId;
    public ObjectId puppetId;
    public Date timestamp;
}
