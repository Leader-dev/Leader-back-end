package com.leader.api.controller.trend;

import com.leader.api.data.org.member.OrgMemberTitleInfo;
import com.leader.api.data.trend.puppet.PuppetInfo;
import com.leader.api.service.org.member.OrgMemberTitleService;
import com.leader.api.service.service.ImageService;
import com.leader.api.service.trend.PuppetIdService;
import com.leader.api.service.trend.PuppetInfoService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/puppet")
public class PuppetController {

    private final PuppetIdService puppetIdService;
    private final PuppetInfoService puppetInfoService;
    private final OrgMemberTitleService titleService;
    private final ImageService imageService;

    @Autowired
    public PuppetController(PuppetIdService puppetIdService, PuppetInfoService puppetInfoService,
                            OrgMemberTitleService titleService, ImageService imageService) {
        this.puppetIdService = puppetIdService;
        this.puppetInfoService = puppetInfoService;
        this.titleService = titleService;
        this.imageService = imageService;
    }

    public static class QueryObject extends PuppetInfo {
        public ObjectId puppetId;
        public ObjectId orgId;
        public boolean displayTitle;
    }

    @PostMapping("/get-displayed-info")
    public Document getPuppetPublicInfo(@RequestBody QueryObject queryObject) {
        PuppetInfo puppetInfo = puppetInfoService.getPuppetInfo(queryObject.puppetId);
        ObjectId userId = puppetIdService.getUserId(queryObject.puppetId);
        List<OrgMemberTitleInfo> titles = titleService.findDisplayedTitles(userId);

        Document response = new SuccessResponse();
        Document data = new Document();
        data.append("puppetInfo", puppetInfo);
        data.append("titles", titles);
        response.append("data", data);
        return response;
    }

    @PostMapping("/get-info")
    public Document getPuppetInfo() {
        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        PuppetInfo puppetInfo = puppetInfoService.getPuppetInfo(puppetId);
        ObjectId userId = puppetIdService.getCurrentUserId();
        List<OrgMemberTitleInfo> titles = titleService.findTitles(userId);

        Document response = new SuccessResponse();
        Document data = new Document();
        data.append("puppetInfo", puppetInfo);
        data.append("titles", titles);
        response.append("data", data);
        return response;
    }

    @PostMapping("/update-info")
    public Document updatePuppetInfo(@RequestBody QueryObject queryObject) {
        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        puppetInfoService.updatePuppetInfo(puppetId, queryObject);

        return new SuccessResponse();
    }

    @PostMapping("/update-avatar")
    public Document updatePuppetAvatar(@RequestBody QueryObject queryObject) {
        imageService.assertUploadedTempImage(queryObject.avatarUrl);

        ObjectId puppetId = puppetIdService.getCurrentPuppetId();
        String prevAvatarUrl = puppetInfoService.getPuppetInfo(puppetId).avatarUrl;
        puppetInfoService.updatePuppetAvatar(puppetId, queryObject.avatarUrl);

        imageService.confirmUploadImage(queryObject.avatarUrl);
        imageService.deleteImage(prevAvatarUrl);

        return new SuccessResponse();
    }

    @PostMapping("/set-display-title")
    public Document setDisplayTitle(@RequestBody QueryObject queryObject) {
        ObjectId userId = puppetIdService.getCurrentUserId();
        titleService.updateDisplayTitle(userId, queryObject.orgId, queryObject.displayTitle);

        return new SuccessResponse();
    }
}
