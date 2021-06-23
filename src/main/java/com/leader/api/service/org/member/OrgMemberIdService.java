package com.leader.api.service.org.member;

import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.ThreadDataUtil;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class OrgMemberIdService {

    private static final String ORG_ID = "orgId";

    private final UserIdService userIdService;
    private final OrgMemberRepository membershipRepository;
    private final ThreadDataUtil threadDataUtil;

    public OrgMemberIdService(UserIdService userIdService,
                              OrgMemberRepository membershipRepository,
                              ThreadDataUtil threadDataUtil) {
        this.userIdService = userIdService;
        this.membershipRepository = membershipRepository;
        this.threadDataUtil = threadDataUtil;
    }

    public void setOrgId(ObjectId orgId) {
        threadDataUtil.set(ORG_ID, orgId);
    }

    public ObjectId getCurrentOrgId() {
        return threadDataUtil.get(ORG_ID, ObjectId.class);
    }

    public boolean isMemberInCurrentOrganization(ObjectId memberId) {
        return membershipRepository.existsByOrgIdAndId(getCurrentOrgId(), memberId);
    }

    public void assertMemberInCurrentOrganization(ObjectId memberId) {
        if (!isMemberInCurrentOrganization(memberId)) {
            throw new InternalErrorException("Member not in organization.");
        }
    }

    public ObjectId getCurrentMemberId() {
        ObjectId orgId = getCurrentOrgId();
        ObjectId userId = userIdService.getCurrentUserId();
        OrgMember member = membershipRepository.findByOrgIdAndUserId(orgId, userId);
        return member.id;
    }
}
