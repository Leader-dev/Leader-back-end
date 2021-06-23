package com.leader.api.data.org.announcement;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "org_announce_confirm")
public class OrgAnnouncementConfirmation {

    public static String CONFIRMED = "confirmed";
    public static String NOT_CONFIRMED = "not-confirmed";

    @Id
    public ObjectId id;
    public ObjectId announceId;
    public ObjectId memberId;
    public String status;
}
