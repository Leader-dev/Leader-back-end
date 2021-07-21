package com.leader.api.data.org.attendance;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "org_attendance")
public class OrgAttendance {
    @Id
    public ObjectId id;
    public String title;
    public ObjectId initializedMemberId;
    public Date attendanceDate;
    public Date createdDate;
    public int headCount;

    public List<ObjectId> toMemberIds;
}
