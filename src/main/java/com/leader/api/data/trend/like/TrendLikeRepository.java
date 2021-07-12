package com.leader.api.data.trend.like;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrendLikeRepository extends MongoRepository<TrendLike, ObjectId> {

    long countByTrendItemId(ObjectId trendItemId);

    boolean existsByTrendItemIdAndPuppetId(ObjectId trendItemId, ObjectId puppetId);

    void deleteByTrendItemIdAndPuppetId(ObjectId trendItemId, ObjectId puppetId);

    void deleteByTrendItemId(ObjectId trendItemId);
}
