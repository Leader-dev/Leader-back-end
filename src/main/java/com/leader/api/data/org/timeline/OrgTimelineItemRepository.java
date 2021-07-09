package com.leader.api.data.org.timeline;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgTimelineItemRepository extends MongoRepository<OrgTimelineItem, ObjectId> {

    List<OrgTimelineItem> findByOrgIdOrderByTimestampAsc(ObjectId orgId);

    void deleteByOrgIdAndId(ObjectId orgId, ObjectId id);
}
