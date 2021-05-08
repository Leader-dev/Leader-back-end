package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "user_authcode")
public class AuthCodeRecord {

    @Id
    public ObjectId id;
    public String phone;
    public String authcode;
    public Date timestamp;
}
