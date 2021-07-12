package com.leader.api.controller.trend;

import com.leader.api.data.org.member.OrgMemberTitleInfo;
import com.leader.api.data.trend.item.TrendItem;
import com.leader.api.data.trend.item.TrendItemDetail;
import com.leader.api.data.user.UserInfo;
import com.leader.api.service.org.member.OrgMemberTitleService;
import com.leader.api.service.service.ImageService;
import com.leader.api.service.trend.TrendService;
import com.leader.api.service.user.UserInfoService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/trend")
public class TrendController {

    private final TrendService trendService;
    private final UserIdService userIdService;
    private final ImageService imageService;
    private final UserInfoService userInfoService;
    private final OrgMemberTitleService titleService;

    @Autowired
    public TrendController(TrendService trendService, UserIdService userIdService, ImageService imageService,
                           UserInfoService userInfoService, OrgMemberTitleService titleService) {
        this.trendService = trendService;
        this.userIdService = userIdService;
        this.imageService = imageService;
        this.userInfoService = userInfoService;
        this.titleService = titleService;
    }

    public static class QueryObject {
        public int pageNumber;
        public int pageSize;
        public ObjectId trendItemId;
        public ObjectId userId;
        public boolean anonymous;
        public ObjectId orgId;
        public String content;
        public String description;
        public ArrayList<String> imageUrls;
    }

    @PostMapping("/list")
    public Document listTrends(@RequestBody QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        List<TrendItemDetail> trends = trendService.getTrends(userId, PageRequest.of(queryObject.pageNumber, queryObject.pageSize));

        Document response = new SuccessResponse();
        response.append("trends", trends);
        return response;
    }

    @PostMapping("/list-sent")
    public Document listSentTrends(@RequestBody QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        List<TrendItemDetail> trends = trendService.getSentTrends(userId, PageRequest.of(queryObject.pageNumber, queryObject.pageSize));

        Document response = new SuccessResponse();
        response.append("trends", trends);
        return response;
    }

    @PostMapping("/send")
    public Document sendTrend(@RequestBody QueryObject queryObject) {
        imageService.assertUploadedTempImages(queryObject.imageUrls);

        ObjectId userId = userIdService.getCurrentUserId();
        trendService.sendTrend(
                userId,
                queryObject.anonymous,
                queryObject.orgId,
                queryObject.content,
                queryObject.imageUrls
        );

        imageService.confirmUploadImages(queryObject.imageUrls);

        return new SuccessResponse();
    }

    @PostMapping("/like")
    public Document likeTrend(@RequestBody QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        trendService.likeTrend(userId, queryObject.trendItemId);

        return new SuccessResponse();
    }

    @PostMapping("/unlike")
    public Document unlikeTrend(@RequestBody QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        trendService.unlikeTrend(userId, queryObject.trendItemId);

        return new SuccessResponse();
    }

    @PostMapping("/delete")
    public Document deleteTrend(@RequestBody QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        TrendItem item = trendService.getTrendItem(queryObject.trendItemId);
        trendService.deleteTrend(userId, queryObject.trendItemId);

        imageService.deleteImages(item.imageUrls);

        return new SuccessResponse();
    }

    @PostMapping("/report")
    public Document reportTrend(@RequestBody QueryObject queryObject) {
        ObjectId userId = userIdService.getCurrentUserId();
        trendService.reportTrend(userId, queryObject.trendItemId, queryObject.description, queryObject.imageUrls);

        return new SuccessResponse();
    }

    @PostMapping("/get-user-info")
    public Document getUserInfo(@RequestBody QueryObject queryObject) {
        UserInfo userInfo = userInfoService.getUserInfo(queryObject.userId);
        List<OrgMemberTitleInfo> titles = titleService.findDisplayedTitles(queryObject.userId);

        Document response = new SuccessResponse();
        Document data = new Document();
        data.append("userInfo", userInfo);
        data.append("titles", titles);
        response.append("data", data);
        return response;
    }
}
