package com.leader.api.data.org.timeline;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgTimelineItemRepository extends MongoRepository<OrgTimelineItem, ObjectId> {

    List<OrgTimelineItem> findByOrganizationIdOrderByTimestampAsc(ObjectId organizationId);

    void deleteByOrganizationIdAndId(ObjectId organizationId, ObjectId id);
}
