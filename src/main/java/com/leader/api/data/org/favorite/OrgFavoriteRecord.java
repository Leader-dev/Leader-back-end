package com.leader.api.data.org.favorite;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("org_favorite")
public class OrgFavoriteRecord {

    @Id
    public ObjectId id;
    public ObjectId orgId;
    public ObjectId userId;
}
