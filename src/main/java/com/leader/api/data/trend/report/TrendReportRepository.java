package com.leader.api.data.trend.report;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrendReportRepository extends MongoRepository<TrendReport, ObjectId> {
}
