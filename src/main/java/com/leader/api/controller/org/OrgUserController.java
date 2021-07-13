package com.leader.api.controller.org;

import com.leader.api.data.org.OrgDetail;
import com.leader.api.data.org.OrgLobbyOverview;
import com.leader.api.data.org.OrgPublicInfo;
import com.leader.api.data.org.Organization;
import com.leader.api.data.org.member.OrgJoinedOverview;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.report.OrgReport;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.application.OrgApplicationService;
import com.leader.api.service.org.favorite.OrgFavoriteService;
import com.leader.api.service.org.member.OrgMemberService;
import com.leader.api.service.org.report.OrgReportService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.service.service.ImageService;
import com.leader.api.service.user.UserInfoService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.response.ErrorResponse;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org")
public class OrgUserController {

    private final UserIdService userIdService;
    private final UserInfoService userInfoService;
    private final OrganizationService organizationService;
    private final OrgMemberService membershipService;
    private final OrgApplicationService applicationService;
    private final OrgFavoriteService favoriteService;
    private final OrgReportService reportService;
    private final OrgStructureService structureService;
    private final ImageService imageService;

    @Autowired
    public OrgUserController(UserIdService userIdService, UserInfoService userInfoService, OrganizationService organizationService,
                             OrgMemberService membershipService, OrgApplicationService applicationService, OrgFavoriteService favoriteService,
                             OrgReportService reportService, OrgStructureService structureService, ImageService imageService) {
        this.userIdService = userIdService;
        this.userInfoService = userInfoService;
        this.organizationService = organizationService;
        this.membershipService = membershipService;
        this.applicationService = applicationService;
        this.favoriteService = favoriteService;
        this.reportService = reportService;
        this.structureService = structureService;
        this.imageService = imageService;
    }

    public static class QueryObject {
        public OrgPublicInfo publicInfo;
        public OrgReport reportInfo;
    }

    @PostMapping("/detail")
    public Document organizationDetail(@RequestBody OrgCommonController.QueryObject queryObject) {
        // find organization
        OrgDetail detail = organizationService.getOrganizationDetail(queryObject.orgId);
        if (detail == null) {
            return new ErrorResponse("invalid_organization");
        }

        ObjectId userId = userIdService.getCurrentUserId();
        String applicationStatus = applicationService.getApplicationEntranceStatus(queryObject.orgId, userId);
        boolean favorite = favoriteService.isFavoriteOrganization(queryObject.orgId, userId);

        Document response = new SuccessResponse();
        Document data = new Document();
        data.append("detail", detail);
        data.append("applicationStatus", applicationStatus);
        data.append("favorite", favorite);
        response.append("data", data);
        return response;
    }

    @PostMapping("/add-to-favorite")
    public Document favorOrganization(@RequestBody OrgCommonController.QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        favoriteService.addOrganizationToFavorite(queryObject.orgId, userId);

        return new SuccessResponse();
    }

    @PostMapping("/remove-from-favorite")
    public Document notFavorOrganization(@RequestBody OrgCommonController.QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        favoriteService.removeOrganizationFromFavorite(queryObject.orgId, userId);

        return new SuccessResponse();
    }

    @PostMapping("/list-favorite")
    public Document listFavoriteOrganizations() {
        ObjectId userId = userIdService.getCurrentUserId();
        List<OrgLobbyOverview> organizations = favoriteService.listFavoriteOrganizations(userId);

        Document response = new SuccessResponse();
        response.append("list", organizations);
        return response;
    }

    @PostMapping("/create")
    public Document createOrganization(@RequestBody QueryObject queryObject) {
        imageService.assertUploadedTempImage(queryObject.publicInfo.posterUrl);

        ObjectId userid = userIdService.getCurrentUserId();
        String nickname = userInfoService.getUserNickname(userid);

        // create and join organization
        Organization organization = organizationService.createNewOrganization(queryObject.publicInfo);
        OrgMember member = membershipService.joinOrganization(organization.id, userid, nickname);
        structureService.setMemberToPresident(member.id);

        imageService.confirmUploadImage(queryObject.publicInfo.posterUrl);

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
        imageService.assertUploadedTempImages(queryObject.reportInfo.imageUrls);

        queryObject.reportInfo.senderUserId = userIdService.getCurrentUserId();
        reportService.sendReport(queryObject.reportInfo);

        imageService.confirmUploadImages(queryObject.reportInfo.imageUrls);

        return new SuccessResponse();
    }
}
