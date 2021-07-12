package com.leader.api.service.org.member;

import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.org.member.OrgMemberTitleInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgMemberTitleService {

    private final OrgMemberRepository memberRepository;

    @Autowired
    public OrgMemberTitleService(OrgMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void updateDisplayTitle(ObjectId userId, ObjectId orgId, boolean displayTitle) {
        OrgMember member = memberRepository.findByOrgIdAndUserId(orgId, userId);
        member.displayTitle = displayTitle;
        memberRepository.save(member);
    }

    public List<OrgMemberTitleInfo> findTitles(ObjectId userId) {
        return memberRepository.lookupJoinedOrganizationTitlesByUserId(userId);
    }

    public List<OrgMemberTitleInfo> findDisplayedTitles(ObjectId userId) {
        return memberRepository.lookupJoinedOrganizationTitlesByUserIdAndDisplayTitle(userId, true);
    }
}
