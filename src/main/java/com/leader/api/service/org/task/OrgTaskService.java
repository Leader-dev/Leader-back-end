package com.leader.api.service.org.task;

import com.leader.api.data.org.member.OrgMemberInfoOverview;
import com.leader.api.data.org.task.*;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.leader.api.data.org.task.OrgTaskSubmission.*;

@Service
public class OrgTaskService {
    private final OrgTaskRepository taskRepository;
    private final OrgTaskSubmissionRepository taskSubmissionRepository;
    private final DateUtil dateUtil;

    @Autowired
    public OrgTaskService(OrgTaskRepository taskRepository, OrgTaskSubmissionRepository taskSubmissionRepository, DateUtil dateUtil) {
        this.taskRepository = taskRepository;
        this.taskSubmissionRepository = taskSubmissionRepository;
        this.dateUtil = dateUtil;
    }


    // ====================== Task Service =========================== //
    public List<OrgTaskSubmissionUserOverview> listReceivedTasks(ObjectId userId) {
        return taskSubmissionRepository.lookupSubmissionsByUserId(userId);
    }

    public OrgTaskDetail getTaskDetail(ObjectId userId, ObjectId taskId) {
        return taskSubmissionRepository.lookupDetailByMemberIdAndTaskId(userId, taskId);
    }

    private void updateSubmissionCount (ObjectId taskId) {
        taskRepository.findById(taskId).ifPresent( task -> {
            final long notSubmittedCount = taskSubmissionRepository.countByTaskIdAndStatus(taskId, NOT_SUBMITTED);
            final long pendingCount = taskSubmissionRepository.countByTaskIdAndStatus(taskId, PENDING);
            final long passedCount = taskSubmissionRepository.countByTaskIdAndStatus(taskId, PASSED);
            final long rejectedCount = taskSubmissionRepository.countByTaskIdAndStatus(taskId, REJECTED);
            final long cancelledCount = taskSubmissionRepository.countByTaskIdAndStatus(taskId, CANCELLED);

            final long caseClosed = passedCount + cancelledCount + pendingCount;
            final long caseNotClosed = notSubmittedCount + rejectedCount;

            task.submittedCount = caseClosed;
            task.notSubmittedCount = caseNotClosed;

            taskRepository.save(task);
        });
    }
    // New Task Service
    private void copyValidItemsTo(OrgTask task, OrgTaskBasicCreate basicCreate) {
        task.title = basicCreate.title;
        task.description = basicCreate.description;
        task.coverUrl = basicCreate.coverUrl;
        task.imageUrls = basicCreate.imageUrls;
    }

    private OrgTask insertNewTask(ObjectId senderMemberId, OrgTaskBasicCreate basicInfo) {
        OrgTask thisTask = new OrgTask();
        thisTask.publishUserId = senderMemberId;
        thisTask.publicationDate = dateUtil.getCurrentDate();

        // Statistical update


        copyValidItemsTo(thisTask, basicInfo);
        return taskRepository.insert(thisTask);
    }

    public void sendTask(ObjectId senderMemberId, List<ObjectId> receiverMemberIds, OrgTaskBasicCreate basicInfo) {
        // Validity check
        dateUtil.assertDateIsAfterNow(basicInfo.dueDate);

        // First, create a new task and gets its taskId
        ObjectId taskId = insertNewTask(senderMemberId, basicInfo).id;

        // Then, create corresponding records
        for (ObjectId receiverMemberId: receiverMemberIds) {
            insertNewSubmission(receiverMemberId, taskId);
        }

        // Update statistical data
        updateSubmissionCount(taskId);
    }

    public List<OrgTaskOverview> listSentTasks (List<ObjectId> managableIds) {
        //return taskRepository.findByPublishUserIdIsIn(managableIds, OrgTaskOverview.class);
        ArrayList<OrgTaskOverview> allOverviews = new ArrayList<>();

        for (ObjectId managableId : managableIds) {
            allOverviews.addAll(taskRepository.lookupByPublishUserId(managableId));
        }

        return allOverviews;
    }

    // ====================== Submission Service =========================== //
    private void insertNewSubmission(ObjectId submitMemberId, ObjectId taskId) {
        OrgTaskSubmission thisSubmission = new OrgTaskSubmission();
        thisSubmission.memberId = submitMemberId;
        thisSubmission.taskId = taskId;
        thisSubmission.status = NOT_SUBMITTED;
        thisSubmission.submissionAttempts = new ArrayList<>();
        taskSubmissionRepository.insert(thisSubmission);
    }

    public void submitTask (ObjectId memberId, ObjectId taskId, String submissionDescription, ArrayList<String> submissionImageUrls) {
        OrgTaskSubmission thisSubmission = taskSubmissionRepository.findByMemberIdAndTaskId(memberId, taskId);

        // Validation
        if (!thisSubmission.status.equals(NOT_SUBMITTED) && !thisSubmission.status.equals(REJECTED)) {
            throw new InternalErrorException("Error: Invalid state! Old state states task has already been closed!");
        }

        // Add submission record
        thisSubmission.status = PENDING;

        OrgTaskSubmissionAttempt thisSubmissionAttempt = new OrgTaskSubmissionAttempt();
        thisSubmissionAttempt.submissionDate = dateUtil.getCurrentDate();
        thisSubmissionAttempt.submissionDescription = submissionDescription;
        thisSubmissionAttempt.submissionImageUrls = submissionImageUrls;

        thisSubmission.submissionAttempts.add(thisSubmissionAttempt);
        taskSubmissionRepository.save(thisSubmission);

        // Update task statistical data
        updateSubmissionCount(taskId);
    }

    public void replyToSubmission (ObjectId memberId, ObjectId taskId, ObjectId reviewPersonId, String newStatus, String reviewNote) {

        OrgTaskSubmission thisSubmission = taskSubmissionRepository.findByMemberIdAndTaskId(memberId, taskId);

        // Check previous task status
        if (!thisSubmission.status.equals(PENDING)) {
            throw new InternalErrorException("Error: Invalid state! This task does not needs to be replied!");
        }

        // Update task status
        if (newStatus.equals(PASSED)) {thisSubmission.status = PASSED; }
        else if (newStatus.equals(REJECTED)) {thisSubmission.status = REJECTED;}
        else {
            throw new InternalErrorException("Error: Invalid state! New state must be either passed or rejected.");
        }

        // Update reply to the submission attempt record
        int submissionPointer = thisSubmission.submissionAttempts.size() - 1;
        OrgTaskSubmissionAttempt thisSubmissionAttempt = thisSubmission.submissionAttempts.get(submissionPointer);

        thisSubmissionAttempt.reviewDate = dateUtil.getCurrentDate();
        thisSubmissionAttempt.reviewNote = reviewNote;
        thisSubmissionAttempt.reviewPersonId = reviewPersonId;

        thisSubmission.submissionAttempts.set(submissionPointer, thisSubmissionAttempt);
        taskSubmissionRepository.save(thisSubmission);

        // Update task statistical data
        updateSubmissionCount(taskId);
    }

    public void cancelTask (ObjectId taskId) {
        List<OrgTaskSubmission> taskSubmissions = taskSubmissionRepository.findAllByTaskId(taskId);
        for (OrgTaskSubmission thisSubmission : taskSubmissions) {
            thisSubmission.status = CANCELLED;
            taskSubmissionRepository.save(thisSubmission);
        }
        updateSubmissionCount(taskId);
    }

    public void changeDueDate (ObjectId taskId, Date newDate){
        dateUtil.assertDateIsAfterNow(newDate);
        OrgTask task = taskRepository.findById(taskId).orElse(null);
        assert task != null;

        task.dueDate = newDate;
        taskRepository.save(task);
    }

    public void changeDescription (ObjectId taskId, String newDescription){
        OrgTask task = taskRepository.findById(taskId).orElse(null);
        assert task != null;

        task.description = newDescription;
        taskRepository.save(task);
    }

    public List<OrgTaskSubmissionAdminOverview> listByStatus (ObjectId taskId, String status) {
        List<OrgTaskSubmissionAdminOverview> result = taskSubmissionRepository.lookupByTaskIdAndStatus(taskId, status);
        for (OrgTaskSubmissionAdminOverview thisResult : result) {
            thisResult.calculateLatestSubmission();
        }
        return result;
    }

    public List<OrgTaskSubmissionAdminOverview> listByStatuses (ObjectId taskId, String[] statuses) {
        ArrayList<OrgTaskSubmissionAdminOverview> thisList = new ArrayList<>();
        for (String status: statuses){
            List<OrgTaskSubmissionAdminOverview> thatList = listByStatus(taskId, status);
            thisList.addAll(thatList);
        }
        return thisList;

    }

    public OrgTask getTask(ObjectId taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }
}
