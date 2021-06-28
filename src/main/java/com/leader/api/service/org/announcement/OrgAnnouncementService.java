package com.leader.api.service.org.announcement;

import com.leader.api.data.org.announcement.*;
import com.leader.api.data.org.member.OrgMemberInfoOverview;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.leader.api.data.org.announcement.OrgAnnouncementConfirmation.CONFIRMED;
import static com.leader.api.data.org.announcement.OrgAnnouncementConfirmation.NOT_CONFIRMED;

@Service
public class OrgAnnouncementService {

    private final OrgAnnouncementRepository announcementRepository;
    private final OrgAnnouncementConfirmationRepository confirmationRepository;
    private final DateUtil dateUtil;

    @Autowired
    public OrgAnnouncementService(OrgAnnouncementRepository announcementRepository,
                                  OrgAnnouncementConfirmationRepository confirmationRepository,
                                  DateUtil dateUtil) {
        this.announcementRepository = announcementRepository;
        this.confirmationRepository = confirmationRepository;
        this.dateUtil = dateUtil;
    }

    private void copyValidItemsTo(OrgAnnouncement announcement, OrgAnnouncementBasicInfo basicInfo) {
        announcement.title = basicInfo.title;
        announcement.content = basicInfo.content;
        announcement.coverUrl = basicInfo.coverUrl;
        announcement.imageUrls = basicInfo.imageUrls;
    }

    private OrgAnnouncement insertNewAnnouncement(ObjectId senderMemberId, OrgAnnouncementBasicInfo basicInfo) {
        OrgAnnouncement announcement = new OrgAnnouncement();
        announcement.senderMemberId = senderMemberId;
        announcement.sendDate = dateUtil.getCurrentDate();
        announcement.notConfirmedCount = 0;
        announcement.confirmedCount = 0;
        copyValidItemsTo(announcement, basicInfo);
        return announcementRepository.insert(announcement);
    }

    private void insertNewConfirmation(ObjectId toMemberId, ObjectId announceId) {
        OrgAnnouncementConfirmation confirmation = new OrgAnnouncementConfirmation();
        confirmation.announceId = announceId;
        confirmation.memberId = toMemberId;
        confirmation.status = NOT_CONFIRMED;
        confirmationRepository.insert(confirmation);
    }

    private void updateConfirmCount(ObjectId announceId) {
        announcementRepository.findById(announceId).ifPresent(announcement -> {
            announcement.notConfirmedCount = confirmationRepository.countByAnnounceIdAndStatus(announceId, NOT_CONFIRMED);
            announcement.confirmedCount = confirmationRepository.countByAnnounceIdAndStatus(announceId, CONFIRMED);
            announcementRepository.save(announcement);
        });
    }

    public boolean isSentToMember(ObjectId memberId, ObjectId announceId) {
        return confirmationRepository.existsByMemberIdAndAnnounceId(memberId, announceId);
    }

    public void assertIsSentToMember(ObjectId memberId, ObjectId announceId) {
        if (!isSentToMember(memberId, announceId)) {
            throw new InternalErrorException("Announcement is not sent to member.");
        }
    }

    public List<OrgAnnouncementOverview> listReceivedAnnouncements(ObjectId memberId) {
        return confirmationRepository.lookupByMemberId(memberId);
    }

    public void confirmReceivedAnnouncement(ObjectId memberId, ObjectId announceId) {
        OrgAnnouncementConfirmation confirmation = confirmationRepository.findByMemberIdAndAnnounceId(memberId, announceId);
        confirmation.status = CONFIRMED;
        confirmationRepository.save(confirmation);
        updateConfirmCount(announceId);
    }

    public List<OrgAnnouncementOverview> listSentAnnouncements(ObjectId senderMemberId) {
        return announcementRepository.findBySenderMemberId(senderMemberId, OrgAnnouncementOverview.class);
    }

    public List<OrgAnnouncementOverview> listSentAnnouncements(List<ObjectId> senderMemberIds) {
        return announcementRepository.findBySenderMemberIdIsIn(senderMemberIds, OrgAnnouncementOverview.class);
    }

    public void sendAnnouncement(ObjectId senderMemberId, List<ObjectId> toMemberIds, OrgAnnouncementBasicInfo basicInfo) {
        ObjectId announceId = insertNewAnnouncement(senderMemberId, basicInfo).id;
        for (ObjectId toMemberId : toMemberIds) {
            insertNewConfirmation(toMemberId, announceId);
        }
        updateConfirmCount(announceId);
    }

    public List<OrgMemberInfoOverview> listByConfirmStatus(ObjectId announceId, String status) {
        return confirmationRepository.lookupByAnnounceIdAndStatus(announceId, status);
    }

    public void deleteAnnouncement(ObjectId announceId) {
        announcementRepository.deleteById(announceId);
        confirmationRepository.deleteByAnnounceId(announceId);
    }

    public OrgAnnouncement getAnnouncement(ObjectId announceId) {
        return announcementRepository.findById(announceId).orElse(null);
    }

    public OrgAnnouncementDetail getDetail(ObjectId memberId, ObjectId announceId) {
        return confirmationRepository.lookupByMemberIdAndAnnounceId(memberId, announceId);
    }
}
