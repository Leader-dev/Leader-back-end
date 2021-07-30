package com.leader.api.controller.org.manage;

import com.leader.api.data.org.department.OrgDepartmentOverview;
import com.leader.api.data.org.member.OrgMemberInfo;
import com.leader.api.data.org.member.OrgMemberOverview;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.member.OrgMemberInfoService;
import com.leader.api.service.org.structure.OrgStructureService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.leader.api.service.org.authorization.OrgAuthority.BASIC;
import static com.leader.api.service.org.authorization.OrgAuthority.STRUCTURE_MANAGEMENT;

@RestController
@RequestMapping("/org/manage/structure")
public class OrgStructureController {

    private final OrgAuthorizationService authorizationService;
    private final OrgStructureService structureService;
    private final OrgMemberIdService memberIdService;
    private final OrgMemberInfoService memberInfoService;

    @Autowired
    public OrgStructureController(OrgAuthorizationService authorizationService,
                                  OrgStructureService structureService,
                                  OrgMemberIdService memberIdService,
                                  OrgMemberInfoService memberInfoService) {
        this.authorizationService = authorizationService;
        this.structureService = structureService;
        this.memberIdService = memberIdService;
        this.memberInfoService = memberInfoService;
    }

    public static class QueryObject {
        public ObjectId departmentId;
        public ObjectId parentId;
        public ObjectId userId;
        public ObjectId memberId;
        public String name;
        public String searchText;
        public String title;
    }

    @PostMapping("/list-members")
    public Document listMembers(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        List<OrgMemberOverview> members = structureService.listMembers(orgId, queryObject.departmentId);

        Document response = new SuccessResponse();
        response.append("members", members);
        return response;
    }

    @PostMapping("/list-departments")
    public Document listDepartments(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        List<OrgDepartmentOverview> departments =
                structureService.listDepartments(orgId, queryObject.parentId, OrgDepartmentOverview.class);

        Document response = new SuccessResponse();
        response.append("departments", departments);
        return response;
    }

    @PostMapping("/search")
    public Document searchMembers(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        List<OrgMemberOverview> members = structureService.searchMembers(orgId, queryObject.searchText);

        Document response = new SuccessResponse();
        response.append("members", members);
        return response;
    }

    @PostMapping("/member-info")
    public Document showMemberInfo(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        memberIdService.assertMemberInCurrentOrganization(queryObject.memberId);
        OrgMemberInfo info = memberInfoService.getMemberInfo(queryObject.memberId);

        Document response = new SuccessResponse();
        response.append("memberInfo", info);
        return response;
    }

    @PostMapping("/create-department")
    public Document createDepartment(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(STRUCTURE_MANAGEMENT);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        structureService.createDepartment(orgId, queryObject.parentId, queryObject.name);

        return new SuccessResponse();
    }

    @PostMapping("/rename-department")
    public Document renameDepartment(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(STRUCTURE_MANAGEMENT);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        structureService.assertDepartmentInOrganization(orgId, queryObject.departmentId);
        structureService.renameDepartment(queryObject.departmentId, queryObject.name);

        return new SuccessResponse();
    }

    @PostMapping("/delete-department")
    public Document deleteDepartment(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(STRUCTURE_MANAGEMENT);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        structureService.assertDepartmentInOrganization(orgId, queryObject.departmentId);
        structureService.deleteDepartment(queryObject.departmentId);

        return new SuccessResponse();
    }

    @PostMapping("/set-general-manager")
    public Document setGeneralManager(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(STRUCTURE_MANAGEMENT);

        memberIdService.assertMemberInCurrentOrganization(queryObject.memberId);
        structureService.setMemberToGeneralManager(queryObject.memberId);

        return new SuccessResponse();
    }

    @PostMapping("/set-department-manager")
    public Document setDepartmentManager(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(STRUCTURE_MANAGEMENT);

        memberIdService.assertMemberInCurrentOrganization(queryObject.memberId);
        structureService.setMemberToDepartmentManager(queryObject.memberId, queryObject.departmentId);

        return new SuccessResponse();
    }

    @PostMapping("/set-member")
    public Document setMember(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(STRUCTURE_MANAGEMENT);

        memberIdService.assertMemberInCurrentOrganization(queryObject.memberId);
        structureService.setMemberToMember(queryObject.memberId, queryObject.departmentId);

        return new SuccessResponse();
    }

    @PostMapping("/update-title")
    public Document updateMemberTitle(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(STRUCTURE_MANAGEMENT);

        memberInfoService.updateMemberTitle(queryObject.memberId, queryObject.title);

        return new SuccessResponse();
    }

    @PostMapping("/dismiss")
    public Document removeMember(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(STRUCTURE_MANAGEMENT);

        structureService.dismissMember(queryObject.memberId);

        return new SuccessResponse();
    }
}
