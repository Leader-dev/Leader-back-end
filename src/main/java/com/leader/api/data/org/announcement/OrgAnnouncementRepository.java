package com.leader.api.data.org.announcement;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgAnnouncementRepository extends MongoRepository<OrgAnnouncement, ObjectId> {

    <T> List<T> findBySenderMemberId(ObjectId senderUserId, Class<T> type);

    <T> List<T> findBySenderMemberIdIn(List<ObjectId> senderUserIds, Class<T> type);

    boolean existsBySenderMemberIdAndId(ObjectId senderUserId, ObjectId announceId);

    void deleteBySenderMemberIdAndId(ObjectId senderUserId, ObjectId announceId);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { _id: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_member'," +
            "       localField: 'senderMemberId'," +
            "       foreignField: '_id'" +
            "       as: 'senderMemberInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$senderMemberInfo'" +
            "}"
    })
    OrgAnnouncementDetail lookupById(ObjectId announceId);
}
