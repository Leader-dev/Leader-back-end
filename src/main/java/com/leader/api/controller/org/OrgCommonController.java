package com.leader.api.controller.org;

import com.leader.api.data.org.OrgLobbyOverview;
import com.leader.api.data.org.OrgPosterOverview;
import com.leader.api.data.org.Organization;
import com.leader.api.service.org.OrgTypeService;
import com.leader.api.service.org.OrganizationService;
import com.leader.api.service.org.query.OrgQueryObject;
import com.leader.api.service.org.query.OrgQueryService;
import com.leader.api.util.response.ErrorResponse;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
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
    private final OrgTypeService typeService;
    private final OrgQueryService queryService;

    @Autowired
    public OrgCommonController(OrganizationService organizationService,
                               OrgTypeService typeService,
                               OrgQueryService queryService) {
        this.organizationService = organizationService;
        this.typeService = typeService;
        this.queryService = queryService;
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
        List<OrgPosterOverview> pic = queryService.findOrganizationsByNumber(5);
        List<OrgLobbyOverview> list = queryService.findOrganizationsByNumber(9);

        Document response = new SuccessResponse();
        response.append("pic", pic);
        response.append("list", list);
        return response;
    }

    @PostMapping("/list")
    public Document listOrganizations(@RequestBody OrgQueryObject queryObject) {
        // find organizations
        Page<OrgLobbyOverview> list = queryService.findRunningOrganizationsByQueryObject(queryObject);

        Document response = new SuccessResponse();
        response.append("list", list.getContent());
        response.append("totalPages", list.getTotalPages());
        response.append("totalNumber", list.getTotalElements());
        return response;
    }

    @PostMapping("/detail")
    public Document organizationDetail(@RequestBody OrgQueryObject queryObject) {
        // find organization
        Organization detail = organizationService.getOrganization(queryObject.orgId);
        if (detail == null) {
            return new ErrorResponse("invalid_organization");
        }

        Document response = new SuccessResponse();
        response.append("detail", detail);
        return response;
    }
}
