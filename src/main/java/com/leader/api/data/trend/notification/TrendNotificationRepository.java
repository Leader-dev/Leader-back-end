package com.leader.api.data.trend.notification;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface TrendNotificationRepository extends MongoRepository<TrendNotification, ObjectId> {

    long countByToPuppetIdAndReadFalse(ObjectId toPuppetId);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { toPuppetId: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'puppet_list'," +
            "       localField: 'puppetId'," +
            "       foreignField: '_id'," +
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
            "       from: 'trend_item'," +
            "       localField: 'trendItemId'," +
            "       foreignField: '_id'," +
            "       as: 'trendItemInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: {" +
            "       path: '$trendItemInfo'," +
            "       preserveNullAndEmptyArrays: true" +
            "   }" +
            "}",
            "{ $sort: { sendDate : -1 } }"
    })
    List<TrendNotificationOverview> lookupByToPuppetIdOrderBySendDateDesc(ObjectId toPuppetId, Pageable pageable);

    List<TrendNotification> findByToPuppetIdAndSendDateLessThanEqualAndReadFalse(ObjectId toPuppetId, Date sendDate);

    boolean existsByTypeAndPuppetIdAndTrendItemId(String type, ObjectId puppetId, ObjectId trendItemId);

    void deleteByToPuppetIdAndId(ObjectId toPuppetId, ObjectId id);
}
