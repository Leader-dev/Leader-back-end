package com.leader.api.controller.org;

import com.leader.api.data.org.OrgPublicInfo;
import com.leader.api.data.org.Organization;
import com.leader.api.data.org.member.OrgJoinedOverview;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRole;
import com.leader.api.data.org.report.OrgReport;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberService;
import com.leader.api.service.org.report.OrgReportService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org")
public class OrgUserController {

    private final UserIdService userIdService;
    private final OrganizationService organizationService;
    private final OrgMemberService membershipService;
    private final OrgAuthorizationService authorizationService;
    private final OrgReportService reportService;

    public OrgUserController(UserIdService userIdService, OrganizationService organizationService,
                             OrgMemberService membershipService, OrgAuthorizationService authorizationService,
                             OrgReportService reportService) {
        this.userIdService = userIdService;
        this.organizationService = organizationService;
        this.membershipService = membershipService;
        this.authorizationService = authorizationService;
        this.reportService = reportService;
    }

    @PostMapping("/create")
    public Document createOrganization(@RequestBody OrgPublicInfo newOrganization) {
        ObjectId userid = userIdService.getCurrentUserId();

        // create and join organization
        Organization organization = organizationService.createNewOrganization(newOrganization);
        OrgMember member = membershipService.joinOrganization(organization.id, userid);
        authorizationService.setRolesIn(member.id, OrgMemberRole.president());

        return new SuccessResponse();
    }

    @PostMapping("/joined")
    public Document listJoinedOrganizations() {
        ObjectId userid = userIdService.getCurrentUserId();

        // get joined organizations
        List<OrgJoinedOverview> list = membershipService.findJoinedOrganizations(userid);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }

    @PostMapping("/report")
    public Document reportOrganization(@RequestBody OrgReport report) {
        report.senderUserId = userIdService.getCurrentUserId();
        reportService.sendReport(report);

        return new SuccessResponse();
    }
}
