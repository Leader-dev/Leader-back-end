package com.leader.api.data.app;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "app_info")
public class AppInfo {

    @Id
    public ObjectId id;
    public org.bson.Document info;
}
