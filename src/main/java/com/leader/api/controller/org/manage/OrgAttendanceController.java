package com.leader.api.controller.org.manage;

import com.leader.api.data.org.attendance.*;
import com.leader.api.data.org.member.OrgMemberInfo;
import com.leader.api.service.attendance.OrgAttendanceService;
import com.leader.api.service.org.authorization.OrgAuthority;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.leave.OrgLeaveService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/org/manage/attendance")
public class OrgAttendanceController {
    private final OrgAttendanceService orgAttendanceService;
    private final DateUtil dateUtil;
    private final OrgAuthorizationService orgAuthorizationService;
    private final OrgStructureService orgStructureService;
    private final OrgLeaveService leaveService;
    private final OrgMemberIdService memberIdService;

    @Autowired
    public OrgAttendanceController(OrgAttendanceService attendanceService, DateUtil dateUtil, OrgAuthorizationService authorizationService, OrgStructureService structureService, OrgLeaveService leaveService, OrgMemberIdService memberIdService) {
        this.orgAttendanceService = attendanceService;
        this.dateUtil = dateUtil;
        this.orgAuthorizationService = authorizationService;
        this.orgStructureService = structureService;
        this.leaveService = leaveService;
        this.memberIdService = memberIdService;
    }

    public static class QueryObject {
    // -> set-attendance
        public String title;
        public ObjectId orgId;
        public Date attendanceDate;
        public List<OrgAttendanceSubmission> attendanceRecords;

    // -> view-records
        public ObjectId attendanceId;

    }

    private void assertCurrentMemberCanManageThisAttendance(ObjectId attendanceId) {
        orgAuthorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        ObjectId initializedMemberId = orgAttendanceService.getInitializedMemberId(attendanceId);
        orgAuthorizationService.assertCurrentMemberCanManageMember(initializedMemberId);
    }

    @PostMapping("/manageable-list")
    public Document manageableList () {
        orgAuthorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        List<ObjectId> manageableMembers = orgAuthorizationService.listManageableMemberIdsOfCurrentMember();
        List<OrgMemberInfo> memberInfos = orgAttendanceService.generateManageableList(manageableMembers);

        Document response = new SuccessResponse();
        response.append("list", memberInfos);
        return response;
        // Todo add leave list
    }

    @PostMapping("/leave-list")
    public Document leaveList(@RequestBody QueryObject queryObject){
        orgAuthorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        List<ObjectId> manageableMemberIds = orgAuthorizationService.listManageableMemberIdsOfCurrentMember();

        Date attendanceDate = queryObject.attendanceDate;
        ArrayList <ObjectId> leaveList = leaveService.leaveList(manageableMemberIds, attendanceDate);

        Document response = new SuccessResponse();
        response.append("leaveList", leaveList);
        return response;
    }

    @PostMapping("/set-attendance")
    public Document setAttendance (@RequestBody QueryObject queryObject){
        // Authorization check
        orgAuthorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        List<OrgAttendanceSubmission> attendanceRecords = queryObject.attendanceRecords;
        List<ObjectId> toMemberIds = attendanceRecords.stream()
                .map(orgAttendanceSubmission -> orgAttendanceSubmission.memberId)
                .collect(Collectors.toList());
        orgAuthorizationService.assertCurrentMemberCanManageAllMembers(toMemberIds);

        // As there are two tasks, I put them in controller layer for better validation check
        // Data validation check
        for (OrgAttendanceSubmission thisRecord : attendanceRecords) {
            orgStructureService.assertMemberIsInDepartment(thisRecord.departmentId, thisRecord.memberId);
            if (!Arrays.asList(OrgAttendanceSubmission.ACCEPTED_STATES).contains(thisRecord.status)) {
                throw new InternalErrorException("Invalid state!");
            }
        }

        // Create new attendance event
        ObjectId initializedMember = memberIdService.getCurrentMemberId();
        String title = queryObject.title;
        Date attendanceDate = queryObject.attendanceDate;

        ObjectId attendanceEventId = orgAttendanceService.insertNewAttendanceRecord(initializedMember, title, attendanceDate, toMemberIds);

        // Insert attendance records
        orgAttendanceService.insertMultipleNewAttendanceSubmissions(attendanceEventId, attendanceRecords);

        return new SuccessResponse();
    }

    @PostMapping("/view-records")
    public Document viewRecords () {
        orgAuthorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        List<ObjectId> manageableMembers = orgAuthorizationService.listManageableMemberIdsOfCurrentMember();
        List<OrgAttendanceOverview> attendanceOverviews = orgAttendanceService.listPublishedAttendances(manageableMembers);

        Document response = new SuccessResponse();
        response.append("list", attendanceOverviews);
        return response;
    }

    @PostMapping("/view-detail")
    public Document viewDetail (@RequestBody QueryObject queryObject) {
        ObjectId attendanceId = queryObject.attendanceId;
        assertCurrentMemberCanManageThisAttendance(attendanceId);

        Document response = new SuccessResponse();
        OrgAttendance attendanceRecord = orgAttendanceService.getAttendanceRecordByAttendanceId(attendanceId);
        response.append("initializedMember", attendanceRecord.initializedMemberId);
        response.append("title", attendanceRecord.title);
        response.append("attendanceDate", attendanceRecord.attendanceDate);

        List<OrgAttendanceSubmissionDetail> attendanceRecords = orgAttendanceService.listAllSingleRecords(attendanceId);
        response.append("attendanceRecords", attendanceRecords);
        return response;
    }

    @PostMapping("/amend-record")
    public Document amendRecord (@RequestBody QueryObject queryObject) {
        orgAuthorizationService.assertCurrentMemberHasAuthority(OrgAuthority.ATTENDANCE);
        List<OrgAttendanceSubmission> attendanceRecords = queryObject.attendanceRecords;
        List<ObjectId> toMemberIds = attendanceRecords.stream()
                .map(orgAttendanceSubmission -> orgAttendanceSubmission.memberId)
                .collect(Collectors.toList());
        orgAuthorizationService.assertCurrentMemberCanManageAllMembers(toMemberIds);

        // I think there's no need to validate if user can manage the initializer,
        // as any member with the auth could change it?

        ObjectId attendanceId = queryObject.attendanceId;
        orgAttendanceService.amendRecords(attendanceId, attendanceRecords);

        return new SuccessResponse();

    }

    // == User interface == //
    @PostMapping("/my-records")
    public Document myRecords (){
        orgAuthorizationService.assertCurrentMemberHasAuthority(OrgAuthority.BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        List<OrgAttendanceSubmissionUserView> attendanceList = orgAttendanceService.findMyAttendanceRecords(memberId);

        Document response = new SuccessResponse();
        response.append("list", attendanceList);
        return response;
    }

}
