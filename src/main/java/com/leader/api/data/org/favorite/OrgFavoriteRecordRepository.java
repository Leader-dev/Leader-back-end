package com.leader.api.data.org.favorite;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgFavoriteRecordRepository extends MongoRepository<OrgFavoriteRecord, ObjectId> {

    @Aggregation(pipeline = {
            "{" +
            "   $match: { userId: ?0 }" +
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
            "   $replaceRoot: { newRoot: '$orgInfo' }" +
            "}"
    })
    <T> List<T> lookupOrganizationsByUserId(ObjectId userId, Class<T> type);

    boolean existsByOrgIdAndUserId(ObjectId orgId, ObjectId userId);

    void deleteByOrgIdAndUserId(ObjectId orgId, ObjectId userId);
}
