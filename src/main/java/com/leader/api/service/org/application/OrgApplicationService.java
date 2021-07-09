package com.leader.api.service.org.application;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.application.*;
import com.leader.api.data.org.application.notification.OrgApplicationNotificationRepository;
import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.department.OrgDepartmentRepository;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.member.OrgMemberService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.leader.api.data.org.application.OrgApplication.*;

@Service
public class OrgApplicationService {

    private final OrgDepartmentRepository departmentRepository;
    private final OrgApplicationRepository applicationRepository;
    private final OrgApplicationNotificationRepository notificationRepository;
    private final OrganizationService organizationService;
    private final OrgMemberService membershipService;
    private final OrgStructureService structureService;
    private final DateUtil dateUtil;

    public enum ReplyAction {
        ACCEPT,
        DECLINE
    }

    @Autowired
    public OrgApplicationService(OrgDepartmentRepository departmentRepository,
                                 OrgApplicationRepository applicationRepository,
                                 OrgApplicationNotificationRepository notificationRepository,
                                 OrganizationService organizationService,
                                 OrgMemberService membershipService,
                                 OrgStructureService structureService,
                                 DateUtil dateUtil) {
        this.departmentRepository = departmentRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
        this.organizationService = organizationService;
        this.membershipService = membershipService;
        this.structureService = structureService;
        this.dateUtil = dateUtil;
    }

    private static boolean compareQuestions(OrgApplicationForm applicationForm, List<String> questions) {
        if (applicationForm.size() != questions.size()) {
            return false;
        }
        for (int i = 0; i < questions.size(); i++) {
            if (!applicationForm.get(i).question.equals(questions.get(i))) {
                return false;
            }
        }
        return true;
    }

    public void sendApplication(ObjectId userid, ObjectId orgId, String name, ObjectId departmentId, OrgApplicationForm applicationForm) {
        organizationService.assertOrganizationExists(orgId);
        if (membershipService.isMember(orgId, userid)) {
            throw new InternalErrorException("User already in organization.");
        }

        Organization organization = organizationService.getOrganization(orgId);
        if (!organization.applicationScheme.open) {
            throw new InternalErrorException("Application not open.");
        }

        if (organization.applicationScheme.maximumApplication != -1 &&
                applicationRepository.countByOrgIdAndStatus(orgId, PENDING) >= organization.applicationScheme.maximumApplication) {
            throw new InternalErrorException("Application full.");
        }

        if (name == null || !compareQuestions(applicationForm, organization.applicationScheme.questions)) {
            throw new InternalErrorException("Invalid application questions.");
        }

        OrgDepartment department = null;
        if (organization.applicationScheme.appointDepartment) {
            if (departmentId == null) {
                throw new InternalErrorException("Department not appointed.");
            }
            department = departmentRepository.findById(departmentId).orElse(null);
            if (department == null) {
                throw new InternalErrorException("Invalid department.");
            }
        }

        OrgApplication application = new OrgApplication();

        application.name = name;
        application.orgId = orgId;
        application.applicantUserId = userid;

        if (department != null) {
            application.departmentId = department.id;
        }

        application.applicationForm = applicationForm;
        application.sendDate = dateUtil.getCurrentDate();
        application.status = PENDING;

        applicationRepository.insert(application);
    }

    public List<OrgApplicationSentOverview> getSentApplications(ObjectId userid) {
        return applicationRepository.lookupByApplicantUserIdIncludeOrgInfo(userid, OrgApplicationSentOverview.class);
    }

    public OrgApplicationSentDetail getApplication(ObjectId userid, ObjectId applicationId) {
        OrgApplicationSentDetail detail = applicationRepository.lookupByIdIncludeOrgInfo(applicationId);
        if (!userid.equals(detail.applicantUserId)) {
            throw new InternalErrorException("Invalid application.");
        }
        return detail;
    }

    public void readNotification(ObjectId userid, ObjectId notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (!applicationRepository.existsByApplicantUserIdAndId(userid, notification.applicationId)) {
                throw new InternalErrorException("Invalid application.");
            }
            notification.unread = false;
            notificationRepository.save(notification);
        });
    }

    public void replyToApplication(ObjectId userid, ObjectId applicationId, ReplyAction action) {
        OrgApplication application =
                applicationRepository.findByApplicantUserIdAndId(userid, applicationId);

        if (application == null) {
            throw new InternalErrorException("Application not exist.");
        }

        if (!PASSED.equals(application.status)) {
            throw new InternalErrorException("Application not passed.");
        }

        if (ReplyAction.ACCEPT == action) {
            application.status = ACCEPTED;
            OrgMember member = membershipService.joinOrganization(application.orgId, userid, application.name);
            if (application.departmentId != null) {
                structureService.setMemberToMember(member.id, application.departmentId);
            }
        }
        if (ReplyAction.DECLINE == action) {
            application.status = DECLINED;
        }

        applicationRepository.save(application);
    }
}
