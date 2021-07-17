package com.leader.api.data.org.task;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgTaskSubmissionRepository extends MongoRepository<OrgTaskSubmission, ObjectId> {


    @Aggregation(pipeline = {
            "{" +
            "   $match: {memberId: ?0}" +
            "}",

            "{" +
            "   $lookup: {" +
            "       from: 'org_task'," +
            "       localField: 'taskId'," +
            "       foreignField: '_id'" +
            "       as: 'taskInfo'" +
            "   }" +
            "}",

            "{" +
            "   $unwind: '$taskInfo'" +
            "}",

            "{" +
            "   $replaceRoot: {" +
                    "newRoot: {" +
                    "   $mergeObjects: [ " +
                    "       {status: '$status'}, " +
                    "       '$taskInfo' ] " +
                    "} " +
                "}" +
            "}",

            "{" +
            "   $lookup: {" +
            "       from: 'org_member'," +
            "       localField: 'publishUserId'," +
            "       foreignField: '_id'" +
            "       as: 'senderMemberInfo'" +
            "   }" +
            "}",

            "{" +
            "   $unwind: '$senderMemberInfo'" +
            "}"
    })
    List<OrgTaskSubmissionUserOverview> lookupSubmissionsByUserId(ObjectId userId);

    @Aggregation(pipeline = {
            "{" +
                    "   $match: {memberId: ?0, taskId: ?1}" +
                    "}",

            "{" +
                    "   $lookup: {" +
                    "       from: 'org_task'," +
                    "       localField: 'taskId'," +
                    "       foreignField: '_id'" +
                    "       as: 'taskInfo'" +
                    "   }" +
                    "}",

            "{" +
                    "   $unwind: '$taskInfo'" +
                    "}",

            "{" +
                    "   $replaceRoot: {" +
                    "newRoot: {" +
                    "   $mergeObjects: [ " +
                    "       {status: '$status', submissionAttempts: '$submissionAttempts', submissionId: '$_id'}, " +
                    "       '$taskInfo' ] " +
                    "} " +
                    "}" +
                    "}",

            "{" +
                    "   $lookup: {" +
                    "       from: 'org_member'," +
                    "       localField: 'publishUserId'," +
                    "       foreignField: '_id'" +
                    "       as: 'senderMemberInfo'" +
                    "   }" +
                    "}",

            "{" +
                    "   $unwind: '$senderMemberInfo'" +
            "}"
    })
    OrgTaskDetail lookupDetailByMemberIdAndTaskId(ObjectId memberId, ObjectId taskId);

    OrgTaskSubmission findByMemberIdAndTaskId(ObjectId memberId, ObjectId TaskId); // Reply to a specific task

    List<OrgTaskSubmission> findAllByTaskId (ObjectId taskId); // When changing the whole task status, i.e. Calcelling it

    int countByTaskIdAndStatus (ObjectId taskId, String status);

    @Aggregation(pipeline= {
            "{" +
                "$match: {taskId: ?0, status: ?1}" +
            "}",

            // Receiving Member
            "{" +
                "$lookup: { " +
                    "from: 'org_member'," +
                    "localField: 'memberId'," +
                    "foreignField: '_id'," +
                    "as: 'receiverMemberInfo'" +
                "}" +
            "}",

            "{" +
                "$unwind : '$receiverMemberInfo'" +
            "}",

            // Task detail
            "{" +
                "$lookup:{" +
                    "from: 'org_task'," +
                    "localField: 'taskId'," +
                    "foreignField: '_id'," +
                    "as: taskInfo" +
                "}" +
            "}",
            "{" +
                "$unwind: '$taskInfo'" +
            "}",

            "{" +
                "$replaceRoot: {" +
                    "newRoot : {" +
                        "$mergeObjects: [ " +
                "       { status: '$status', submissionAttempts: '$submissionAttempts', receiverMemberInfo: '$receiverMemberInfo'}, " +
                "       '$taskInfo' ] " +
                "   }" +
                "}" +
            "}",

    })
    List<OrgTaskSubmissionAdminOverview> lookupByTaskIdAndStatus (ObjectId taskId, String status);

}
