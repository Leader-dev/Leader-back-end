package com.leader.api.controller.org.manage;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import com.leader.api.data.org.task.*;
import com.leader.api.service.org.authorization.OrgAuthority;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.task.OrgTaskService;
import com.leader.api.service.service.ImageService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/org/manage/tasks")
public class OrgTasksController {

    private final OrgAuthorizationService authorizationService;

    private final OrgMemberIdService memberIdService;
    private final OrgTaskService orgTaskService;

    private final ImageService imageService;

    public OrgTasksController(OrgAuthorizationService authorizationService, OrgMemberIdService memberIdService, OrgTaskService orgTaskService, ImageService imageService) {
        this.authorizationService = authorizationService;
        this.memberIdService = memberIdService;
        this.orgTaskService = orgTaskService;
        this.imageService = imageService;
    }

    public static class QueryObject {
        public ObjectId taskId;

        // -> publishTask
        public List<ObjectId> toMemberIds;
        public OrgTaskBasicCreate taskInfo;

        // -> submitTaskResult

        public String submissionDescription;
        public ArrayList<String> submissionImageUrls;

        // -> replyToSubmission
        public ObjectId toMemberId;
        public String newStatus;
        public String reviewNote;

        // -> Admin Maintenence Service
        public String status;
    }

    private void assertCurrentMemberCanManageThisTask(ObjectId taskId){
        // Authorization check 1: Can manage tasks
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.TASK);
        // Authorization check 2: Can manage this task
        ObjectId publisherId = orgTaskService.getTask(taskId).publishUserId;
        authorizationService.assertCurrentMemberCanManageMember(publisherId);
    }

    @PostMapping("/list-received")
    public Document listTasks() {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        List<OrgTaskSubmissionUserOverview> taskOverviews = orgTaskService.listReceivedTasks(memberId);

        Document response = new SuccessResponse();
        response.append("list", taskOverviews);
        return response;
    }

    @PostMapping("/detail")
    public Document taskDetail(@RequestBody QueryObject queryObject) {
        // For user to check own task detail; For manager to check use below function viewSubmission
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        ObjectId taskId = queryObject.taskId;

        OrgTaskDetail task = orgTaskService.getTaskDetail(memberId, taskId);

        Document response = new SuccessResponse();
        response.append("detail", task);
        return response;
    }

    @PostMapping("/publish")
    public Document publishTask(@RequestBody QueryObject queryObject) {
        // Authority Check
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.TASK);
        authorizationService.assertCurrentMemberCanManageAllMembers(queryObject.toMemberIds);

        // Image upload Check
        imageService.assertUploadedTempImage(queryObject.taskInfo.coverUrl);
        imageService.assertUploadedTempImages(queryObject.taskInfo.imageUrls);

        orgTaskService.sendTask(memberIdService.getCurrentMemberId(), queryObject.toMemberIds, queryObject.taskInfo);

        return new SuccessResponse();

    }

    // ====================== Submission Service =========================== //



    @PostMapping("/submit")
    public Document submitTaskResult(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.BASIC);

        imageService.assertUploadedTempImages(queryObject.submissionImageUrls);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        ObjectId taskId = queryObject.taskId;

        orgTaskService.submitTask(memberId, taskId, queryObject.submissionDescription, queryObject.submissionImageUrls);

        return new SuccessResponse();
    }

    // ====================== Admin Maintenance Service =========================== //
    @PostMapping("/list-published")
    public Document listPublishedTasks() {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.TASK);
        List<ObjectId> manageableMembers = authorizationService.listManageableManagerIdsOfCurrentMember();
        List<OrgTaskOverview> tasks = orgTaskService.listSentTasks(manageableMembers); // Allows higher hierarchy to access

        Document response = new SuccessResponse();
        response.append("published-tasks", tasks);

        return response;
    }

    @PostMapping("/view-member-submission")
    public Document viewSubmission(@RequestBody QueryObject queryObject) {
        // Exactly the same as user query, except memberId is designated by the admin
        ObjectId taskId = queryObject.taskId;
        assertCurrentMemberCanManageThisTask(taskId);

        ObjectId memberId = queryObject.toMemberId;
        OrgTaskDetail task = orgTaskService.getTaskDetail(memberId, taskId);

        Document response = new SuccessResponse();
        response.append("detail", task);
        return response;
    }

    // Query according to status


    @PostMapping("/query-by-status")
    public Document queryByStatus(@RequestBody QueryObject queryObject) {
        ObjectId taskId = queryObject.taskId;
        assertCurrentMemberCanManageThisTask(taskId);

        String[] statusCollection = OrgTaskSubmission.getStatusCollection(queryObject.status);

        List<OrgTaskSubmissionAdminOverview> memberOverviews = orgTaskService.listByStatuses(taskId, statusCollection);

        Document response = new SuccessResponse();
        response.append("members", memberOverviews);
        return response;
    }

    @PostMapping("/reply-submission")
    public Document replyToSubmission(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(OrgAuthority.TASK);
        authorizationService.assertCurrentMemberCanManageMember(queryObject.toMemberId);

        ObjectId reviewPersonId = memberIdService.getCurrentMemberId();
        ObjectId taskId = queryObject.taskId;
        ObjectId toMemberId = queryObject.toMemberId;

        orgTaskService.replyToSubmission(toMemberId, taskId, reviewPersonId, queryObject.newStatus, queryObject.reviewNote);

        return new SuccessResponse();
    }

    @PostMapping("/cancel-task")
    public Document cancelTask(@RequestBody QueryObject queryObject){
        ObjectId taskId = queryObject.taskId;
        assertCurrentMemberCanManageThisTask(taskId);

        orgTaskService.cancelTask(taskId);
        return new SuccessResponse();
    }

    @PostMapping("/amend-task")
    public Document amendTask(@RequestBody QueryObject queryObject){
        ObjectId taskId = queryObject.taskId;
        assertCurrentMemberCanManageThisTask(taskId);

        OrgTaskBasicCreate amendments = queryObject.taskInfo;

        // Only supports amendments on due date and description
        if (amendments.title != null || amendments.coverUrl != null || amendments.imageUrls != null){
            throw new InternalErrorException("Not supported change! Only due date and description can be changed.");
        }
        if (amendments.dueDate != null){
            orgTaskService.changeDueDate(taskId, amendments.dueDate);
        }
        if (amendments.description != null){
            orgTaskService.changeDescription(taskId, amendments.description);
        }
        return new SuccessResponse();
    }
}
