package com.leader.api.data.service;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("image_record")
public class ImageRecord {

    public static String PENDING = "pending";
    public static String USING = "using";
    public static String INVALID = "invalid";

    @Id
    public ObjectId id;
    public ObjectId uploadUserId;
    public String imageUrl;
    public String status;
    public Date uploadUrlExpire;
}
