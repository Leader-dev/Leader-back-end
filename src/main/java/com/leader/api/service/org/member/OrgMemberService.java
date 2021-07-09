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

    private OrgMember insertNewMembership(ObjectId orgId, ObjectId userid, String name) {
        if (isMember(orgId, userid)) {
            throw new InternalErrorException("Already in organization");
        }
        OrgMember orgMembership = new OrgMember();
        orgMembership.orgId = orgId;
        orgMembership.userId = userid;
        orgMembership.name = name;
        orgMembership.roles = new ArrayList<>();
        orgMembership.roles.add(OrgMemberRole.member());
        synchronized (membershipRepository) {
            orgMembership.numberId = secureService.generateRandomNumberId(
                    MEMBER_NUMBER_ID_LENGTH,
                    membershipRepository::existsByNumberId
            );
            return membershipRepository.insert(orgMembership);
        }
    }

    private void deleteMembership(ObjectId orgId, ObjectId userId) {
        membershipRepository.deleteByOrgIdAndUserId(orgId, userId);
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

    public void assertIsMember(ObjectId orgId, ObjectId userid) {
        if (!isMember(orgId, userid)) {
            throw new InternalErrorException("User not in organization.");
        }
    }

    public OrgMember joinOrganization(ObjectId orgId, ObjectId userId, String name) {
        OrgMember member = insertNewMembership(orgId, userId, name);
        updateOrganizationMemberCount(orgId);
        return member;
    }

    public void leaveOrganization(ObjectId orgId, ObjectId userId) {
        deleteMembership(orgId, userId);
        updateOrganizationMemberCount(orgId);
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
}
