package com.leader.api.service.org.member;

import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.member.OrgJoinedOverview;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.org.member.OrgMemberRole;
import com.leader.api.service.util.SecureService;
import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrgMemberService {

    public static final int MEMBER_NUMBER_ID_LENGTH = 5;

    private final OrganizationRepository organizationRepository;
    private final OrgMemberRepository membershipRepository;
    private final SecureService secureService;

    @Autowired
    public OrgMemberService(OrganizationRepository organizationRepository,
                            OrgMemberRepository membershipRepository,
                            SecureService secureService) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.secureService = secureService;
    }

    private boolean membershipExists(ObjectId orgId, ObjectId userId) {
        return membershipRepository.existsByOrgIdAndUserId(orgId, userId);
    }

    private OrgMember insertNewMembership(ObjectId organizationId, ObjectId userid, String name) {
        if (isMember(organizationId, userid)) {
            throw new InternalErrorException("Already in organization");
        }
        OrgMember orgMembership = new OrgMember();
        orgMembership.numberId = secureService.generateRandomNumberId(
                MEMBER_NUMBER_ID_LENGTH,
                membershipRepository::existsByNumberId
        );
        orgMembership.orgId = organizationId;
        orgMembership.userId = userid;
        orgMembership.roles = new ArrayList<>();
        orgMembership.name = name;
        orgMembership.roles = new ArrayList<>();
        orgMembership.roles.add(OrgMemberRole.member());
        return membershipRepository.insert(orgMembership);
    }

    private void deleteMembership(ObjectId organizationId, ObjectId userId) {
        membershipRepository.deleteByOrgIdAndUserId(organizationId, userId);
    }

    private void updateOrganizationMemberCount(ObjectId orgId) {
        organizationRepository.findById(orgId).ifPresent(organization -> {
            organization.memberCount = membershipRepository.countByOrgId(orgId);
            organizationRepository.save(organization);
        });
    }

    public boolean isMember(ObjectId orgId, ObjectId userId) {
        return membershipExists(orgId, userId);
    }

    public void assertIsMember(ObjectId organizationId, ObjectId userid) {
        if (!isMember(organizationId, userid)) {
            throw new InternalErrorException("User not in organization.");
        }
    }

    public OrgMember getMember(ObjectId memberId) {
        return membershipRepository.findById(memberId).orElse(null);
    }

    public ObjectId getOrgId(ObjectId memberId) {
        return getMember(memberId).orgId;
    }

    public List<OrgJoinedOverview> findJoinedOrganizations(ObjectId userid) {
        return membershipRepository.lookupJoinedOrganizationsByUserId(userid);
    }

    public OrgMember joinOrganization(ObjectId organizationId, ObjectId userId, String name) {
        OrgMember member = insertNewMembership(organizationId, userId, name);
        updateOrganizationMemberCount(organizationId);
        return member;
    }

    public void leaveOrganization(ObjectId organizationId, ObjectId userId) {
        deleteMembership(organizationId, userId);
        updateOrganizationMemberCount(organizationId);
    }
}
