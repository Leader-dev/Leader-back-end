package com.leader.api.data.org.timeline;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collation = "org_timeline_item")
public class OrgTimelineItem {

    @Id
    public ObjectId id;
    public ObjectId orgId;
    public Date timestamp;
    public String description;

}
