package com.leader.api.data.org.report;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrgReportRepository extends MongoRepository<OrgReport, ObjectId> {
}
