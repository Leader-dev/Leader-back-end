package com.leader.api.service.org.application;

import com.leader.api.data.org.OrgApplicationQuestion;
import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.application.*;
import com.leader.api.data.org.application.notification.OrgApplicationNotification;
import com.leader.api.data.org.application.notification.OrgApplicationNotificationRepository;
import com.leader.api.data.org.department.OrgDepartmentRepository;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.service.org.member.OrgMemberService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.leader.api.data.org.application.OrgApplication.*;

@Service
public class OrgApplicationService {

    public static final String CLOSED = "closed";
    public static final String FULL = "full";
    public static final String JOINED = "joined";
    public static final String APPLIED = "applied";
    public static final String AVAILABLE = "available";

    public static final List<String> ONGOING_STATUSES = Arrays.asList(PENDING, PASSED);

    private final OrgDepartmentRepository departmentRepository;
    private final OrgApplicationRepository applicationRepository;
    private final OrgApplicationNotificationRepository notificationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgMemberService memberService;
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
                                 OrganizationRepository organizationRepository,
                                 OrgMemberService memberService,
                                 OrgStructureService structureService,
                                 DateUtil dateUtil) {
        this.departmentRepository = departmentRepository;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
        this.organizationRepository = organizationRepository;
        this.memberService = memberService;
        this.structureService = structureService;
        this.dateUtil = dateUtil;
    }

    private static boolean checkQuestions(OrgApplicationForm applicationForm, List<OrgApplicationQuestion> questions) {
        if (questions == null) {
            return applicationForm == null || applicationForm.size() == 0;
        }
        if (applicationForm == null) {
            return questions.size() == 0;
        }
        if (applicationForm.size() != questions.size()) {
            return false;
        }
        for (int i = 0; i < questions.size(); i++) {
            if (!applicationForm.get(i).question.equals(questions.get(i).question)) {
                return false;
            }
            if (questions.get(i).required && applicationForm.get(i).answer.length() == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOngoingApplication(ObjectId orgId, ObjectId userId) {
        return applicationRepository.existsByOrgIdAndApplicantUserIdAndStatusIn(orgId, userId, ONGOING_STATUSES);
    }

    private Organization getOrganization(ObjectId orgId) {
        return organizationRepository.findById(orgId).orElse(null);
    }

    public String getApplicationEntranceStatus(ObjectId orgId, ObjectId userId) {
        Organization organization = getOrganization(orgId);
        if (!organization.applicationScheme.open) {
            return CLOSED;
        }
        if (organization.applicationScheme.maximumApplication != -1 &&
                organization.receivedApplicationCount >= organization.applicationScheme.maximumApplication) {
            return FULL;
        }
        if (memberService.isMember(orgId, userId)) {
            return JOINED;
        }
        if (hasOngoingApplication(orgId, userId)) {
            return APPLIED;
        }
        return AVAILABLE;
    }

    public void sendApplication(ObjectId orgId, ObjectId userId, String name, ObjectId departmentId, OrgApplicationForm applicationForm) {
        String entranceStatus = getApplicationEntranceStatus(orgId, userId);
        if (!AVAILABLE.equals(entranceStatus)) {
            throw new InternalErrorException("Application not available.");
        }

        Organization organization = getOrganization(orgId);
        if (name == null || !checkQuestions(applicationForm, organization.applicationScheme.questions)) {
            throw new InternalErrorException("Invalid application questions.");
        }

        if (organization.applicationScheme.appointDepartment) {
            if (departmentId == null) {
                throw new InternalErrorException("Department not appointed.");
            }
            if (!departmentRepository.existsById(departmentId)) {
                throw new InternalErrorException("Invalid department.");
            }
        }

        OrgApplication application = new OrgApplication();

        application.name = name;
        application.orgId = orgId;
        application.applicantUserId = userId;
        application.departmentId = departmentId;
        application.applicationForm = applicationForm;
        application.sendDate = dateUtil.getCurrentDate();
        application.status = PENDING;

        applicationRepository.insert(application);

        // increase counter by 1
        organization.receivedApplicationCount++;
        organizationRepository.save(organization);
    }

    public List<OrgApplicationSentOverview> getSentApplications(ObjectId userId) {
        return applicationRepository.lookupByApplicantUserIdIncludeOrgInfo(userId, OrgApplicationSentOverview.class);
    }

    public OrgApplicationSentDetail getApplication(ObjectId userId, ObjectId applicationId) {
        OrgApplicationSentDetail detail = applicationRepository.lookupByIdIncludeOrgInfo(applicationId);
        if (!userId.equals(detail.applicantUserId)) {
            throw new InternalErrorException("Invalid application.");
        }
        return detail;
    }

    public OrgApplicationNotification getNotificationAndSetToRead(ObjectId userId, ObjectId notificationId) {
        OrgApplicationNotification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            throw new InternalErrorException("Invalid notification.");
        }
        if (!applicationRepository.existsByApplicantUserIdAndId(userId, notification.applicationId)) {
            throw new InternalErrorException("Invalid application.");
        }
        notification.unread = false;
        notificationRepository.save(notification);
        return notification;
    }

    public void replyToApplication(ObjectId userId, ObjectId applicationId, ReplyAction action) {
        OrgApplication application =
                applicationRepository.findByApplicantUserIdAndId(userId, applicationId);

        if (application == null) {
            throw new InternalErrorException("Application not exist.");
        }

        if (!PASSED.equals(application.status)) {
            throw new InternalErrorException("Application not passed.");
        }

        if (ReplyAction.ACCEPT == action) {
            application.status = ACCEPTED;
            OrgMember member = memberService.joinOrganization(application.orgId, userId, application.name);
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
