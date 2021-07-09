package com.leader.api.data.org.member;

import org.bson.types.ObjectId;

import java.util.Objects;

public class OrgMemberRole {

    public static final String PRESIDENT = "president";
    public static final String GENERAL_MANAGER = "general-manager";
    public static final String DEPARTMENT_MANAGER = "department-manager";
    public static final String RECRUIT_MANAGER = "recruit-manager";
    public static final String MEMBER = "member";

    private static final OrgMemberRole PRESIDENT_INSTANCE = new OrgMemberRole(PRESIDENT);
    private static final OrgMemberRole GENERAL_MANAGER_INSTANCE = new OrgMemberRole(GENERAL_MANAGER);
    private static final OrgMemberRole DEPARTMENT_MANAGER_INSTANCE = new OrgMemberRole(DEPARTMENT_MANAGER);
    private static final OrgMemberRole RECRUIT_MANAGER_INSTANCE = new OrgMemberRole(RECRUIT_MANAGER);
    private static final OrgMemberRole MEMBER_INSTANCE = new OrgMemberRole(MEMBER);

    public static OrgMemberRole president() {
        return PRESIDENT_INSTANCE;
    }

    public static OrgMemberRole generalManager() {
        return GENERAL_MANAGER_INSTANCE;
    }

    public static OrgMemberRole departmentManager() {
        return DEPARTMENT_MANAGER_INSTANCE;
    }

    public static OrgMemberRole departmentManager(ObjectId departmentId) {
        return new OrgMemberRole(DEPARTMENT_MANAGER, departmentId);
    }

    public static OrgMemberRole recruitManager() {
        return RECRUIT_MANAGER_INSTANCE;
    }

    public static OrgMemberRole recruitManager(ObjectId departmentId) {
        return new OrgMemberRole(RECRUIT_MANAGER, departmentId);
    }

    public static OrgMemberRole member() {
        return MEMBER_INSTANCE;
    }

    public static OrgMemberRole member(ObjectId departmentId) {
        return new OrgMemberRole(MEMBER, departmentId);
    }

    public String name;
    public ObjectId departmentId;

    // unused by project code but necessary for MongoDB to create instance
    public OrgMemberRole() {}

    public OrgMemberRole(String name) {
        this.name = name;
    }

    public OrgMemberRole(String name, ObjectId departmentId) {
        this.name = name;
        this.departmentId = departmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrgMemberRole that = (OrgMemberRole) o;
        return Objects.equals(name, that.name) && Objects.equals(departmentId, that.departmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, departmentId);
    }
}
