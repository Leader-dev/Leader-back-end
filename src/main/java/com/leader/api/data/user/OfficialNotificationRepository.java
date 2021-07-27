package com.leader.api.data.user;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OfficialNotificationRepository extends MongoRepository<OfficialNotification, ObjectId> {

    @Aggregation(pipeline = {
            "{" +
            "   $match: { $or: [ { toAll: true }, { userId: ?0 } ] }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'official_notification_read'," +
            "       let: { notificationId: '$_id', userId: ?0 }," +
            "       pipeline: [{ $match: { $expr: { $and: [" +
            "           { $eq: ['$notificationId', '$$notificationId'] }," +
            "           { $eq: ['$userId', '$$userId'] }" +
            "       ]}}}]," +
            "       as: 'readInfo'" +
            "   }" +
            "}",
            "{" +
            "   $addFields: {" +
            "       read: { $ne: [0, { $size: '$readInfo' }] }" +
            "   }" +
            "}",
            "{ $sort: { sendDate: -1 } }"
    })
    <T> List<T> lookupReceived(ObjectId userId, Class<T> type);

    @Aggregation(pipeline = {
            "{" +
            "   $match: ?0" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'user_list'," +
            "       localField: 'userId'," +
            "       foreignField: '_id'," +
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
            "       from: 'official_notification_read'," +
            "       localField: '_id'," +
            "       foreignField: 'notificationId'" +
            "       as: 'reads'" +
            "   }" +
            "}",
            "{ $sort: { sendDate: -1 } }"
    })
    <T> List<T> lookupSent(Document query, Class<T> type);

    default OfficialNotificationSentDetail lookupById(ObjectId notificationId) {
        return lookupSent(new Document("_id", notificationId), OfficialNotificationSentDetail.class).stream().findFirst().orElse(null);
    }

    OfficialNotification findByUserIdAndId(ObjectId userId, ObjectId id);
}
