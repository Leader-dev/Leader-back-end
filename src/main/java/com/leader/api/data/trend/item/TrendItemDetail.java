package com.leader.api.data.trend.item;

import com.leader.api.data.user.UserInfo;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class TrendItemDetail {

    @Id
    public ObjectId id;
    public ObjectId userId;
    public UserInfo userInfo;
    public String orgName;
    public String orgTitle;
    public boolean anonymous;
    public Date sendDate;
    public String content;
    public ArrayList<String> imageUrls;
    public Long likeCount;
    public boolean liked;
}
