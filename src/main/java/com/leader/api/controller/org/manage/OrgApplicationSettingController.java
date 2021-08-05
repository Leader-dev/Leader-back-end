package com.leader.api.controller.org.manage;

import com.leader.api.data.org.OrgApplicationScheme;
import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.department.OrgDepartmentRecruitInfo;
import com.leader.api.data.org.member.OrgMemberOverview;
import com.leader.api.service.org.application.OrgApplicationSettingService;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.structure.OrgStructureService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.leader.api.service.org.authorization.OrgAuthority.RECRUIT_SETTING;
import static com.leader.api.util.response.SuccessResponse.success;

@RestController
@RequestMapping("/org/manage/apply/setting")
public class OrgApplicationSettingController {

    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService memberIdService;
    private final OrgApplicationSettingService settingService;
    private final OrgStructureService structureService;

    @Autowired
    public OrgApplicationSettingController(OrgAuthorizationService authorizationService, OrgMemberIdService memberIdService,
                                           OrgApplicationSettingService settingService, OrgStructureService structureService) {
        this.authorizationService = authorizationService;
        this.memberIdService = memberIdService;
        this.settingService = settingService;
        this.structureService = structureService;
    }

    public static class QueryObject {
        public OrgApplicationScheme scheme;
        public Boolean resetReceivedApplicationCount;
        public ObjectId departmentId;
        public ObjectId memberId;
    }

    @PostMapping("/get-scheme")
    public Document getApplicationScheme() {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT_SETTING);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        OrgApplicationScheme scheme = settingService.getApplicationScheme(orgId);
        int receivedApplicationCount = settingService.getReceivedApplicationCount(orgId);

        return success().data(
                "scheme", scheme,
                "receivedApplicationCount", receivedApplicationCount
        );
    }

    @PostMapping("/set-scheme")
    public Document setApplicationScheme(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT_SETTING);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        settingService.setApplicationScheme(orgId, queryObject.scheme);
        if (queryObject.resetReceivedApplicationCount != null && queryObject.resetReceivedApplicationCount) {
            settingService.resetReceivedApplicationCount(orgId);
        }

        return success();
    }

    @PostMapping("/get-recruit-manager-info")
    public Document getRecruitManagerId(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT_SETTING);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        OrgMemberOverview memberInfo = null;
        if (queryObject.departmentId != null) {
            ObjectId memberId = settingService.getDepartmentRecruitManagerId(queryObject.departmentId);
            if (memberId != null) {
                memberInfo = structureService.getMemberOverview(memberId);
            }
        }
        List<OrgDepartment> departments = structureService.listDepartments(orgId, queryObject.departmentId, OrgDepartment.class);
        List<OrgDepartmentRecruitInfo> departmentsInfo = settingService.getDepartmentsRecruitManagerInfo(departments);

        return success().data(
                "memberInfo", memberInfo,
                "departments", departmentsInfo
        );
    }

    @PostMapping("/set-recruit-manager")
    public Document setRecruitManager(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT_SETTING);

        settingService.setMemberToRecruitManager(queryObject.memberId, queryObject.departmentId);

        return success();
    }
}
