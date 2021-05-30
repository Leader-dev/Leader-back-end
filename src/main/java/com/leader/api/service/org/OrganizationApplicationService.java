package com.leader.api.service.org;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.application.*;
import com.leader.api.data.org.manage.department.OrganizationDepartment;
import com.leader.api.data.org.manage.department.OrganizationDepartmentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrganizationApplicationService {

    @Autowired
    private OrganizationDepartmentRepository departmentRepository;

    @Autowired
    private OrganizationApplicationRepository applicationRepository;

    @Autowired
    private OrganizationService organizationService;

    public void sendApplication(ObjectId organizationId, ObjectId departmentId, OrganizationApplicationForm applicationForm, ObjectId userid) {
        organizationService.assertOrganizationExists(organizationId);
        Organization organization = organizationService.getOrganization(organizationId);
        if (!organization.applicationScheme.open) {
            throw new RuntimeException("Application not open");
        }
        OrganizationDepartment department = departmentRepository.findById(departmentId).orElse(null);
        if (organization.applicationScheme.appointDepartment && department == null) {
            throw new RuntimeException("Department not appointed");
        }

        if (organization.applicationScheme.auth) {
            OrganizationApplication application = new OrganizationApplication();

            application.organizationId = organizationId;
            application.applicantUserId = userid;

            if (department != null) {
                application.departmentId = department.id;
                application.auditUserId = department.applicationAuditUserId;
            }

            application.applicationForm = applicationForm;
            application.timestamp = new Date();
            application.status = "pending";

            applicationRepository.insert(application);
        } else {
            organizationService.joinOrganization(organizationId, userid);
        }
    }

    public List<OrganizationApplicationSentOverview> getSentApplications(ObjectId userid) {
        return applicationRepository.findAllByApplicantUserIdIncludeInfo(userid, OrganizationApplicationSentOverview.class);
    }

    public OrganizationApplicationDetail getApplication(ObjectId userid, ObjectId applicationId) {
        return applicationRepository.findByApplicantUserIdAndIdIncludeInfo(
                userid, applicationId, OrganizationApplicationDetail.class);
    }

    public void replyToApplication(ObjectId userid, ObjectId applicationId, String action) {
        OrganizationApplication application =
                applicationRepository.findByApplicantUserIdAndId(userid, applicationId);
        if ("passed".equals(application.status)) {
            throw new RuntimeException("Application not passed");
        }
        if ("accept".equals(action)) {
            application.status = "accepted";
        } else if ("decline".equals(action)) {
            application.status = "declined";
        } else {
            throw new RuntimeException("Invalid action");
        }
        applicationRepository.save(application);

        organizationService.joinOrganization(application.organizationId, userid);
    }
}
