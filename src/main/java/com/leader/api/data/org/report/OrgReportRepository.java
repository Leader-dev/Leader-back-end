package com.leader.api.data.org.report;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgReportRepository extends MongoRepository<OrgReport, ObjectId> {

    @Aggregation(pipeline = {
            "{" +
            "   $match: ?0" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_list'," +
            "       localField: 'orgId'," +
            "       foreignField: '_id'," +
            "       as: 'orgInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$orgInfo'" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'user_list'," +
            "       localField: 'senderUserId'," +
            "       foreignField: '_id'," +
            "       as: 'senderUserInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$senderUserInfo'" +
            "}"
    })
    <T> List<T> lookupByQuery(Document query, Pageable pageable, Class<T> type);
}
