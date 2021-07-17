package com.leader.api.service.org.authorization;

import com.leader.api.data.org.member.OrgMemberOverview;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.org.member.OrgMemberRole;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.structure.OrgStructureQueryService;
import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrgAuthorizationService {

    private final OrgMemberRepository membershipRepository;
    private final OrgRoleAuthorityService roleAuthorityService;
    private final OrgMemberIdService memberIdService;
    private final OrgStructureQueryService structureQueryService;

    @Autowired
    public OrgAuthorizationService(
            OrgMemberRepository membershipRepository,
            OrgRoleAuthorityService roleAuthorityService,
            OrgMemberIdService memberIdService,
            OrgStructureQueryService structureQueryService) {
        this.membershipRepository = membershipRepository;
        this.roleAuthorityService = roleAuthorityService;
        this.memberIdService = memberIdService;
        this.structureQueryService = structureQueryService;
    }

    private List<OrgMemberRole> findRolesIn(ObjectId memberId) {
        return membershipRepository.lookupRolesByMemberId(memberId);
    }

    public boolean hasAuthorityIn(OrgAuthority authority, ObjectId departmentId, ObjectId memberId) {
        if (!membershipRepository.existsById(memberId)) {
            return false;
        }
        List<OrgMemberRole> roles = membershipRepository.lookupRolesByMemberId(memberId);
        return roleAuthorityService.rolesHasAuthority(authority, departmentId, roles);
    }

    public boolean currentMemberHasAuthorityIn(OrgAuthority authority, ObjectId departmentId) {
        ObjectId memberId = memberIdService.getCurrentMemberId();
        return hasAuthorityIn(authority, departmentId, memberId);
    }

    public void assertCurrentMemberHasAuthority(OrgAuthority authority, ObjectId departmentId) {
        boolean hasAuthority = currentMemberHasAuthorityIn(authority, departmentId);
        if (!hasAuthority) {
            throw new InternalErrorException("Member does not have authority.");
        }
    }

    public void assertCurrentMemberHasAuthority(OrgAuthority authority) {
        assertCurrentMemberHasAuthority(authority, null);
    }

    public boolean currentMemberCanManageAll(List<ObjectId> memberIds) {
        ObjectId currentOrgId = memberIdService.getCurrentOrgId();

        if (!structureQueryService.isAllMemberOfOrganization(currentOrgId, memberIds)) {
            return false;
        }

        ObjectId currentMemberId = memberIdService.getCurrentMemberId();
        List<OrgMemberRole> roles = findRolesIn(currentMemberId);
        return roleAuthorityService.canManageMembers(memberIds, roles);
    }

    public boolean currentMemberCanManage(ObjectId memberId) {
        return currentMemberCanManageAll(Collections.singletonList(memberId));
    }

    public void assertCurrentMemberCanManageAllMembers(List<ObjectId> memberIds) {
        boolean canManageAll = currentMemberCanManageAll(memberIds);
        if (!canManageAll) {
            throw new InternalErrorException("Member does not have authority to some member(s).");
        }
    }

    public void assertCurrentMemberCanManageMember(ObjectId memberId) {
        assertCurrentMemberCanManageAllMembers(Collections.singletonList(memberId));
    }

    public List<OrgMemberOverview> listManageableMembersOfCurrentMember() {
        ObjectId currentOrgId = memberIdService.getCurrentOrgId();
        ObjectId currentMemberId = memberIdService.getCurrentMemberId();
        List<OrgMemberRole> roles = findRolesIn(currentMemberId);
        return roleAuthorityService.listManageableMembers(currentOrgId, roles);
    }

    public List<ObjectId> listManageableMemberIdsOfCurrentMember() {
        return listManageableMembersOfCurrentMember().stream().map(member -> member.id).collect(Collectors.toList());
    }

    public List<OrgMemberOverview> listManageableManagersOfCurrentMember() {
        ObjectId currentOrgId = memberIdService.getCurrentOrgId();
        ObjectId currentMemberId = memberIdService.getCurrentMemberId();
        List<OrgMemberRole> roles = findRolesIn(currentMemberId);
        return roleAuthorityService.listManageableManagers(currentOrgId, roles);
    }

    public List<ObjectId> listManageableManagerIdsOfCurrentMember() {
        return listManageableManagersOfCurrentMember().stream().map(member -> member.id).collect(Collectors.toList());
    }
}
