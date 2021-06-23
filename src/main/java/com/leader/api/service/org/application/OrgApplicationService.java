package com.leader.api.service.org.application;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.application.*;
import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.department.OrgDepartmentRepository;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.member.OrgMemberService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.leader.api.data.org.application.OrgApplication.*;

@Service
public class OrgApplicationService {

    private final OrgDepartmentRepository departmentRepository;
    private final OrgApplicationRepository applicationRepository;
    private final OrganizationService organizationService;
    private final OrgMemberService membershipService;

    public enum ReplyAction {
        ACCEPT,
        DECLINE
    }

    @Autowired
    public OrgApplicationService(OrgDepartmentRepository departmentRepository,
                                 OrgApplicationRepository applicationRepository,
                                 OrganizationService organizationService,
                                 OrgMemberService membershipService) {
        this.departmentRepository = departmentRepository;
        this.applicationRepository = applicationRepository;
        this.organizationService = organizationService;
        this.membershipService = membershipService;
    }

    public void sendApplication(ObjectId organizationId, ObjectId departmentId, OrgApplicationForm applicationForm, ObjectId userid) {
        organizationService.assertOrganizationExists(organizationId);
        Organization organization = organizationService.getOrganization(organizationId);
        if (!organization.applicationScheme.open) {
            throw new RuntimeException("Application not open");
        }
        OrgDepartment department = departmentRepository.findById(departmentId).orElse(null);
        if (organization.applicationScheme.appointDepartment && department == null) {
            throw new RuntimeException("Department not appointed");
        }

        if (organization.applicationScheme.auth) {
            OrgApplication application = new OrgApplication();

            application.orgId = organizationId;
            application.applicantUserId = userid;

            if (department != null) {
                application.departmentId = department.id;
            }

            application.applicationForm = applicationForm;
            application.timestamp = new Date();
            application.status = "pending";

            applicationRepository.insert(application);
        } else {
            membershipService.joinOrganization(organizationId, userid);
        }
    }

    public List<OrgApplicationSentOverview> getSentApplications(ObjectId userid) {
        return applicationRepository.findAllByApplicantUserIdIncludeInfo(userid, OrgApplicationSentOverview.class);
    }

    public OrgApplicationDetail getApplication(ObjectId userid, ObjectId applicationId) {
        return applicationRepository.findByApplicantUserIdAndIdIncludeInfo(
                userid, applicationId, OrgApplicationDetail.class);
    }

    public void replyToApplication(ObjectId userid, ObjectId applicationId, ReplyAction action) {
        OrgApplication application =
                applicationRepository.findByApplicantUserIdAndId(userid, applicationId);

        if (application == null) {
            throw new RuntimeException("Application not exist.");
        }

        if (PASSED.equals(application.status)) {
            throw new RuntimeException("Application not passed.");
        }

        if (ReplyAction.ACCEPT == action) {
            application.status = ACCEPTED;
            membershipService.joinOrganization(application.orgId, userid);
        }
        if (ReplyAction.DECLINE == action) {
            application.status = DECLINED;
        }

        applicationRepository.save(application);
    }
}
