package com.leader.api.data.org.member;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgMemberRepository extends MongoRepository<OrgMember, ObjectId> {

    List<OrgMember> findAllByOrgId(ObjectId organizationId);

    long countByOrgId(ObjectId organizationId);

    List<OrgMember> findAllByUserId(ObjectId userId);

    List<OrgMember> findByOrgIdAndRolesContaining(ObjectId organizationId, OrgMemberRole role);

    List<OrgMember> findByRolesContaining(OrgMemberRole role);

    boolean existsByOrgIdAndRolesContainingAndId(ObjectId organizationId, OrgMemberRole role, ObjectId memberId);

    boolean existsByRolesContainingAndId(OrgMemberRole role, ObjectId memberId);

    long countByUserId(ObjectId userId);

    OrgMember findByOrgIdAndUserId(ObjectId organizationId, ObjectId userId);

    boolean existsByOrgIdAndUserId(ObjectId organizationId, ObjectId userId);

    void deleteByOrgIdAndUserId(ObjectId organizationId, ObjectId userId);

    boolean existsByOrgIdAndId(ObjectId orgId, ObjectId memberId);

    <T> List<T> findByOrgIdAndNameContaining(ObjectId orgId, String searchText);

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
            "   $match: { orgId: ?0, userId: ?1 }" +
            "}",
            "{" +
            "   $unwind: '$roles'" +
            "}",
            "{" +
            "   $replaceRoot: { newRoot: '$roles' }" +
            "}"
    })
    List<OrgMemberRole> lookupRolesByOrgIdAndUserId(ObjectId organizationId, ObjectId userId);

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
}
