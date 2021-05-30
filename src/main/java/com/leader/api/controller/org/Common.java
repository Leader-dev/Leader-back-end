package com.leader.api.controller.org;

import com.leader.api.data.org.*;
import com.leader.api.data.org.membership.OrganizationJoinedOverview;
import com.leader.api.data.org.report.OrganizationReport;
import com.leader.api.util.response.ErrorResponse;
import com.leader.api.util.response.SuccessResponse;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.util.SessionService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/org/common")
public class Common {

    private final OrganizationService organizationService;

    private final SessionService sessionService;

    @Autowired
    public Common(OrganizationService organizationService, SessionService sessionService) {
        this.organizationService = organizationService;
        this.sessionService = sessionService;
    }

    @PostMapping("/types")
    public Document getOrganizationTypes() {
        // convert types from object list to key-value-pair object, with alias being the key
        Document typesMapping = organizationService.getTypeAliasMapping();

        Document response = new SuccessResponse();
        response.append("types", typesMapping);
        return response;
    }

    @PostMapping("/index")
    public Document getOrganizationIndex() {
        // TODO Use more intelligent way to decide content
        List<OrganizationPosterOverview> pic = organizationService.findOrganizationsByNumber(5, OrganizationPosterOverview.class);
        List<OrganizationLobbyOverview> list = organizationService.findOrganizationsByNumber(9, OrganizationLobbyOverview.class);

        Document response = new SuccessResponse();
        response.append("pic", pic);
        response.append("list", list);
        return response;
    }

    @PostMapping("/list")
    public Document listOrganizations(@RequestBody OrganizationQueryObject queryObject) {
        // find organizations
        Page<OrganizationLobbyOverview> list = organizationService.findRunningOrganizationsByQueryObject(queryObject);

        Document response = new SuccessResponse();
        response.append("list", list.getContent());
        response.append("totalPages", list.getTotalPages());
        response.append("totalNumber", list.getTotalElements());
        return response;
    }

    @PostMapping("/detail")
    public Document organizationDetail(@RequestBody OrganizationQueryObject queryObject) {
        // find organization
        Organization detail = organizationService.getOrganization(queryObject.organizationId);
        if (detail == null) {
            return new ErrorResponse("invalid_organization");
        }

        Document response = new SuccessResponse();
        response.append("detail", detail);
        return response;
    }

    @PostMapping("/create")
    public Document createOrganization(@RequestBody Organization newOrganization, HttpSession session) {
        ObjectId userid = sessionService.getUserIdFromSession(session);

        // insert organization
        organizationService.createOrganization(newOrganization, userid);

        return new SuccessResponse();
    }

    @PostMapping("/joined")
    public Document listJoinedOrganizations(HttpSession session) {
        ObjectId userid = sessionService.getUserIdFromSession(session);

        // get joined organizations
        List<OrganizationJoinedOverview> list = organizationService.findJoinedOrganizations(userid);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }

    @PostMapping("/report")
    public Document reportOrganization(@RequestBody OrganizationReport report, HttpSession session) {
        report.senderUserId = sessionService.getUserIdFromSession(session);
        organizationService.sendReport(report);

        return new SuccessResponse();
    }
}
