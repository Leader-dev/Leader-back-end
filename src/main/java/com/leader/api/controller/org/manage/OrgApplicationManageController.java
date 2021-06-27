package com.leader.api.controller.org.manage;

import com.leader.api.data.org.application.OrgApplicationReceivedDetail;
import com.leader.api.data.org.application.OrgApplicationReceivedOverview;
import com.leader.api.data.org.application.notification.OrgApplicationNotification;
import com.leader.api.service.org.application.OrgApplicationManageService;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
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

import static com.leader.api.service.org.application.OrgApplicationManageService.ApplicationResult.PASS;
import static com.leader.api.service.org.application.OrgApplicationManageService.ApplicationResult.REJECT;
import static com.leader.api.service.org.authorization.OrgAuthority.RECRUIT;

@RestController
@RequestMapping("/org/manage/apply")
public class OrgApplicationManageController {

    public static final String PASS_RESULT = "pass";
    public static final String REJECT_RESULT = "reject";

    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService memberIdService;
    private final OrgApplicationManageService applicationManageService;

    @Autowired
    public OrgApplicationManageController(OrgAuthorizationService authorizationService,
                                          OrgMemberIdService memberIdService,
                                          OrgApplicationManageService applicationManageService) {
        this.authorizationService = authorizationService;
        this.memberIdService = memberIdService;
        this.applicationManageService = applicationManageService;
    }

    public static class QueryObject {
        public ObjectId applicationId;
        public OrgApplicationNotification notification;
        public String result;
    }

    @PostMapping("/list-received")
    public Document listReceivedApplications() {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        List<OrgApplicationReceivedOverview> list = applicationManageService.listReceived(memberId);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }

    @PostMapping("/detail")
    public Document getApplicationDetail(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        OrgApplicationReceivedDetail detail = applicationManageService.getDetail(memberId, queryObject.applicationId);

        Document response = new SuccessResponse();
        response.append("detail", detail);
        return response;
    }

    @PostMapping("/send-notification")
    public Document sendApplicationNotification(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        applicationManageService.sendNotification(memberId, queryObject.applicationId, queryObject.notification);

        return new SuccessResponse();
    }

    @PostMapping("/send-result")
    public Document sendApplicationResult(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        if (PASS_RESULT.equals(queryObject.result)) {
            applicationManageService.sendResult(memberId, queryObject.applicationId, PASS);
        } else if (REJECT_RESULT.equals(queryObject.result)) {
            applicationManageService.sendResult(memberId, queryObject.applicationId, REJECT);
        } else {
            throw new InternalErrorException("Invalid result.");
        }

        return new SuccessResponse();
    }

    @PostMapping("/list-operated")
    public Document listOperatedApplications() {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        List<OrgApplicationReceivedOverview> list = applicationManageService.listOperated(memberId);

        Document response = new SuccessResponse();
        response.append("list", list);
        return response;
    }
}
