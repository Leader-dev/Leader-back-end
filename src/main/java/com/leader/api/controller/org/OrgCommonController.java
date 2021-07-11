package com.leader.api.controller.org;

import com.leader.api.data.org.OrgDetail;
import com.leader.api.data.org.OrgLobbyOverview;
import com.leader.api.data.org.OrgPosterOverview;
import com.leader.api.service.org.OrgTypeService;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.application.OrgApplicationService;
import com.leader.api.service.org.query.OrgQueryObject;
import com.leader.api.service.org.query.OrgQueryService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.response.ErrorResponse;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org")
public class OrgCommonController {

    private final OrganizationService organizationService;
    private final OrgApplicationService applicationService;
    private final OrgTypeService typeService;
    private final OrgQueryService queryService;
    private final UserIdService userIdService;

    @Autowired
    public OrgCommonController(OrganizationService organizationService,
                               OrgApplicationService applicationService,
                               OrgTypeService typeService,
                               OrgQueryService queryService,
                               UserIdService userIdService) {
        this.organizationService = organizationService;
        this.applicationService = applicationService;
        this.typeService = typeService;
        this.queryService = queryService;
        this.userIdService = userIdService;
    }

    public static class QueryObject {
        public ObjectId orgId;
    }

    @PostMapping("/types")
    public Document getOrganizationTypes() {
        // convert types from object list to key-value-pair object, with alias being the key
        Document typesMapping = typeService.getTypeAliasMapping();

        Document response = new SuccessResponse();
        response.append("types", typesMapping);
        return response;
    }

    @PostMapping("/index")
    public Document getOrganizationIndex() {
        // TODO Use more intelligent way to decide content
        List<OrgPosterOverview> pic = queryService.findOrganizationsByNumber(5, OrgPosterOverview.class);
        List<OrgLobbyOverview> list = queryService.findOrganizationsByNumber(9, OrgLobbyOverview.class);

        Document response = new SuccessResponse();
        Document index = new Document();
        index.append("pic", pic);
        index.append("list", list);
        response.append("index", index);
        return response;
    }

    @PostMapping("/list")
    public Document listOrganizations(@RequestBody OrgQueryObject queryObject) {
        // find organizations
        Page<OrgLobbyOverview> list = queryService.findRunningOrganizationsByQueryObject(queryObject);

        Document response = new SuccessResponse();
        Document result = new Document();
        result.append("list", list.getContent());
        result.append("totalPages", list.getTotalPages());
        result.append("totalNumber", list.getTotalElements());
        response.append("result", result);
        return response;
    }

    @PostMapping("/detail")
    public Document organizationDetail(@RequestBody QueryObject queryObject) {
        // find organization
        OrgDetail detail = organizationService.getOrganizationDetail(queryObject.orgId);
        if (detail == null) {
            return new ErrorResponse("invalid_organization");
        }

        ObjectId userId = userIdService.getCurrentUserId();
        String applicationStatus = applicationService.getApplicationEntranceStatus(queryObject.orgId, userId);

        Document response = new SuccessResponse();
        Document data = new Document();
        data.append("detail", detail);
        data.append("applicationStatus", applicationStatus);
        response.append("data", data);
        return response;
    }
}
