package com.leader.api.controller.org.manage;

import com.leader.api.data.org.OrgApplicationScheme;
import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.util.response.ErrorResponse;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.leader.api.service.org.authorization.OrgAuthority.RECRUIT_SETTING;

@RestController
@RequestMapping("/org/manage/apply")
public class OrgApplicationManageController {

    private final OrganizationRepository organizationRepository;
    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService memberIdService;

    @Autowired
    public OrgApplicationManageController(OrganizationRepository organizationRepository,
                                          OrgAuthorizationService authorizationService,
                                          OrgMemberIdService memberIdService) {
        this.organizationRepository = organizationRepository;
        this.authorizationService = authorizationService;
        this.memberIdService = memberIdService;
    }

    private static class QueryObject {
        OrgApplicationScheme scheme;
    }

    @PostMapping("/updatescheme")
    public Document updateApplicationScheme(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(
                RECRUIT_SETTING,
                null
        );

        ObjectId orgId = memberIdService.getCurrentOrgId();
        Organization organization = organizationRepository.findById(orgId).orElse(null);
        if (organization == null) {
            return new ErrorResponse("invalid_organization");
        }
        organization.applicationScheme = queryObject.scheme;
        organizationRepository.save(organization);

        return new SuccessResponse();
    }
}
