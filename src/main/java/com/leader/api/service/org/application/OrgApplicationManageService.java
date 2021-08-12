package com.leader.api.service.org.application;

import com.leader.api.data.org.application.OrgApplication;
import com.leader.api.data.org.application.OrgApplicationReceivedDetail;
import com.leader.api.data.org.application.OrgApplicationReceivedOverview;
import com.leader.api.data.org.application.OrgApplicationRepository;
import com.leader.api.data.org.application.notification.OrgApplicationNotification;
import com.leader.api.data.org.application.notification.OrgApplicationNotificationRepository;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.service.org.authorization.OrgRoleService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.leader.api.data.org.application.OrgApplication.*;
import static com.leader.api.data.org.member.OrgMemberRole.*;

@Service
public class OrgApplicationManageService {

    private final OrgRoleService roleService;
    private final OrgApplicationRepository applicationRepository;
    private final OrgApplicationNotificationRepository notificationRepository;
    private final OrgMemberRepository memberRepository;
    private final DateUtil dateUtil;

    public enum ApplicationResult {
        PASS,
        REJECT
    }

    @Autowired
    public OrgApplicationManageService(OrgRoleService roleService, OrgApplicationRepository applicationRepository,
                                       OrgApplicationNotificationRepository notificationRepository, OrgMemberRepository memberRepository,
                                       DateUtil dateUtil) {
        this.roleService = roleService;
        this.applicationRepository = applicationRepository;
        this.notificationRepository = notificationRepository;
        this.memberRepository = memberRepository;
        this.dateUtil = dateUtil;
    }

    private List<ObjectId> getRecruitDepartments(ObjectId memberId) {
        return roleService.findAllRoleDepartmentIds(memberId, RECRUIT_MANAGER);
    }

    private List<ObjectId> getManageableDepartments(ObjectId memberId) {
        return roleService.findAllRoleDepartmentIds(memberId, RECRUIT_MANAGER, DEPARTMENT_MANAGER);
    }

    private void copyValidItemsTo(OrgApplicationNotification target, OrgApplicationNotification source) {
        target.title = source.title;
        target.content = source.content;
        target.imageUrls = source.imageUrls;
    }

    private OrgApplication findApplication(ObjectId applicationId) {
        return applicationRepository.findById(applicationId).orElse(null);
    }

    private OrgMember findMember(ObjectId memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

    private ObjectId findOrgIdOfMember(ObjectId memberId) {
        return findMember(memberId).orgId;
    }

    public void assertCanManageApplication(ObjectId memberId, ObjectId applicationId) {
        OrgApplication application = applicationRepository.findById(applicationId).orElseThrow(
                () -> new InternalErrorException("Invalid application."));
        if (roleService.hasRole(memberId, PRESIDENT)) {
            return;
        }
        if (getRecruitDepartments(memberId).contains(application.departmentId)) {
            return;
        }
        throw new InternalErrorException("Invalid application.");
    }

    public void assertCanSeeApplication(ObjectId memberId, ObjectId applicationId) {
        OrgApplication application = applicationRepository.findById(applicationId).orElseThrow(
                () -> new InternalErrorException("Invalid application."));
        if (roleService.hasRole(memberId, PRESIDENT, GENERAL_MANAGER)) {
            return;
        }
        if (getManageableDepartments(memberId).contains(application.departmentId)) {
            return;
        }
        throw new InternalErrorException("Invalid application.");
    }

    public List<OrgApplicationReceivedOverview> listReceived(ObjectId memberId) {
        if (roleService.hasRole(memberId, PRESIDENT)) {
            ObjectId orgId = findOrgIdOfMember(memberId);
            return applicationRepository.lookupByOrgIdAndStatus(orgId, PENDING);
        }
        List<ObjectId> departmentIds = getRecruitDepartments(memberId);
        return applicationRepository.lookupByDepartmentIdInAndStatus(departmentIds, PENDING);
    }

    public OrgApplicationReceivedDetail getDetail(ObjectId applicationId) {
        return applicationRepository.lookupByIdIncludeUserInfo(applicationId);
    }

    public OrgApplicationNotification getNotificationDetail(ObjectId notificationId) {
        return notificationRepository.findById(notificationId).orElse(null);
    }

    public ObjectId getApplicationIdOfNotification(ObjectId notificationId) {
        OrgApplicationNotification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return null;
        }
        return notification.applicationId;
    }

    public void sendNotification(ObjectId applicationId, OrgApplicationNotification notification) {
        OrgApplicationNotification newNotification = new OrgApplicationNotification();
        copyValidItemsTo(newNotification, notification);
        newNotification.applicationId = applicationId;
        newNotification.sendDate = dateUtil.getCurrentDate();
        newNotification.unread = true;
        notificationRepository.insert(newNotification);
    }

    public void sendResult(ObjectId memberId, ObjectId applicationId, ApplicationResult result) {
        OrgApplication application = findApplication(applicationId);
        if (result == ApplicationResult.PASS) {
            application.status = PASSED;
        } else if (result == ApplicationResult.REJECT) {
            application.status = REJECTED;
        } else {
            throw new InternalErrorException("Invalid result.");
        }
        application.operateMemberId = memberId;
        application.operateDate = dateUtil.getCurrentDate();
        applicationRepository.save(application);
    }

    public List<OrgApplicationReceivedOverview> listOperated(ObjectId memberId) {
        if (roleService.hasRole(memberId, PRESIDENT, GENERAL_MANAGER)) {
            ObjectId orgId = findOrgIdOfMember(memberId);
            return applicationRepository.lookupByOrgIdAndStatus(orgId, PASSED, REJECTED, ACCEPTED, DECLINED);
        }
        List<ObjectId> departmentIds = getManageableDepartments(memberId);
        return applicationRepository.lookupByDepartmentIdInAndStatus(departmentIds, PASSED, REJECTED, ACCEPTED, DECLINED);
    }
}
