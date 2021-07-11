package com.leader.api.service.org.structure;

import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.department.OrgDepartmentRepository;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberOverview;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.org.member.OrgMemberRole;
import com.leader.api.service.org.authorization.OrgRoleService;
import com.leader.api.service.org.authorization.OrgRoleUtil;
import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.leader.api.data.org.member.OrgMemberRole.*;
import static com.leader.api.service.org.authorization.OrgRoleUtil.anyRoleExistIn;

@Service
@Primary  // set to primary to distinguish with OrgStructureService when resolving dependency
public class OrgStructureQueryService {

    protected final OrgMemberRepository memberRepository;
    protected final OrgDepartmentRepository departmentRepository;
    protected final OrgRoleService roleService;

    public OrgStructureQueryService(OrgMemberRepository memberRepository,
                                    OrgDepartmentRepository departmentRepository,
                                    OrgRoleService roleService) {
        this.memberRepository = memberRepository;
        this.departmentRepository = departmentRepository;
        this.roleService = roleService;
    }

    private static final Function<OrgMember, OrgMemberOverview> TO_MEMBER_OVERVIEW_MAPPER = membership -> {
        OrgMemberOverview overview = new OrgMemberOverview();
        overview.id = membership.id;
        overview.name = membership.name;
        overview.title = membership.title;
        if (OrgRoleUtil.roleNameExistsIn(membership.roles, DEPARTMENT_MANAGER)) {
            overview.roleName = DEPARTMENT_MANAGER;
        } else if (OrgRoleUtil.anyRoleNameExistIn(membership.roles, GENERAL_MANAGER, PRESIDENT)) {
            overview.roleName = GENERAL_MANAGER;
        } else {
            overview.roleName = MEMBER;
        }
        return overview;
    };

    private static List<OrgMemberOverview> mapToMemberOverviewList(List<OrgMember> members) {
        return members.stream().map(TO_MEMBER_OVERVIEW_MAPPER).collect(Collectors.toList());
    }

    private List<OrgMember> findMembersOfOrganization(ObjectId orgId) {
        return memberRepository.findAllByOrgId(orgId);
    }

    private List<OrgMember> findMembers(ObjectId orgId, ObjectId departmentId) {
        OrgMemberRole role = member(departmentId);
        return memberRepository.findByOrgIdAndRolesContaining(orgId, role);
    }

    private List<OrgMember> findMembersOfOrganizationWithRoles(ObjectId orgId, OrgMemberRole... roles) {
        List<OrgMember> members = findMembersOfOrganization(orgId);
        return members.stream().filter(member -> anyRoleExistIn(member.roles, roles)).collect(Collectors.toList());
    }

    public boolean isDepartmentInOrganization(ObjectId orgId, ObjectId departmentId) {
        return departmentRepository.existsByOrgIdAndId(orgId, departmentId);
    }

    public void assertDepartmentInOrganization(ObjectId orgId, ObjectId departmentId) {
        if (!isDepartmentInOrganization(orgId, departmentId)) {
            throw new InternalErrorException("Department not in organization.");
        }
    }

    public boolean isPresident(ObjectId memberId) {
        OrgMemberRole role = OrgMemberRole.president();
        return memberRepository.existsByRolesContainingAndId(role, memberId);
    }

    public boolean isAllNotPresident(List<ObjectId> memberIds) {
        for (ObjectId memberId : memberIds) {
            if (isPresident(memberId)) {
                return false;
            }
        }
        return true;
    }

    public boolean isMemberOfOrganization(ObjectId orgId, ObjectId memberId) {
        return memberRepository.existsByOrgIdAndId(orgId, memberId);
    }

    public boolean isMemberOfDepartment(ObjectId departmentId, ObjectId memberId) {
        OrgMemberRole role = member(departmentId);
        return memberRepository.existsByRolesContainingAndId(role, memberId);
    }

    public boolean isAllMemberOfOrganization(ObjectId orgId, List<ObjectId> memberIds) {
        for (ObjectId memberId : memberIds) {
            if (!isMemberOfOrganization(orgId, memberId)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllMemberOfDepartment(ObjectId departmentId, List<ObjectId> memberIds) {
        for (ObjectId memberId : memberIds) {
            if (!isMemberOfDepartment(departmentId, memberId)) {
                return false;
            }
        }
        return true;
    }

    public OrgDepartment getMemberDepartment(ObjectId memberId) {
        OrgMemberRole role = roleService.findRoleByName(memberId, MEMBER);
        if (role == null || role.departmentId == null) {
            return null;
        }
        return departmentRepository.findById(role.departmentId).orElse(null);
    }

    public <T> List<T> listDepartments(ObjectId orgId, ObjectId parentId, Class<T> type) {
        return departmentRepository.findByOrgIdAndParentId(orgId, parentId, type);
    }

    public List<OrgMemberOverview> listMembers(ObjectId orgId, ObjectId departmentId) {
        return mapToMemberOverviewList(findMembers(orgId, departmentId));
    }

    public List<OrgMemberOverview> listMembersOfOrganization(ObjectId orgId) {
        return mapToMemberOverviewList(findMembersOfOrganization(orgId));
    }

    public List<OrgMemberOverview> listMembersOfOrganizationWithRoles(ObjectId orgId, OrgMemberRole... roles) {
        return mapToMemberOverviewList(findMembersOfOrganizationWithRoles(orgId, roles));
    }

    public List<OrgMemberOverview> searchMembers(ObjectId orgId, String searchText) {
        return memberRepository.findByOrgIdAndNameContainingAndResignedFalse(orgId, searchText, OrgMemberOverview.class);
    }
}
