package com.leader.api.service.attendance;

import com.leader.api.data.org.attendance.*;
import com.leader.api.data.org.member.OrgMemberInfo;
import com.leader.api.service.org.member.OrgMemberInfoService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class OrgAttendanceService {
    private final OrgAttendanceRepository orgAttendanceRepository;
    private final OrgAttendanceSubmissionRepository attendanceSubmissionRepository;
    private final DateUtil dateUtil;
    private final OrgMemberInfoService orgMemberInfoService;

    @Autowired
    public OrgAttendanceService(OrgAttendanceRepository attendanceRepository,
                                OrgAttendanceSubmissionRepository attendanceSubmissionRepository,
                                DateUtil dateUtil, OrgMemberInfoService memberInfoService) {
        this.orgAttendanceRepository = attendanceRepository;
        this.attendanceSubmissionRepository = attendanceSubmissionRepository;
        this.dateUtil = dateUtil;
        this.orgMemberInfoService = memberInfoService;
    }


    public ObjectId getInitializedMemberId(ObjectId attendanceId){
        OrgAttendance thisAttendance = orgAttendanceRepository.findById(attendanceId).orElse(null);
        if (thisAttendance==null) {throw new InternalErrorException("Attendance record does not exist!");}
        return thisAttendance.initializedMemberId;

    }

    public OrgAttendance getAttendanceRecordByAttendanceId (ObjectId attendanceId) {
        OrgAttendance thisAttendance = orgAttendanceRepository.findById(attendanceId).orElse(null);
        if (thisAttendance==null) {throw new InternalErrorException("Attendance record does not exist!");}
        return thisAttendance;
    }


    public List<OrgMemberInfo> generateManageableList (List<ObjectId> manageableMemberIds) {
        ArrayList<OrgMemberInfo> allMemberInfo = new ArrayList<>();
        for (ObjectId thisMemberId : manageableMemberIds) {
            allMemberInfo.add(orgMemberInfoService.getMemberInfo(thisMemberId));
        }
        return allMemberInfo;
    }

    public ObjectId insertNewAttendanceRecord (ObjectId initializedMember, String title, Date attendanceDate,
                                           List<ObjectId> toMemberIds){
        OrgAttendance thisAttendanceRecord = new OrgAttendance();
        thisAttendanceRecord.initializedMemberId = initializedMember;
        thisAttendanceRecord.title = title;

        dateUtil.assertDateIsAfterNow(attendanceDate);
        thisAttendanceRecord.attendanceDate = attendanceDate;
        thisAttendanceRecord.createdDate = dateUtil.getCurrentDate();

        thisAttendanceRecord.toMemberIds = toMemberIds;
        thisAttendanceRecord.headCount = toMemberIds.size();
        return orgAttendanceRepository.insert(thisAttendanceRecord).id;
    }

    public void insertNewAttendanceSubmission (OrgAttendanceSubmission thisSubmission) {
        attendanceSubmissionRepository.insert(thisSubmission);
    }

    public void insertMultipleNewAttendanceSubmissions (ObjectId attendanceEventId, List<OrgAttendanceSubmission> orgAttendanceSubmissions) {
        for (OrgAttendanceSubmission thisSubmission : orgAttendanceSubmissions){
            thisSubmission.attendanceEventId = attendanceEventId;
            insertNewAttendanceSubmission(thisSubmission);
        }
    }

    public ArrayList<OrgAttendanceOverview> listPublishedAttendances(List<ObjectId> manageableMemberIds) {
        ArrayList<OrgAttendanceOverview> attendanceOverviews = new ArrayList<>();

        for (ObjectId memberId : manageableMemberIds) {
            attendanceOverviews.addAll(orgAttendanceRepository.lookUpByInitializedMemberId(memberId));
        }
        return attendanceOverviews;
    }

    public List<OrgAttendanceSubmissionDetail> listAllSingleRecords(ObjectId attendanceId){
        return attendanceSubmissionRepository.lookUpByAttendanceId(attendanceId);
    }

    public void amendRecord (ObjectId attendanceId, ObjectId memberId, String newStatus) {
        if (!Arrays.asList(OrgAttendanceSubmission.ACCEPTED_STATES).contains(newStatus)) {
            throw new InternalErrorException("!Invalid state!");
        }
        OrgAttendanceSubmission thisSubmission = attendanceSubmissionRepository.findByAttendanceEventIdAndMemberId(attendanceId, memberId);
        thisSubmission.status = newStatus;
        attendanceSubmissionRepository.save(thisSubmission);
    }

    public void amendRecords (ObjectId attendanceId, List<OrgAttendanceSubmission> attendanceRecords) {
        for (OrgAttendanceSubmission submission : attendanceRecords) {
            amendRecord(attendanceId, submission.memberId, submission.status);
        }
    }

    public List<OrgAttendanceSubmissionUserView> findMyAttendanceRecords (ObjectId memberId) {
        return attendanceSubmissionRepository.lookupMyRecordsByMemberId(memberId);
    }

}
