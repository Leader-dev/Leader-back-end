package com.leader.api.controller.org;

import com.leader.api.data.org.application.OrgApplicationForm;
import com.leader.api.data.org.application.OrgApplicationSentDetail;
import com.leader.api.data.org.application.OrgApplicationSentOverview;
import com.leader.api.data.org.department.OrgDepartmentOverview;
import com.leader.api.service.org.application.OrgApplicationService;
import com.leader.api.service.org.structure.OrgStructureQueryService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
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
import static com.leader.api.util.response.SuccessResponse.success;

@RestController
@RequestMapping("/org/apply")
public class OrgApplicationController {

    public static String ACCEPT_ACTION = "accept";
    public static String DECLINE_ACTION = "decline";

    private final OrgApplicationService applicationService;
    private final OrgStructureQueryService queryService;
    private final UserIdService userIdService;

    @Autowired
    public OrgApplicationController(OrgApplicationService applicationService, OrgStructureQueryService queryService,
                                    UserIdService userIdService) {
        this.applicationService = applicationService;
        this.queryService = queryService;
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

        return success();
    }

    @PostMapping("/list")
    public Document listApplications() {
        ObjectId userid = userIdService.getCurrentUserId();

        List<OrgApplicationSentOverview> list = applicationService.getSentApplications(userid);

        return success(
                "list", list
        );
    }

    @PostMapping("/list-departments")
    public Document listDepartments(@RequestBody QueryObject queryObject) {
        List<OrgDepartmentOverview> departments =
                queryService.listDepartments(queryObject.orgId, null, OrgDepartmentOverview.class);

        return success(
                "departments", departments
        );
    }

    @PostMapping("/detail")
    public Document applicationDetail(@RequestBody QueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();

        OrgApplicationSentDetail application = applicationService.getApplication(userid, queryObject.applicationId);

        return success(
                "detail", application
        );
    }

    @PostMapping("/read-notification")
    public Document readNotification(@RequestBody QueryObject queryObject) {
        ObjectId userid = userIdService.getCurrentUserId();

        applicationService.readNotification(userid, queryObject.notificationId);

        return success();
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

        return success();
    }
}
