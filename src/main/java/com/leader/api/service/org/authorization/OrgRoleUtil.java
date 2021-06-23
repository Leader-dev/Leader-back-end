package com.leader.api.service.org.authorization;

import com.leader.api.data.org.member.OrgMemberRole;

import java.util.List;

public class OrgRoleUtil {

    public static OrgMemberRole findRoleIn(List<OrgMemberRole> roles, String name) {
        for (OrgMemberRole role : roles) {
            if (name.equals(role.name)) {
                return role;
            }
        }
        return null;
    }

    public static boolean roleExistsIn(List<OrgMemberRole> roles, String name) {
        for (OrgMemberRole role : roles) {
            if (name.equals(role.name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyRoleExistIn(List<OrgMemberRole> roles, String... names) {
        for (String name : names) {
            if (roleExistsIn(roles, name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean roleExistsIn(List<OrgMemberRole> roles, OrgMemberRole queryRole) {
        for (OrgMemberRole role : roles) {
            if (queryRole.name.equals(role.name)) {
                if (queryRole.departmentId == null || queryRole.departmentId.equals(role.departmentId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean anyRoleExistIn(List<OrgMemberRole> roles, OrgMemberRole... queryRoles) {
        for (OrgMemberRole queryRole : queryRoles) {
            if (roleExistsIn(roles, queryRole)) {
                return true;
            }
        }
        return false;
    }

    public static boolean roleExistsInStrict(List<OrgMemberRole> roles, OrgMemberRole queryRole) {
        return roles.contains(queryRole);
    }
}
