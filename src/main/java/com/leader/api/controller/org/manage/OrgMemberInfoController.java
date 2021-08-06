package com.leader.api.controller.org.manage;

import com.leader.api.data.org.member.OrgMemberInfo;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.member.OrgMemberInfoService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.service.service.ImageService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.leader.api.service.org.authorization.OrgAuthority.BASIC;
import static com.leader.api.util.response.SuccessResponse.success;

@RestController
@RequestMapping("/org/manage/member-info")
public class OrgMemberInfoController {

    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService memberIdService;
    private final OrgMemberInfoService memberInfoService;
    private final OrgStructureService structureService;
    private final ImageService imageService;

    @Autowired
    public OrgMemberInfoController(OrgAuthorizationService authorizationService, OrgMemberIdService memberIdService,
                                   OrgMemberInfoService memberInfoService, OrgStructureService structureService, ImageService imageService) {
        this.authorizationService = authorizationService;
        this.memberIdService = memberIdService;
        this.memberInfoService = memberInfoService;
        this.structureService = structureService;
        this.imageService = imageService;
    }

    public static class QueryObject {
        public OrgMemberInfo memberInfo;
        public String avatarUrl;
    }

    @PostMapping("/get")
    public Document showMemberInfo() {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        OrgMemberInfo info = memberInfoService.getMemberInfo(memberId);

        return success(
                "memberInfo", info
        );
    }

    @PostMapping("/set")
    public Document updateMemberInfo(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        memberInfoService.updateMemberInfo(memberId, queryObject.memberInfo);

        return success();
    }

    @PostMapping("/set-avatar")
    public Document updateMemberAvatar(@RequestBody QueryObject queryObject) {
        imageService.assertUploadedTempImage(queryObject.avatarUrl);

        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        memberInfoService.updateMemberAvatar(memberId, queryObject.avatarUrl);

        imageService.confirmUploadImage(queryObject.avatarUrl);

        return success();
    }

    @PostMapping("/resign")
    public Document resignFromOrganization() {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        structureService.dismissMember(memberId);

        return success();
    }
}
