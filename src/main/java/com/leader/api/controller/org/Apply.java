package com.leader.api.controller.org;

import com.leader.api.data.org.application.OrganizationApplicationDetail;
import com.leader.api.data.org.application.OrganizationApplicationForm;
import com.leader.api.data.org.application.OrganizationApplicationSentOverview;
import com.leader.api.service.org.OrganizationApplicationService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/org/apply")
public class Apply {

    private final OrganizationApplicationService applicationService;

    private final UserIdService userIdService;

    @Autowired
    public Apply(OrganizationApplicationService applicationService, UserIdService userIdService) {
        this.applicationService = applicationService;
        this.userIdService = userIdService;
    }

    private static class ApplyQueryObject {
        ObjectId organizationId;
        ObjectId departmentId;
        OrganizationApplicationForm applicationForm;
        ObjectId applicationId;
        String action;
    }

    @PostMapping("/send")
    public Document requestApplyForm(@RequestBody ApplyQueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();
        applicationService.sendApplication(
                queryObject.organizationId,
                queryObject.departmentId,
                queryObject.applicationForm,
                userid
        );

        return new SuccessResponse();
    }

    @PostMapping("/list")
    public Document listApplications() {
        ObjectId userid = userIdService.getCurrentUserId();

        List<OrganizationApplicationSentOverview> list = applicationService.getSentApplications(userid);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }

    @PostMapping("/detail")
    public Document applicationDetail(@RequestBody ApplyQueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();

        OrganizationApplicationDetail application = applicationService.getApplication(userid, queryObject.applicationId);

        Document response = new SuccessResponse();
        response.append("detail", application);
        return response;
    }

    @PostMapping("/reply")
    public Document replyToApplication(@RequestBody ApplyQueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();

        applicationService.replyToApplication(userid, queryObject.applicationId, queryObject.action);

        return new SuccessResponse();
    }
}
