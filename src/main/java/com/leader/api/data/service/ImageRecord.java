package com.leader.api.data.service;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("image_record")
public class ImageRecord {

    public static String TEMP = "temp";
    public static String USING = "using";

    @Id
    public ObjectId id;
    public ObjectId uploadUserId;
    public String imageUrl;
    public String status;
}
