package com.leader.api.controller.org.manage;


import com.leader.api.data.org.attendance.OrgLeave;
import com.leader.api.data.org.attendance.OrgLeaveDetail;
import com.leader.api.data.org.attendance.OrgLeaveUserOverview;
import com.leader.api.service.org.attendance.OrgLeaveService;
import com.leader.api.service.org.authorization.OrgAuthority;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.service.ImageService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/org/manage/leave")
public class OrgLeaveController {

    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService orgMemberIdService;
    private final OrgLeaveService orgLeaveService;
    private final ImageService imageService;

    public OrgLeaveController(OrgAuthorizationService authorizationService, OrgMemberIdService orgMemberIdService, OrgLeaveService orgLeaveService, ImageService imageService) {
        this.authorizationService = authorizationService;
        this.orgMemberIdService = orgMemberIdService;
        this.orgLeaveService = orgLeaveService;
        this.imageService = imageService;
    }

    public static class QueryObject{
    // -> newLeave
        public String leaveTitle;
        public String leaveType;
        public String leaveDetail;
        public Date leaveStartDate;
        public Date leaveEndDate;
        public ArrayList<String> leaveImageUrls;
        public ObjectId orgId;
    // -> viewList
        public String status;
    // -> viewDetail
        public ObjectId applicationId;
    // -> replyLeave
        public ObjectId applicationMemberId;
        public String reviewNote;
    }



    @PostMapping("/new-leave")
    public Document newLeave (@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.BASIC);

        ObjectId memberId = orgMemberIdService.getCurrentMemberId();
        String leaveTitle = queryObject.leaveTitle;
        String leaveType = queryObject.leaveType;
        String leaveDetail = queryObject.leaveDetail;
        Date leaveStartDate = queryObject.leaveStartDate;
        Date leaveEndDate = queryObject.leaveEndDate;
        ArrayList<String> leaveImageUrls = queryObject.leaveImageUrls;

        imageService.assertUploadedTempImages(leaveImageUrls);

        orgLeaveService.insertNewLeaveApplication(memberId, leaveTitle, leaveType, leaveDetail, leaveStartDate, leaveEndDate, leaveImageUrls);

        return new SuccessResponse();
    }

    @PostMapping("/view-list")
    public Document viewList (@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.BASIC);

        ObjectId memberId = orgMemberIdService.getCurrentMemberId();
        String status = queryObject.status;

        List<OrgLeaveUserOverview> leaveList = orgLeaveService.queryApplication(memberId, status);

        Document response = new SuccessResponse();
        response.append("list", leaveList);
        return response;
    }

    @PostMapping("/detail")
    public Document viewDetail(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.BASIC);

        ObjectId memberId = orgMemberIdService.getCurrentMemberId();
        ObjectId applicationId = queryObject.applicationId;

        OrgLeaveDetail leaveDetail = orgLeaveService.getDetail(applicationId, memberId);

        Document response = new SuccessResponse();
        response.append("detail", leaveDetail);
        return response;
    }

    @PostMapping("/list-pending")
    public Document listPending () {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        List<ObjectId> manageableMembers = authorizationService.listManageableMemberIdsOfCurrentMember();
        List<OrgLeaveUserOverview> overviews = orgLeaveService.listByStatus(manageableMembers, OrgLeave.PENDING);

        Document response = new SuccessResponse();
        response.append("list", overviews);
        return response;
    }

    @PostMapping("/list-processed")
    public Document listProcessed () {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        String[] processesStatuses = {OrgLeave.APPROVED, OrgLeave.REJECTED};
        List<ObjectId> manageableMembers = authorizationService.listManageableMemberIdsOfCurrentMember();
        List<OrgLeaveUserOverview> overviews = orgLeaveService.listByStatuses(manageableMembers, processesStatuses);

        Document response = new SuccessResponse();
        response.append("list", overviews);
        return response;
    }

    @PostMapping("/view-submission")
    public Document viewSubmission (@RequestBody QueryObject queryObject) {
        // Admin's version of '/detail'
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        ObjectId applicationMemberId = queryObject.applicationMemberId;
        ObjectId applicationId = queryObject.applicationId;
        authorizationService.assertCurrentMemberCanManageMember(applicationMemberId);

        OrgLeaveDetail leaveDetail = orgLeaveService.getDetail(applicationId, applicationMemberId);

        Document response = new SuccessResponse();
        response.append("detail", leaveDetail);
        return response;
    }

    @PostMapping("/reply-leave")
    public Document replyLeave (@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        ObjectId applicationMemberId = queryObject.applicationMemberId;
        authorizationService.assertCurrentMemberCanManageMember(applicationMemberId);
        // No need to assert the person can manage this task

        ObjectId reviewMemberId = orgMemberIdService.getCurrentMemberId();
        String reviewNote = queryObject.reviewNote;
        String reviewStatus = queryObject.status;
        ObjectId applicationId = queryObject.applicationId;

        orgLeaveService.replyToApplication(applicationId, applicationMemberId, reviewMemberId, reviewStatus, reviewNote);

        return new SuccessResponse();
    }



}
