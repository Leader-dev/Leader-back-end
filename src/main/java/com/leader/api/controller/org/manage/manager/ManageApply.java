package com.leader.api.controller.org.manage.manager;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationApplicationScheme;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.membership.OrganizationMembershipRepository;
import com.leader.api.response.ErrorResponse;
import com.leader.api.response.SuccessResponse;
import com.leader.api.util.Util;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/org/manage/apply")
public class ManageApply {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMembershipRepository membershipRepository;

    private static class ApplicationQueryObject {
        ObjectId organizationId;
        OrganizationApplicationScheme scheme;
    }

    @PostMapping("/updatescheme")
    public Document updateApplicationScheme(@RequestBody ApplicationQueryObject queryObject, HttpSession session) {
        ObjectId userid = Util.getUserIdFromSession(session);
        if (!membershipRepository.existsByOrganizationIdAndUserId(queryObject.organizationId, userid)) {
            return new ErrorResponse("invalid_organization");
        }
        Organization organization = organizationRepository.findById(queryObject.organizationId).orElse(null);
        if (organization == null) {
            return new ErrorResponse("invalid_organization");
        }
        organization.applicationScheme = queryObject.scheme;
        organizationRepository.save(organization);
        return new SuccessResponse();
    }
}
