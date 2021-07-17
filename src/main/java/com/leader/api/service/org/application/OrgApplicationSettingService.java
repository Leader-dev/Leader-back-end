package com.leader.api.service.org.application;

import com.leader.api.data.org.OrgApplicationScheme;
import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.department.OrgDepartmentRecruitInfo;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.org.member.OrgMemberRole;
import com.leader.api.service.org.authorization.OrgRoleService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.leader.api.data.org.member.OrgMemberRole.recruitManager;

@Service
public class OrgApplicationSettingService {

    private final OrganizationRepository organizationRepository;
    private final OrgMemberRepository memberRepository;
    private final OrgRoleService roleService;

    public OrgApplicationSettingService(OrganizationRepository organizationRepository, OrgMemberRepository memberRepository,
                                        OrgRoleService roleService) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.roleService = roleService;
    }

    private Organization getOrganization(ObjectId orgId) {
        return organizationRepository.findById(orgId).orElse(null);
    }

    public OrgApplicationScheme getApplicationScheme(ObjectId orgId) {
        Organization organization = getOrganization(orgId);
        return organization.applicationScheme;
    }

    public void setApplicationScheme(ObjectId orgId, OrgApplicationScheme scheme) {
        Organization organization = getOrganization(orgId);
        organization.applicationScheme = scheme;
        organizationRepository.save(organization);
    }

    public int getReceivedApplicationCount(ObjectId orgId) {
        Organization organization = getOrganization(orgId);
        return organization.receivedApplicationCount;
    }

    public void resetReceivedApplicationCount(ObjectId orgId) {
        Organization organization = getOrganization(orgId);
        organization.receivedApplicationCount = 0;
        organizationRepository.save(organization);
    }

    public ObjectId getDepartmentRecruitManagerId(ObjectId departmentId) {
        List<OrgMember> member = memberRepository.findByRolesContaining(recruitManager(departmentId));
        if (member.size() == 0) {
            return null;
        }
        return member.get(0).id;
    }

    public List<OrgDepartmentRecruitInfo> getDepartmentsRecruitManagerInfo(List<OrgDepartment> departments) {
        return departments.stream().map(department -> {
            OrgDepartmentRecruitInfo recruitInfo = new OrgDepartmentRecruitInfo();
            recruitInfo.id = department.id;
            recruitInfo.name = department.name;
            if (getDepartmentRecruitManagerId(department.id) == null) {
                recruitInfo.recruitManagerCount = 0;
            } else {
                recruitInfo.recruitManagerCount = 1;
            }
            return recruitInfo;
        }).collect(Collectors.toList());
    }

    public void removeMemberFromRecruitManager(ObjectId memberId, ObjectId departmentId) {
        roleService.removeRolesIn(memberId, recruitManager(departmentId));
    }

    public void setMemberToRecruitManager(ObjectId memberId, ObjectId departmentId) {
        ObjectId previousMemberId = getDepartmentRecruitManagerId(departmentId);
        if (previousMemberId != null) {
            removeMemberFromRecruitManager(previousMemberId, departmentId);
        }
        OrgMemberRole role = recruitManager(departmentId);
        if (memberId != null && !roleService.hasRole(memberId, role)) {
            roleService.addRolesIn(memberId, role);
        }
    }
}
