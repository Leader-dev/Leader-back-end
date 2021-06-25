package com.leader.api.service.org.authorization;

import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.org.member.OrgMemberRole;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Service
public class OrgRoleService {

    private final OrgMemberRepository memberRepository;

    @Autowired
    public OrgRoleService(OrgMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    private List<OrgMemberRole> findRoles(ObjectId memberId) {
        return memberRepository.lookupRolesByMemberId(memberId);
    }

    private void operateAndSaveMembership(OrgMember member, Consumer<OrgMember> callable) {
        callable.accept(member);
        memberRepository.save(member);
    }

    private void operateAndSaveMembership(ObjectId memberId, Consumer<OrgMember> callable) {
        memberRepository.findById(memberId).ifPresent(member -> operateAndSaveMembership(member, callable));
    }

    public OrgMemberRole findRole(ObjectId memberId, String name) {
        return OrgRoleUtil.findRoleIn(findRoles(memberId), name);
    }

    public boolean hasRole(ObjectId memberId, String name) {
        return OrgRoleUtil.roleExistsIn(findRoles(memberId), name);
    }

    // use when you want to erase all previous roles and set a completely new group of roles
    public void setRolesIn(ObjectId memberId, OrgMemberRole... roles) {
        operateAndSaveMembership(memberId, membership -> membership.roles = new ArrayList<>(Arrays.asList(roles)));
    }

    // use when you want to keep all previous roles and append a group of roles
    public void addRolesIn(ObjectId memberId, OrgMemberRole... roles) {
        operateAndSaveMembership(memberId, membership -> membership.roles.addAll(Arrays.asList(roles)));
    }

    // use when you want to update one specific role with the same name as given one
    // if no role with same name, than insert the new role
    // throws exception when multiple roles with same name is found
    public void updateRoleDepartmentIdIn(ObjectId memberId, OrgMemberRole role) {
        operateAndSaveMembership(memberId, membership -> {
            OrgMemberRole existingRole = OrgRoleUtil.findRoleIn(membership.roles, role.name);
            if (existingRole != null) {
                membership.roles.remove(existingRole);
            }
            membership.roles.add(role);
        });
    }

    // use when you want to remove all roles with specific names
    public void removeRolesIn(ObjectId memberId, String... names) {
        operateAndSaveMembership(memberId, membership -> {
            for (String name: names) {
                membership.roles.removeIf(role -> name.equals(role.name));
            }
        });
    }
}
