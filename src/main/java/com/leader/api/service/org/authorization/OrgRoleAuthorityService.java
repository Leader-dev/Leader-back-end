package com.leader.api.service.org.authorization;

import com.leader.api.data.org.member.OrgMemberOverview;
import com.leader.api.data.org.member.OrgMemberRole;
import com.leader.api.service.org.structure.OrgStructureQueryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.leader.api.data.org.member.OrgMemberRole.*;
import static com.leader.api.service.org.authorization.OrgAuthority.*;
import static com.leader.api.service.org.authorization.OrgRoleUtil.*;

@Service
public class OrgRoleAuthorityService {

    private final OrgStructureQueryService structureQueryService;

    private static final List<OrgAuthority> GENERAL_MANAGER_AUTHORITIES = Arrays.asList(
            ADMIN_FUNCTIONS,
            ATTENDANCE,
            BULLETIN_MANAGEMENT,
            TIMELINE_MANAGEMENT,
            TASK,
            STRUCTURE_MANAGEMENT
    );

    private static final List<OrgAuthority> DEPARTMENT_MANAGER_AUTHORITIES = Arrays.asList(
            ADMIN_FUNCTIONS,
            ATTENDANCE,
            TIMELINE_MANAGEMENT,
            TASK,
            STRUCTURE_MANAGEMENT
    );

    private static final List<OrgAuthority> RECRUIT_MANAGER_AUTHORITIES = Collections.singletonList(
            RECRUIT
    );

    @Autowired
    public OrgRoleAuthorityService(OrgStructureQueryService structureQueryService) {
        this.structureQueryService = structureQueryService;
    }

    public boolean rolesHasAuthority(OrgAuthority authority, ObjectId departmentId,
                                     List<OrgMemberRole> roles) {
        if (authority == BASIC) {
            return true;
        }
        if (roleExistsInStrict(roles, president())) {
            return true;
        }
        if (roleExistsInStrict(roles, generalManager()) &&
                GENERAL_MANAGER_AUTHORITIES.contains(authority)) {
            return true;
        }
        if (roleExistsInStrict(roles, departmentManager(departmentId)) &&
                DEPARTMENT_MANAGER_AUTHORITIES.contains(authority)) {
            return true;
        }
        if (roleExistsIn(roles, recruitManager()) &&
                RECRUIT_MANAGER_AUTHORITIES.contains(authority)) {
            return true;
        }
        return false;
    }

    public boolean canManageMembers(List<ObjectId> memberIds, List<OrgMemberRole> roles) {
        if (roles.contains(president())) {
            return true;
        }
        if (roles.contains(generalManager())) {
            return structureQueryService.isAllNotPresident(memberIds);
        }
        OrgMemberRole departmentManagerRole = findRoleIn(roles, DEPARTMENT_MANAGER);
        if (departmentManagerRole != null) {
            return structureQueryService.isAllMemberOfDepartment(departmentManagerRole.departmentId, memberIds);
        }
        return false;
    }

    public List<OrgMemberOverview> listManageableMembers(ObjectId orgId, List<OrgMemberRole> roles) {
        if (roles.contains(president()) || roles.contains(generalManager())) {
            return structureQueryService.listMembersOfOrganization(orgId);
        }
        OrgMemberRole departmentManagerRole = findRoleIn(roles, DEPARTMENT_MANAGER);
        if (departmentManagerRole != null) {
            return structureQueryService.listMembers(orgId, departmentManagerRole.departmentId);
        }
        return Collections.emptyList();
    }

    public List<OrgMemberOverview> listManageableManagers(ObjectId orgId, List<OrgMemberRole> roles) {
        if (roles.contains(president())) {
            return structureQueryService.listMembersOfOrganizationWithRoles(orgId, president(), generalManager(), departmentManager());
        }
        if (roles.contains(generalManager())) {
            return structureQueryService.listMembersOfOrganizationWithRoles(orgId, generalManager(), departmentManager());
        }
        OrgMemberRole departmentManagerRole = findRoleIn(roles, DEPARTMENT_MANAGER);
        if (departmentManagerRole != null) {
            return structureQueryService.listMembersOfOrganizationWithRoles(orgId, generalManager(), departmentManagerRole);
        }
        return Collections.emptyList();
    }
}
