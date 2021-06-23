package com.leader.api.data.org.announcement;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgAnnouncementConfirmationRepository extends MongoRepository<OrgAnnouncementConfirmation, ObjectId> {

    OrgAnnouncementConfirmation findByMemberIdAndAnnounceId(ObjectId memberId, ObjectId announceId);

    boolean existsByMemberIdAndAnnounceId(ObjectId memberId, ObjectId announceId);

    void deleteByAnnounceId(ObjectId announceId);

    long countByAnnounceIdAndStatus(ObjectId announceId, String status);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { announceId: ?0, status: ?1 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_member'," +
            "       localField: 'memberId'," +
            "       foreignField: '_id'" +
            "       as: 'memberInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$memberInfo'" +
            "}",
            "{" +
            "   $replaceRoot: { newRoot: '$memberInfo' }" +
            "}"
    })
    List<OrgMemberInfoOverview> lookupByAnnounceIdAndStatus(ObjectId announceId, String status);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { memberId: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_announce'," +
            "       localField: 'announceId'," +
            "       foreignField: '_id'" +
            "       as: 'announceInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$announceInfo'" +
            "}",
            "{" +
            "   $replaceRoot: { newRoot: { $mergeObjects: [ { status: '$status' }, '$announceInfo' ] } }" +
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
    List<OrgAnnouncementOverview> lookupByMemberId(ObjectId memberId);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { memberId: ?0, announceId: ?1 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_announce'," +
            "       localField: 'announceId'," +
            "       foreignField: '_id'" +
            "       as: 'announceInfo'" +
            "   }" +
            "}",
            "{" +
            "   $unwind: '$announceInfo'" +
            "}",
            "{" +
            "   $replaceRoot: { newRoot: { $mergeObjects: [ { status: '$status' }, '$announceInfo' ] } }" +
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
    OrgAnnouncementDetail lookupByMemberIdAndAnnounceId(ObjectId memberId, ObjectId announceId);
}
