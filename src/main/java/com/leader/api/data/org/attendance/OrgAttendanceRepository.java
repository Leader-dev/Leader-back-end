package com.leader.api.data.org.attendance;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgAttendanceRepository extends MongoRepository<OrgAttendance, ObjectId> {

    @Aggregation(pipeline = {
            "{" +
                "$match : {initializedMemberId : ?0}" +
            "}",
            "{" +
                "$lookup : {" +
                    "from: 'org_member'," +
                    "localField: 'initializedMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'initializedMemberInfo'" +
                "}" +
            "}",
            "{" +
                "$unwind: '$initializedMemberInfo'" +
            "}"
    })
    List<OrgAttendanceOverview> lookUpByInitializedMemberId (ObjectId initializedMemberId);
}
