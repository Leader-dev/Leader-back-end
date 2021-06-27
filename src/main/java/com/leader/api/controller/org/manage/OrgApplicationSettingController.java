package com.leader.api.controller.org.manage;

import com.leader.api.data.org.OrgApplicationScheme;
import com.leader.api.data.org.department.OrgDepartment;
import com.leader.api.data.org.department.OrgDepartmentRecruitInfo;
import com.leader.api.service.org.application.OrgApplicationSettingService;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.leader.api.service.org.authorization.OrgAuthority.RECRUIT_SETTING;

@RestController
@RequestMapping("/org/manage/apply/setting")
public class OrgApplicationSettingController {

    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService memberIdService;
    private final OrgApplicationSettingService settingService;
    private final OrgStructureService structureService;

    public OrgApplicationSettingController(OrgAuthorizationService authorizationService, OrgMemberIdService memberIdService,
                                           OrgApplicationSettingService settingService, OrgStructureService structureService) {
        this.authorizationService = authorizationService;
        this.memberIdService = memberIdService;
        this.settingService = settingService;
        this.structureService = structureService;
    }

    public static class QueryObject {
        public OrgApplicationScheme scheme;
        public ObjectId departmentId;
        public ObjectId memberId;
    }

    @PostMapping("/get-scheme")
    public Document getApplicationScheme() {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT_SETTING);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        OrgApplicationScheme scheme = settingService.getApplicationScheme(orgId);

        Document response = new SuccessResponse();
        response.append("scheme", scheme);
        return response;
    }

    @PostMapping("/set-scheme")
    public Document setApplicationScheme(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT_SETTING);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        settingService.updateApplicationScheme(orgId, queryObject.scheme);

        return new SuccessResponse();
    }

    @PostMapping("/get-recruit-manager-info")
    public Document getRecruitManagerId(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT_SETTING);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        ObjectId memberId = null;
        if (queryObject.departmentId != null) {
            memberId = settingService.getDepartmentRecruitManagerId(queryObject.departmentId);
        }
        List<OrgDepartment> departments = structureService.listDepartments(orgId, queryObject.departmentId, OrgDepartment.class);
        List<OrgDepartmentRecruitInfo> departmentsInfo = settingService.getDepartmentsRecruitManagerInfo(departments);

        Document response = new SuccessResponse();
        response.append("memberId", memberId);
        response.append("departments", departmentsInfo);
        return response;
    }

    @PostMapping("/set-recruit-manager")
    public Document setRecruitManager(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT_SETTING);

        settingService.setMemberToRecruitManager(queryObject.memberId, queryObject.departmentId);

        return new SuccessResponse();
    }
}
