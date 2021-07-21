package com.leader.api.data.org.attendance;

import com.leader.api.data.org.member.OrgMemberInfoOverview;

import java.util.Date;

public class OrgAttendanceSubmissionUserView {
    public String title;
    public Date attendanceDate;
    public String status;
    public int headCount;
    public OrgMemberInfoOverview initializedMemberInfo;
}
