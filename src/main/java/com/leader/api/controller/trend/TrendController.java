package com.leader.api.controller.trend;

import com.leader.api.data.trend.item.TrendItem;
import com.leader.api.data.trend.item.TrendItemDetail;
import com.leader.api.service.service.ImageService;
import com.leader.api.service.trend.PuppetIdService;
import com.leader.api.service.trend.TrendService;
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
    private final PuppetIdService puppetIdService;
    private final ImageService imageService;

    @Autowired
    public TrendController(TrendService trendService, PuppetIdService puppetIdService, ImageService imageService) {
        this.trendService = trendService;
        this.puppetIdService = puppetIdService;
        this.imageService = imageService;
    }

    public static class QueryObject {
        public int pageNumber;
        public int pageSize;
        public ObjectId trendItemId;
        public boolean anonymous;
        public ObjectId orgId;
        public String content;
        public String description;
        public ArrayList<String> imageUrls;
    }

    @PostMapping("/list")
    public Document listTrends(@RequestBody QueryObject queryObject) {
        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        List<TrendItemDetail> trends = trendService.getTrends(puppetId, PageRequest.of(queryObject.pageNumber, queryObject.pageSize));

        Document response = new SuccessResponse();
        response.append("trends", trends);
        return response;
    }

    @PostMapping("/list-sent")
    public Document listSentTrends(@RequestBody QueryObject queryObject) {
        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        List<TrendItemDetail> trends = trendService.getSentTrends(puppetId, PageRequest.of(queryObject.pageNumber, queryObject.pageSize));

        Document response = new SuccessResponse();
        response.append("trends", trends);
        return response;
    }

    @PostMapping("/send")
    public Document sendTrend(@RequestBody QueryObject queryObject) {
        imageService.assertUploadedTempImages(queryObject.imageUrls);

        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        ObjectId userId = puppetIdService.getCurrentUserId();
        trendService.sendTrend(
                puppetId,
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
        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        trendService.likeTrend(puppetId, queryObject.trendItemId);

        return new SuccessResponse();
    }

    @PostMapping("/unlike")
    public Document unlikeTrend(@RequestBody QueryObject queryObject) {
        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        trendService.unlikeTrend(puppetId, queryObject.trendItemId);

        return new SuccessResponse();
    }

    @PostMapping("/delete")
    public Document deleteTrend(@RequestBody QueryObject queryObject) {
        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        TrendItem item = trendService.getTrendItem(queryObject.trendItemId);
        trendService.deleteTrend(puppetId, queryObject.trendItemId);

        imageService.deleteImages(item.imageUrls);

        return new SuccessResponse();
    }

    @PostMapping("/report")
    public Document reportTrend(@RequestBody QueryObject queryObject) {
        ObjectId userId = puppetIdService.getCurrentUserId();
        trendService.reportTrend(userId, queryObject.trendItemId, queryObject.description, queryObject.imageUrls);

        return new SuccessResponse();
    }
}
