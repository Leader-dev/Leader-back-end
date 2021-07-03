package com.leader.api.controller.org.manage;

import com.leader.api.data.org.timeline.OrgTimelineItem;
import com.leader.api.service.org.authorization.OrgAuthorizationService;
import com.leader.api.service.org.member.OrgMemberIdService;
import com.leader.api.service.org.timeline.OrgTimelineService;
import com.leader.api.util.response.SuccessResponse;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.leader.api.service.org.authorization.OrgAuthority.BASIC;
import static com.leader.api.service.org.authorization.OrgAuthority.TIMELINE_MANAGEMENT;

@RestController
@RequestMapping("/org/manage/timeline")
public class OrgTimelineController {

    private final OrgAuthorizationService authorizationService;
    private final OrgTimelineService timelineService;
    private final OrgMemberIdService memberIdService;

    @Autowired
    public OrgTimelineController(OrgAuthorizationService authorizationService,
                                 OrgTimelineService timelineService,
                                 OrgMemberIdService memberIdService) {
        this.authorizationService = authorizationService;
        this.timelineService = timelineService;
        this.memberIdService = memberIdService;
    }

    public static class QueryObject {
        public Date timestamp;
        public String description;
        public ObjectId timelineItemId;
    }

    @PostMapping("/list")
    public Document listTimelineItems() {
        authorizationService.assertCurrentMemberHasAuthority(BASIC);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        List<OrgTimelineItem> items = timelineService.getTimelineOf(orgId);

        Document response = new SuccessResponse();
        response.append("timeline", items);
        return response;
    }

    @PostMapping("/insert")
    public Document insertTimelineItem(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(TIMELINE_MANAGEMENT);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        timelineService.insertItem(orgId, queryObject.timestamp, queryObject.description);

        return new SuccessResponse();
    }

    @PostMapping("/delete")
    public Document deleteTimelineItem(@RequestBody QueryObject queryObject) {
        authorizationService.assertCurrentMemberHasAuthority(TIMELINE_MANAGEMENT);

        ObjectId orgId = memberIdService.getCurrentOrgId();
        timelineService.deleteItemWithId(orgId, queryObject.timelineItemId);

        return new SuccessResponse();
    }
}
