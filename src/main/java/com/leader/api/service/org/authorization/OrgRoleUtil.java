package com.leader.api.service.org.authorization;

import com.leader.api.data.org.member.OrgMemberRole;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static List<OrgMemberRole> findAllRoleIn(List<OrgMemberRole> roles, String name) {
        ArrayList<OrgMemberRole> list = new ArrayList<>();
        for (OrgMemberRole role : roles) {
            if (name.equals(role.name)) {
                list.add(role);
            }
        }
        return list;
    }

    public static List<ObjectId> findAllRoleDepartmentIds(List<OrgMemberRole> roles, String... names) {
        ArrayList<ObjectId> list = new ArrayList<>();
        List<String> nameList = Arrays.asList(names);
        for (OrgMemberRole role : roles) {
            if (nameList.contains(role.name) && !list.contains(role.departmentId)) {
                list.add(role.departmentId);
            }
        }
        return list;
    }

    public static boolean roleNameExistsIn(List<OrgMemberRole> roles, String name) {
        for (OrgMemberRole role : roles) {
            if (name.equals(role.name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyRoleNameExistIn(List<OrgMemberRole> roles, String... names) {
        for (String name : names) {
            if (roleNameExistsIn(roles, name)) {
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
