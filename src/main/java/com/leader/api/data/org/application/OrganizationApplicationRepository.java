package com.leader.api.data.org.application;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OrganizationApplicationRepository extends MongoRepository<OrganizationApplication, ObjectId> {

    @Aggregation(pipeline = {
            "{" +
            "   $match: { applicantUserId: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_list'," +
            "       localField: 'organizationId'," +
            "       foreignField: '_id'" +
            "       as: 'organizationInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$organizationInfo'" +
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
            "       localField: 'organizationId'," +
            "       foreignField: '_id'" +
            "       as: 'organizationInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$organizationInfo'" +
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

    OrganizationApplication findByApplicantUserIdAndId(ObjectId applicationUserId, ObjectId id);
}
