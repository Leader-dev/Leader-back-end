package com.leader.api.controller.org.manage;

import com.leader.api.data.org.announcement.OrgAnnouncement;
import com.leader.api.data.org.announcement.OrgAnnouncementBasicInfo;
import com.leader.api.data.org.announcement.OrgAnnouncementDetail;
import com.leader.api.data.org.announcement.OrgAnnouncementOverview;
import com.leader.api.data.org.member.OrgMemberInfoOverview;
import com.leader.api.service.org.announcement.OrgAnnouncementService;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.service.ImageService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.leader.api.data.org.announcement.OrgAnnouncementConfirmation.NOT_CONFIRMED;
import static com.leader.api.service.org.authorization.OrgAuthority.ANNOUNCEMENT_MANAGEMENT;
import static com.leader.api.service.org.authorization.OrgAuthority.BASIC;

@RestController
@RequestMapping("/org/manage/announce")
public class OrgAnnouncementController {

    private final OrgAnnouncementService announcementService;
    private final OrgMemberIdService memberIdService;
    private final OrgAuthorizationService authorizationService;
    private final ImageService imageService;

    @Autowired
    public OrgAnnouncementController(OrgAnnouncementService announcementService,
                                     OrgMemberIdService memberIdService,
                                     OrgAuthorizationService authorizationService,
                                     ImageService imageService) {
        this.announcementService = announcementService;
        this.memberIdService = memberIdService;
        this.authorizationService = authorizationService;
        this.imageService = imageService;
    }

    public static class QueryObject {
        public ObjectId announceId;
        public List<ObjectId> toMemberIds;
        public OrgAnnouncementBasicInfo announceInfo;
    }

    @PostMapping("/list-received")
    public Document listReceivedAnnouncements() {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        List<OrgAnnouncementOverview> announcements = announcementService.listReceivedAnnouncements(memberId);

        Document response = new SuccessResponse();
        response.append("list", announcements);
        return response;
    }

    @PostMapping("/detail")
    public Document getAnnouncementDetail(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        OrgAnnouncementDetail announcement = announcementService.getDetail(memberId, queryObject.announceId);

        Document response = new SuccessResponse();
        response.append("detail", announcement);
        return response;
    }

    @PostMapping("/confirm")
    public Document confirmReceivedAnnouncement(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        announcementService.confirmReceivedAnnouncement(memberId, queryObject.announceId);

        return new SuccessResponse();
    }

    @PostMapping("/list-sent")
    public Document listSentAnnouncements() {
        authorizationService.assertCurrentMemberHasAuthority(ANNOUNCEMENT_MANAGEMENT);

        List<ObjectId> managerIds = authorizationService.listManageableManagerIdsOfCurrentMember();
        List<OrgAnnouncementOverview> announcements = announcementService.listSentAnnouncements(managerIds);

        Document response = new SuccessResponse();
        response.append("list", announcements);
        return response;
    }

    @PostMapping("/send")
    public Document sendAnnouncement(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(ANNOUNCEMENT_MANAGEMENT);
        authorizationService.assertCurrentMemberCanManageAllMembers(queryObject.toMemberIds);

        imageService.assertUploadedTempImage(queryObject.announceInfo.coverUrl);
        imageService.assertUploadedTempImages(queryObject.announceInfo.imageUrls);

        ObjectId memberId = memberIdService.getCurrentMemberId();
        announcementService.sendAnnouncement(memberId, queryObject.toMemberIds, queryObject.announceInfo);

        imageService.confirmUploadImage(queryObject.announceInfo.coverUrl);
        imageService.confirmUploadImages(queryObject.announceInfo.imageUrls);

        return new SuccessResponse();
    }

    @PostMapping("/list-not-confirmed")
    public Document listConfirmInfo(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(ANNOUNCEMENT_MANAGEMENT);

        OrgAnnouncement announcement = announcementService.getAnnouncement(queryObject.announceId);
        authorizationService.assertCurrentMemberCanManageMember(announcement.senderMemberId);
        List<OrgMemberInfoOverview> notConfirmed = announcementService.listByConfirmStatus(announcement.id, NOT_CONFIRMED);

        Document response = new SuccessResponse();
        response.append("notConfirmed", notConfirmed);
        return response;
    }

    @PostMapping("/delete")
    public Document deleteAnnouncement(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(ANNOUNCEMENT_MANAGEMENT);

        OrgAnnouncement announcement = announcementService.getAnnouncement(queryObject.announceId);
        authorizationService.assertCurrentMemberCanManageMember(announcement.senderMemberId);
        announcementService.deleteAnnouncement(announcement.id);

        imageService.deleteImage(announcement.coverUrl);
        imageService.deleteImages(announcement.imageUrls);

        return new SuccessResponse();
    }
}
