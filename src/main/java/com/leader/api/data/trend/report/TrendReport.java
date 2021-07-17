package com.leader.api.data.trend.report;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "trend_report")
public class TrendReport {

    @Id
    public ObjectId id;
    public ObjectId trendItemId;
    public ObjectId senderUserId;
    public String description;
    public ArrayList<String> imageUrls;
}
