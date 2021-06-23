package com.leader.api.data.org.application;

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
    <T> List<T> findAllByApplicantUserIdIncludeInfo(ObjectId applicationUserId, Class<T> type);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { applicantUserId: ?0, _id: ?1 }" +
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
            "       from: 'org_application_notification'," +
            "       localField: '_id'," +
            "       foreignField: 'applicationId'" +
            "       as: 'notifications'" +
            "   }" +
            "}",
    })
    <T> T findByApplicantUserIdAndIdIncludeInfo(ObjectId applicationUserId, ObjectId id, Class<T> type);

    boolean existsByApplicantUserIdAndId(ObjectId applicationUserId, ObjectId id);

    OrgApplication findByApplicantUserIdAndId(ObjectId applicationUserId, ObjectId id);

    List<OrgApplication> findByOrgId(ObjectId orgId);
}
