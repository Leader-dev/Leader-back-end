package com.leader.api.data.org.leave;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface OrgLeaveRepository extends MongoRepository<OrgLeave, ObjectId> {
    OrgLeave findByIdAndApplicationMemberId (ObjectId id, ObjectId applicationMemberId);

    boolean existsByApplicationMemberIdAndStatusAndLeaveStartDateBeforeAndLeaveEndDateAfter (
            ObjectId applicationMemberId, String Status, Date leaveStartDate, Date leaveEndDate);

    @Aggregation(pipeline = {
            "{ $match : {applicationMemberId : ?0, status : ?1} }",
            "{" +
                "$lookup: {" +
                    "from: 'org_member'," +
                    "localField: 'applicationMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'applicationMemberInfoOverview'" +
                "}" +
            "}",
            "{ $unwind: '$applicationMemberInfoOverview'}",
            "{" +
                    "$lookup: {" +
                    "from: 'org_member'," +
                    "localField: 'reviewMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'reviewMemberInfoOverview'" +
                    "}" +
                "}",
            "{" +
                "$unwind: {" +
                    "path: '$reviewMemberInfoOverview'," +
                    "preserveNullAndEmptyArrays: true" +
                "}" +
            "}",
    })
    List<OrgLeaveUserOverview> lookUpByApplicationMemberIdAndStatus(ObjectId applicationMemberId, String status);

    @Aggregation(pipeline = {
            "{ $match : {applicationMemberId : ?0} }",
            "{" +
                "$lookup: {" +
                    "from: 'org_member'," +
                    "localField: 'applicationMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'applicationMemberInfoOverview'" +
                "}" +
            "}",
            "{ $unwind: '$applicationMemberInfoOverview'}",
            "{" +
                "$lookup: {" +
                    "from: 'org_member'," +
                    "localField: 'reviewMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'reviewMemberInfoOverview'" +
                    "}" +
                "}",
            "{" +
                    "$unwind: {" +
                    "path: '$reviewMemberInfoOverview'," +
                    "preserveNullAndEmptyArrays: true" +
                "}" +
            "}",
    })
    List<OrgLeaveUserOverview> lookUpByApplicationMemberId(ObjectId applicationMemberId);


    @Aggregation(pipeline = {
            "{ $match : {_id : ?0, applicationMemberId : ?1} }",
            "{" +
                "$lookup: {" +
                    "from: 'org_member'," +
                    "localField: 'applicationMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'applicationMemberInfoOverview'" +
                "}" +
            "}",
            "{ $unwind: '$applicationMemberInfoOverview'}",
            "{" +
                "$lookup: {" +
                    "from: 'org_member'," +
                    "localField: 'reviewMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'reviewMemberInfoOverview'" +
                "}" +
            "}",
            "{" +
                "$unwind: {" +
                    "path: '$reviewMemberInfoOverview'," +
                    "preserveNullAndEmptyArrays: true" +
                "}" +
            "}",
    })
    OrgLeaveDetail lookUpByIdAndApplicationMemberId (ObjectId id, ObjectId applicationMemberId);



    @Aggregation(pipeline = {
            "{ $match : {applicationMemberId : ?0, status : ?1} }",
            "{" +
                "$lookup: {" +
                    "from: 'org_member'," +
                    "localField: 'applicationMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'applicationMemberInfoOverview'" +
                "}" +
            "}",
            "{ $unwind: '$applicationMemberInfoOverview'}",
            "{" +
                "$lookup: {" +
                    "from: 'org_member'," +
                    "localField: 'reviewMemberId'," +
                    "foreignField: '_id'," +
                    "as: 'reviewMemberInfoOverview'" +
                    "}" +
                "}",
            "{" +
                "$unwind: {" +
                    "path: '$reviewMemberInfoOverview'," +
                    "preserveNullAndEmptyArrays: true" +
                "}" +
            "}",
    })
    OrgLeaveUserOverview LookUpByApplicationMemberIdAndStatus (ObjectId applicationMemberId, String status);
}
