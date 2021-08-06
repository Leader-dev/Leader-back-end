package com.leader.api.service.org.member;

import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberInfo;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.service.org.structure.OrgStructureService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrgMemberInfoService {

    private final OrgMemberRepository memberRepository;
    private final OrgStructureService structureService;

    @Autowired
    public OrgMemberInfoService(OrgMemberRepository memberRepository, OrgStructureService structureService) {
        this.memberRepository = memberRepository;
        this.structureService = structureService;
    }

    private OrgMember findMember(ObjectId memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

    private void saveMember(OrgMember member) {
        memberRepository.save(member);
    }

    private void copyValidItemsTo(OrgMember member, OrgMemberInfo info) {
        member.name = info.name;
        member.phone = info.phone;
        member.email = info.email;
    }

    private void copyItemsTo(OrgMemberInfo info, OrgMember member) {
        info.numberId = member.numberId;
        info.name = member.name;
        info.title = member.title;
        info.phone = member.phone;
        info.email = member.email;
        info.avatarUrl = member.avatarUrl;
    }

    public OrgMemberInfo getMemberInfo(ObjectId memberId) {
        OrgMember member = findMember(memberId);
        OrgMemberInfo info = new OrgMemberInfo();
        copyItemsTo(info, member);
        OrgDepartment department = structureService.getMemberDepartment(memberId);
        if (department != null) {
            info.departmentName = department.name;
        }
        return info;
    }

    public void updateMemberInfo(ObjectId memberId, OrgMemberInfo info) {
        OrgMember member = findMember(memberId);
        copyValidItemsTo(member, info);
        saveMember(member);
        if (structureService.isPresident(memberId)) {
            structureService.setOrgPresidentInfo(memberId);
        }
    }

    public void updateMemberTitle(ObjectId memberId, String title) {
        OrgMember member = findMember(memberId);
        member.title = title;
        saveMember(member);
    }

    public void updateMemberAvatar(ObjectId memberId, String avatarUrl) {
        OrgMember member = findMember(memberId);
        member.avatarUrl = avatarUrl;
        saveMember(member);
    }
}
