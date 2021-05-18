package com.leader.api.controller.org;

import com.leader.api.data.org.*;
import com.leader.api.data.org.membership.OrganizationJoinedOverview;
import com.leader.api.data.org.membership.OrganizationMembershipRepository;
import com.leader.api.data.org.report.OrganizationReport;
import com.leader.api.data.org.report.OrganizationReportRepository;
import com.leader.api.data.org.type.OrganizationTypeProject;
import com.leader.api.data.org.type.OrganizationTypeRepository;
import com.leader.api.response.ErrorResponse;
import com.leader.api.response.SuccessResponse;
import com.leader.api.util.Util;
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

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMembershipRepository organizationMembershipRepository;

    @Autowired
    private OrganizationReportRepository organizationReportRepository;

    @Autowired
    private OrganizationTypeRepository organizationTypeRepository;

    @PostMapping("/types")
    public Document getOrganizationTypes() {
        List<OrganizationTypeProject> types = organizationTypeRepository.findAllByAliasNotNull(OrganizationTypeProject.class);

        Document typesResponse = new Document();
        types.forEach(t -> typesResponse.append(t.alias, t));  // convert object list to key-value-pair object
        Document response = new SuccessResponse();
        response.append("types", typesResponse);
        return response;
    }

    @PostMapping("/list")
    public Document listOrganizations(@RequestBody OrganizationQueryObject queryObject) {
        // find organizations
        Page<OrganizationLobbyOverview> list = organizationRepository.findRunningOrganizationsByQueryObject(queryObject);

        Document response = new SuccessResponse();
        response.append("list", list.getContent());
        response.append("totalPages", list.getTotalPages());
        response.append("totalNumber", list.getTotalElements());
        return response;
    }

    @PostMapping("/detail")
    public Document organizationDetail(@RequestBody OrganizationQueryObject queryObject) {
        // find organization
        Organization detail = organizationRepository.findByIdAndStatus(queryObject.organizationId, "running");
        if (detail == null) {
            return new ErrorResponse("invalid_organization");
        }

        Document response = new SuccessResponse();
        response.append("detail", detail);
        return response;
    }

    @PostMapping("/create")
    public Document createOrganization(@RequestBody Organization newOrganization, HttpSession session) {
        ObjectId userid = Util.getUserIdFromSession(session);

        // insert organization
        Organization insertedOrg = organizationRepository.insertNewOrganization(newOrganization);

        // insert membership
        organizationMembershipRepository.insertNewMembership(insertedOrg.id, userid);

        return new SuccessResponse();
    }

    @PostMapping("/joined")
    public Document listJoinedOrganizations(HttpSession session) {
        ObjectId userid = Util.getUserIdFromSession(session);

        // get joined organizations
        List<OrganizationJoinedOverview> list = organizationMembershipRepository.lookupJoinedOrganizationsByUserId(userid);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }

    @PostMapping("/report")
    public Document reportOrganization(@RequestBody OrganizationReport report, HttpSession session) {
        report.senderUserId = Util.getUserIdFromSession(session);
        organizationReportRepository.insertNewReport(report);

        return new SuccessResponse();
    }
}
