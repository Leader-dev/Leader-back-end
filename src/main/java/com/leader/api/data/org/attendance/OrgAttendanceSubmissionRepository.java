package com.leader.api.data.org.attendance;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgAttendanceSubmissionRepository extends MongoRepository<OrgAttendanceSubmission, ObjectId> {

    @Aggregation(pipeline = {
            "{" +
                "$match : {attendanceEventId : ?0}" +
            "}",
            "{" +
                "$lookup : {" +
                    "from: 'org_member'," +
                    "localField: 'memberId'," +
                    "foreignField: '_id'," +
                    "as: memberInfo" +
                "}" +
            "}",
            "{" +
                "$unwind: $memberInfo" +
            "}"
    })
    List<OrgAttendanceSubmissionDetail> lookUpByAttendanceId (ObjectId attendanceId);

    @Aggregation(pipeline = {
            "{" +
                "$match: { memberId: ?0 }" +
            "}",
            "{" +
                "$lookup : {" +
                    "from : 'org_attendance'," +
                    "localField : 'attendanceEventId'," +
                    "foreignField : '_id'" +
                    "as : attendanceEvent" +
                "}" +
            "}",
            "{" +
                "$unwind : '$attendanceEvent'" +
            "}",
            "{" +
                "$replaceRoot : {" +
                    "newRoot: {" +
                        "$mergeObjects : [" +
                            "{status : '$status' }," +
                            "'$attendanceEvent'" +
                        "]" +
                    "}" +
                "}" +
            "}",
            "{" +
                "$lookup : {" +
                    "from: 'org_member'," +
                    "localField: 'initializedMemberId'," +
                    "foreignField: '_id'," +
                    "as: initializedMemberInfo" +
                "}" +
            "}",
            "{" +
                "$unwind: $initializedMemberInfo" +
            "}"
    })
    List<OrgAttendanceSubmissionUserView> lookupMyRecordsByMemberId (ObjectId memberId);

    OrgAttendanceSubmission findByAttendanceEventIdAndMemberId(ObjectId attendanceEventId, ObjectId submissionId);
}
