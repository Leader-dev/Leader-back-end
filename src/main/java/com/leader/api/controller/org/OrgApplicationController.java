package com.leader.api.controller.org;

import com.leader.api.data.org.application.OrgApplicationForm;
import com.leader.api.data.org.application.OrgApplicationSentDetail;
import com.leader.api.data.org.application.OrgApplicationSentOverview;
import com.leader.api.service.org.application.OrgApplicationService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.leader.api.service.org.application.OrgApplicationService.ReplyAction.ACCEPT;
import static com.leader.api.service.org.application.OrgApplicationService.ReplyAction.DECLINE;

@RestController
@RequestMapping("/org/apply")
public class OrgApplicationController {

    public static String ACCEPT_ACTION = "accept";
    public static String DECLINE_ACTION = "decline";

    private final OrgApplicationService applicationService;

    private final UserIdService userIdService;

    @Autowired
    public OrgApplicationController(OrgApplicationService applicationService, UserIdService userIdService) {
        this.applicationService = applicationService;
        this.userIdService = userIdService;
    }

    private static class QueryObject {
        public ObjectId orgId;
        public String name;
        public ObjectId departmentId;
        public OrgApplicationForm applicationForm;
        public ObjectId applicationId;
        public ObjectId notificationId;
        public String action;
    }

    @PostMapping("/send")
    public Document requestApplyForm(@RequestBody QueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();
        applicationService.sendApplication(
                queryObject.orgId,
                userid,
                queryObject.name,
                queryObject.departmentId,
                queryObject.applicationForm
        );

        return new SuccessResponse();
    }

    @PostMapping("/list")
    public Document listApplications() {
        ObjectId userid = userIdService.getCurrentUserId();

        List<OrgApplicationSentOverview> list = applicationService.getSentApplications(userid);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }

    @PostMapping("/detail")
    public Document applicationDetail(@RequestBody QueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();

        OrgApplicationSentDetail application = applicationService.getApplication(userid, queryObject.applicationId);

        Document response = new SuccessResponse();
        response.append("detail", application);
        return response;
    }

    @PostMapping("/read-notification")
    public Document readNotification(@RequestBody QueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();

        applicationService.readNotification(userid, queryObject.notificationId);

        return new SuccessResponse();
    }

    @PostMapping("/reply")
    public Document replyToApplication(@RequestBody QueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();

        if (ACCEPT_ACTION.equals(queryObject.action)) {
            applicationService.replyToApplication(userid, queryObject.applicationId, ACCEPT);
        } else if (DECLINE_ACTION.equals(queryObject.action)) {
            applicationService.replyToApplication(userid, queryObject.applicationId, DECLINE);
        } else {
            throw new InternalErrorException("Invalid action.");
        }

        return new SuccessResponse();
    }
}
