package com.leader.api.service.org.attendance;

import com.leader.api.data.org.attendance.OrgLeave;
import com.leader.api.data.org.attendance.OrgLeaveDetail;
import com.leader.api.data.org.attendance.OrgLeaveRepository;
import com.leader.api.data.org.attendance.OrgLeaveUserOverview;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrgLeaveService {
    private final OrgLeaveRepository leaveRepository;
    private final DateUtil dateUtil;

    @Autowired
    public OrgLeaveService(OrgLeaveRepository leaveRepository, DateUtil dateUtil) {
        this.leaveRepository = leaveRepository;
        this.dateUtil = dateUtil;
    }

    public void insertNewLeaveApplication(ObjectId applicationMemberId, String leaveTitle, String leaveType,
                                          String leaveDetail, Date leaveStartDate, Date leaveEndDate, ArrayList<String> leaveImageUrls){
        OrgLeave newLeave = new OrgLeave();
        newLeave.applicationMemberId = applicationMemberId;

        if (!leaveType.equals(OrgLeave.SICK_LEAVE) && !leaveType.equals(OrgLeave.REASON_LEAVE)) {
            throw new InternalErrorException("Error: Invalid leave type!");
        }
        newLeave.leaveTitle = leaveTitle;
        newLeave.leaveType = leaveType;
        newLeave.leaveDetail = leaveDetail;

        // Date service
        dateUtil.assertDateIsAfterNow(leaveStartDate);
        newLeave.leaveStartDate = leaveStartDate;

        dateUtil.assertDateIsAfterNow(leaveEndDate);
        newLeave.leaveEndDate = leaveEndDate;

        newLeave.leaveImageUrls = leaveImageUrls;

        // Auto generated
        newLeave.submittedDate = dateUtil.getCurrentDate();
        newLeave.status = OrgLeave.PENDING;

        leaveRepository.insert(newLeave);
    }

    public List<OrgLeaveUserOverview> queryApplication(ObjectId applicationMemberId, String status) {
        if (status == null){
            return leaveRepository.lookUpByApplicationMemberId(applicationMemberId);
        }
        else {
            return leaveRepository.lookUpByApplicationMemberIdAndStatus(applicationMemberId, status);
        }
    }

    public OrgLeaveDetail getDetail (ObjectId leaveId, ObjectId applicationMemberId) {
        return leaveRepository.lookUpByIdAndApplicationMemberId(leaveId, applicationMemberId);
    }

    public List<OrgLeaveUserOverview> listByStatus (List<ObjectId> manageableUserIds, String status) {
        ArrayList<OrgLeaveUserOverview> allOverviews = new ArrayList<>();
        for (ObjectId thisUserId : manageableUserIds) {
            allOverviews.addAll(leaveRepository.lookUpByApplicationMemberIdAndStatus(thisUserId, status));
        }
        return allOverviews;
    }

    public List<OrgLeaveUserOverview> listByStatuses (List<ObjectId> manageableUserIds, String[] statuses) {
        ArrayList<OrgLeaveUserOverview> allOverviews = new ArrayList<>();
        for (String status : statuses) {
            allOverviews.addAll(listByStatus(manageableUserIds, status));

        }
        return allOverviews;
    }

    public void replyToApplication (ObjectId leaveId, ObjectId applicationMemberId, ObjectId reviewMemberId,
                                    String reviewStatus, String reviewNote) {
        OrgLeave thisLeave = leaveRepository.findByIdAndApplicationMemberId(leaveId, applicationMemberId);
        if (thisLeave == null) {
            throw new InternalErrorException("Leave application does not exist!");
        }
        else if (!thisLeave.status.equals(OrgLeave.PENDING)) {
            throw new InternalErrorException("Invalid state! The task does not need to be reviewed!");
        }

        thisLeave.reviewMemberId = reviewMemberId;

        if (!reviewStatus.equals(OrgLeave.APPROVED) && !reviewStatus.equals(OrgLeave.REJECTED)) {
            throw new InternalErrorException("Invalid state! New state must be either approved or rejected!");
        }
        thisLeave.status = reviewStatus;

        thisLeave.reviewDate = dateUtil.getCurrentDate();
        thisLeave.reviewNote = reviewNote;

        leaveRepository.save(thisLeave);
    }

}
