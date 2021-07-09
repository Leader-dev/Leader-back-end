package com.leader.api.service.org.timeline;

import com.leader.api.data.org.timeline.OrgTimelineItem;
import com.leader.api.data.org.timeline.OrgTimelineItemRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrgTimelineService {

    private final OrgTimelineItemRepository timelineItemRepository;

    @Autowired
    public OrgTimelineService(OrgTimelineItemRepository timelineItemRepository) {
        this.timelineItemRepository = timelineItemRepository;
    }

    public List<OrgTimelineItem> getTimelineOf(ObjectId orgId) {
        return timelineItemRepository.findByOrgIdOrderByTimestampAsc(orgId);
    }

    public void insertItem(ObjectId orgId, Date timestamp, String description) {
        OrgTimelineItem item = new OrgTimelineItem();
        item.orgId = orgId;
        item.timestamp = timestamp;
        item.description = description;
        timelineItemRepository.insert(item);
    }

    public void deleteItemWithId(ObjectId orgId, ObjectId timelineItemId) {
        timelineItemRepository.deleteByOrgIdAndId(orgId, timelineItemId);
    }
}
