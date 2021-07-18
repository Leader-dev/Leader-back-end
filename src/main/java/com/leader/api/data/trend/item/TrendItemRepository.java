package com.leader.api.data.trend.item;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TrendItemRepository extends MongoRepository<TrendItem, ObjectId> {

    Optional<TrendItem> findByPuppetIdAndId(ObjectId puppetId, ObjectId id);

    @Aggregation(pipeline = {
            "{" +
            "   $match: ?0" +
            "}",
            "{" +
            "   $set: {" +
            "       puppetId: { $cond: ['$anonymous', null, '$puppetId'] }" +
            "       orgName: { $cond: ['$anonymous', null, '$orgName'] }" +
            "       orgTitle: { $cond: ['$anonymous', null, '$orgTitle'] }" +
            "   }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'puppet_list'," +
            "       localField: 'puppetId'," +
            "       foreignField: '_id'" +
            "       as: 'puppetInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: {" +
            "       path: '$puppetInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'trend_like'," +
            "       let: { trendItemId: '$_id', puppetId: ?1 }," +
            "       pipeline: [{ $match: { $expr: { $and: [" +
            "           { $eq: ['$trendItemId', '$$trendItemId'] }" +
            "           { $eq: ['$puppetId', '$$puppetId'] }" +
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
    List<TrendItemDetail> lookupByQueryOrderBySendDateDesc(Document query, ObjectId puppetId, Pageable pageable);

    default List<TrendItemDetail> lookupByOrderBySendDateDesc(ObjectId puppetId, Pageable pageable) {
        return lookupByQueryOrderBySendDateDesc(new Document(), puppetId, pageable);
    }

    default List<TrendItemDetail> lookupByPuppetIdOrderBySendDateDesc(ObjectId puppetId, Pageable pageable) {
        return lookupByQueryOrderBySendDateDesc(new Document("puppetId", puppetId), puppetId, pageable);
    }

    default TrendItemDetail lookupByIdOrderBySendDateDesc(ObjectId puppetId, ObjectId id) {
        return lookupByQueryOrderBySendDateDesc(new Document("id", id), puppetId, Pageable.unpaged()).get(0);
    }
}
