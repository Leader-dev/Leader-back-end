package com.leader.api.data.token;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Document(collection = "tokens")
public class TokenRecord {

    @Id
    public UUID id;
    public Date created;
    public Date accessed;
    public org.bson.Document data;
}
