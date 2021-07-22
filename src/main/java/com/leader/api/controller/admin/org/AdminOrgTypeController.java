package com.leader.api.controller.admin.org;

import com.leader.api.data.org.type.OrgType;
import com.leader.api.service.admin.org.AdminOrgTypeService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/org/type")
public class AdminOrgTypeController {

    private final AdminOrgTypeService typeService;

    @Autowired
    public AdminOrgTypeController(AdminOrgTypeService typeService) {
        this.typeService = typeService;
    }

    public static class QueryObject {
        public OrgType type;
        public ObjectId typeId;
    }

    @PostMapping("/list")
    public Document listTypes() {
        Document response = new SuccessResponse();
        response.append("types", typeService.getOrgTypes());
        return response;
    }

    @PostMapping("/save")
    public Document saveType(@RequestBody QueryObject queryObject) {
        typeService.saveOrgType(queryObject.type);

        return new SuccessResponse();
    }

    @PostMapping("/type/delete")
    public Document deleteType(@RequestBody QueryObject queryObject) {
        typeService.deleteOrgType(queryObject.typeId);

        return new SuccessResponse();
    }

    @PostMapping("/move-up")
    public Document moveUpType(@RequestBody QueryObject queryObject) {
        typeService.moveUpOrgType(queryObject.typeId);

        return new SuccessResponse();
    }

    @PostMapping("/move-down")
    public Document moveDownType(@RequestBody QueryObject queryObject) {
        typeService.moveDownOrgType(queryObject.typeId);

        return new SuccessResponse();
    }
}
