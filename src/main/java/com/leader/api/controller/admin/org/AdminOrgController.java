package com.leader.api.controller.admin.org;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.type.OrgType;
import com.leader.api.service.admin.org.AdminOrgService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
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
        public OrgType type;
        public ObjectId typeId;
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

    @PostMapping("/type/list")
    public Document listTypes() {
        Document response = new SuccessResponse();
        response.append("types", orgService.getOrgTypes());
        return response;
    }

    @PostMapping("/type/save")
    public Document saveType(@RequestBody QueryObject queryObject) {
        orgService.saveOrgType(queryObject.type);

        return new SuccessResponse();
    }

    @PostMapping("/type/delete")
    public Document deleteType(@RequestBody QueryObject queryObject) {
        orgService.deleteOrgType(queryObject.typeId);

        return new SuccessResponse();
    }

    @PostMapping("/type/move-up")
    public Document moveUpType(@RequestBody QueryObject queryObject) {
        orgService.moveUpOrgType(queryObject.typeId);

        return new SuccessResponse();
    }

    @PostMapping("/type/move-down")
    public Document moveDownType(@RequestBody QueryObject queryObject) {
        orgService.moveDownOrgType(queryObject.typeId);

        return new SuccessResponse();
    }
}
