package com.leader.api.controller.org;

import com.leader.api.data.org.OrgLobbyOverview;
import com.leader.api.data.org.OrgPosterOverview;
import com.leader.api.service.org.OrgTypeService;
import com.leader.api.service.org.query.OrgQueryObject;
import com.leader.api.service.org.query.OrgQueryService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org")
public class OrgCommonController {

    @Value("${leader.qr-code-url-template}")
    private String QR_CODE_URL_TEMPLATE;

    private final OrgTypeService typeService;
    private final OrgQueryService queryService;

    @Autowired
    public OrgCommonController(OrgTypeService typeService,
                               OrgQueryService queryService) {
        this.typeService = typeService;
        this.queryService = queryService;
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

    @PostMapping("/home")
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

    @PostMapping("/qr-code-url")
    public Document getQRCodeUrl(@RequestBody QueryObject queryObject) {
        String url = String.format(QR_CODE_URL_TEMPLATE, queryObject.orgId.toString());

        Document response = new SuccessResponse();
        response.append("url", url);
        return response;
    }
}
