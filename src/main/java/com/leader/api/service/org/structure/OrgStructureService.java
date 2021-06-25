package com.leader.api.service.org.structure;

import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.department.OrgDepartmentRepository;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.org.member.OrgMemberRole;
import com.leader.api.service.org.authorization.OrgRoleService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.leader.api.data.org.member.OrgMemberRole.*;

@Service
public class OrgStructureService extends OrgStructureQueryService {

    private final OrganizationRepository organizationRepository;

    public OrgStructureService(OrgDepartmentRepository departmentRepository,
                               OrgMemberRepository memberRepository,
                               OrganizationRepository organizationRepository,
                               OrgRoleService roleService) {
        super(memberRepository, departmentRepository, roleService);
        this.organizationRepository = organizationRepository;
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

    public void deleteDepartment(ObjectId departmentId) {
        // remove all members of the department
        OrgMemberRole role = member(departmentId);
        List<OrgMember> memberships = memberRepository.findByRolesContaining(role);
        memberships.forEach(membership -> membership.roles.removeIf(role1 -> MEMBER.equals(role1.name)));
        memberRepository.saveAll(memberships);

        // remove department itself
        departmentRepository.deleteById(departmentId);
    }

    public void setMemberToPresident(ObjectId memberId) {
        roleService.setRolesIn(memberId, president());
        setOrgPresidentInfo(memberId);
    }

    public void removeMemberFromPresident(ObjectId memberId) {
        roleService.removeRolesIn(memberId, PRESIDENT);
    }

    public void setMemberToGeneralManager(ObjectId memberId) {
        roleService.removeRolesIn(memberId, DEPARTMENT_MANAGER, MEMBER);
        roleService.updateRoleDepartmentIdIn(memberId, generalManager());
    }

    public void setMemberToDepartmentManager(ObjectId departmentId, ObjectId memberId) {
        roleService.removeRolesIn(memberId, GENERAL_MANAGER);
        roleService.updateRoleDepartmentIdIn(memberId, departmentManager(departmentId));
        roleService.updateRoleDepartmentIdIn(memberId, member(departmentId));
    }

    public void setMemberToMember(ObjectId departmentId, ObjectId memberId) {
        roleService.removeRolesIn(memberId, GENERAL_MANAGER, DEPARTMENT_MANAGER);
        roleService.updateRoleDepartmentIdIn(memberId, member(departmentId));
    }
}
