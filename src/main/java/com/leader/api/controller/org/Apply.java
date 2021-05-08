package com.leader.api.controller.org;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.application.*;
import com.leader.api.data.org.manage.department.OrganizationDepartment;
import com.leader.api.data.org.manage.department.OrganizationDepartmentRepository;
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
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/org/apply")
public class Apply {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMembershipRepository membershipRepository;

    @Autowired
    private OrganizationDepartmentRepository departmentRepository;

    @Autowired
    private OrganizationApplicationRepository applicationRepository;

    private static class ApplyQueryObject {
        ObjectId organizationId;
        ObjectId departmentId;
        OrganizationApplicationForm applicationForm;
        ObjectId applicationId;
        String action;
    }

    @PostMapping("/send")
    public Document requestApplyForm(@RequestBody ApplyQueryObject queryObject, HttpSession session) {
        ObjectId userid = Util.getUserIdFromSession(session);
        Organization organization = organizationRepository.findById(queryObject.organizationId).orElse(null);
        if (organization == null) {
            return new ErrorResponse("organization_not_exits");
        }
        if (organization.applicationScheme.open) {
            return new ErrorResponse("application_not_open");
        }
        OrganizationDepartment department = departmentRepository.findById(queryObject.departmentId).orElse(null);
        if (department == null && organization.applicationScheme.appointDepartment) {
            return new ErrorResponse("application_require_department");
        }

        if (organization.applicationScheme.auth) {
            OrganizationApplication application = new OrganizationApplication();

            application.organizationId = queryObject.organizationId;
            application.applicantUserId = userid;

            if (department != null) {
                application.departmentId = department.id;
                application.auditUserId = department.applicationAuditUserId;
            }

            application.applicationForm = queryObject.applicationForm;
            application.timestamp = new Date();
            application.status = "pending";

            applicationRepository.insert(application);
        } else {
            membershipRepository.insertNewMembership(queryObject.organizationId, userid);
        }

        return new SuccessResponse();
    }

    @PostMapping("/list")
    public Document listApplications(HttpSession session) {
        ObjectId userid = Util.getUserIdFromSession(session);

        List<OrganizationApplicationSentOverview> list =
                applicationRepository.findAllByApplicantUserIdIncludeInfo(userid, OrganizationApplicationSentOverview.class);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }

    @PostMapping("/detail")
    public Document applicationDetail(@RequestBody ApplyQueryObject queryObject, HttpSession session) {
        ObjectId userid = Util.getUserIdFromSession(session);

        OrganizationApplicationDetail application =
                applicationRepository.findByApplicantUserIdAndIdIncludeInfo(
                        userid, queryObject.applicationId, OrganizationApplicationDetail.class);

        Document response = new SuccessResponse();
        response.append("detail", application);
        return response;
    }

    @PostMapping("/reply")
    public Document replyToApplication(@RequestBody ApplyQueryObject queryObject, HttpSession session) {
        ObjectId userid = Util.getUserIdFromSession(session);

        OrganizationApplication application =
                applicationRepository.findByApplicantUserIdAndId(userid, queryObject.applicationId);
        if (!"passed".equals(application.status)) {
            return new ErrorResponse("invalid_application");
        }
        if ("accept".equals(queryObject.action)) {
            application.status = "accepted";
        } else if ("decline".equals(queryObject.action)) {
            application.status = "declined";
        } else {
            return new ErrorResponse("invalid_action");
        }
        applicationRepository.save(application);

        membershipRepository.insertNewMembership(application.organizationId, userid);

        Document response = new SuccessResponse();
        response.append("detail", application);
        return response;
    }
}
