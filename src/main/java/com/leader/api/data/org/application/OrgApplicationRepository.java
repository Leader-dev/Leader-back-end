package com.leader.api.data.org.application;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface OrgApplicationRepository extends MongoRepository<OrgApplication, ObjectId> {

    List<OrgApplication> findByOperateDateBeforeAndStatusEquals(Date date, String status);

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
            "   $unwind: {" +
            "       path: '$orgInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
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
            "   $unwind: {" +
            "       path: '$orgInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
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
            "   $unwind: {" +
            "       path: '$departmentInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
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
            "   $unwind: {" +
            "       path: '$applicantUserInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
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
            "   $unwind: {" +
            "       path: '$departmentInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
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
            "   $unwind: {" +
            "       path: '$operateMemberInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
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
            "   $unwind: {" +
            "       path: '$applicantUserInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
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
            "   $unwind: {" +
            "       path: '$operateMemberInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
            "}"
    })
    List<OrgApplicationReceivedOverview> lookupByQuery(Document query);

    boolean existsByApplicantUserIdAndId(ObjectId applicantUserId, ObjectId id);

    OrgApplication findByApplicantUserIdAndId(ObjectId applicantUserId, ObjectId id);

    boolean existsByOrgIdAndApplicantUserIdAndStatusIn(ObjectId orgId, ObjectId applicantUserId, List<String> statuses);

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
