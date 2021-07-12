package com.leader.api.data.trend.item;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TrendItemRepository extends MongoRepository<TrendItem, ObjectId> {

    Optional<TrendItem> findByUserIdAndId(ObjectId userId, ObjectId id);

    @Aggregation(pipeline = {
            "{" +
            "   $match: ?0" +
            "}",
            "{" +
            "   $set: {" +
            "       userId: { $cond: ['$anonymous', null, '$userId'] }" +
            "       orgName: { $cond: ['$anonymous', null, '$orgName'] }" +
            "       orgTitle: { $cond: ['$anonymous', null, '$orgTitle'] }" +
            "   }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'user_list'," +
            "       localField: 'userId'," +
            "       foreignField: '_id'" +
            "       as: 'userInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: {" +
            "       path: '$userInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'trend_like'," +
            "       let: { trendItemId: '$_id', userId: ?1 }," +
            "       pipeline: [{ $match: { $expr: { $and: [" +
            "           { $eq: ['$trendItemId', '$$trendItemId'] }" +
            "           { $eq: ['$userId', '$$userId'] }" +
                    "]}}}]," +
            "       as: 'likeInfo'" +
            "   }" +
            "}",
            "{" +
            "   $set: {" +
            "       liked: { $ne: [0, { $size: '$likeInfo' }] }" +
            "   }" +
            "}",
            "{ $sort: { sendDate : -1 } }"
    })
    List<TrendItemDetail> lookupByQueryOrderBySendDateDesc(Document query, ObjectId userId, Pageable pageable);

    default List<TrendItemDetail> lookupByOrderBySendDateDesc(ObjectId userId, Pageable pageable) {
        return lookupByQueryOrderBySendDateDesc(new Document(), userId, pageable);
    }

    default List<TrendItemDetail> lookupByUserIdOrderBySendDateDesc(ObjectId userId, Pageable pageable) {
        return lookupByQueryOrderBySendDateDesc(new Document("userId", userId), userId, pageable);
    }
}
