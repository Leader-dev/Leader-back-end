package com.leader.api.data.trend.notification;

import com.leader.api.data.trend.item.TrendItemInfo;
import com.leader.api.data.trend.puppet.PuppetTrendOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class TrendNotificationOverview {

    @Id
    public ObjectId id;
    public Date sendDate;
    public String type;
    public ObjectId puppetId;
    public PuppetTrendOverview puppetInfo;
    public ObjectId trendItemId;
    public TrendItemInfo trendItemInfo;
    public boolean read;
}
