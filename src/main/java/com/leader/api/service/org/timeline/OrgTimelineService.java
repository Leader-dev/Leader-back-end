package com.leader.api.service.org.timeline;

import com.leader.api.data.org.timeline.OrgTimelineItem;
import com.leader.api.data.org.timeline.OrgTimelineItemRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrgTimelineService {

    private final OrgTimelineItemRepository timelineItemRepository;

    public OrgTimelineService(OrgTimelineItemRepository timelineItemRepository) {
        this.timelineItemRepository = timelineItemRepository;
    }

    public List<OrgTimelineItem> getTimelineOf(ObjectId organizationId) {
        return timelineItemRepository.findByOrganizationIdOrderByTimestampAsc(organizationId);
    }

    public void insertItem(ObjectId organizationId, Date timestamp, String description) {
        OrgTimelineItem item = new OrgTimelineItem();
        item.organizationId = organizationId;
        item.timestamp = timestamp;
        item.description = description;
        timelineItemRepository.insert(item);
    }

    public void deleteItemWithId(ObjectId organizationId, ObjectId timelineItemId) {
        timelineItemRepository.deleteByOrganizationIdAndId(organizationId, timelineItemId);
    }
}
