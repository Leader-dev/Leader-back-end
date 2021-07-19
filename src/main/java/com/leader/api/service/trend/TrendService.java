package com.leader.api.service.trend;

import com.leader.api.data.org.Organization;
import com.leader.api.data.org.OrganizationRepository;
import com.leader.api.data.org.member.OrgMember;
import com.leader.api.data.org.member.OrgMemberRepository;
import com.leader.api.data.trend.item.TrendItem;
import com.leader.api.data.trend.item.TrendItemDetail;
import com.leader.api.data.trend.item.TrendItemRepository;
import com.leader.api.data.trend.like.TrendLike;
import com.leader.api.data.trend.like.TrendLikeRepository;
import com.leader.api.data.trend.report.TrendReport;
import com.leader.api.data.trend.report.TrendReportRepository;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrendService {

    private final TrendItemRepository itemRepository;
    private final TrendLikeRepository likeRepository;
    private final TrendReportRepository reportRepository;
    private final OrganizationRepository organizationRepository;
    private final OrgMemberRepository memberRepository;
    private final TrendNotificationService notificationService;
    private final DateUtil dateUtil;

    @Autowired
    public TrendService(TrendItemRepository itemRepository, TrendLikeRepository likeRepository, TrendReportRepository reportRepository,
                        OrganizationRepository organizationRepository, OrgMemberRepository memberRepository, TrendNotificationService notificationService, DateUtil dateUtil) {
        this.itemRepository = itemRepository;
        this.likeRepository = likeRepository;
        this.reportRepository = reportRepository;
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.notificationService = notificationService;
        this.dateUtil = dateUtil;
    }

    public List<TrendItemDetail> getTrends(ObjectId puppetId, Pageable pageable) {
        return itemRepository.lookupByOrderBySendDateDesc(puppetId, pageable);
    }

    public List<TrendItemDetail> getSentTrends(ObjectId puppetId, Pageable pageable) {
        return itemRepository.lookupByPuppetIdOrderBySendDateDesc(puppetId, pageable);
    }

    public TrendItemDetail getDetail(ObjectId puppetId, ObjectId trendItemId) {
        return itemRepository.lookupByIdOrderBySendDateDesc(puppetId, trendItemId);
    }

    public TrendItem getTrendItem(ObjectId trendItemId) {
        return itemRepository.findById(trendItemId).orElse(null);
    }

    public void sendTrend(ObjectId puppetId,
                          ObjectId userId,
                          boolean anonymous,
                          ObjectId orgId,
                          String content,
                          ArrayList<String> imageUrls) {
        Organization organization = organizationRepository.findById(orgId, Organization.class);
        OrgMember member = memberRepository.findByOrgIdAndUserId(orgId, userId);

        TrendItem item = new TrendItem();
        item.puppetId = puppetId;
        item.orgName = organization.name;
        item.orgTitle = member.title;
        item.anonymous = anonymous;
        item.sendDate = dateUtil.getCurrentDate();
        item.content = content;
        item.imageUrls = imageUrls;
        item.likeCount = 0L;
        itemRepository.insert(item);
    }

    public void likeTrend(ObjectId puppetId, ObjectId trendItemId) {
        if (!likeRepository.existsByTrendItemIdAndPuppetId(trendItemId, puppetId)) {
            itemRepository.findById(trendItemId).ifPresent(item -> {
                TrendLike like = new TrendLike();
                like.trendItemId = trendItemId;
                like.puppetId = puppetId;
                likeRepository.insert(like);

                item.likeCount = likeRepository.countByTrendItemId(trendItemId);
                itemRepository.save(item);

                notificationService.sendLikeNotification(item.puppetId, puppetId, trendItemId);
            });
        }
    }

    public void unlikeTrend(ObjectId puppetId, ObjectId trendItemId) {
        itemRepository.findById(trendItemId).ifPresent(item -> {
            likeRepository.deleteByTrendItemIdAndPuppetId(trendItemId, puppetId);

            item.likeCount = likeRepository.countByTrendItemId(trendItemId);
            itemRepository.save(item);
        });
    }

    public long countLikes(ObjectId puppetId) {
        return itemRepository.countLikesByPuppetId(puppetId).orElse(0L);
    }

    public void deleteTrend(ObjectId puppetId, ObjectId trendItemId) {
        itemRepository.findByPuppetIdAndId(puppetId, trendItemId).ifPresent(item -> {
            likeRepository.deleteByTrendItemId(trendItemId);
            itemRepository.delete(item);
        });
    }

    public void reportTrend(ObjectId userId, ObjectId trendItemId, String description, ArrayList<String> imageUrls) {
        TrendReport report = new TrendReport();
        report.senderUserId = userId;
        report.trendItemId = trendItemId;
        report.description = description;
        report.imageUrls = imageUrls;
        reportRepository.insert(report);
    }
}
