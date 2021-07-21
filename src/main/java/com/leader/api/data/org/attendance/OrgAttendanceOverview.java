package com.leader.api.data.org.attendance;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class OrgAttendanceOverview {

    @Id
    public ObjectId id;
    public OrgMemberInfoOverview initializedMemberInfo;
    public int headCount;
    public Date attendanceDate;
}

