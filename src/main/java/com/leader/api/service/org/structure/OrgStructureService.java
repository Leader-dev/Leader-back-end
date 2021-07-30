package com.leader.api.service.org.structure;

import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.department.OrgDepartmentRepository;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.org.member.OrgMemberRole;
import com.leader.api.service.org.authorization.OrgRoleService;
import com.leader.api.service.org.member.OrgMemberService;
import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.leader.api.data.org.member.OrgMemberRole.*;

@Service
public class OrgStructureService extends OrgStructureQueryService {

    private final OrganizationRepository organizationRepository;
    private final OrgMemberService memberService;

    public OrgStructureService(OrgDepartmentRepository departmentRepository,
                               OrgMemberRepository memberRepository,
                               OrganizationRepository organizationRepository,
                               OrgRoleService roleService, OrgMemberService memberService) {
        super(memberRepository, departmentRepository, roleService);
        this.organizationRepository = organizationRepository;
        this.memberService = memberService;
    }

    public void setOrgPresidentInfo(ObjectId memberId) {
        memberRepository.findById(memberId).ifPresent(member -> {
            organizationRepository.findById(member.orgId).ifPresent(organization -> {
                organization.presidentName = member.name;
                organizationRepository.save(organization);
            });
        });
    }

    public void createDepartment(ObjectId organizationId, ObjectId parentId, String name) {
        OrgDepartment department = new OrgDepartment();
        department.orgId = organizationId;
        department.parentId = parentId;
        department.name = name;
        departmentRepository.insert(department);
    }

    public void renameDepartment(ObjectId departmentId, String name) {
        departmentRepository.findById(departmentId).ifPresent(department -> {
            department.name = name;
            departmentRepository.save(department);
        });
    }

    public void deleteDepartment(ObjectId departmentId) {
        // remove all members of the department
        OrgMemberRole role = member(departmentId);
        List<OrgMember> members = memberRepository.findByRolesContaining(role);
        for (OrgMember member : members) {
            member.roles.removeIf(role1 -> DEPARTMENT_MANAGER.equals(role1.name) || MEMBER.equals(role1.name));
            member.roles.add(member());
        }
        memberRepository.saveAll(members);

        // remove department itself
        departmentRepository.deleteById(departmentId);
    }

    public void setMemberToPresident(ObjectId memberId) {
        roleService.setRolesIn(memberId, president(), member());
        setOrgPresidentInfo(memberId);
    }

    public void setMemberToNoDepartmentMember(ObjectId memberId) {
        roleService.setRolesIn(memberId, member());
    }

    public void setMemberToGeneralManager(ObjectId memberId) {
        roleService.removeRolesIn(memberId, DEPARTMENT_MANAGER);
        roleService.updateRoleDepartmentIdIn(memberId, generalManager());
        roleService.updateRoleDepartmentIdIn(memberId, member());
    }

    public void setMemberToDepartmentManager(ObjectId memberId, ObjectId departmentId) {
        roleService.removeRolesIn(memberId, GENERAL_MANAGER);
        roleService.updateRoleDepartmentIdIn(memberId, departmentManager(departmentId));
        roleService.updateRoleDepartmentIdIn(memberId, member(departmentId));
    }

    public void setMemberToMember(ObjectId memberId, ObjectId departmentId) {
        roleService.removeRolesIn(memberId, GENERAL_MANAGER, DEPARTMENT_MANAGER);
        roleService.updateRoleDepartmentIdIn(memberId, member(departmentId));
    }

    public void dismissMember(ObjectId memberId) {
        if (isPresident(memberId)) {
            throw new InternalErrorException("Cannot dismiss president.");
        }

        memberRepository.findById(memberId).ifPresent(member -> {
            member.userId = null;
            member.roles = new ArrayList<>();
            member.title = null;
            member.phone = null;
            member.email = null;
            member.resigned = true;
            memberRepository.save(member);

            memberService.updateOrganizationMemberCount(member.orgId);
        });
    }
}
