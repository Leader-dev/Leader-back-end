package com.leader.api.controller.org;

import com.leader.api.data.org.OrgPublicInfo;
import com.leader.api.data.org.Organization;
import com.leader.api.data.org.member.OrgJoinedOverview;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.report.OrgReport;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.member.OrgMemberService;
import com.leader.api.service.org.report.OrgReportService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.service.user.UserService;
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
    private final UserService userService;
    private final OrganizationService organizationService;
    private final OrgMemberService membershipService;
    private final OrgReportService reportService;
    private final OrgStructureService structureService;

    public OrgUserController(UserIdService userIdService, UserService userService, OrganizationService organizationService,
                             OrgMemberService membershipService, OrgReportService reportService,
                             OrgStructureService structureService) {
        this.userIdService = userIdService;
        this.userService = userService;
        this.organizationService = organizationService;
        this.membershipService = membershipService;
        this.reportService = reportService;
        this.structureService = structureService;
    }

    public static class QueryObject {
        public OrgPublicInfo publicInfo;
        public OrgReport reportInfo;
    }

    @PostMapping("/create")
    public Document createOrganization(@RequestBody QueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();
        String nickname = userService.getUserInfo(userid).nickname;

        // create and join organization
        Organization organization = organizationService.createNewOrganization(queryObject.publicInfo);
        OrgMember member = membershipService.joinOrganization(organization.id, userid, nickname);
        structureService.setMemberToPresident(member.id);

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
    public Document reportOrganization(@RequestBody QueryObject queryObject) {
        queryObject.reportInfo.senderUserId = userIdService.getCurrentUserId();
        reportService.sendReport(queryObject.reportInfo);

        return new SuccessResponse();
    }
}
