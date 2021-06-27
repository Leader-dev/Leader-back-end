package com.leader.api.controller.org.manage;

import com.leader.api.data.org.member.OrgMemberInfo;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.member.OrgMemberInfoService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.leader.api.service.org.authorization.OrgAuthority.BASIC;

@RestController
@RequestMapping("/org/manage/member-info")
public class OrgMemberInfoController {

    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService memberIdService;
    private final OrgMemberInfoService memberInfoService;

    @Autowired
    public OrgMemberInfoController(OrgAuthorizationService authorizationService, OrgMemberIdService memberIdService,
                                   OrgMemberInfoService memberInfoService) {
        this.authorizationService = authorizationService;
        this.memberIdService = memberIdService;
        this.memberInfoService = memberInfoService;
    }

    public static class QueryObject {
        public OrgMemberInfo memberInfo;
    }

    @PostMapping("/get")
    public Document showMemberInfo() {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        OrgMemberInfo info = memberInfoService.getMemberInfo(memberId);

        Document response = new SuccessResponse();
        response.append("memberInfo", info);
        return response;
    }

    @PostMapping("/set")
    public Document updateMemberInfo(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        memberInfoService.updateMemberInfo(memberId, queryObject.memberInfo);

        return new SuccessResponse();
    }
}
