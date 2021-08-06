package com.leader.api.data.org.member;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgMemberRepository extends MongoRepository<OrgMember, ObjectId> {

    boolean existsByNumberId(String numberId);

    List<OrgMember> findAllByOrgId(ObjectId orgId);

    List<OrgMember> findAllByUserId(ObjectId userId);

    long countByOrgIdAndResignedFalse(ObjectId orgId);

    List<OrgMember> findByOrgIdAndRolesContaining(ObjectId orgId, OrgMemberRole role);

    List<OrgMember> findByRolesContaining(OrgMemberRole role);

    boolean existsByRolesContainingAndId(OrgMemberRole role, ObjectId memberId);

    OrgMember findByOrgIdAndUserId(ObjectId orgId, ObjectId userId);

    boolean existsByOrgIdAndUserId(ObjectId orgId, ObjectId userId);

    boolean existsByOrgIdAndId(ObjectId orgId, ObjectId memberId);

    <T> List<T> findByOrgIdAndNameContainingAndResignedFalse(ObjectId orgId, String searchText, Class<T> type);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { userId: ?0 }" +
            "}",
            "{" +
            "   $lookup: {" +
            "       from: 'org_list'," +
            "       localField: 'orgId'," +
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
    List<OrgJoinedOverview> lookupJoinedOrganizationsByUserId(ObjectId userId);

    @Aggregation(pipeline = {
            "{" +
            "   $match: ?0" +
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
            "   $addFields: {" +
            "       orgName: '$orgInfo.name'" +
            "   }" +
            "}"
    })
    List<OrgMemberTitleInfo> lookupJoinedOrganizationTitlesByQuery(Document query);

    @Aggregation(pipeline = {
            "{" +
            "   $match: { _id: ?0 }" +
            "}",
            "{" +
            "   $unwind: '$roles'" +
            "}",
            "{" +
            "   $replaceRoot: { newRoot: '$roles' }" +
            "}"
    })
    List<OrgMemberRole> lookupRolesByMemberId(ObjectId memberId);

    default List<OrgMemberTitleInfo> lookupJoinedOrganizationTitlesByUserId(ObjectId userId) {
        Document query = new Document("userId", userId);
        return lookupJoinedOrganizationTitlesByQuery(query);
    }

    default List<OrgMemberTitleInfo> lookupJoinedOrganizationTitlesByUserIdAndDisplayTitle(ObjectId userId, boolean displayTitle) {
        Document query = new Document();
        query.append("userId", userId);
        query.append("displayTitle", displayTitle);
        return lookupJoinedOrganizationTitlesByQuery(query);
    }
}
