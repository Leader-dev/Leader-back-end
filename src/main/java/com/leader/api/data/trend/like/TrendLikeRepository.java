package com.leader.api.data.trend.like;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrendLikeRepository extends MongoRepository<TrendLike, ObjectId> {

    long countByTrendItemId(ObjectId trendItemId);

    boolean existsByTrendItemIdAndUserId(ObjectId trendItemId, ObjectId userId);

    void deleteByTrendItemIdAndUserId(ObjectId trendItemId, ObjectId userId);

    void deleteByTrendItemId(ObjectId trendItemId);
}
