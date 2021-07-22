package com.leader.api.controller.admin.org;

import com.leader.api.data.org.Organization;
import com.leader.api.service.admin.org.AdminOrgService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/org")
public class AdminOrgController {

    private final AdminOrgService orgService;

    @Autowired
    public AdminOrgController(AdminOrgService orgService) {
        this.orgService = orgService;
    }

    public static class QueryObject {
        public Organization info;
    }

    @PostMapping("/list")
    public Document list() {
        Document response = new SuccessResponse();
        response.append("list", orgService.getOrganizations().getContent());
        return response;
    }

    @PostMapping("/update")
    public Document update(@RequestBody QueryObject queryObject) {
        orgService.updateOrganization(queryObject.info);

        return new SuccessResponse();
    }
}
