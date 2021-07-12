package com.leader.api.data.trend.item;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "trend_item")
public class TrendItem {

    @Id
    public ObjectId id;
    public ObjectId userId;
    public String orgName;
    public String orgTitle;
    public boolean anonymous;
    public Date sendDate;
    public String content;
    public ArrayList<String> imageUrls;
    public Long likeCount;
}
