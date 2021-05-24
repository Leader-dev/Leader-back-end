package com.leader.api.controller.org;

import com.leader.api.data.org.application.*;
import com.leader.api.response.SuccessResponse;
import com.leader.api.service.OrganizationApplicationService;
import com.leader.api.util.SessionUtil;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/org/apply")
public class Apply {

    @Autowired
    private OrganizationApplicationService applicationService;

    private static class ApplyQueryObject {
        ObjectId organizationId;
        ObjectId departmentId;
        OrganizationApplicationForm applicationForm;
        ObjectId applicationId;
        String action;
    }

    @PostMapping("/send")
    public Document requestApplyForm(@RequestBody ApplyQueryObject queryObject, HttpSession session) {
        ObjectId userid = SessionUtil.getUserIdFromSession(session);
        applicationService.sendApplication(
                queryObject.organizationId,
                queryObject.departmentId,
                queryObject.applicationForm,
                userid
        );

        return new SuccessResponse();
    }

    @PostMapping("/list")
    public Document listApplications(HttpSession session) {
        ObjectId userid = SessionUtil.getUserIdFromSession(session);

        List<OrganizationApplicationSentOverview> list = applicationService.getSentApplications(userid);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }

    @PostMapping("/detail")
    public Document applicationDetail(@RequestBody ApplyQueryObject queryObject, HttpSession session) {
        ObjectId userid = SessionUtil.getUserIdFromSession(session);

        OrganizationApplicationDetail application = applicationService.getApplication(userid, queryObject.applicationId);

        Document response = new SuccessResponse();
        response.append("detail", application);
        return response;
    }

    @PostMapping("/reply")
    public Document replyToApplication(@RequestBody ApplyQueryObject queryObject, HttpSession session) {
        ObjectId userid = SessionUtil.getUserIdFromSession(session);

        applicationService.replyToApplication(userid, queryObject.applicationId, queryObject.action);

        return new SuccessResponse();
    }
}
