package com.leader.api.data.org.membership;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrganizationMembershipRepository extends MongoRepository<OrganizationMembership, ObjectId> {

    List<OrganizationMembership> findAllByOrganizationId(ObjectId organizationId);

    long countByOrganizationId(ObjectId organizationId);

    List<OrganizationMembership> findAllByUserId(ObjectId userId);

    long countByUserId(ObjectId userId);

    OrganizationMembership findByOrganizationIdAndUserId(ObjectId organizationId, ObjectId userId);

    boolean existsByOrganizationIdAndUserId(ObjectId organizationId, ObjectId userId);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { userId: ?0 }" +
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
            "   $replaceRoot: { newRoot: '$organizationInfo' }" +
            "}"
    })
    List<OrganizationJoinedOverview> lookupJoinedOrganizationsByUserId(ObjectId userId);
}
