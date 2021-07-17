package com.leader.api.data.trend.puppet;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PuppetRepository extends MongoRepository<Puppet, ObjectId> {

    Puppet findByUserId(ObjectId userId);

    <T> T findById(ObjectId id, Class<T> type);
}
