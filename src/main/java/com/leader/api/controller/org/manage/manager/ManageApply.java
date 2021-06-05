package com.leader.api.controller.org.manage.manager;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationApplicationScheme;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.membership.OrganizationMembershipRepository;
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

@RestController
@RequestMapping("/org/manage/apply")
public class ManageApply {

    private final OrganizationRepository organizationRepository;

    private final OrganizationMembershipRepository membershipRepository;

    private final UserIdService userIdService;

    @Autowired
    public ManageApply(OrganizationRepository organizationRepository,
                       OrganizationMembershipRepository membershipRepository, UserIdService userIdService) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.userIdService = userIdService;
    }

    private static class ApplicationQueryObject {
        ObjectId organizationId;
        OrganizationApplicationScheme scheme;
    }

    @PostMapping("/updatescheme")
    public Document updateApplicationScheme(@RequestBody ApplicationQueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();
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
