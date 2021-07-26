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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrgMemberService {

    @Value("${leader.member-number-id-length}")
    public int MEMBER_NUMBER_ID_LENGTH;

    private final OrganizationRepository organizationRepository;
    private final OrgMemberRepository memberRepository;
    private final SecureService secureService;

    @Autowired
    public OrgMemberService(OrganizationRepository organizationRepository,
                            OrgMemberRepository memberRepository,
                            SecureService secureService) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.secureService = secureService;
    }

    private boolean membershipExists(ObjectId orgId, ObjectId userId) {
        return memberRepository.existsByOrgIdAndUserId(orgId, userId);
    }

    private OrgMember insertNewMembership(ObjectId orgId, ObjectId userid, String name) {
        if (isMember(orgId, userid)) {
            throw new InternalErrorException("Already in organization");
        }
        OrgMember newMember = new OrgMember();
        newMember.orgId = orgId;
        newMember.userId = userid;
        newMember.name = name;
        newMember.roles = new ArrayList<>();
        newMember.roles.add(OrgMemberRole.member());
        newMember.resigned = false;
        newMember.displayTitle = true;
        synchronized (memberRepository) {
            newMember.numberId = secureService.generateRandomNumberId(
                    MEMBER_NUMBER_ID_LENGTH,
                    memberRepository::existsByNumberId
            );
            return memberRepository.insert(newMember);
        }
    }

    public void updateOrganizationMemberCount(ObjectId orgId) {
        organizationRepository.findById(orgId).ifPresent(organization -> {
            organization.memberCount = memberRepository.countByOrgIdAndResignedFalse(orgId);
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

    public List<OrgJoinedOverview> findJoinedOrganizations(ObjectId userid) {
        return memberRepository.lookupJoinedOrganizationsByUserId(userid);
    }
}
