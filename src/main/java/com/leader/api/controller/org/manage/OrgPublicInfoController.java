package com.leader.api.controller.org.manage;

import com.leader.api.data.org.OrgPublicInfo;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.service.ImageService;
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
    private final ImageService imageService;

    public OrgPublicInfoController(OrganizationService organizationService,
                                   OrgAuthorizationService authorizationService,
                                   OrgMemberIdService orgMemberIdService,
                                   ImageService imageService) {
        this.organizationService = organizationService;
        this.authorizationService = authorizationService;
        this.orgMemberIdService = orgMemberIdService;
        this.imageService = imageService;
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
        imageService.assertUploadedTempImage(queryObject.publicInfo.posterUrl);

        authorizationService.assertCurrentMemberHasAuthority(PUBLIC_INFO_MANAGEMENT);

        ObjectId orgId = orgMemberIdService.getCurrentOrgId();
        OrgPublicInfo prevPublicInfo = organizationService.getPublicInfo(orgId);
        organizationService.updateOrganizationPublicInfo(orgId, queryObject.publicInfo);

        imageService.confirmUploadImage(queryObject.publicInfo.posterUrl);
        imageService.deleteImage(prevPublicInfo.posterUrl);

        return new SuccessResponse();
    }
}
