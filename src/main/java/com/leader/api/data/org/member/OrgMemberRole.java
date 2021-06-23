package com.leader.api.data.org.member;

import org.bson.types.ObjectId;

import java.util.Objects;

public class OrgMemberRole {

    public static final String PRESIDENT = "president";
    public static final String GENERAL_MANAGER = "general-manager";
    public static final String DEPARTMENT_MANAGER = "department-manager";
    public static final String RECRUIT_MANAGER = "recruit-manager";
    public static final String MEMBER = "member";

    public static OrgMemberRole president() {
        return new OrgMemberRole(PRESIDENT);
    }

    public static OrgMemberRole generalManager() {
        return new OrgMemberRole(GENERAL_MANAGER);
    }

    public static OrgMemberRole departmentManager() {
        return new OrgMemberRole(DEPARTMENT_MANAGER);
    }

    public static OrgMemberRole departmentManager(ObjectId departmentId) {
        return new OrgMemberRole(DEPARTMENT_MANAGER, departmentId);
    }

    public static OrgMemberRole recruitManager() {
        return new OrgMemberRole(RECRUIT_MANAGER);
    }

    public static OrgMemberRole recruitManager(ObjectId departmentId) {
        return new OrgMemberRole(RECRUIT_MANAGER, departmentId);
    }

    public static OrgMemberRole member() {
        return new OrgMemberRole(MEMBER);
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
