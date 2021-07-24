package com.leader.api.data.trend.item;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TrendItemRepository extends MongoRepository<TrendItem, ObjectId> {

    Optional<TrendItem> findByPuppetIdAndId(ObjectId puppetId, ObjectId id);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { puppetId: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'trend_like'," +
            "       localField: '_id'," +
            "       foreignField: 'trendItemId'" +
            "       as: 'likes'" +
            "   }" +
            "}",
            "{ $unwind: '$likes' }",
            "{ $count: 'likes' }"
    })
    Optional<Long> countLikesByPuppetId(ObjectId puppet);

    @Aggregation(pipeline = {
            "{" +
            "   $match: ?0" +
            "}",
            "{" +
            "   $addFields: {" +
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
            "   $addFields: {" +
            "       liked: { $ne: [0, { $size: '$likeInfo' }] }" +
            "   }" +
            "}"
    })
    List<TrendItemDetail> lookupByQuery(Document query, ObjectId puppetId, Pageable pageable);

    default List<TrendItemDetail> lookupBy(ObjectId puppetId, Pageable pageable) {
        return lookupByQuery(new Document(), puppetId, pageable);
    }

    default List<TrendItemDetail> lookupByPuppetId(ObjectId puppetId, Pageable pageable) {
        return lookupByQuery(new Document("puppetId", puppetId), puppetId, pageable);
    }

    default List<TrendItemDetail> lookupBySendDateAfter(ObjectId puppetId, Date date, Pageable pageable) {
        return lookupByQuery(new Document("sendDate", new Document("$gt", date)), puppetId, pageable);
    }

    default TrendItemDetail lookupById(ObjectId puppetId, ObjectId id) {
        return lookupByQuery(new Document("_id", id), puppetId, Pageable.unpaged()).get(0);
    }
}
