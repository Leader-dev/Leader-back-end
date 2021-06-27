package com.leader.api.data.org.application;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgApplicationRepository extends MongoRepository<OrgApplication, ObjectId> {

    @Aggregation(pipeline = {
            "{" +
            "   $match: { applicantUserId: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_list'," +
            "       localField: 'orgId'," +
            "       foreignField: '_id'" +
            "       as: 'orgInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$orgInfo'" +
            "}"
    })
    <T> List<T> lookupByApplicantUserIdIncludeOrgInfo(ObjectId applicationUserId, Class<T> type);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { _id: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_list'," +
            "       localField: 'orgId'," +
            "       foreignField: '_id'" +
            "       as: 'orgInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$orgInfo'" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_department'," +
            "       localField: 'departmentId'," +
            "       foreignField: '_id'" +
            "       as: 'departmentInfo'" +
            "   }" +
            "}",
            "{" +
            "   $set: { departmentInfo: { $first: '$departmentInfo' } }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_application_notification'," +
            "       localField: '_id'," +
            "       foreignField: 'applicationId'" +
            "       as: 'notifications'" +
            "   }" +
            "}"
    })
    OrgApplicationSentDetail lookupByIdIncludeOrgInfo(ObjectId id);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { _id: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'user_list'," +
            "       localField: 'applicantUserId'," +
            "       foreignField: '_id'" +
            "       as: 'applicantUserInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$applicantUserInfo'" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_department'," +
            "       localField: 'departmentId'," +
            "       foreignField: '_id'" +
            "       as: 'departmentInfo'" +
            "   }" +
            "}",
            "{" +
            "   $set: { departmentInfo: { $first: '$departmentInfo' } }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_member'," +
            "       localField: 'operateMemberId'," +
            "       foreignField: '_id'" +
            "       as: 'operateMemberInfo'" +
            "   }" +
            "}",
            "{" +
            "   $set: { operateMemberInfo: { $first: '$operateMemberInfo' } }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_application_notification'," +
            "       localField: '_id'," +
            "       foreignField: 'applicationId'" +
            "       as: 'notifications'" +
            "   }" +
            "}"
    })
    OrgApplicationReceivedDetail lookupByIdIncludeUserInfo(ObjectId id);

    @Aggregation(pipeline = {
            "{" +
            "   $match: ?0" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'user_list'," +
            "       localField: 'applicantUserId'," +
            "       foreignField: '_id'" +
            "       as: 'applicantUserInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$applicantUserInfo'" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_member'," +
            "       localField: 'operateMemberId'," +
            "       foreignField: '_id'" +
            "       as: 'operateMemberInfo'" +
            "   }" +
            "}",
            "{" +
            "   $set: { operateMemberInfo: { $first: '$operateMemberInfo' } }" +
            "}"
    })
    List<OrgApplicationReceivedOverview> lookupByQuery(Document query);

    boolean existsByApplicantUserIdAndId(ObjectId applicationUserId, ObjectId id);

    OrgApplication findByApplicantUserIdAndId(ObjectId applicationUserId, ObjectId id);

    List<OrgApplication> findByOrgId(ObjectId orgId);

    long countByOrgIdAndStatus(ObjectId orgId, String status);

    default List<OrgApplicationReceivedOverview> lookupByOrgIdAndStatus(ObjectId orgId, String... status) {
        Document query = new Document();
        query.append("orgId", orgId);
        query.append("status", new Document("$in", status));
        return lookupByQuery(query);
    }

    default List<OrgApplicationReceivedOverview> lookupByDepartmentIdInAndStatus(List<ObjectId> departmentIds, String... status) {
        Document query = new Document();
        query.append("departmentId", new Document("$in", departmentIds));
        query.append("status", new Document("$in", status));
        return lookupByQuery(query);
    }
}
