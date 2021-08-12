package com.leader.api.controller.org.manage;

import com.leader.api.data.org.application.OrgApplicationReceivedDetail;
import com.leader.api.data.org.application.OrgApplicationReceivedOverview;
import com.leader.api.data.org.application.notification.OrgApplicationNotification;
import com.leader.api.service.org.application.OrgApplicationManageService;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.service.ImageService;
import com.leader.api.util.InternalErrorException;
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
import static com.leader.api.util.response.SuccessResponse.success;

@RestController
@RequestMapping("/org/manage/apply")
public class OrgApplicationManageController {

    public static final String PASS_RESULT = "pass";
    public static final String REJECT_RESULT = "reject";

    private final OrgAuthorizationService authorizationService;
    private final OrgMemberIdService memberIdService;
    private final OrgApplicationManageService applicationManageService;
    private final ImageService imageService;

    @Autowired
    public OrgApplicationManageController(OrgAuthorizationService authorizationService,
                                          OrgMemberIdService memberIdService,
                                          OrgApplicationManageService applicationManageService,
                                          ImageService imageService) {
        this.authorizationService = authorizationService;
        this.memberIdService = memberIdService;
        this.applicationManageService = applicationManageService;
        this.imageService = imageService;
    }

    public static class QueryObject {
        public ObjectId applicationId;
        public ObjectId notificationId;
        public OrgApplicationNotification notification;
        public String result;
    }

    @PostMapping("/list-received")
    public Document listReceivedApplications() {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        List<OrgApplicationReceivedOverview> list = applicationManageService.listReceived(memberId);

        return success(
                "list", list
        );
    }

    @PostMapping("/detail")
    public Document getApplicationDetail(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        applicationManageService.assertCanSeeApplication(memberId, queryObject.applicationId);
        OrgApplicationReceivedDetail detail = applicationManageService.getDetail(queryObject.applicationId);

        return success(
                "detail", detail
        );
    }

    @PostMapping("/notification-detail")
    public Document getApplicationNotificationDetail(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        ObjectId applicationId = applicationManageService.getApplicationIdOfNotification(queryObject.notificationId);
        applicationManageService.assertCanSeeApplication(memberId, applicationId);
        OrgApplicationNotification detail = applicationManageService.getNotificationDetail(queryObject.notificationId);

        return success(
                "detail", detail
        );
    }

    @PostMapping("/send-notification")
    public Document sendApplicationNotification(@RequestBody QueryObject queryObject) {
        imageService.assertUploadedTempImages(queryObject.notification.imageUrls);

        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        applicationManageService.assertCanManageApplication(memberId, queryObject.applicationId);
        applicationManageService.sendNotification(queryObject.applicationId, queryObject.notification);

        imageService.confirmUploadImages(queryObject.notification.imageUrls);

        return success();
    }

    @PostMapping("/send-result")
    public Document sendApplicationResult(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        applicationManageService.assertCanManageApplication(memberId, queryObject.applicationId);
        if (PASS_RESULT.equals(queryObject.result)) {
            applicationManageService.sendResult(memberId, queryObject.applicationId, PASS);
        } else if (REJECT_RESULT.equals(queryObject.result)) {
            applicationManageService.sendResult(memberId, queryObject.applicationId, REJECT);
        } else {
            throw new InternalErrorException("Invalid result.");
        }

        return success();
    }

    @PostMapping("/list-operated")
    public Document listOperatedApplications() {
        authorizationService.assertCurrentMemberHasAuthority(RECRUIT);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        List<OrgApplicationReceivedOverview> list = applicationManageService.listOperated(memberId);

        return success(
                "list", list
        );
    }
}
