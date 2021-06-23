package com.leader.api.controller.org.manage;

import com.leader.api.data.org.OrgPublicInfo;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.leader.api.service.org.authorization.OrgAuthority.PUBLIC_INFO_MANAGEMENT;

@RestController
@RequestMapping("/org/manage/public-info")
public class OrgPublicInfoController {

    private final OrganizationService organizationService;
    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService orgMemberIdService;

    public OrgPublicInfoController(OrganizationService organizationService,
                                   OrgAuthorizationService authorizationService, OrgMemberIdService orgMemberIdService) {
        this.organizationService = organizationService;
        this.authorizationService = authorizationService;
        this.orgMemberIdService = orgMemberIdService;
    }

    public static class QueryObject {
        public OrgPublicInfo publicInfo;
    }

    @PostMapping("/get")
    public Document getPublicInfo() {
        authorizationService.assertCurrentMemberHasAuthority(PUBLIC_INFO_MANAGEMENT);

        ObjectId orgId = orgMemberIdService.getCurrentOrgId();
        OrgPublicInfo publicInfo = organizationService.getPublicInfo(orgId);

        Document response = new SuccessResponse();
        response.append("publicInfo", publicInfo);
        return response;
    }

    @PostMapping("/set")
    public Document setPublicInfo(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(PUBLIC_INFO_MANAGEMENT);

        ObjectId orgId = orgMemberIdService.getCurrentOrgId();
        organizationService.updateOrganizationPublicInfo(orgId, queryObject.publicInfo);

        return new SuccessResponse();
    }
}
