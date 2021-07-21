package com.leader.api.data.org.attendance;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "org_attendance_submission")
public class OrgAttendanceSubmission {
    public static String ON_TIME = "on-time";
    public static String LATE = "late";
    public static String ABSENT = "absent";
    public static String LEAVE = "leave";

    public static String[] ACCEPTED_STATES = {ON_TIME, LATE, ABSENT, LEAVE};

    @Id
    public ObjectId id;
    public String departmentName;
    public ObjectId departmentId;
    public ObjectId attendanceEventId;
    public ObjectId memberId;
    public String status;
}
